package com.luxoft.codingchallenge.services.themoviedb

import okhttp3.HttpUrl

interface Configuration {
    val serverUrl: HttpUrl
    val language: String
    val apiKey: String
    val imageUrlPattern: String
    val imageUrlPatternFixedWidthChunkPattern: String
    val backdropSizes: IntArray
    val posterSizes: IntArray
    val imageUrlPatternOriginalWidthChunk: String
}