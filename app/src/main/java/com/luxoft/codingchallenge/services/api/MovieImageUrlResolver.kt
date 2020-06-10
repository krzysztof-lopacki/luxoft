package com.luxoft.codingchallenge.services.api

import androidx.annotation.DimenRes

/**
 * Component resolving urls of the images associated with movies.
 */
interface MovieImageUrlResolver {

    /**
     * Resolves the url required to download an image.
     * @param imageFileName Filename of the image.
     * @param type  Type of the image. Poster or backdrop. Specifying the type of the image
     *              is required to choose the best matching version of the image in terms
     *              of the resolution.
     * @param widthHint The desired width of the downloaded image. Usually the images provider can
     *                  provide many versions of the same image, each in different resolution.
     *                  The url returned by this method will point to the version
     *                  which matches the desired width best.
     * @return Resolved url of the image. This url may be used for download directly.
     */
    fun getImageUrl(imageFileName: String?, type: ImageType, widthHint: Int): String?

    /**
     * Resolves the url required to download an image.
     * @param imageFileName Filename of the image.
     * @param type  Type of the image. Poster or backdrop. Specifying the type of the image
     *              is required to choose the best matching version of the image in terms
     *              of the resolution.
     * @param widthHint The desired width of the downloaded image specified as Android dimension resource.
     *                  Usually the images provider can provide many versions of the same image,
     *                  each in different resolution. The url returned by this method will point
     *                  to the version which matches the desired width best.
     * @return Resolved url of the image. This url may be used for download directly.
     */
    fun getImageUrlRes(imageFileName: String?, type: ImageType, @DimenRes widthHint: Int): String?

    /**
     * Image type.
     */
    enum class ImageType {
        /**
         * Movie's poster.
         */
        POSTER,

        /**
         * The image or movie frame that advertise the movie.
         */
        BACKDROP
    }
}