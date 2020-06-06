package com.luxoft.codingchallenge.services.moviesrepository

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.moviesrepository.db.MovieEntity
import org.junit.Assert.*
import org.junit.Test

class MoviesInTheatresUpdaterTest {

    @Test
    fun isTailOfTest() {
        assertTrue(isTailOf(listOf(1, 2, 3, 4, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertTrue(isTailOf(listOf(4, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertTrue(isTailOf(listOf(3, 4, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertTrue(isTailOf(emptyList(), emptyList()))
        assertTrue(isTailOf(emptyList(), listOf(1, 2, 3, 4, 5, 6).toMovies()))

        assertFalse(isTailOf(listOf(14, 15, 16).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(4, 5).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(4, 6, 5).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(4, 5, 6, 7).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(4, 5, 7, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(3, 5, 4, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(3, 4, 5, 5, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(3, 4, 4, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(4, 7, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(0, 1, 2, 3, 4, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(1, 2, 3).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(6, 5, 4).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isTailOf(listOf(1, 2, 3).toMovieEntities(), emptyList()))
    }

    @Test
    fun isHeadOfTest() {
        assertTrue(isHeadOf(listOf(1, 2, 3, 4, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertTrue(isHeadOf(listOf(1, 2, 3).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertTrue(isHeadOf(listOf(1, 2, 3, 4).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertTrue(isHeadOf(emptyList(), emptyList()))
        assertTrue(isHeadOf(emptyList(), listOf(1, 2, 3, 4, 5, 6).toMovies()))

        assertFalse(isHeadOf(listOf(14, 15, 16).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(2, 2).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(2, 1, 3).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(0, 1, 2, 3).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 7, 2, 3).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 3, 2, 4).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 2, 3, 3, 3, 4).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 2, 2, 4).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 7, 3).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 2, 3, 4, 5, 6, 7).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(4, 5, 6).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(3, 2, 1).toMovieEntities(), listOf(1, 2, 3, 4, 5, 6).toMovies()))
        assertFalse(isHeadOf(listOf(1, 2, 3).toMovieEntities(), emptyList()))
    }

    @Test
    fun searchForGapsTest() {
        assertFalse(searchForGaps(listOf(1,2,3).toMovieEntities()))
        assertFalse(searchForGaps(listOf(-3,-2,-1).toMovieEntities()))
        assertFalse(searchForGaps(listOf(1).toMovieEntities()))
        assertFalse(searchForGaps(listOf<Int>().toMovieEntities()))

        assertTrue(searchForGaps(listOf(1,3).toMovieEntities()))
        assertTrue(searchForGaps(listOf(3,2,1).toMovieEntities()))
    }

}

fun List<Int>.toMovies(): List<Movie> {
    return map {
        Movie(it.toLong(), "")
    }
}

fun List<Int>.toMovieEntities(): List<MovieEntity> {
    return map {
        MovieEntity(null, Movie(it.toLong(), ""), it.toLong())
    }
}
