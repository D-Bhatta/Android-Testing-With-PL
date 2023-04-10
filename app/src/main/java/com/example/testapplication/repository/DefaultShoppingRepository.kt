// DefaultShoppingRepository.kt
package com.example.testapplication.repository

import androidx.lifecycle.LiveData
import com.example.testapplication.Resource
import com.example.testapplication.data.local.LocalDataSource
import com.example.testapplication.data.local.ShoppingDao
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.RemoteDataSource
import com.example.testapplication.data.remote.response.ImageResponse
import javax.inject.Inject

/**
 * Repository holding all possible operations on shopping data.
 *
 * 1. Create new shopping items.
 * 2. Delete shopping items.
 * 3. Observe the shopping items as [LiveData].
 * 4. Observe the total price shopping list as [LiveData].
 * 5. Search for images related to shopping items.
 */
class DefaultShoppingRepository @Inject constructor(
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