package com.luxoft.codingchallenge.viewmodels

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.utils.rxjava.toLiveData
import io.reactivex.BackpressureStrategy

class MovieDetailsViewModel(private val movieToDisplay: Movie, moviesRepository: MoviesRepository): AbstractFavouritesViewModel(moviesRepository) {
    val movie = moviesRepository.isMovieInFavourites(movieToDisplay.id)
        .map { isFavourite ->
            if (movieToDisplay.isFavourite != isFavourite) {
                movieToDisplay.copy(isFavourite = isFavourite)
            } else {
                movieToDisplay
            }
        }
        .toLiveData(BackpressureStrategy.LATEST)
}