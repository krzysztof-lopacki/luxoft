package com.luxoft.codingchallenge.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Movie (
    /**
     * Local movie's identifier.
     */
    val id: Long,

    /**
     * Title of the movie.
     */
    val title: String,

    /**
     * Filename of the image with the movie's poster.
     */
    val posterPath: String? = null,

    /**
     * Filename of the image with the movie related backdrop.
     */
    val backdropPath: String? = null,

    /**
     * Short descriptions of the movie.
     */
    val overview: String? = null,

    /**
     * The release date of the movie.
     */
    val releaseDate: Date? = null,

    /**
     * Average rating.
     */
    val voteAverage: Double? = null,

    /**
     * Specifies whether movie is added to the user's Favourites.
     */
    val isFavourite: Boolean? = null
) : Parcelable