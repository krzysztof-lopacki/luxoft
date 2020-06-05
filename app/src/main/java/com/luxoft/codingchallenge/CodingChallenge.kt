package com.luxoft.codingchallenge

import android.app.Application
import com.luxoft.codingchallenge.modules.TheMovieDBModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CodingChallenge : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CodingChallenge)
            modules(TheMovieDBModule.createModule(this@CodingChallenge))
        }
    }
}