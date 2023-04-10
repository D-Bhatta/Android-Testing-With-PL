// DefaultRemoteDatasource.kt
package com.example.testapplication.data.remote

import com.example.testapplication.Message
import com.example.testapplication.Resource
import com.example.testapplication.data.remote.response.ImageResponse
import javax.inject.Inject

/**
 * Implementation of [RemoteDataSource].
 *
 * Search for images related to shopping items using [pixabayAPI].
 */
class DefaultRemoteDataSource @Inject constructor(private val pixabayAPI: PixabayAPI) :
    RemoteDataSource {
    override suspend fun searchImages(
        searchQuery: String, imageType: String?
    ): Resource<ImageResponse> {
        return try {
            val response =
                imageType?.let { pixabayAPI.searchImages(searchQuery, imageType = imageType) }
                    ?: pixabayAPI.searchImages(searchQuery)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error(Message(buildString {
                    append(response.message())
                    append(response.errorBody())
                    @Suppress("HardCodedStringLiteral") append("Error encountered while searching for images.")
                }), null)
            } else {
                Resource.error(Message(buildString {
                    append(response.message())
                    append(response.errorBody())
                    @Suppress("HardCodedStringLiteral") append("API request not successful")
                }), null)
            }
        } catch (e: Exception) {
            @Suppress("HardCodedStringLiteral") Resource.error(
                Message("Error encountered while attempting to request API."), null
            )
        }
    }
}
