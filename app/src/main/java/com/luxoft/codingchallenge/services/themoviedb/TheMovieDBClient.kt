package com.luxoft.codingchallenge.services.themoviedb

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.models.Page
import com.luxoft.codingchallenge.services.api.MoviesInTheatersFetcher
import com.luxoft.codingchallenge.services.api.MoviesSearch
import com.luxoft.codingchallenge.utils.network.enableTls12OnPreLollipop
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val SEARCH = "search/movie"
private const val NOW_PLAYING = "movie/now_playing"
private const val PARAM_API_KEY = "api_key"
private const val PARAM_LANGUAGE = "language"
private const val PARAM_PAGE = "page"
private const val PARAM_SEARCH_QUERY = "query"

class TheMovieDBClient(factory: Converter.Factory , private val configuration: Configuration) : MoviesSearch, MoviesInTheatersFetcher {
    private val remoteService : RemoteService

    init {
        val okHttpClient = enableTls12OnPreLollipop(OkHttpClient.Builder()).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(configuration.serverUrl)
            .addConverterFactory(factory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttpClient)
            .build()
        remoteService = retrofit.create(RemoteService::class.java)
    }

    override fun searchForTheMovie(query: String, page: Int): Single<Page<Movie>> {
        return remoteService.search(configuration.apiKey, configuration.language, query, page)
    }

    override fun getMoviesInTheaters(page: Int): Single<Page<Movie>> {
        return remoteService.nowPlaying(configuration.apiKey, configuration.language, page)
    }

    private interface RemoteService {
        @GET(NOW_PLAYING)
        fun nowPlaying(@Query(PARAM_API_KEY) apiKey: String,
                       @Query(PARAM_LANGUAGE) language: String,
                       @Query(PARAM_PAGE) page: Int): Single<Page<Movie>>

        @GET(SEARCH)
        fun search(@Query(PARAM_API_KEY) apiKey: String,
                   @Query(PARAM_LANGUAGE) language: String,
                   @Query(PARAM_SEARCH_QUERY) query: String,
                   @Query(PARAM_PAGE) page: Int): Single<Page<Movie>>
    }
}