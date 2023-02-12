// PixabayAPI.kt
@file:Suppress("HardCodedStringLiteral")

package com.example.testapplication.data.remote

import com.example.testapplication.BuildConfig
import com.example.testapplication.Constants.IMAGE_TYPE
import com.example.testapplication.data.remote.response.ImageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface to interact with the Pixabay API through Retrofit.
 */
interface PixabayAPI {
    /**
     * Method to search the Pixabay API for images.
     *
     * @param[searchQuery] The query to search for.
     * @param[key] Pixabay API key.
     * @param[imageType] Type of image to search for.
     *
     * @return[ImageResponse]
     */
    @GET("/api/")
    suspend fun searchImages(
        @Query("q") searchQuery: String,
        @Query("key") key: String = BuildConfig.PIXABAY_API_KEY,
        @Query("image_type") imageType: String = IMAGE_TYPE
    ): Response<ImageResponse>
}