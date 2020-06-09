package com.luxoft.codingchallenge.viewmodels

import androidx.lifecycle.ViewModel
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.utils.rxjava.subscribeAndIgnoreErrors
import com.luxoft.codingchallenge.utils.rxjava.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.CompositeDisposable

class MovieDetailsViewModel(private val movieToDisplay: Movie, private val moviesRepository: MoviesRepository): ViewModel() {
    private val subscriptions = CompositeDisposable()

    val movie = moviesRepository.isMovieInFavourites(movieToDisplay.id)
        .map { isFavourite ->
            if (movieToDisplay.isFavourite != isFavourite) {
                movieToDisplay.copy(isFavourite = isFavourite)
            } else {
                movieToDisplay
            }
        }
        .toLiveData(BackpressureStrategy.LATEST)

    override fun onCleared() {
        subscriptions.clear()
    }

    fun onToggleFavouriteClicked() {
        val movieData = movie.value!!
        if (movieData.isFavourite == true) {
            moviesRepository.removeMovieFromFavourites(movieData.id).subscribeAndIgnoreErrors(subscriptions)
        } else {
            moviesRepository.addMovieToFavourites(movieData.id).subscribeAndIgnoreErrors(subscriptions)
        }
    }
}