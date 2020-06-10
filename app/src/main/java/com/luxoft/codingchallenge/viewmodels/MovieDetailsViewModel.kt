package com.luxoft.codingchallenge.viewmodels

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MoviesRepository
import com.luxoft.codingchallenge.utils.rxjava.toLiveData
import io.reactivex.BackpressureStrategy

/**
 * View model providing up-to-date movie description.
 */
class MovieDetailsViewModel(private val movieToDisplay: Movie, moviesRepository: MoviesRepository): AbstractFavouritesViewModel(moviesRepository) {
    /**
     * Stream with a movie description which is updated each time any movie related data is changed,
     * e.g. movie is added/removed from favourites.
     */
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