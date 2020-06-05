package com.luxoft.codingchallenge.services.themoviedb

import okhttp3.HttpUrl


data class Configuration (
    val serverUrl: HttpUrl,
    val language: String,
    val apiKey: String
)