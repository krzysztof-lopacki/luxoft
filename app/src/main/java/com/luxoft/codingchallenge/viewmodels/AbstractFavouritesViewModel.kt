package com.luxoft.codingchallenge.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.utils.livedata.HandleableEvent
import com.luxoft.codingchallenge.utils.rxjava.subscribeAndIgnoreErrors
import io.reactivex.disposables.CompositeDisposable

abstract class AbstractFavouritesViewModel(private val moviesRepository: MoviesRepository): ViewModel() {
    private val subscriptions = CompositeDisposable()

    val onAddedToFavourites = MutableLiveData<HandleableEvent<Movie?>>()

    val onRemovedFromFavourites = MutableLiveData<HandleableEvent<Movie?>>()

    fun onToggleFavouriteClicked(movie: Movie?) {
        if (movie == null) {
            return
        }
        if (movie.isFavourite == true) {
            moviesRepository.removeMovieFromFavourites(movie.id)
                .doOnComplete {
                    onRemovedFromFavourites.postValue(HandleableEvent(movie))
                }
                .subscribeAndIgnoreErrors(subscriptions)
        } else {
            moviesRepository.addMovieToFavourites(movie.id)
                .doOnComplete {
                    onAddedToFavourites.postValue(HandleableEvent(movie))
                }
                .subscribeAndIgnoreErrors(subscriptions)
        }
    }

    override fun onCleared() {
        subscriptions.clear()
    }
}