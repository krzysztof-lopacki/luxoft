package com.luxoft.codingchallenge.models

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalItems: Int,
    val totalPages: Int
)