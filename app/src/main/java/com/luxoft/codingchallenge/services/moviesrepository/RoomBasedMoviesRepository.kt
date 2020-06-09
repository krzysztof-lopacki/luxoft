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
        val addStreamRequest = PublishSubject.create<Observable<PagedList<Movie>>>()
        val additionalSubstreams = Observable.merge(addStreamRequest)

        // main db stream
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(configuration.pageSize)
            .build()
        val dbStream = RxPagedListBuilder(moviesDao.getAllPagedList(), config)
            .setBoundaryCallback(object : PagedList.BoundaryCallback<Movie>() {
                override fun onItemAtEndLoaded(itemAtEnd: Movie) {
                    addStreamRequest.onNext(moviesInTheatresUpdater.loadNextPage.toErrorlessObservable())
                }
                override fun onItemAtFrontLoaded(itemAtFront: Movie) {
                    addStreamRequest.onNext(moviesInTheatresUpdater.refreshFirstPageIfRequired.toErrorlessObservable())
                }
                override fun onZeroItemsLoaded() {
                    addStreamRequest.onNext(moviesInTheatresUpdater.refreshFirstPage.toErrorlessObservable())
                }
            })
            .buildObservable()
            .share()

        // Sll streams together:
        // 1. Main stream providing data from DB.
        // 2. Initial refresh stream started on subscription.
        // 3. A stream gathering all substreams created during the lifetime of the current subscription.
        moviesInTheatres = Observable.mergeArray(
            dbStream,
            moviesInTheatresUpdater.refreshFirstPage.toErrorlessObservable(),
            additionalSubstreams
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
                            movie.copy(isFavourite = favouriteIds.contains(movie.id))
                        }
                    }
            }
    }
}

private fun <T> Completable.toErrorlessObservable(): Observable<T> {
    return onErrorComplete().toObservable()
}
