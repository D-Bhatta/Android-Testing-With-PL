// FakeRemoteDataSourceAndroidTest.kt
package com.example.testapplication.data.remote

import com.example.testapplication.Message
import com.example.testapplication.Resource
import com.example.testapplication.data.remote.response.ImageResponse

class FakeRemoteDataSourceAndroidTest : RemoteDataSource {
    private var networkError = NetworkErrors.NONE
    fun setNetworkError(error: NetworkErrors) {
        networkError = error
    }

    override suspend fun searchImages(
        searchQuery: String, imageType: String?
    ): Resource<ImageResponse> {
        if (networkError.equals(NetworkErrors.NETWORK_UNREACHABLE)) {
            @Suppress("HardCodedStringLiteral") return Resource.error(
                Message("Error encountered while attempting to request API."), null
            )
        }
        return Resource.success(ImageResponse(listOf(), 0, 0))
    }
}

enum class NetworkErrors {
    NONE, NETWORK_UNREACHABLE

}
