package com.luxoft.codingchallenge.services.api

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import io.reactivex.rxjava3.core.Single

interface MoviesSearch {
    fun searchForTheMovie(query: String, page: Int): Single<Page<Movie>>
}