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

/**
 * #REVIEW#: Unfortunately the responses from endpoints provided by The Movie DB
 * are not suitable for proper caching. In fact they are even not suitable for proper paging
 * if we want to simply show results on the lazy loading list without additional logic.
 *
 * This class exists to mitigate issues that may occur while using The Movie DB API
 * and in ideal world where The Move DB provides better responses won't be required at all
 * (or at least the number of validations would be much shorter).
 *
 * The main problems with The Movie DB:
 * First, assume that:
 * - A new movie may be added to the Now Playing list at any time, even every minute or every a few seconds
 * - A huge bunch of movies may be added at the same time (e.g. on Friday)
 * - paging starts from the first element on the Now Playing list. This start point changes each time
 *   a new movie is added to the Now Playing list on the server side
 * - Items provided by the api are sorted, be the field used for sorting is not provided in the models.
 *
 * This leads to problems like these:
 *
 * 1.   - you are on the page 2
 *      - a movie is added to the Now Playing list
 *      - page 3 is downloaded
 *
 *      Result: First item on the page 3 is the same item that was downloaded with the page 2
 *      as all items shifted when the new item was added. On the lazy loading list this means that
 *      a duplicate will be shown.
 *
 *      Possible solution on the server side:
 *      API may provide a method to obtain stable results, e.g.
 *      - an option specify in the request the point when paging starts. It could be a movie ID
 *        or a date when the movie was added to the Now Playing list.
 *      - each page may contain a link to the next page with a token describing how perform a query
 *        that provides stable results.
 *
 *     Solution on the app side (implemented):
 *     Each new page should be compared with the already downloaded data.
 *     Only new movies should be added to the local list.
 *
 *
 * 2.   - you are on the page 2
 *      - a huge bunch of movies is added to the Now Playing list on the server (e.g. 100)
 *      - page 3 is downloaded
 *
 *      Result: all the downloaded data is a new data and it is newer than the data on the page 2,
 *      not older as expected. If the user loads page 4, 5, 6...N he/she will eventually load the same
 *      data that was downloaded with the page 1 and 2, as all the data on the server was shifted
 *      when those 100 movies were added.
 *
 *      Possible solution on the server side:
 *      API should expose the field on which the sorting was performed. This will make
 *      possible to compare the new data with the already downloaded data and put it to the cache
 *      after or before the already downloaded data.
 *
 *      Solution on the app side (implemented):
 *      Complete solution is not possible. App assumes that a new data is in fact the data of the requested
 *      page. When in the future, with the next pages the same data is downloaded again the problem
 *      is detected and app invalidates the whole cache.
 *
 * 3.   - you have already downloaded a few pages
 *      - a huge bunch of movies is added to the Now Playing list on the server (e.g. 100)
 *      - you are again downloading page 1 (using pull to refresh)
 *      - the download page contains only new items
 *      Result: It is unknown whether the new page 1 is adjacent to the locally cached data
 *      or there is a big gap between the newly downloaded page 1 and originally downloaded page 1 in cache.
 *      It is not known whether the list may be still shown a continuous list.
 *
 *      Possible solution on the server side:
 *      Server may provide ability to load pages with negative numbers PLUS provide a way
 *      to provide stable results (check point 1.)
 *
 *      Solution on the app side (implemented):
 *      The only possible solution right now is to invalidate the cache.
 *
 * 4.   - you have already downloaded a few pages
 *      - a single movie is added to the Now Playing list on the server
 *      - you are again downloading page 1 (using pull to refresh)
 *      Result: almost entire new page data is already in cache. Only 1 movie is new. If we simply add
 *      items to cache there will be duplicates.
 *
 *      Possible solution on the server side:
 *      Same as in point 3.
 *
 *      Solution on the app side (implemented):
 *      Application compares new page data with cached data and adds only new movies to the cache.
 *
 * 5.   - you have downloaded 5 pages
 *      - a movie from the page 5 is removed on the server from the Now Playing list
 *      - same movie is added again at the head of Now Playing list
 *      - you refresh the first page
 *      Result: if we simply the movie again there will be a duplicate in the cache.
 *
 *      Possible solution on the server side:
 *      Not really possible.
 *
 *      Solution on the app side (implemented):
 *      If application detects reordering it invalidates the cache.
 *
 */
/**
 * A class responsible for updating the cache of the Now Playing list.
 */
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