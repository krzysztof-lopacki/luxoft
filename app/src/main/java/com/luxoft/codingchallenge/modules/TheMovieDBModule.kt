package com.luxoft.codingchallenge.modules

import com.luxoft.codingchallenge.services.api.MovieImageUrlResolver
import com.luxoft.codingchallenge.services.api.MoviesInTheatersFetcher
import com.luxoft.codingchallenge.services.api.MoviesSearch
import com.luxoft.codingchallenge.services.themoviedb.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import retrofit2.Converter

val theMovieDBModule = module {
    val moduleQualifier = named("TheMovieDB")

    single<Configuration> {
        ResourceBasedConfiguration(androidContext())
    }

    single<Converter.Factory> (moduleQualifier) {
        createTheMovieDBConverterFactory()
    }

    single {
        TheMovieDBClient(get(moduleQualifier), get())
    } binds arrayOf(MoviesSearch::class, MoviesInTheatersFetcher::class)

    single<MovieImageUrlResolver> {
        TheMovieDBImageUrlsResolver(androidContext(), get())
    }
}