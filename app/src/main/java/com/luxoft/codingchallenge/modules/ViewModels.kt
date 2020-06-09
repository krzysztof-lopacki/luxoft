package com.luxoft.codingchallenge.modules

import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.viewmodels.MovieDetailsViewModel
import com.luxoft.codingchallenge.viewmodels.MoviesInTheatresListViewModel
import com.luxoft.codingchallenge.viewmodels.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module injecting view models.
 */
val viewModelsModule = module {
    viewModel {
        MoviesInTheatresListViewModel(get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        (movie: Movie) -> MovieDetailsViewModel(movie, get())
    }
}