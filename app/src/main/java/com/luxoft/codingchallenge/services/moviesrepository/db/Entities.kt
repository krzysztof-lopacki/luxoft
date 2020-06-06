package com.luxoft.codingchallenge.services.moviesrepository.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luxoft.codingchallenge.models.Movie

@Entity(tableName = "movies")
data class MovieEntity(
    /**
     * Internal Room index.
     */
    @PrimaryKey(autoGenerate = true)
    val dbId: Long?,

    /**
     * Movie to be stored.
     */
    @Embedded
    val movie: Movie,

    /**
     * Index used for sorting movies.
     * Unfortunately there is no a field in the [Movie] class that can be used for sorting.
     */
    val sortingIndex: Long
)

@Entity(tableName = "favourites")
data class FavouriteMovieEntity (
    /**
     * Internal Room index.
     */
    @PrimaryKey(autoGenerate = true)
    val dbId: Long?,

    /**
     * Identifier of the movie that would be added to the favourites.
     */
    val movieId: Long,

    /**
     * Specifies whether the movie is added to the favourites.
     * In fact, all the the movies that are added to the 'favourites' will have this column set to true.
     */
    val isFavourite: Boolean
)