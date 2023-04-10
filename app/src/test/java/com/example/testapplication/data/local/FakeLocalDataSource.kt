// FakeLocalDataSource.kt
package com.example.testapplication.data.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FakeLocalDataSource : LocalDataSource {

    private val shoppingItems = mutableListOf<ShoppingItem>()
    private val observableShoppingItems = MutableLiveData<List<ShoppingItem>>(shoppingItems)
    private val observableTotalPrice = MutableLiveData<Double>()

    private fun refreshLiveData() {
        observableShoppingItems.postValue(shoppingItems)
        observableTotalPrice.postValue(getTotalPrice())
    }

    private fun getTotalPrice(): Double {
        return shoppingItems.sumOf { it.price }
    }

    override suspend fun createShoppingItem(item: ShoppingItem) {
        shoppingItems.add(item)
        refreshLiveData()
    }

    override suspend fun deleteShoppingItem(item: ShoppingItem): Int {
        if (shoppingItems.remove(item)) {
            refreshLiveData()
            return 1
        }
        return 0
    }

    override fun observeAllShoppingItems(): LiveData<List<ShoppingItem>> {
        return observableShoppingItems
    }

    override fun observeTotalPrice(): LiveData<Double> {
        return observableTotalPrice
    }

}