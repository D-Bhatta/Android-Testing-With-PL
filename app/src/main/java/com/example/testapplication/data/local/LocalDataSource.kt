// LocalDataSource.kt
package com.example.testapplication.data.local

import androidx.lifecycle.LiveData

/**
 * Represents all the operations that can be performed with local data sources.
 */
interface LocalDataSource {

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
}