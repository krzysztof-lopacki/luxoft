package com.luxoft.codingchallenge.services.themoviedb

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.converter.gson.GsonConverterFactory

private const val SERVER_DATE_PATTERN = "yyyy-MM-dd"

internal fun createTheMovieDBConverterFactory(): GsonConverterFactory {
    val gson = GsonBuilder()
        .setDateFormat(SERVER_DATE_PATTERN)
        .setFieldNamingStrategy { field ->
            when (field.name) {
                "items" -> "results"
                "totalItems" -> "total_results"
                else -> FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
            }
        }
        .create()
    return GsonConverterFactory.create(gson)
}