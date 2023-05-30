// ShoppingViewModel.kt
package com.example.testapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapplication.Event
import com.example.testapplication.Resource
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.response.ImageResponse
import com.example.testapplication.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for shopping screens.
 *
 * @property[shoppingItems] Observable list of [ShoppingItem] instances.
 * @property[totalPrice] Observable value of total price.
 * @property[images] Observable list of [ImageResponse] instances.
 * @property[currentImageUrl] URL to the image of [ShoppingItem] being inserted.
 * @property[insertShoppingItem] Observable events related to [ShoppingItem] being inserted.
 *  See [Event] and [Resource] for more details.
 */
@HiltViewModel
class ShoppingViewModel @Inject constructor(private val repository: ShoppingRepository) :
    ViewModel() {
    val shoppingItems: LiveData<List<ShoppingItem>> = repository.observeAllShoppingItems()
    val totalPrice: LiveData<Double> = repository.observeTotalPrice()

    private val _images = MutableLiveData<Event<Resource<ImageResponse>>>()
    val images: LiveData<Event<Resource<ImageResponse>>> = _images

    private val _currentImageUrl = MutableLiveData<String>()
    val currentImageUrl: LiveData<String> = _currentImageUrl

    private val _insertShoppingItemStatus = MutableLiveData<Event<Resource<ShoppingItem>>>()
    val insertShoppingItem: LiveData<Event<Resource<ShoppingItem>>> = _insertShoppingItemStatus

    /**
     * Set the selected image's URL.
     */
    fun setCurrentImageUrl(url: String) {
        _currentImageUrl.postValue(url)
    }

    /**
     * Delete a [ShoppingItem] from the data source.
     */
    fun deleteShoppingItem(shoppingItem: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(shoppingItem)
        }
    }

    private fun createShoppingItem(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.createShoppingItem(shoppingItem)
    }

    /**
     * Insert a [ShoppingItem] into the data source.
     */
    fun insertShoppingItem(itemName: String, amountString: String, priceString: String) {
        return
    }

    /**
     * Search for an image using a query string.
     *
     * @param[imageQuery] The query to search for.
     */
    fun searchForImage(imageQuery: String) {
        return
    }
}
