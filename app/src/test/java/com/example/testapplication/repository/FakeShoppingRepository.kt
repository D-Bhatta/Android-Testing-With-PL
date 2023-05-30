// FakeShoppingRepository.kt
package com.example.testapplication.repository

import androidx.lifecycle.LiveData
import com.example.testapplication.Resource
import com.example.testapplication.data.local.LocalDataSource
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.RemoteDataSource
import com.example.testapplication.data.remote.response.ImageResponse

class FakeShoppingRepository constructor(
    private val remoteDataSource: RemoteDataSource, private val localDataSource: LocalDataSource
) : ShoppingRepository {
    override suspend fun createShoppingItem(item: ShoppingItem) {
        localDataSource.createShoppingItem(item)
    }

    override suspend fun deleteShoppingItem(item: ShoppingItem): Int {
        return localDataSource.deleteShoppingItem(item)
    }

    override fun observeAllShoppingItems(): LiveData<List<ShoppingItem>> {
        return localDataSource.observeAllShoppingItems()
    }

    override fun observeTotalPrice(): LiveData<Double> {
        return localDataSource.observeTotalPrice()
    }

    override suspend fun searchImages(
        searchQuery: String, imageType: String?
    ): Resource<ImageResponse> {
        return remoteDataSource.searchImages(searchQuery, imageType)
    }
}