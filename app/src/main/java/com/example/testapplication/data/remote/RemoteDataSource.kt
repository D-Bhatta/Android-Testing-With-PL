// RemoteDataSource.kt
package com.example.testapplication.data.remote

import com.example.testapplication.Resource
import com.example.testapplication.data.remote.response.ImageResponse

/**
 * Represents all the operations that can be performed with remote data sources.
 */
interface RemoteDataSource {

    /**
     * Search for an image using a [String] query.
     *
     * @param[searchQuery] The query to search for.
     * @param[imageType] Optional, type of image to search for.
     */
    suspend fun searchImages(searchQuery: String, imageType: String?): Resource<ImageResponse>
}