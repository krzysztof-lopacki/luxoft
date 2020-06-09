package com.luxoft.codingchallenge.utils.ui

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.luxoft.codingchallenge.R

/**
 * Creates a configured [Toast].
 *
 * @param applicationContext Application context.
 * @param message Message to be shown on toast.
 * @param duration How long the [Toast] should be shown.
 * @param background resource used as background
 * @param layout Layout of the Toast. Should be a child of [TextView].
 */
fun createToast(applicationContext: Context,
                @StringRes message: Int,
                duration: Int = Toast.LENGTH_SHORT,
                @DrawableRes background: Int = R.drawable.background_red_rounded,
                @LayoutRes layout: Int = R.layout.toast_layout): Toast {
    val toastElements = createToast(applicationContext, message, duration, layout)
    toastElements.first.background = ContextCompat.getDrawable(applicationContext, background)
    return toastElements.second
}

/**
 * Creates a configured [Toast].
 *
 * @param applicationContext Application context.
 * @param message Message to be shown on toast.
 * @param duration How long the [Toast] should be shown.
 * @param backgroundColor color resource used as background
 * @param layout Layout of the Toast. Should be a child of [TextView].
 */
fun createToastWithPlainBackground(applicationContext: Context,
                @StringRes message: Int,
                duration: Int = Toast.LENGTH_SHORT,
                @ColorRes backgroundColor: Int,
                @LayoutRes layout: Int = R.layout.toast_layout): Toast {
    val toastElements = createToast(applicationContext, message, duration, layout)
    toastElements.first.setBackgroundColor(ContextCompat.getColor(applicationContext, backgroundColor))
    return toastElements.second
}

/**
 * Creates a configured [Toast].
 *
 * @param applicationContext Application context.
 * @param message Message to be shown on toast.
 * @param duration How long the [Toast] should be shown.
 * @param layout Layout of the Toast. Should be a child of [TextView].
 */
fun createToast(applicationContext: Context,
                @StringRes message: Int,
                duration: Int,
                @LayoutRes layout: Int): Pair<TextView, Toast> {
    val toastView = LayoutInflater.from(applicationContext).inflate(layout, null) as TextView
    toastView.text = applicationContext.resources.getString(message)
    val toast = Toast(applicationContext)
    toast.setGravity(Gravity.BOTTOM, 0, applicationContext.resources.getDimensionPixelSize(R.dimen.toast_bottom_margin))
    toast.duration = duration
    toast.view = toastView
    return toastView to toast
}