package com.luxoft.codingchallenge.modules

import android.content.Context
import com.luxoft.codingchallenge.R
import com.luxoft.codingchallenge.services.api.MoviesInTheatersFetcher
import com.luxoft.codingchallenge.services.api.MoviesSearch
import com.luxoft.codingchallenge.services.themoviedb.Configuration
import com.luxoft.codingchallenge.services.themoviedb.TheMovieDBClient
import com.luxoft.codingchallenge.services.themoviedb.createTheMovieDBConverterFactory
import okhttp3.HttpUrl
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import retrofit2.Converter
import java.util.*


object TheMovieDBModule {
    private val moduleQualifier = named("TheMovieDB")

    fun createModule(context: Context) = module {

        single {
            Configuration(HttpUrl.get(context.getString(R.string.config_moviedb_url)),
                Locale.getDefault().language,
                context.getString(R.string.config_moviedb_api_key))
        }

        single<Converter.Factory> (moduleQualifier) {
            createTheMovieDBConverterFactory()
        }

        single {
            TheMovieDBClient(get(moduleQualifier), get())
        } binds arrayOf(MoviesSearch::class, MoviesInTheatersFetcher::class)

    }
}