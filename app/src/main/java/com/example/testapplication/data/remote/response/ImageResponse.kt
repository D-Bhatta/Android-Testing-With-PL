// ImageResponse.kt
package com.example.testapplication.data.remote.response

/**
 * Representation of the Pixabay API response.
 *
 * @property[hits] List of images as [ImageResult] objects.
 * @property[total] Total number of image hits for the query.
 * @property[totalHits] Total number of images accessible through the API.
 */
data class ImageResponse(
    val hits: List<ImageResult>,
    val total: Int,
    val totalHits: Int
)
