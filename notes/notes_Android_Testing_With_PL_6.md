# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Create 4 fragments](#create-4-fragments)
  - [Create an Event class](#create-an-event-class)
  - [ViewModel construction](#viewmodel-construction)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Create 4 fragments

- Move the `MainActivity.kt` file to a new `ui` package.
- Create 4 fragments in the `ui` package: `ShoppingFragment.kt`, `LicenseFragment.kt`, `ImagePickFragment.kt`, and `AddShoppingItemFragment.kt`.

```kotlin
// ShoppingFragment.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import com.example.testapplication.R

class ShoppingFragment: Fragment(R.layout.fragment_shopping) {
}
```

```kotlin
// LicenseFragment.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import com.example.testapplication.R

class LicenseFragment: Fragment(R.layout.fragment_license) {
}
```

```kotlin
// ImagePickFragment.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import com.example.testapplication.R

class ImagePickFragment: Fragment(R.layout.fragment_image_pick) {
}
```

```kotlin
// AddShoppingItemFragment.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import com.example.testapplication.R

class AddShoppingItemFragment: Fragment(R.layout.fragment_add_shopping_item) {
}
```

## Create an Event class

- Create an Event class to pass Event data throughout the application.

```kotlin
// Event.kt
package com.example.testapplication

import java.util.UUID

/**
 * Holds information about events.
 *
 * @param[content] Information stored in the event.
 *
 * @property[interceptionStatus] Status of event interception.
 * @property[eventStatus] Status of the event lifecycle.
 * @property[eventID] Unique identifier of the event.
 */
open class Event<out T>(private val content: T) {
    var interceptionStatus: InterceptionStatus = InterceptionStatus.UNINTERCEPTED
        private set

    var eventStatus: EventStatus = EventStatus.ONGOING

    val eventID: String = createId()

    private fun createId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Return the content if the [Event] has not been intercepted.
     */
    fun getContentIfUnintercepted(): T? {
        return if (interceptionStatus == InterceptionStatus.INTERCEPTED) {
            null
        } else {
            interceptionStatus = InterceptionStatus.INTERCEPTED
            content
        }
    }

    /**
     * Extract the content without affecting the [interceptionStatus]
     */
    fun peekContent(): T = content
}

/**
 * Status of the event.
 */
enum class EventStatus {
    /**
     * Event has been cancelled.
     */
    CANCELLED,

    /**
     * Event resource has been destroyed.
     */
    DESTROYED,

    /**
     * Event is currently in progress.
     */
    ONGOING,

    /**
     * Event resource has been created.
     */
    CREATED
}

@Suppress("KDocMissingDocumentation")
enum class InterceptionStatus {
    INTERCEPTED, UNINTERCEPTED
}

```

## ViewModel construction

- Add dependencies required for `ViewModel` to work: local and remote data sources, and repository.

```kotlin
package com.example.testapplication

import android.content.Context
import androidx.room.Room
import com.example.testapplication.Constants.DATABASE_NAME
import com.example.testapplication.Constants.PIXABAY_BASE_URL
import com.example.testapplication.data.local.DefaultLocalDataSource
import com.example.testapplication.data.local.LocalDataSource
import com.example.testapplication.data.local.ShoppingDao
import com.example.testapplication.data.local.ShoppingListDatabase
import com.example.testapplication.data.remote.DefaultRemoteDataSource
import com.example.testapplication.data.remote.PixabayAPI
import com.example.testapplication.data.remote.RemoteDataSource
import com.example.testapplication.repository.DefaultShoppingRepository
import com.example.testapplication.repository.ShoppingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Dependency injection module for objects that will live for the entire lifetime of the
 * application.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the app database [ShoppingListDatabase].
     */
    @Singleton
    @Provides
    fun provideShoppingListDatabase(
        @ApplicationContext context: Context
    ): ShoppingListDatabase =
        Room.databaseBuilder(context, ShoppingListDatabase::class.java, DATABASE_NAME).build()

    /**
     * Provides the DAO [ShoppingDao] for the [ShoppingListDatabase]
     */
    @Singleton
    @Provides
    fun provideShoppingDao(
        database: ShoppingListDatabase
    ): ShoppingDao = database.shoppingDao()

    /**
     * Provides the [PixabayAPI] Retrofit instance.
     */
    fun providePixabayAPI(): PixabayAPI {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(
            PIXABAY_BASE_URL
        ).build().create(PixabayAPI::class.java)
    }

    /**
     * Provides the [RemoteDataSource] implementation instance.
     */
    @Singleton
    @Provides
    fun provideRemoteDataSource(
        pixabayAPI: PixabayAPI
    ): RemoteDataSource {
        return DefaultRemoteDataSource(pixabayAPI)
    }

    /**
     * Provides the [LocalDataSource] implementation instance.
     */
    @Singleton
    @Provides
    fun provideLocalDataSource(
        shoppingDao: ShoppingDao
    ): LocalDataSource {
        return DefaultLocalDataSource(shoppingDao)
    }

    /**
     * Provides an [ShoppingRepository] implementation in [DefaultShoppingRepository].
     */
    @Singleton
    @Provides
    fun provideDefaultShoppingRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource
    ): ShoppingRepository {
        return DefaultShoppingRepository(remoteDataSource, localDataSource)
    }
}

```

- Create a `ShoppingViewModel` that will be shared between all the fragments.
- We leave some methods blank, we will use TDD to develop them.

```kotlin
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
