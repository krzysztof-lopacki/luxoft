package com.luxoft.codingchallenge.modules

import android.content.Context
import androidx.room.Room
import com.luxoft.codingchallenge.R
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.services.moviesrepository.Configuration
import com.luxoft.codingchallenge.services.moviesrepository.MoviesInTheatresUpdater
import com.luxoft.codingchallenge.services.moviesrepository.RoomBasedMoviesRepository
import com.luxoft.codingchallenge.services.moviesrepository.db.MoviesDatabase
import com.pioneer.drv.sdk.rxjava.createSingleThreadScheduler
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val moviesRepositoryModule = module {
    val moduleQualifier = named("MoviesRepository")

    single {
        getDatabase(androidContext()).getMoviesDao()
    }

    single {
        getDatabase(androidContext()).getFavouritesDao()
    }

    single(moduleQualifier) {
        createSingleThreadScheduler("scheduler-db")
    }

    single(moduleQualifier) {
        val context = androidContext()
        context.getSharedPreferences(context.getString(R.string.config_movies_repository_shared_preferences_file), Context.MODE_PRIVATE)
    }

    single {
        MoviesInTheatresUpdater(get(), get(), get(moduleQualifier), get(), get(moduleQualifier))
    }

    single<MoviesRepository> {
        RoomBasedMoviesRepository(get(), get(), get(), get(), get(moduleQualifier), get())
    }

    single {
        val resources = androidApplication().resources
        Configuration (
            resources.getInteger(R.integer.config_movies_repository_page_size),
            resources.getInteger(R.integer.config_movies_repository_first_page_lifetime).toLong()
        )
    }
}

private lateinit var database: MoviesDatabase
private fun getDatabase(applicationContext: Context): MoviesDatabase {
    if (!::database.isInitialized) {
        database = Room.databaseBuilder(applicationContext, MoviesDatabase::class.java, "movies-database").build()
    }
    return database
}