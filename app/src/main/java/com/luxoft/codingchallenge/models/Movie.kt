package com.luxoft.codingchallenge.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Movie (
    val id: Long,
    val title: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val overview: String? = null,
    val releaseDate: Date? = null,
    val voteAverage: Double? = null,
    val isFavourite: Boolean? = null
) : Parcelable