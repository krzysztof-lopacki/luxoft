package com.luxoft.codingchallenge.models

/**
 * Data page that is part of some list.
 */
data class Page<T>(
    /**
     * List of the items held by this page.
     */
    val items: List<T>,

    /**
     * Number of the page.
     */
    val page: Int,

    /**
     * Total number of the items on the list that this page is part of.
     */
    val totalItems: Int,

    /**
     * Total number of the pages constituting to the list that this page is part of.
     */
    val totalPages: Int
)