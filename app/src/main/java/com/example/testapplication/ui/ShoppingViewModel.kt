// ShoppingViewModel.kt
package com.example.testapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapplication.Constants
import com.example.testapplication.Event
import com.example.testapplication.Message
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
 * @property[insertShoppingItemStatus] Observable events related to [ShoppingItem] being inserted.
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
    val insertShoppingItemStatus: LiveData<Event<Resource<ShoppingItem>>> =
        _insertShoppingItemStatus

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

    fun createShoppingItem(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.createShoppingItem(shoppingItem)
    }

    /**
     * Insert a [ShoppingItem] into the data source.
     */
    @Suppress("ReturnCount")
    fun insertShoppingItem(itemName: String, amountString: String, priceString: String) {
        if (itemName.isEmpty() || amountString.isEmpty() || priceString.isEmpty()) {
            _insertShoppingItemStatus.postValue(
                Event(
                    Resource.error(Message(Errors.INPUT_IS_EMPTY.errorMessage), null)
                )
            )
            return
        }
        if (itemName.length > Constants.SHOPPING_ITEM_NAME_LENGTH ||

            priceString.length > Constants.SHOPPING_ITEM_PRICE_LENGTH
        ) {
            _insertShoppingItemStatus.postValue(
                Event(
                    Resource.error(Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.errorMessage), null)
                )
            )
            return
        }
        val amount: Int = try {
            amountString.toInt()
        } catch (e: NumberFormatException) {
            _insertShoppingItemStatus.postValue(
                Event(
                    Resource.error(Message(Errors.AMOUNT_NOT_VALID.errorMessage), null)
                )
            )
            return
        }

        val shoppingItem = ShoppingItem(
            itemName,
            amount,
            priceString.toDouble(),
            _currentImageUrl.value ?: ""
        )

        createShoppingItem(shoppingItem)
        setCurrentImageUrl("")
        _insertShoppingItemStatus.postValue(Event(Resource.success(shoppingItem)))
    }

    /**
     * Search for an image using a query string.
     *
     * @param[imageQuery] The query to search for.
     */
    fun searchForImage(imageQuery: String) {
        if (imageQuery.isEmpty()) {
            return
        }

        _images.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response: Resource<ImageResponse> = repository.searchImages(imageQuery, null)
            _images.value = Event(response)
        }
    }
}

@Suppress("KDocMissingDocumentation", "HardCodedStringLiteral")
enum class Errors(val errorMessage: String) {
    INPUT_EXCEEDS_CONSTRAINTS("The input value exceeds the constraints of the application."),

    INPUT_IS_EMPTY(
        "The input value cannot be empty here."
    ),

    AMOUNT_NOT_VALID("The amount exceeds valid limits.")
}
