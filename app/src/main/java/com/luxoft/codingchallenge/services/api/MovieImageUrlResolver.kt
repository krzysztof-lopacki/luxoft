package com.luxoft.codingchallenge.services.api

import androidx.annotation.DimenRes

interface MovieImageUrlResolver {

    fun getImageUrl(imageFileName: String?, type: ImageType, widthHint: Int): String?

    fun getImageUrlRes(imageFileName: String?, type: ImageType, @DimenRes widthHint: Int): String?

    enum class ImageType {
        POSTER,
        BACKDROP
    }
}