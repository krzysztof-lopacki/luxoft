package com.luxoft.codingchallenge.services.api

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import io.reactivex.Single

/**
 * Component providing paged list of the movies that are in theatres currently.
 */
interface MoviesInTheatersFetcher {
    /**
     * Provides paged list of the movies that are in theatres currently.
     * @param page Page of the movies list to be downloaded.
     * @return Stream signaling single page of the list of now playing movies.
     */
    fun getMoviesInTheaters(page: Int): Single<Page<Movie>>
}