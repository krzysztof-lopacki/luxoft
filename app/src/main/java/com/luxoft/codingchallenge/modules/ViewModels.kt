package com.luxoft.codingchallenge.modules

import com.luxoft.codingchallenge.viewmodels.MoviesInTheatresListViewModel
import com.luxoft.codingchallenge.viewmodels.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel {
        MoviesInTheatresListViewModel(get())
    }

    viewModel {
        SearchViewModel(get())
    }
}