package com.luxoft.codingchallenge.services.themoviedb

import com.google.gson.stream.MalformedJsonException
import com.luxoft.codingchallenge.testutils.ReadFromResources.readTestResourceFile
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.net.HttpURLConnection

private const val SERVER_DATE_PATTERN = "%1\$tY-%1\$tm-%1\$td"

private const val SERVER_URL = "/"

private const val NOW_PLAYING_PATH = "/movie/now_playing"
private const val SEARCH_FOR_MOVIE_PATH = "/search/movie"
private const val API_KEY_PARAM = "api_key="
private const val API_KEY_VALUE = "validKey"
private const val LANGUAGE_PARAM = "language="
private const val LANGUAGE_VALUE = "en-US"
private const val PAGE_PARAM = "page="
private const val PAGE_VALUE = 1
private const val QUERY_PARAM = "query="
private const val QUERY_VALUE = "la land"
private const val QUERY_ENCODED_VALUE = "la%20land"

private const val SERVER_RESPONSE_1_ITEMS_COUNT = 20
private const val SERVER_RESPONSE_1_PAGE = 1
private const val SERVER_RESPONSE_1_TOTAL_PAGES = 31
private const val SERVER_RESPONSE_1_TOTAL_ITEMS = 614
private const val SERVER_RESPONSE_1_MOVIE_1_ID = 338762L
private const val SERVER_RESPONSE_1_MOVIE_1_TITLE = "Bloodshot"
private const val SERVER_RESPONSE_1_MOVIE_1_VOTE_AVERAGE = 7.0
private const val SERVER_RESPONSE_1_MOVIE_1_POSTER_PATH = "/8WUVHemHFH2ZIP6NWkwlHWsyrEL.jpg"
private const val SERVER_RESPONSE_1_MOVIE_1_BACKDROP_PATH = "/ocUrMYbdjknu2TwzMHKT9PBBQRw.jpg"
private const val SERVER_RESPONSE_1_MOVIE_1_OVERVIEW = "Fake overview."
private const val SERVER_RESPONSE_1_MOVIE_1_RELEASE_DATE = "2020-03-05"
private const val SERVER_RESPONSE_2_MOVIE_1_ID = 338762L
private const val SERVER_RESPONSE_2_MOVIE_1_TITLE = "Bloodshot"
private const val SERVER_RESPONSE_3_MOVIE_1_ID = 338762L
private const val SERVER_RESPONSE_3_MOVIE_1_TITLE = "Bloodshot"

class TheMovieDBClientTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var sut: TheMovieDBClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val configuration = Configuration(mockWebServer.url(SERVER_URL), LANGUAGE_VALUE, API_KEY_VALUE)
        sut = TheMovieDBClient(createTheMovieDBConverterFactory(), configuration)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun nowPlaying_validateHeaders() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path != null
                    && request.path!!.startsWith(NOW_PLAYING_PATH)
                    && request.path!!.contains("$API_KEY_PARAM$API_KEY_VALUE")
                    && request.path!!.contains("$LANGUAGE_PARAM$LANGUAGE_VALUE")
                    && request.path!!.contains("$PAGE_PARAM$PAGE_VALUE")) {
                    MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .setBody(readTestResourceFile("server_response_0.json"))
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }

        val testObserver = sut.getMoviesInTheaters(PAGE_VALUE).test()
        testObserver.await()
        testObserver.assertComplete()
    }

    @Test
    fun nowPlaying_validateBody_completeData() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_1.json")))

        val testObserver = sut.getMoviesInTheaters(PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        assertEquals(SERVER_RESPONSE_1_ITEMS_COUNT, result.items.size)
        assertEquals(SERVER_RESPONSE_1_PAGE, result.page)
        assertEquals(SERVER_RESPONSE_1_TOTAL_PAGES, result.totalPages)
        assertEquals(SERVER_RESPONSE_1_TOTAL_ITEMS, result.totalItems)

        assertEquals(SERVER_RESPONSE_1_MOVIE_1_ID, result.items[0].id)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_TITLE, result.items[0].title)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_VOTE_AVERAGE, result.items[0].voteAverage)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_POSTER_PATH, result.items[0].posterPath)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_BACKDROP_PATH, result.items[0].backdropPath)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_OVERVIEW, result.items[0].overview)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_RELEASE_DATE, String.format(SERVER_DATE_PATTERN, result.items[0].releaseDate))
    }


    @Test
    fun nowPlaying_validateBody_optionalFieldsNull() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_2.json")))

        val testObserver = sut.getMoviesInTheaters(PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        assertEquals(SERVER_RESPONSE_2_MOVIE_1_ID, result.items[0].id)
        assertEquals(SERVER_RESPONSE_2_MOVIE_1_TITLE, result.items[0].title)
        assertEquals(null, result.items[0].voteAverage)
        assertEquals(null, result.items[0].posterPath)
        assertEquals(null, result.items[0].backdropPath)
        assertEquals(null, result.items[0].overview)
        assertEquals(null, result.items[0].releaseDate)
    }

    @Test
    fun nowPlaying_validateBody_optionalFieldsMissing() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_3.json")))

        val testObserver = sut.getMoviesInTheaters(PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        assertEquals(SERVER_RESPONSE_3_MOVIE_1_ID, result.items[0].id)
        assertEquals(SERVER_RESPONSE_3_MOVIE_1_TITLE, result.items[0].title)
        assertEquals(null, result.items[0].voteAverage)
        assertEquals(null, result.items[0].posterPath)
        assertEquals(null, result.items[0].backdropPath)
        assertEquals(null, result.items[0].overview)
        assertEquals(null, result.items[0].releaseDate)
    }

    @Test
    fun nowPlaying_validateResponse_malformedJson() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_4.json")))

        val testObserver = sut.getMoviesInTheaters(PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertError(MalformedJsonException::class.java)
    }

    @Test
    fun nowPlaying_validateResponse_404() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(404))

        val testObserver = sut.getMoviesInTheaters(PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertError { error ->
            error is HttpException && error.code() == 404
        }
    }

    @Test
    fun searchForMovie_validateHeaders() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return if (request.path != null
                    && request.path!!.startsWith(SEARCH_FOR_MOVIE_PATH)
                    && request.path!!.contains("$API_KEY_PARAM$API_KEY_VALUE")
                    && request.path!!.contains("$LANGUAGE_PARAM$LANGUAGE_VALUE")
                    && request.path!!.contains("$PAGE_PARAM$PAGE_VALUE")
                    && request.path!!.contains("$QUERY_PARAM$QUERY_ENCODED_VALUE")) {
                    MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                        .setBody(readTestResourceFile("server_response_0.json"))
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }

        val testObserver = sut.searchForTheMovie(QUERY_VALUE, PAGE_VALUE).test()
        testObserver.await()
        testObserver.assertComplete()
    }

    @Test
    fun searchForMovie_validateBody_completeData() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_1.json")))

        val testObserver = sut.searchForTheMovie(QUERY_VALUE, PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        assertEquals(SERVER_RESPONSE_1_ITEMS_COUNT, result.items.size)
        assertEquals(SERVER_RESPONSE_1_PAGE, result.page)
        assertEquals(SERVER_RESPONSE_1_TOTAL_PAGES, result.totalPages)
        assertEquals(SERVER_RESPONSE_1_TOTAL_ITEMS, result.totalItems)

        assertEquals(SERVER_RESPONSE_1_MOVIE_1_ID, result.items[0].id)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_TITLE, result.items[0].title)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_VOTE_AVERAGE, result.items[0].voteAverage)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_POSTER_PATH, result.items[0].posterPath)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_BACKDROP_PATH, result.items[0].backdropPath)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_OVERVIEW, result.items[0].overview)
        assertEquals(SERVER_RESPONSE_1_MOVIE_1_RELEASE_DATE, String.format(SERVER_DATE_PATTERN, result.items[0].releaseDate))
    }


    @Test
    fun searchForMovie_validateBody_optionalFieldsNull() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_2.json")))

        val testObserver = sut.searchForTheMovie(QUERY_VALUE, PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        assertEquals(SERVER_RESPONSE_2_MOVIE_1_ID, result.items[0].id)
        assertEquals(SERVER_RESPONSE_2_MOVIE_1_TITLE, result.items[0].title)
        assertEquals(null, result.items[0].voteAverage)
        assertEquals(null, result.items[0].posterPath)
        assertEquals(null, result.items[0].backdropPath)
        assertEquals(null, result.items[0].overview)
        assertEquals(null, result.items[0].releaseDate)
    }

    @Test
    fun searchForMovie_validateBody_optionalFieldsMissing() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_3.json")))

        val testObserver = sut.searchForTheMovie(QUERY_VALUE, PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertComplete()
        testObserver.assertValueCount(1)

        val result = testObserver.values()[0]
        assertEquals(SERVER_RESPONSE_3_MOVIE_1_ID, result.items[0].id)
        assertEquals(SERVER_RESPONSE_3_MOVIE_1_TITLE, result.items[0].title)
        assertEquals(null, result.items[0].voteAverage)
        assertEquals(null, result.items[0].posterPath)
        assertEquals(null, result.items[0].backdropPath)
        assertEquals(null, result.items[0].overview)
        assertEquals(null, result.items[0].releaseDate)
    }

    @Test
    fun searchForMovie_validateResponse_malformedJson() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readTestResourceFile("server_response_4.json")))

        val testObserver = sut.searchForTheMovie(QUERY_VALUE, PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertError(MalformedJsonException::class.java)
    }

    @Test
    fun searchForMovie_validateResponse_404() {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(404))

        val testObserver = sut.searchForTheMovie(QUERY_VALUE, PAGE_VALUE).test()
        testObserver.await()

        testObserver.assertError { error ->
            error is HttpException && error.code() == 404
        }
    }
}