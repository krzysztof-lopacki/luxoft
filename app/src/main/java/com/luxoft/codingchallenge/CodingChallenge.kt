package com.luxoft.codingchallenge

import android.app.Application
import android.util.Log
import com.luxoft.codingchallenge.modules.moviesRepositoryModule
import com.luxoft.codingchallenge.modules.theMovieDBModule
import com.luxoft.codingchallenge.modules.viewModelsModule
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CodingChallenge : Application() {

    override fun onCreate() {
        super.onCreate()

        manageRxJavaUndeliverableExceptions()

        startKoin {
            androidLogger()
            androidContext(this@CodingChallenge)
            modules(theMovieDBModule, moviesRepositoryModule, viewModelsModule)
        }
    }

    /**
     * #REVIEW# Unfortunately in the RxJava2 world it possible that an exception may occur when no one
     * longer listens to the failing stream.
     * Consider a scenario:
     * 1.   Imagine an Unzipper which decompresses archives.
     *      The unzip method loos like this: fun unzip(file: File): Completable.
     * 2    Client subscribes on the UI thread to the unziping Completable.
     * 3.   Unzip operation starts on the IO thread.
     * 4.   Client unsubscribes on the UI thread from the unzip Completable.
     * 5.   At this point no one listensto the unzipping stream (Completable) any more.
     * 6.   Unzipping operations cancels as the are no subscribers.
     * 7.   An error happens during the cancelling of the unzip operation.
     * 8a.  Error is NOT handled by any client -> It is wrapped in [UndeliverableException] and thrown
     *      As a result application crashes.
     * OR
     * 8b.  A global error handler is registered and handles all [UndeliverableException]s.
     *      Other error are throw as usually.
     */
    private fun manageRxJavaUndeliverableExceptions() {
        RxJavaPlugins.setErrorHandler errorHandling@{ error ->
            synchronized(this) {
                if (error is UndeliverableException) {
                    Log.e("#RxJava", "UnhandledExceptionsHandler caught UndeliverableException", error)
                } else {
                    throw error
                }
            }
        }
    }
}