package com.luxoft.codingchallenge.services.themoviedb

import android.content.Context
import com.luxoft.codingchallenge.R
import okhttp3.HttpUrl
import java.util.*

class ResourceBasedConfiguration(context: Context): Configuration {
    override val serverUrl: HttpUrl = HttpUrl.get(context.getString(R.string.config_moviedb_url))
    override val language: String = Locale.getDefault().language
    override val apiKey: String = context.getString(R.string.config_moviedb_api_key)
    override val imageUrlPattern: String = context.getString(R.string.config_moviedb_image_url_pattern)
    override val backdropSizes: IntArray = context.resources.getIntArray(R.array.config_moviedb_image_backdrop_sizes)
    override val posterSizes: IntArray = context.resources.getIntArray(R.array.config_moviedb_image_poster_sizes)
    override val imageUrlPatternOriginalWidthChunk: String = context.getString(R.string.config_moviedb_image_original_size_chunk)
    override val imageUrlPatternFixedWidthChunkPattern: String = context.getString(R.string.config_moviedb_image_fixed_width_chunk_pattern)
}