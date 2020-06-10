package com.luxoft.codingchallenge.services.themoviedb

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.luxoft.codingchallenge.models.Page
import retrofit2.converter.gson.GsonConverterFactory

private const val SERVER_DATE_PATTERN = "yyyy-MM-dd"

/**
 * #REVIEW#: This converter is not required at all. I could have used [SerializedName] annotation
 * directly on the [Page] fields but I prefer not to pollute app's models with the annotations
 * that are specific to the 3rd party solutions, external libraries etc. 
 */
internal fun createTheMovieDBConverterFactory(): GsonConverterFactory {
    val gson = GsonBuilder()
        .setDateFormat(SERVER_DATE_PATTERN)
        .setFieldNamingStrategy { field ->
            when (field.name) {
                "items" -> "results" // #REVIEW#: this is obsolete. I just prefer the name 'items' in the Page model.
                "totalItems" -> "total_results" // #REVIEW#: this is obsolete. I just prefer the name 'totalItems' in the Page model.
                else -> FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
            }
        }
        .create()
    return GsonConverterFactory.create(gson)
}