package com.luxoft.codingchallenge.services.api

import androidx.paging.PagedList
import com.luxoft.codingchallenge.models.Movie
import io.reactivex.Completable
import io.reactivex.Observable

interface MoviesRepository {

    val moviesInTheatres: Observable<PagedList<Movie>>

    fun loadRecentMoviesInTheatres(): Completable

    val isLoadingRecentMoviesInTheatres: Observable<Boolean>

    val loadingRecentMoviesInTheatresErrors: Observable<Throwable>

    fun loadMoreMoviesInTheatres(): Completable

    val isLoadingMoreMoviesInTheatres: Observable<Boolean>

    val loadingMoreMoviesInTheatresErrors: Observable<Throwable>

    fun addMovieToFavourites(movieId: Long): Completable

    fun removeMovieFromFavourites(movieId: Long): Completable

    fun isMovieInFavourites(movieId: Long): Observable<Boolean>

    fun searchForTheMovie(query: String): Observable<List<Movie>>
}