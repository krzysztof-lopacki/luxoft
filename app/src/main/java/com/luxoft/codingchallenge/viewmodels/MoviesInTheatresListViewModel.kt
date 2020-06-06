package com.luxoft.codingchallenge.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.luxoft.codingchallenge.models.LoadingStatus
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.utils.livedata.HandleableEvent
import com.luxoft.codingchallenge.utils.rxjava.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable

class MoviesInTheatresListViewModel(private val moviesRepository: MoviesRepository): ViewModel() {
    private val subscriptions = CompositeDisposable()

    val moviesInTheatres = moviesRepository.moviesInTheatres
        .toLiveData(BackpressureStrategy.LATEST)

    val loadingRecentMoviesInTheatresStatus = moviesRepository.isLoadingRecentMoviesInTheatres
        .map { isLoading -> isLoading.toLoadingStatus() }
        .toLiveData(BackpressureStrategy.LATEST)

    val loadingRecentMoviesErrors = moviesRepository.loadingRecentMoviesInTheatresErrors
        .map { error -> HandleableEvent(error) }
        .toLiveData(BackpressureStrategy.LATEST)

    val loadingMoreMoviesInTheatresStatus = MediatorLiveData<LoadingStatus>()

    init {
        val loadingMoreMoviesInTheatresBiStatus = moviesRepository.isLoadingMoreMoviesInTheatres
            .map { isLoading -> isLoading.toLoadingStatus() }
            .toLiveData(BackpressureStrategy.LATEST)
        loadingMoreMoviesInTheatresStatus.addSource(loadingMoreMoviesInTheatresBiStatus) {
            loadingMoreMoviesInTheatresStatus.value = it
        }
        val loadingMoreMoviesInTheatresErrorStatus = moviesRepository.loadingMoreMoviesInTheatresErrors
            .map { LoadingStatus.FAILED }
            .toLiveData(BackpressureStrategy.LATEST)
        loadingMoreMoviesInTheatresStatus.addSource(loadingMoreMoviesInTheatresErrorStatus) {
            loadingMoreMoviesInTheatresStatus.value = it
        }
    }

    fun loadMoreMoviesInTheatres() {
        moviesRepository.loadMoreMoviesInTheatres().subscribeAndIgnoreErrors(subscriptions)
    }

    fun loadRecentMoviesInTheatres() {
        moviesRepository.loadRecentMoviesInTheatres().subscribeAndIgnoreErrors(subscriptions)
    }

    override fun onCleared() {
        subscriptions.clear()
    }
}

private fun Completable.subscribeAndIgnoreErrors(subscriptionManager: CompositeDisposable) {
    subscriptionManager.add(onErrorComplete().subscribe())
}

private fun Boolean.toLoadingStatus(): LoadingStatus {
    return if (this) LoadingStatus.LOADING else LoadingStatus.NOT_LOADING
}