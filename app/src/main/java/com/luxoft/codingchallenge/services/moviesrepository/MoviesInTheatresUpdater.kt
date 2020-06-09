package com.luxoft.codingchallenge.services.moviesrepository

import android.content.SharedPreferences
import android.util.Log
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import com.luxoft.codingchallenge.services.api.MoviesInTheatersFetcher
import com.luxoft.codingchallenge.services.moviesrepository.db.MovieEntity
import com.luxoft.codingchallenge.services.moviesrepository.db.MoviesDao
import com.luxoft.codingchallenge.utils.errors.resolveMessage
import com.luxoft.codingchallenge.utils.time.time
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlin.math.max

private const val SHARED_PREFERENCES_LAST_REFRESH_KEY = "lastRefresh"
private const val SHARED_PREFERENCES_LAST_PAGE_LOADED_KEY = "lastPageLoaded"
private const val SHARED_PREFERENCES_PAGES_TOTAL_KEY = "pagesTotal"
private const val TAG = "#MoviesUpdater"

class MoviesInTheatresUpdater(private val moviesDao: MoviesDao,
                              private val moviesInTheatersFetcher: MoviesInTheatersFetcher,
                              private val dbScheduler: Scheduler,
                              private val configuration: Configuration,
                              private val sharedPreferences: SharedPreferences) {
    private val refreshingFistPageDispatcher = BehaviorSubject.create<Boolean>()
    val refreshingFistPage: Observable<Boolean> = refreshingFistPageDispatcher
    private val refreshingFistPageErrorsDispatcher = PublishSubject.create<Throwable>()
    val refreshingFistPageErrors: Observable<Throwable> = refreshingFistPageErrorsDispatcher

    private val loadingNextPageDispatcher = BehaviorSubject.create<Boolean>()
    val loadingNextPage: Observable<Boolean> = loadingNextPageDispatcher
    private val loadingNextPageErrorsDispatcher = PublishSubject.create<Throwable>()
    val loadingNextPageErrors: Observable<Throwable> = loadingNextPageErrorsDispatcher

    init {
        refreshingFistPageDispatcher.onNext(false)
        loadingNextPageDispatcher.onNext(false)
    }

    private var lastFirstPageRefresh: Long = sharedPreferences.getLong(SHARED_PREFERENCES_LAST_REFRESH_KEY, 0)
        set(value) {
            if (field != value) {
                field = value
                sharedPreferences.edit().putLong(SHARED_PREFERENCES_LAST_REFRESH_KEY, value).apply()
            }
        }

    private var lastPageLoaded: Int = sharedPreferences.getInt(SHARED_PREFERENCES_LAST_PAGE_LOADED_KEY, 0)
        set(value) {
            if (field != value) {
                field = value
                sharedPreferences.edit().putInt(SHARED_PREFERENCES_LAST_PAGE_LOADED_KEY, value).apply()
            }
        }

    private var totalPages: Int = sharedPreferences.getInt(SHARED_PREFERENCES_PAGES_TOTAL_KEY, 1)
        set(value) {
            if (field != value) {
                field = value
                sharedPreferences.edit().putInt(SHARED_PREFERENCES_PAGES_TOTAL_KEY, value).apply()
            }
        }

    val refreshFirstPageIfRequired = Completable.defer {
        if (time() - lastFirstPageRefresh > configuration.firstPageLifetime) {
            refreshFirstPage
        } else {
            Completable.complete()
        }
    }

    val refreshFirstPage: Completable = moviesInTheatersFetcher.getMoviesInTheaters(1)
        .observeOn(dbScheduler)
        .doOnSuccess { newPage ->
            // Nothing on the server -> nothing is played right now in the theatres.
            if (newPage.items.isEmpty()) {
                Log.d(TAG, "refreshFirstPage: Clearing database! There are no movies in theatres!")
                clearData()
                onPageLoaded(newPage)
                return@doOnSuccess
            }

            val remoteMovieIds = newPage.items.map { movie -> movie.id }

            // Get the overlapping movies, that is the common movies of the remote page
            // and the local db.
            val overlappingMovies = moviesDao.getMovies(remoteMovieIds)

            // Check whether the are any overlapping movies.
            // If not, we can't tell whether the new data is adjacent to the local data
            // or there is a gap between the local and the downloaded data.
            if (overlappingMovies.isEmpty()) {
                if (lastPageLoaded == 0) {
                    // There is no overlapping data because this is just a first refresh.
                    prependMovies(newPage.items)
                    onPageLoaded(newPage)
                    return@doOnSuccess
                } else {
                    throw InvalidateDatabase("No overlapping movies.", newPage)
                }
            }

            // Check whether the overlapping movies are consecutive items in the local item.
            // If not it means that some items in the local db are no longer present at the same
            // place on the server side.
            if (searchForGaps(overlappingMovies)) {
                throw InvalidateDatabase("Overlapping items are not consecutive.", newPage)
            }

            // Check if overlapping movies list is a tail of the new movies list.
            // If not, it means that the remote list is different than local list:
            // - the order may be different
            // - one side has more items
            if (!isTailOf(overlappingMovies, newPage.items)) {
                throw InvalidateDatabase("Remote data order is different than local order or one side has more items.", newPage)
            }

            // Check whether the first overlapping item is the first item in the local db (according to sortingIndex).
            // If not it means that the head of the local data is different
            // than the corresponding part on the server.
            if (overlappingMovies.first().sortingIndex != moviesDao.getMinSortingIndex()) {
                throw InvalidateDatabase("The local head has additional movies.", newPage)
            }

            // Everything is fine -> put the new data to the database.
            val lastItemToPrependIndex = newPage.items.indexOfFirst { item -> item.id == overlappingMovies.first().movie.id}
            prependMovies(newPage.items.subList(0, lastItemToPrependIndex))
            onPageLoaded(newPage)
        }
        .onErrorReturn { error: Throwable ->
            if (error is InvalidateDatabase && error.newData != null) {
                Log.d(TAG, "refreshFirstPage: Database will be invalidated! ${error.resolveMessage()}")
                val newPage = error.newData
                clearData()
                prependMovies(newPage.items)
                onPageLoaded(newPage)
                newPage
            } else {
                throw error
            }
        }
        .doOnSubscribe {
            Log.d(TAG, "refreshFirstPage: Refreshing of the first page started.")
            refreshingFistPageDispatcher.onNext(true)
        }
        .doOnSuccess {
            Log.d(TAG, "refreshFirstPage: Refreshing of the first page complete!.")
            refreshingFistPageDispatcher.onNext(false)
        }
        .doOnError { error ->
            Log.d(TAG, "refreshFirstPage: Refreshing of the first page failed: ${error.resolveMessage()}")
            refreshingFistPageDispatcher.onNext(false)
            refreshingFistPageErrorsDispatcher.onNext(error)
        }
        .toObservable()
        .share()
        .ignoreElements()

    val loadNextPage: Completable = Observable.defer {
            if (lastPageLoaded == totalPages) {
                Log.d(TAG, "loadNextPage: Loading of the next page rejected. Last page is loaded already.")
                Observable.just(-1)
            } else {
                moviesInTheatersFetcher.getMoviesInTheaters(lastPageLoaded + 1)
                    .doOnSubscribe {
                        Log.d(TAG, "loadNextPage: Loading of the page ${lastPageLoaded + 1} started.")
                    }
                    .observeOn(dbScheduler)
                    .map { newPage ->
                        if (newPage.items.isEmpty()) {
                            Log.d(TAG, "loadNextPage: Empty page!")
                            onPageLoaded(newPage)
                            return@map -1
                        }

                        val remoteMovieIds = newPage.items.map { movie -> movie.id }

                        // Get the overlapping movies, that is the common movies of the remote page
                        // and the local db.
                        val overlappingMovies = moviesDao.getMovies(remoteMovieIds)

                        if (overlappingMovies.isEmpty()) {
                            // All the downloaded movies are not present in the local db.
                            appendMovies(newPage.items)
                            onPageLoaded(newPage)
                            return@map newPage.items.size
                        }

                        // Check whether the overlapping movies are consecutive items in the local item.
                        // If not it means that some items in the local db are no longer present at the same
                        // place on the server side.
                        if (searchForGaps(overlappingMovies)) {
                            throw InvalidateDatabase("Overlapping items are not consecutive.", newPage)
                        }

                        // Check if overlapping movies list is a head of the new movies list.
                        // If not, it means that the remote list is different than local list:
                        // - the order may be different
                        // - one side has more items
                        if (!isHeadOf(overlappingMovies, newPage.items)) {
                            throw InvalidateDatabase("Remote data order is different than local order or one side has more items.", newPage)
                        }

                        // If there are some items to be added check whether the last overlapping item
                        // is the last item in the local db (according to sortingIndex).
                        // If not it means that the tail of the local data is different
                        // than the corresponding part on the server.
                        if (newPage.items.size > overlappingMovies.size
                            && overlappingMovies.last().sortingIndex != moviesDao.getMaxSortingIndex()) {
                            throw InvalidateDatabase("The local tail has additional movies.", newPage)
                        }

                        // Everything is fine -> put the new data to the database.
                        onPageLoaded(newPage)
                        val firstItemToPrependIndex = newPage.items.indexOfLast { item -> item.id == overlappingMovies.last().movie.id} + 1
                        if (newPage.items.size > firstItemToPrependIndex) {
                            appendMovies(newPage.items.subList(firstItemToPrependIndex, newPage.items.size))
                            newPage.items.size - firstItemToPrependIndex
                        } else {
                            0
                        }
                    }
                    .doOnSuccess {
                        Log.d(TAG, "loadNextPage: Loading of the page $lastPageLoaded complete!")
                    }
                    .toObservable()
            }
        }
        .doOnError { error ->
            Log.d(TAG, "loadNextPage: Loading of the page $lastPageLoaded failed: ${error.resolveMessage()}")
        }
        .repeat() // along with the takeWhile below it will automatically load the next page if nothing new was loaded
        .takeWhile { addedMoviesCount ->
            addedMoviesCount == 0
        }
        .onErrorResumeNext { error: Throwable ->
            if (error is InvalidateDatabase) {
                // If we detect unrecoverable error due to one of the mentioned above edge cases
                // we need to clear the database and populate with fresh data.
                Log.d(TAG, "loadNextPage: Database will be cleared and refreshed! ${error.resolveMessage()}")
                clearData()
                refreshFirstPage.toObservable()
            } else {
                Observable.error(error)
            }
        }
        .doOnComplete {
            Log.d(TAG, "loadNextPage: Whole process of loading of the next page complete!")
            loadingNextPageDispatcher.onNext(false)
        }
        .doOnError { error ->
            loadingNextPageDispatcher.onNext(false)
            loadingNextPageErrorsDispatcher.onNext(error)
        }
        .doOnSubscribe {
            loadingNextPageDispatcher.onNext(true)
        }
        .share()
        .ignoreElements()

    /**
     * Clears database and any persistent data regarding loaded pages.
     */
    fun clearData() {
        moviesDao.clear()
        totalPages = 1
        lastPageLoaded = 0
        lastFirstPageRefresh = 0
    }

    /**
     * Handles the successful loading of a new page.
     * @param loadedPage Description of the loaded page.
     */
    private fun onPageLoaded(loadedPage: Page<Movie>) {
        lastPageLoaded = max(lastPageLoaded, loadedPage.page)
        if (loadedPage.page == 1) lastFirstPageRefresh = time()
        totalPages = loadedPage.totalPages
    }

    /**
     * Add a list of movies to the end of the database.
     * @param movies Movies to be added.
     */
    private fun appendMovies(movies: List<Movie>) {
        putMovies(movies, (moviesDao.getMaxSortingIndex() ?: -1) + 1)
    }

    /**
     * Adds a list of movies at the beginning of the database.
     * @param movies Movie to be added.
     */
    private fun prependMovies(movies: List<Movie>) {
        putMovies(movies, (moviesDao.getMinSortingIndex() ?: movies.size.toLong()) - movies.size)
    }

    /**
     * Adds a list of movies to the database and assigns increasing sorting indices to each of them.
     * @param movies Movies to be added
     * @param firstSortingIndex A sorting index for the first movie.
     */
    private fun putMovies(movies: List<Movie>, firstSortingIndex: Long) {
        moviesDao.add(movies.mapIndexed { index, movie ->
            val entity = MovieEntity(null, movie, firstSortingIndex + index)
            entity
        })
    }
}

/**
 * Exception informing that database should be invalidated, that is cleared and populated again.
 * @param newData   Optional parameter. Defines that new data that should be used
 *                  to populated the invalidated database.
 */
private class InvalidateDatabase(message: String, val newData: Page<Movie>?) : RuntimeException(message)

/**
 * Checks whether provided list of [MovieEntity]s is a tail of the provided list of [Movie]s.
 * The exact equality of items is not taken into account - only the identifiers are in use
 * to check items equality.
 */
internal fun isTailOf(subList: List<MovieEntity>, fullList: List<Movie>): Boolean {
    if (subList.isEmpty()) return true
    if (fullList.isEmpty()) return false

    val firstSublistItemId = subList.first().movie.id
    val firstSublistItemIndex = fullList.indexOfFirst { item -> item.id == firstSublistItemId}

    // missing item
    if (firstSublistItemIndex == -1) return false

    // size differs
    if (subList.size != fullList.size - firstSublistItemIndex) return false

    return areEquals(subList, fullList.subList(firstSublistItemIndex, fullList.size))
}

internal fun isHeadOf(subList: List<MovieEntity>, fullList: List<Movie>): Boolean {
    if (subList.isEmpty()) return true
    if (fullList.isEmpty()) return false

    val lastSublistItemId = subList.last().movie.id
    val lastSublistItemIndex = fullList.indexOfLast { item -> item.id == lastSublistItemId}

    // missing item
    if (lastSublistItemIndex == -1) return false

    // size differs
    if (subList.size != lastSublistItemIndex + 1) return false

    return areEquals(subList, fullList.subList(0, lastSublistItemIndex + 1))
}

/**
 * Checks whether provided [MovieEntity]s list is equal to [Movie]s list.
 * During the check only the identifiers are compared.
 * @param entities [MovieEntity]s to compare
 * @param movies [Movie]s to compare
 */
internal fun areEquals(entities: List<MovieEntity>, movies: List<Movie>): Boolean {
    if (entities.isEmpty() != movies.isEmpty()) return false
    if (entities.isEmpty()) return  true

    if (movies.size != entities.size) return false

    for (index in movies.indices) {
        // content is different or reordered
        if (movies[index].id != entities[index].movie.id) return false
    }
    return true
}

/**
 * Checks whether movies on the list are consecutive movies from the the DB,
 * that is whether the [MovieEntity.sortingIndex] is consecutively increasing.
 */
internal fun searchForGaps(movies: List<MovieEntity>): Boolean {
    if (movies.isEmpty()) return false
    movies.reduce { previousMovie, currentMovie ->
        if (currentMovie.sortingIndex - previousMovie.sortingIndex != 1L) return true
        currentMovie
    }
    return false
}