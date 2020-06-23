package com.luxoft.codingchallenge.services.themoviedb

import android.content.Context
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MovieImageUrlResolver
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

private val MOVIE = Movie(1, "title", "/poster.jpg", "/backdrop.jpg", null, null, null, null)

class TheMovieDBImageUrlsResolverTest {
    private lateinit var sut: MovieImageUrlResolver

    @Before
    fun setup() {
        val context = Mockito.mock(Context::class.java)

        val configuration = object : Configuration {
            override val serverUrl = "http://test.com".toHttpUrl()
            override val language = "pl-PL"
            override val apiKey = "someapikey"
            override val imageUrlPattern: String = "test/%1\$s%2\$s"
            override val backdropSizes: IntArray = intArrayOf(100, 200, 300, 400, 500)
            override val posterSizes: IntArray = intArrayOf(10, 20, 30, 40, 50)
            override val imageUrlPatternFixedWidthChunkPattern = "w%1\$s"
            override val imageUrlPatternOriginalWidthChunk = "original"
        }
        sut = TheMovieDBImageUrlsResolver(context, configuration)
    }

    @Test
    fun getImageUrl() {
        assertEquals("test/w20/poster.jpg", sut.getImageUrl(MOVIE.posterPath, MovieImageUrlResolver.ImageType.POSTER, 15))
        assertEquals("test/original/poster.jpg", sut.getImageUrl(MOVIE.posterPath, MovieImageUrlResolver.ImageType.POSTER, 150))
        assertEquals("test/w10/poster.jpg", sut.getImageUrl(MOVIE.posterPath, MovieImageUrlResolver.ImageType.POSTER, 0))

        assertEquals("test/w200/backdrop.jpg", sut.getImageUrl(MOVIE.backdropPath, MovieImageUrlResolver.ImageType.BACKDROP, 150))
        assertEquals("test/original/backdrop.jpg", sut.getImageUrl(MOVIE.backdropPath, MovieImageUrlResolver.ImageType.BACKDROP, 1500))
        assertEquals("test/w100/backdrop.jpg", sut.getImageUrl(MOVIE.backdropPath, MovieImageUrlResolver.ImageType.BACKDROP, 0))
    }
}