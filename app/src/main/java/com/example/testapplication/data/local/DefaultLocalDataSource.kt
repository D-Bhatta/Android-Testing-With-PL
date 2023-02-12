// DefaultLocalDataSource.kt
package com.example.testapplication.data.local

import androidx.lifecycle.LiveData
import javax.inject.Inject

/**
 * Implementation of [LocalDataSource].
 *
 * Insert items, observe all items, observe the total price,
 * and delete items into the local database.
 */
class DefaultLocalDataSource @Inject constructor(private val shoppingDao: ShoppingDao) :
    LocalDataSource {
    override suspend fun createShoppingItem(item: ShoppingItem) {
        shoppingDao.insertShoppingItem(item)
    }

    override suspend fun deleteShoppingItem(item: ShoppingItem): Int {
        return shoppingDao.deleteShoppingItem(item)
    }

    override fun observeAllShoppingItems(): LiveData<List<ShoppingItem>> {
        return shoppingDao.observeAllShoppingItems()
    }

    override fun observeTotalPrice(): LiveData<Double> {
        return shoppingDao.observeTotalPrice()
    }
}