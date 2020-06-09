package com.luxoft.codingchallenge.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.luxoft.codingchallenge.models.Movie

private const val ARGUMENT_MOVIE = "argumentMovie"

class MovieDetailsActivity : AppCompatActivity() {
    companion object {
        @JvmStatic
        fun createStartingIntent(startingActivity: Activity, movie: Movie): Intent {
            val intent = Intent(startingActivity, MovieDetailsActivity::class.java)
            intent.putExtra(ARGUMENT_MOVIE, movie as Parcelable)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("#DETAILS", "MovieDetailsActivity. show movie: ${intent.extras?.getParcelable<Movie>(ARGUMENT_MOVIE)}")

    }

}