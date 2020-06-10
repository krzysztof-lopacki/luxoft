package com.luxoft.codingchallenge.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.luxoft.codingchallenge.R
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.ui.arguments.ArgumentKeys

/**
 * Activity showing the details of the movies passed as argument.
 */
class MovieDetailsActivity : AppCompatActivity() {
    companion object {
        /**
         * Creates intent starting this activity and passing provided movie as argument.
         */
        @JvmStatic
        fun createStartingIntent(startingActivity: Activity, movie: Movie): Intent {
            val intent = Intent(startingActivity, MovieDetailsActivity::class.java)
            intent.putExtra(ArgumentKeys.ARGUMENT_KEY_MOVIE, movie as Parcelable)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)
    }

    /**
     * Let's pass arguments from the Intent to each attaching fragment.
     */
    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        fragment.arguments = intent.extras
    }
}