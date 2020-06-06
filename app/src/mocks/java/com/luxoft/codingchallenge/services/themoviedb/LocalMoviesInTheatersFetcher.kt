package com.luxoft.codingchallenge.services.themoviedb

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import com.luxoft.codingchallenge.services.api.MoviesInTheatersFetcher
import io.reactivex.Single
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.min
import kotlin.random.Random

private const val INITIAL_COUNT = 90L
private const val ADDING_INTERVAL = 5 * 1000L
private const val PAGE_SIZE = 20L

private var ascending = true

class LocalMoviesInTheatersFetcher : MoviesInTheatersFetcher {
    private val startTime = System.currentTimeMillis()

    override fun getMoviesInTheaters(page: Int): Single<Page<Movie>> {
        return Single.fromCallable {
            val itemsAdded = ((System.currentTimeMillis() - startTime) / ADDING_INTERVAL)
            val totalItems = INITIAL_COUNT + itemsAdded
            val totalPages = ceil(totalItems / PAGE_SIZE.toDouble())
            val items = mutableListOf<Movie>()

            val start = -itemsAdded + (page - 1) * PAGE_SIZE
            val end = min(INITIAL_COUNT, start + PAGE_SIZE)

            if (ascending) {
                for (i in start until end) {
                    val id = if (i >= 0) i + 1 else INITIAL_COUNT - i
                    val date = Date(startTime - i * ADDING_INTERVAL)
                    val index = if (i >= 0) i + 1 else i
                    val title = "Movie #%1\$s / %2\$tH:%2\$tM:%2\$tS %2\$tY-%2\$tm-%2\$td".format(index, date)
                    items.add(Movie(id, title, null, null, "Description of the movie $id / $index.", date, Random(id).nextDouble() * 10))
                }
            } else {
                for (i in (end-1) downTo start) {
                    val id = if (i >= 0) i + 1 else INITIAL_COUNT - i
                    val date = Date(startTime - i * ADDING_INTERVAL)
                    val index = if (i >= 0) i + 1 else i
                    val title = "Movie #%1\$s / %2\$tH:%2\$tM:%2\$tS %2\$tY-%2\$tm-%2\$td".format(index, date)
                    items.add(Movie(id, title, null, null, "Description of the movie $id / $index.", date, Random(id).nextDouble() * 10))
                }
            }

            Page(items, page, totalItems.toInt(), totalPages.toInt())
        }
            .delay(3000, TimeUnit.MILLISECONDS)
    }
}