# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

- We will be using `LiveDataTestUtil.kt` in the `test` source set, so copy it over as `app/src/test/java/com/example/testapplication/LiveDataTestUtil.kt`.
- Update any related licenses as needed.
- We generate a test class for the ViewModel as `ShoppingViewModelTest`.

```kotlin
package com.example.testapplication.ui

import com.example.testapplication.data.local.FakeLocalDataSource
import com.example.testapplication.data.remote.FakeRemoteDataSource
import com.example.testapplication.repository.DefaultShoppingRepository
import org.junit.After
import org.junit.Before

class ShoppingViewModelTest {
    private lateinit var viewModel: ShoppingViewModel

    @Before
    fun setUp() {
        viewModel = ShoppingViewModel(
            DefaultShoppingRepository(FakeRemoteDataSource(), FakeLocalDataSource())
        )
    }

    @After
    fun tearDown() {
    }
}

```

- Since we will be testing code with `LiveData` in it, we set the `InstantTaskExecutorRule` to ensure all asynchronous code in the test will execute instantly, and one after the other.
- Since we will be testing code with coroutines, that create new coroutines other than the top level coroutines that might be created, we need to set the `Dispatcher` for the tests. Preferably to a `TestDispatcher` class like `UnconfinedTestDispatcher`. In local unit tests, the `MainDispatcher` will be unavailable, and coroutines that reference the main thread will throw an exception. Thus, `Dispatchers.setMain` and `Dispatchers.resetMain` is used to set the dispatcher to a `TestDispatcher`. So we create a JUnit [test rule][1] to set to do this for every test needed.
- We write a test to ensure empty data throws an error.
- We set a constant for the maximum length of the name of a `ShoppingItem` and write a test to ensure the constraint is enforced.
- We set a constant for the maximum length of the price of a `ShoppingItem` and write a test to ensure the constraint is enforced.
- We write a test to ensure that if the amount is too high, an error is thrown.
- We write a test to check valid input.

```kotlin
// Constants.kt
package com.example.testapplication

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[DATABASE_NAME] Name of the Room database for the application.
 * @property[PIXABAY_BASE_URL] Base Pixabay API URL.
 * @property[SHOPPING_ITEM_NAME_LENGTH] Max length of name field in `ShoppingItem`.
 * @property[SHOPPING_ITEM_PRICE_LENGTH] Max length of price field in `ShoppingItem`.
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val DATABASE_NAME: String = "shopping_db"
    const val PIXABAY_BASE_URL: String = "https://pixabay.com"
    const val SHOPPING_ITEM_NAME_LENGTH: Int = 25
    const val SHOPPING_ITEM_PRICE_LENGTH: Int = 10
}
```

```kotlin
// TestCoroutineRule
package com.example.testapplication

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule constructor(private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()) :
    TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

```

```kotlin
// ShoppingViewModelTest.kt

package com.example.testapplication.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.testapplication.Constants
import com.example.testapplication.Message
import com.example.testapplication.Status
import com.example.testapplication.TestCoroutineRule
import com.example.testapplication.data.local.FakeLocalDataSource
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.FakeRemoteDataSource
import com.example.testapplication.getOrAwaitValue
import com.example.testapplication.repository.DefaultShoppingRepository
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class ShoppingViewModelTest {
    private lateinit var viewModel: ShoppingViewModel

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        viewModel = ShoppingViewModel(
            DefaultShoppingRepository(FakeRemoteDataSource(), FakeLocalDataSource())
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `insert shopping item with empty field, returns error`() {
        viewModel.insertShoppingItem("", "1", "3.0")

        val valueEmptyName =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueEmptyName?.status).isEqualTo(Status.ERROR)
        assertThat(valueEmptyName?.message).isEqualTo(
            Message(Errors.INPUT_IS_EMPTY.errorMessage)
        )

        viewModel.insertShoppingItem("name", "", "3.0")

        val valueEmptyAmount =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueEmptyAmount?.status).isEqualTo(Status.ERROR)
        assertThat(valueEmptyAmount?.message).isEqualTo(
            Message(Errors.INPUT_IS_EMPTY.errorMessage)
        )

        viewModel.insertShoppingItem("name", "1", "")

        val valueEmptyPrice =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueEmptyPrice?.status).isEqualTo(Status.ERROR)
        assertThat(valueEmptyPrice?.message).isEqualTo(
            Message(Errors.INPUT_IS_EMPTY.errorMessage)
        )
    }

    @Test
    fun `insert shopping item with too long name, returns error`() {
        val tooLongNameString = buildString {
            for (i in 1..Constants.SHOPPING_ITEM_NAME_LENGTH + 1) {
                append(i)
            }
        }
        viewModel.insertShoppingItem(tooLongNameString, "3", "3.0")

        val valueTooLongNameString =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueTooLongNameString?.status).isEqualTo(Status.ERROR)
        assertThat(valueTooLongNameString?.message).isEqualTo(Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.errorMessage))
    }

    @Test
    fun `insert shopping item with too long price, returns error`() {
        val tooLongPriceString = buildString {
            for (i in 1..Constants.SHOPPING_ITEM_PRICE_LENGTH) {
                append(i)
            }
        }

        viewModel.insertShoppingItem("name", "3", tooLongPriceString)

        val valueTooLongPriceString =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()

        assertThat(valueTooLongPriceString?.status).isEqualTo(Status.ERROR)
        assertThat(valueTooLongPriceString?.message).isEqualTo(Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.errorMessage))
    }

    @Test
    fun `insert shopping item with too high amount, returns error`() {
        val tooHighAmount = "99999999999999999999999999999"

        viewModel.insertShoppingItem("name", tooHighAmount, "3.0")

        val valueTooHighAmount =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()

        assertThat(valueTooHighAmount?.status).isEqualTo(Status.ERROR)
        assertThat(valueTooHighAmount?.message).isEqualTo(Message(Errors.AMOUNT_NOT_VALID.errorMessage))
    }

    @Test
    fun `insert shopping item with valid input, returns success`() {
        viewModel.insertShoppingItem("validName", "3", "3.0")

        val valueValidInput =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()

        assertThat(valueValidInput?.status).isEqualTo(Status.SUCCESS)
        assertThat(valueValidInput?.data).isEqualTo(ShoppingItem("validName", 3, 3.0, ""))
    }
}

```

```kotlin
// ShoppingViewModel.kt
package com.example.testapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private fun createShoppingItem(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.createShoppingItem(shoppingItem)
    }

    /**
     * Insert a [ShoppingItem] into the data source.
     */
    fun insertShoppingItem(itemName: String, amountString: String, priceString: String) {
        _insertShoppingItemStatus.postValue(
            Event(
                Resource.error(
                    Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.toString()),
                    null
                )
            )
        )
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

@Suppress("KDocMissingDocumentation", "HardCodedStringLiteral")
enum class Errors(val errorMessage: String) {
    INPUT_EXCEEDS_CONSTRAINTS("The input value exceeds the constraints of the application."), INPUT_IS_EMPTY(
        "The input value cannot be empty here."
    )
}

```

- We run the tests and make sure they fail appropriately.
- We write code to fulfill the tests and run the tests again.

```kotlin
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

    private fun createShoppingItem(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.createShoppingItem(shoppingItem)
    }

    /**
     * Insert a [ShoppingItem] into the data source.
     */
    fun insertShoppingItem(itemName: String, amountString: String, priceString: String) {
        if (itemName.isEmpty() || amountString.isEmpty() || priceString.isEmpty()) {
            _insertShoppingItemStatus.postValue(
                Event(
                    Resource.error(Message(Errors.INPUT_IS_EMPTY.errorMessage), null)
                )
            )
            return
        }
        if (itemName.length > Constants.SHOPPING_ITEM_NAME_LENGTH || priceString.length > Constants.SHOPPING_ITEM_PRICE_LENGTH) {
            _insertShoppingItemStatus.postValue(
                Event(
                    Resource.error(Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.errorMessage), null)
                )
            )
            return
        }
        val amount: Int = try {
            amountString.toInt()
        } catch (e: Exception) {
            _insertShoppingItemStatus.postValue(
                Event(
                    Resource.error(Message(Errors.AMOUNT_NOT_VALID.errorMessage), null)
                )
            )
            return
        }

        val shoppingItem: ShoppingItem = ShoppingItem(
            itemName, amount, priceString.toDouble(), _currentImageUrl.value ?: ""
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
        return
    }
}

@Suppress("KDocMissingDocumentation", "HardCodedStringLiteral")
enum class Errors(val errorMessage: String) {
    INPUT_EXCEEDS_CONSTRAINTS("The input value exceeds the constraints of the application."), INPUT_IS_EMPTY(
        "The input value cannot be empty here."
    ),
    AMOUNT_NOT_VALID("The amount exceeds valid limits.")
}

```

- We see that all the tests pass.
- We fill out the image search function. We use `.value` instead of `postValue` because it immediately  notifies all observers of the change. `postValue` will check if there are any other changes to the value, and only notify observers of the last value, effectively debouncing. Here, we need to notify observers of the `Resource.loading` `Event` before we return a response. So we use `value` to ensure both the events will be sent to the observers.

```kotlin
fun searchForImage(imageQuery: String) {
    if (imageQuery.isEmpty()){
        return
    }

    _images.value = Event(Resource.loading(null))
    viewModelScope.launch {
        val response = repository.searchImages(imageQuery, null)
        _images.value = Event(response)
    }
}
```

## Additional Information

## Errors

### Course

### Screenshots

### Links

- [TestDispatchers](https://developer.android.com/kotlin/coroutines/test#testdispatchers)
- [1]: <https://developer.android.com/kotlin/coroutines/test#setting-main-dispatcher> "Setting the Main dispatcher"

## Notes template

```language

```

![Text](./static/img/name.jpg)

- [Text](Link)
- StackOverflow:
- Android Dev Docs ():
- AndroidX releases:
- ProAndroidDev
- Dagger Docs: [Hilt Application](https://dagger.dev/hilt/application.html)
- Howtodoandroid:
- Medium:
