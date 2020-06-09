package com.luxoft.codingchallenge.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.utils.livedata.HandleableEvent
import com.luxoft.codingchallenge.utils.rxjava.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

private const val TYPING_SAMPLING_PERIOD = 600L

class SearchViewModel(moviesRepository: MoviesRepository) : ViewModel() {
    private val searchInputStream = PublishSubject.create<String>()

    val searchSuggestions = searchInputStream.sample(TYPING_SAMPLING_PERIOD, TimeUnit.MILLISECONDS)
        .switchMap { text ->
            moviesRepository.searchForTheMovie(text)
                .onErrorReturn {
                    emptyList()
                }
        }
        .toLiveData(BackpressureStrategy.LATEST)

    val searchInput = MutableLiveData<HandleableEvent<String>>()

    val onSearchSuggestionClicked = MutableLiveData<HandleableEvent<Movie?>>()

    val showMovieDetailsRequests = onSearchSuggestionClicked

    init {
        // #REVIEW#: It is ok to observe itself. No references are leaked.
        searchInput.observeForever { input ->
            input.handle { text ->
                searchInputStream.onNext(text)
                true
            }
        }
    }
}