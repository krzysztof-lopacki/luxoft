package com.luxoft.codingchallenge.services.moviesrepository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MovieEntity::class, FavouriteMovieEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao
    abstract fun getFavouritesDao(): FavouritesDao
}