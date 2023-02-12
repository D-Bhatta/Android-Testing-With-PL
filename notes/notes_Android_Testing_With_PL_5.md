# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lqen)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Remove cyclic dependency between packages](#remove-cyclic-dependency-between-packages)
  - [Creating a mock repository for testing](#creating-a-mock-repository-for-testing)
  - [Creating a Resource class](#creating-a-resource-class)
  - [Create an interface for repositories](#create-an-interface-for-repositories)
  - [Create a real repository](#create-a-real-repository)
  - [Mocking the remote and local data sources](#mocking-the-remote-and-local-data-sources)
    - [Local data sources interface](#local-data-sources-interface)
    - [Remote data sources interface](#remote-data-sources-interface)
    - [Fake and real implementation of the interfaces](#fake-and-real-implementation-of-the-interfaces)
  - [Create a fake repository](#create-a-fake-repository)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Remove cyclic dependency between packages

Remove cyclic dependency between `com.example.testapplication.data.remote` and `com.example.testapplication` packages by moving part of `Constants.kt` in `com.example.testapplication` to it's own `Constants.kt` file in `com.example.testapplication.data.remote`.

```kotlin
package com.example.testapplication

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[DATABASE_NAME] Name of the Room database for the application.
 * @property[PIXABAY_BASE_URL] Base Pixabay API URL.
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val DATABASE_NAME: String = "shopping_db"
    const val PIXABAY_BASE_URL: String = "https://pixabay.com"
}
```

```kotlin
package com.example.testapplication.data.remote

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[IMAGE_TYPE] Type of image expected from the Pixabay API. It is used as a query
 * parameter. Accepted values: "all", "photo", "illustration", "vector". Default: "all".
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val IMAGE_TYPE: String = "photo"
}
```

- Then we fix import issues.

## Creating a mock repository for testing

There are a number of reasons we might create a mock repository.

1. Repository contains network calls.
   - Network calls are often slow, but tests need to run fast. Using a mock repository allows the test suite to substitute real world network calls with faster alternatives, such as a local network call or a mocked response.
   - It can be hard to mock network errors sometimes, depending on the domain. There might be the need to mock specific states of a network response, or the lack of a response. Using a mock repository enables the use of mocking to test these scenarios.
2. Promotes decoupling of the rest of the app from the repository layer through TDD.
   - Using a mock repository from the start might help uncouple the rest of the app from the design of the repository.

## Creating a Resource class

A `Resource.kt` class is very useful as an utility to communicate the state of resources within the app.

```kotlin
// Resource.kt

package com.example.testapplication

/**
 * Resource class holds a resource value, it's operational status, and an optional message.
 *
 * @param[status] Can be one of the [Status] values.
 * @param[data] Nullable resource value. Even if the [status] is [Status.SUCCESS], the value might
 * still be null. So recommend checking it before using.
 * @param[message] [Message] holds a message value.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: Message?) {
    companion object {
        /**
         * Call this method to return a [Resource] object on successful operation.
         *
         * @param[data] Nullable resource value. Even if the [status] is [Status.SUCCESS], the value
         * might still be null. So recommend checking it before using.
         */
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        /**
         * Call this method to return a [Resource] object after encountering an error during an
         * operation. This error might represent an error in the business domain, as well as errors
         * encountered while performing any operation on the data.
         *
         * @param[message] [Message] holds a message value.
         * @param[data] Nullable resource value. The value might still be null. So recommend
         * checking it before using.
         */
        fun <T> error(message: Message, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        /**
         * Call this method to return a [Resource] value that signals it is involved in an ongoing
         * operation.
         *
         * @param[data] Nullable resource value. The value might still be null. So recommend
         * checking it before using.
         */
        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}


@Suppress("KDocMissingDocumentation")
data class Message(val message: String)

@Suppress("KDocMissingDocumentation")
enum class Status {
    SUCCESS, ERROR, LOADING
}

```

## Create an interface for repositories

By creating an interface for repositories, we can ensure that our code is more modular and less coupled to a particular repository. This is because classes implementing the interface can be swapped easily.

```kotlin
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
```

## Create a real repository

- Create a package `repository` and create a class file inside `DefaultShoppingRepository.kt`.
- We inject `PixabayAPI` and `ShoppingDao` using dagger hilt.
- By implementing `ShoppingRepository.kt` interface, we can ensure that this and the testing repository will always implement the same methods. To quickly implement methods, use `Ctrl/Cmd + I` and select the methods to implement.

```kotlin
// DefaultShoppingRepository.kt
package com.example.testapplication.repository

import androidx.lifecycle.LiveData
import com.example.testapplication.Message
import com.example.testapplication.Resource
import com.example.testapplication.data.local.ShoppingDao
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.PixabayAPI
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
    private val pixabayAPI: PixabayAPI, private val shoppingDao: ShoppingDao
) : ShoppingRepository {
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

    override suspend fun searchImages(
        searchQuery: String, imageType: String?
    ): Resource<ImageResponse> {
        return try {
            val response =
                imageType?.let { pixabayAPI.searchImages(searchQuery, imageType = imageType) }
                    ?: pixabayAPI.searchImages(searchQuery)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error(Message(buildString {
                    append(response.message())
                    append(response.errorBody())
                    @Suppress("HardCodedStringLiteral") append("Error encountered while searching for images.")
                }), null)
            } else {
                Resource.error(Message(buildString {
                    append(response.message())
                    append(response.errorBody())
                    @Suppress("HardCodedStringLiteral") append("API request not successful")
                }), null)
            }
        } catch (e: Exception) {
            @Suppress("HardCodedStringLiteral") Resource.error(
                Message("Error encountered while attempting to request API."), null
            )
        }

    }
}
```

## Mocking the remote and local data sources

I think mocking the repository is a brittle implementation. It would be much better to mock the remote and local data sources than the repository itself. That way, our implementation is more flexible, and we can change the behavior of the repository easily. Otherwise, there would be a large discrepancy in the behavior between the main and test repository classes over time as implementations differ. The test would become largely useless. In addition, we should program the data sources to an interface, rather than an implementation.

This helps the repository class in the following ways:

- The repository can now follow the single responsibility of being the repository, instead of having to handle complex data layer logic.
- Changes in the data layer no longer affect the repository layer significantly.
- Mitigates the chances of a circular dependency emerging between the data and repository layers in the future, as complex logic in the repository layer is reused in the data layer as the application grows.

It has the following effects on the data layer:

- The implementation can be changed without significant breakage in the rest of the code.
- We can introduce, encapsulate, and remove complex data layer logic in the implementation code without removing the abstraction. This allows us to progressively enhance the rest of the application in line with changes in the data layer.
- Complex data layer logic stays within the data layer, instead of being scattered all over the rest of the application.
- It makes it easier to refactor the code, and using an interface standardizes how the rest of the program interacts with the data layer.
- We can switch, add, or remove data sources without introducing breakage in the code.

### Local data sources interface

```kotlin
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
```

### Remote data sources interface

```kotlin
// RemoteDataSource.kt
package com.example.testapplication.data.remote

import com.example.testapplication.Resource
import com.example.testapplication.data.remote.response.ImageResponse

/**
 * Represents all the operations that can be performed with remote data sources.
 */
interface RemoteDataSource {

    /**
     * Search for an image using a [String] query.
     *
     * @param[searchQuery] The query to search for.
     * @param[imageType] Optional, type of image to search for.
     */
    suspend fun searchImages(searchQuery: String, imageType: String?): Resource<ImageResponse>
}
```

### Fake and real implementation of the interfaces

- This is the real implementation of the `LocalDataSource`.

```kotlin
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
```

- This is the real implementation of the `RemoteDataSource`.

```kotlin
// DefaultRemoteDatasource.kt
package com.example.testapplication.data.remote

import com.example.testapplication.Message
import com.example.testapplication.Resource
import com.example.testapplication.data.remote.response.ImageResponse
import javax.inject.Inject

/**
 * Implementation of [RemoteDataSource].
 *
 * Search for images related to shopping items using [pixabayAPI].
 */
class DefaultRemoteDataSource @Inject constructor(private val pixabayAPI: PixabayAPI) :
    RemoteDataSource {
    override suspend fun searchImages(
        searchQuery: String, imageType: String?
    ): Resource<ImageResponse> {
        return try {
            val response =
                imageType?.let { pixabayAPI.searchImages(searchQuery, imageType = imageType) }
                    ?: pixabayAPI.searchImages(searchQuery)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error(Message(buildString {
                    append(response.message())
                    append(response.errorBody())
                    @Suppress("HardCodedStringLiteral") append("Error encountered while searching for images.")
                }), null)
            } else {
                Resource.error(Message(buildString {
                    append(response.message())
                    append(response.errorBody())
                    @Suppress("HardCodedStringLiteral") append("API request not successful")
                }), null)
            }
        } catch (e: Exception) {
            @Suppress("HardCodedStringLiteral") Resource.error(
                Message("Error encountered while attempting to request API."), null
            )
        }
    }
}

```

- This is the fake implementation of the `LocalDataSource`.

```kotlin
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
```

- This is the fake implementation of the `RemoteDataSource`.

```kotlin
// FakeRemoteDataSource.kt
package com.example.testapplication.data.remote

import com.example.testapplication.Message
import com.example.testapplication.Resource
import com.example.testapplication.data.remote.response.ImageResponse

class FakeRemoteDataSource : RemoteDataSource {
    private var networkError = NetworkErrors.NONE
    fun setNetworkError(error: NetworkErrors){
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
        return Resource.success(ImageResponse(listOf(), 0,0))
    }
}

enum class NetworkErrors {
    NONE, NETWORK_UNREACHABLE

}

```

## Create a fake repository

- This will simulate the behavior of our actual repository.
- First we implement our real repository.

```kotlin
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
```

- Then we implement our fake repository.

```kotlin
// FakeShoppingRepository.kt
package com.example.testapplication.repository

import androidx.lifecycle.LiveData
import com.example.testapplication.Resource
import com.example.testapplication.data.local.LocalDataSource
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.RemoteDataSource
import com.example.testapplication.data.remote.response.ImageResponse
import javax.inject.Inject

class FakeShoppingRepository @Inject constructor(
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
```

## Additional Information

## Errors

### Course

### Screenshots

### Links

- .

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
