package com.luxoft.codingchallenge.services.api

import androidx.paging.PagedList
import com.luxoft.codingchallenge.models.Movie
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * An access point to the movies repository.
 * This component should considered as the only public source of movies related data in the application.
 */
interface MoviesRepository {

    /**
     * Paged list of the movies that are in the theatres currently (aka Now Playing list).
     * @return  A stream containing a paged list of movies in theatres. The returned stream dispatches
     *          a new version of the movie's list each time it is updated (e.g. prepended)
     *          or any of its items is modified (e.g. movie is add to the Favourites).
     */
    val moviesInTheatres: Observable<PagedList<Movie>>

    /**
     * Checks whether there are some new movies displayed in theatres since the last update.
     * Prepends the Now Playing list with new data if necessary.
     */
    fun loadRecentMoviesInTheatres(): Completable

    /**
     * Stream informing whether the Now Playing list's head is being updated at the moment.
     * @see loadRecentMoviesInTheatres method form more info about updating the Now Playing list.
     */
    val isLoadingRecentMoviesInTheatres: Observable<Boolean>

    /**
     * Stream notifying that the Now Playing list's head update operation has failed.
     * @see loadRecentMoviesInTheatres method form more info about updating the Now Playing list.
     */
    val loadingRecentMoviesInTheatresErrors: Observable<Throwable>

    /**
     * Forces the loading of more items to the Now Playing list.
     * Most of the time this method is obsolete.
     * Usually Now Playing list prepends automatically with the subsequent items while being used,
     * but when an error happens the auto loading mechanism is stopped.
     * The autoloading is restarted automatically when a new client of the Now Playing list
     * is registered, but if the current client of the list is interested in further updates
     * it has to call the [#loadMoreMoviesInTheatres] method.
     */
    fun loadMoreMoviesInTheatres(): Completable

    /**
     * Stream informing whether more content is being loaded to the Now Playing list at the moment.
     */
    val isLoadingMoreMoviesInTheatres: Observable<Boolean>

    /**
     * Stream notifying that the loading of more data into the Now Playing list has failed.
     */
    val loadingMoreMoviesInTheatresErrors: Observable<Throwable>

    /**
     * Adds the provided movie to the Favourites.
     * @param movieId Identifier of the movie to be added to the Favourites.
     */
    fun addMovieToFavourites(movieId: Long): Completable

    /**
     * Removes the provided movie from the Favourites.
     * @param movieId Identifier of the movie to be removed from the Favourites.
     */
    fun removeMovieFromFavourites(movieId: Long): Completable

    /**
     * Ever updating stream which dispatches information whether the pointed movie is in Favourites.
     * @param movieId Identifier of the movie the client is interested in.
     */
    fun isMovieInFavourites(movieId: Long): Observable<Boolean>

    /**
     * Searches the whole movies database for the movie using the provided query (usually part of the title).
     * @param query text query to be used while searching for the movie.
     */
    fun searchForTheMovie(query: String): Observable<List<Movie>>
}