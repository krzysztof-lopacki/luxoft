package com.luxoft.codingchallenge.modules

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import com.luxoft.codingchallenge.services.api.MoviesInTheatersFetcher
import com.luxoft.codingchallenge.services.api.MoviesSearch
import com.luxoft.codingchallenge.services.themoviedb.LocalMoviesInTheatersFetcher
import io.reactivex.Single
import org.koin.dsl.module

val localMoviesProviderModule = module {
    single<MoviesInTheatersFetcher> {
        LocalMoviesInTheatersFetcher()
    }

    single<MoviesSearch> {
        object : MoviesSearch {
            override fun searchForTheMovie(query: String, page: Int): Single<Page<Movie>> {
                TODO("Not yet implemented")
            }
        }
    }
}