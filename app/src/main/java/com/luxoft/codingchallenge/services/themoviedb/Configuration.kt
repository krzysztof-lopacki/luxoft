package com.luxoft.codingchallenge.services.themoviedb

import okhttp3.HttpUrl

interface Configuration {
    /**
     * The Movie DB API server url.
     */
    val serverUrl: HttpUrl

    /**
     * Language used while requesting data from The Movie DB.
     */
    val language: String

    /**
     * Developer key required to access The Movie DB API.
     */
    val apiKey: String

    /**
     * Url pattern used to resolve paths to the images hosted by The Movie DB.
     * Check the https://developers.themoviedb.org/3/getting-started/images for more details.
     */
    val imageUrlPattern: String

    /**
     * A pattern of the size chunk that is a part of [imageUrlPattern].
     * Check the https://developers.themoviedb.org/3/getting-started/images for more details.
     */
    val imageUrlPatternFixedWidthChunkPattern: String

    /**
     * The list of the possible widths of the backdrop images provided by The Movie DB.
     */
    val backdropSizes: IntArray

    /**
     * The list of the possible widths of the poster images provided by The Movie DB.
     */
    val posterSizes: IntArray

    /**
     * A token that can be used as size chunk of the [imageUrlPattern] while resolving the image url
     * that instructs the server to download the image in the original resolution.
     */
    val imageUrlPatternOriginalWidthChunk: String
}