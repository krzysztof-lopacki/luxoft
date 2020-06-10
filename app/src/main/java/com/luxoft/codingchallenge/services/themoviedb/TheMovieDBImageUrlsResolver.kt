package com.luxoft.codingchallenge.services.themoviedb

import android.content.Context
import androidx.annotation.DimenRes
import com.luxoft.codingchallenge.services.api.MovieImageUrlResolver
import com.luxoft.codingchallenge.services.api.MovieImageUrlResolver.ImageType
import com.luxoft.codingchallenge.utils.arrays.findClosest

/**
 * The resolver that resolves url's to the image resources provided by the The Movie DB online repository.
 */
class TheMovieDBImageUrlsResolver(private val applicationContext: Context,
                                  private val configuration: Configuration) : MovieImageUrlResolver {
    override fun getImageUrl(imageFileName: String?, type: ImageType,
                             widthHint: Int): String? {
        return imageFileName?.let { nonNullFilename ->
            val sizes = getSizes(type)
            val sizeChunk = if (widthHint > sizes.lastOrNull() ?: 0) {
                configuration.imageUrlPatternOriginalWidthChunk
            } else {
                configuration.imageUrlPatternFixedWidthChunkPattern.format(sizes.findClosest(widthHint))
            }
            configuration.imageUrlPattern.format(sizeChunk, nonNullFilename)
        }
    }

    override fun getImageUrlRes(imageFileName: String?, type: ImageType,
                                @DimenRes widthHint: Int): String? {
        return getImageUrl(imageFileName, type, applicationContext.resources.getDimensionPixelSize(widthHint))
    }

    private fun getSizes(type: ImageType): IntArray {
        return if (type == ImageType.POSTER) configuration.posterSizes else configuration.backdropSizes
    }
}

