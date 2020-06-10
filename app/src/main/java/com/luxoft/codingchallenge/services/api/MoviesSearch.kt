package com.luxoft.codingchallenge.services.api

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import io.reactivex.Single

/**
 * Component that searches the movies database and provides paged results.
 */
interface MoviesSearch {

    /**
     * Searches the movies database and provides paged results.
     * @param query Text query used while searching for the movie. Usually part of the title.
     * @param page Page of the search results.
     * @return Stream signaling single page of the results.
     */
    fun searchForTheMovie(query: String, page: Int): Single<Page<Movie>>
}