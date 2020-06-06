package com.luxoft.codingchallenge.modules

import com.luxoft.codingchallenge.viewmodels.MoviesInTheatresListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel {
        MoviesInTheatresListViewModel(get())
    }
}