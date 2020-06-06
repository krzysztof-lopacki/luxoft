package com.luxoft.codingchallenge.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luxoft.codingchallenge.R

/**
 * Main activity holding fragments which provide app features.
 */
class OverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)
    }
}