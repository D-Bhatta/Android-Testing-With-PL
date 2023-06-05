// ShoppingRepository.kt
package com.example.testapplication.repository

import androidx.lifecycle.LiveData
import com.example.testapplication.Resource
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.response.ImageResponse

/**
 * Represents all the data operations that repositories in the app can perform.
 */
interface ShoppingRepository {

    /**
     * Store a new shopping item.
     *
     * Duplicates of this item may already exist, and should be overwritten with the new item.
     *
     * @param[item] A [ShoppingItem] instance.
     */
    suspend fun createShoppingItem(item: ShoppingItem)

    /**
     * Delete a single shopping item.
     *
     * @param[item] A [ShoppingItem] instance.
     * @return[Int] Returns an integer for the number of items deleted in the database. If the item
     * was not present in the database, then `0` will be returned.
     */
    suspend fun deleteShoppingItem(item: ShoppingItem): Int

    /**
     * @return all [ShoppingItem] instances in the database.
     */
    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>

    /**
     * Compute the total price of all shopping items stored in the database.
     */
    fun observeTotalPrice(): LiveData<Double>

    /**
     * Search for an image using a [String] query.
     *
     * @param[searchQuery] The query to search for.
     * @param[imageType] Optional, type of image to search for.
     */
    suspend fun searchImages(searchQuery: String, imageType: String?): Resource<ImageResponse>
}
