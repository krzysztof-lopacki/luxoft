package com.luxoft.codingchallenge.services.moviesrepository

import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.services.api.MoviesSearch
import com.luxoft.codingchallenge.services.moviesrepository.db.FavouritesDao
import com.luxoft.codingchallenge.services.moviesrepository.db.MoviesDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

class RoomBasedMoviesRepository (private val moviesDao: MoviesDao,
                                 private val favouritesDao: FavouritesDao,
                                 private val moviesSearch: MoviesSearch,
                                 private val moviesInTheatresUpdater: MoviesInTheatresUpdater,
                                 private val dbScheduler: Scheduler,
                                 private val configuration: Configuration) : MoviesRepository {
    override lateinit var moviesInTheatres: Observable<PagedList<Movie>>
        private set

    init {
        createMoviesInTheatresStreams()
    }

    private fun createMoviesInTheatresStreams() {
        // stream gathering all substreams created during subscription
        val updateOperations = PublishSubject.create<Completable>()

        // main db stream
        // TODO: converting DataSource.Factory<Int, Movie> to Observable<PagedList<Movie>> (lines 36-64)
        //  should be done in a view model, not here
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(configuration.pageSize)
            .build()
        val dbStream = RxPagedListBuilder(moviesDao.getAllPagedList(), config)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<Movie>() {
                override fun onItemAtEndLoaded(itemAtEnd: Movie) {
                    updateOperations.onNext(moviesInTheatresUpdater.loadNextPage)
                }
                override fun onItemAtFrontLoaded(itemAtFront: Movie) {
                    updateOperations.onNext(moviesInTheatresUpdater.refreshFirstPageIfRequired)
                }
                override fun onZeroItemsLoaded() {
                    updateOperations.onNext(moviesInTheatresUpdater.refreshFirstPage)
                }
            })
            .buildObservable()
            .share()

        // All streams together:
        // 1. Main stream providing data from DB.
        // 2. Initial refresh stream started on subscription.
        // 3. A stream gathering all operations created during the lifetime of the current subscription.
        //    This approach cancels all update operations when no one listens to the main stream anymore.
        moviesInTheatres = Observable.mergeArray(
            dbStream, // the only stream providing data
            moviesInTheatresUpdater.refreshFirstPage.toErrorlessObservable(), // initial refresh
            updateOperations.flatMap { it.toErrorlessObservable<PagedList<Movie>>() } // update operations
        )
        .share()
    }

    override fun loadRecentMoviesInTheatres() = moviesInTheatresUpdater.refreshFirstPage

    override fun loadMoreMoviesInTheatres(): Completable = moviesInTheatresUpdater.loadNextPage

    override val isLoadingMoreMoviesInTheatres = moviesInTheatresUpdater.loadingNextPage

    override val isLoadingRecentMoviesInTheatres = moviesInTheatresUpdater.refreshingFistPage

    override val loadingMoreMoviesInTheatresErrors = moviesInTheatresUpdater.loadingNextPageErrors

    override val loadingRecentMoviesInTheatresErrors = moviesInTheatresUpdater.refreshingFistPageErrors

    override fun addMovieToFavourites(movieId: Long): Completable {
        return Completable.fromCallable {
            favouritesDao.addIfNotPresent(movieId)
        }
        .subscribeOn(dbScheduler)
    }

    override fun removeMovieFromFavourites(movieId: Long): Completable {
        return Completable.fromCallable {
            favouritesDao.remove(movieId)
        }
        .subscribeOn(dbScheduler)
    }

    override fun isMovieInFavourites(movieId: Long) = favouritesDao.isFavourite(movieId)

    override fun searchForTheMovie(query: String): Observable<List<Movie>> {
        return moviesSearch.searchForTheMovie(query, 1)
            .map { page -> page.items }
            .flatMapObservable { items ->
                favouritesDao.getIfFavourite(items.map { movie -> movie.id })
                    .map { favourites ->
                        val favouriteIds = favourites.map { item -> item.movieId }
                        items.map { movie ->
                            val isFavourite = favouriteIds.contains(movie.id)
                            if (movie.isFavourite != isFavourite) {
                                movie.copy(isFavourite = isFavourite)
                            } else {
                                movie
                            }
                        }
                    }
            }
    }
}

private fun <T> Completable.toErrorlessObservable(): Observable<T> {
    return onErrorComplete().toObservable()
}
