# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lqen)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Pixabay API Key and android](#pixabay-api-key-and-android)
  - [Remote response dataclasses](#remote-response-dataclasses)
  - [Pixabay API retrofit interface](#pixabay-api-retrofit-interface)
  - [Setup DI with dagger hilt](#setup-di-with-dagger-hilt)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Pixabay API Key and android

Pixabay API key is granted once per account. It is not possible to protect the API key from being reverse engineered from the app, since it is now enmeshed in client code. API keys like these should always be stored server side. For this project, however, this app will never be distributed. So the threat model is actually a little different.

The threat model here depends upon accidentally checking the API key into the open source Git repository and uploading it to the internet when pushed to remote. Since there is no need for multiple people accessing the key, it is sufficient to keep it in a file locally, unchecked into git. Alternatively, a gradle plugin could be used to integrate with hashicorp vault, and retrieve the API key from there during app startup.

- We create a file in the root of the project called `apikey.properties` and store the secret there as a `key="value"` pair.

```properties
PIXABAY_API_KEY="KAD3JD0FDHGFGHHIEIORE43KNBIONR938943N38U"
```

- We the `apikey.properties` file into root `.gitignore` to avoid checking it into git.
- In the app level `build.grade` file, we create a `Properties` object and a `File` object. We load the `apikey.properties` file into the `Properties` object. We add the `PIXABAY_API_KEY` to the application's `BuildConfig` object using `buildConfigField`.
- We can now use this in our app as `BuildConfig.PIXABAY_API_KEY`.

```kts
import java.util.Properties
...
val properties = File(rootDir, "apikey.properties").inputStream().use {
    Properties().apply { load(it) }
}
val apiKeyProperties = properties.getValue("PIXABAY_API_KEY") as String

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.testapplication"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "PIXABAY_API_KEY", apiKeyProperties)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
```

## Remote response dataclasses

- Create a package `com.example.testapplication.data.remote.response`, effectively creating a `remote` directory with a `response` directory within it.
- We create an `ImageResponse` data class to hold the results from the API call.

```kotlin
// ImageResponse.kt
package com.example.testapplication.data.remote.response

/**
 * Representation of the Pixabay API response.
 *
 * @property[hits] List of images as [ImageResult] objects.
 * @property[total] Total number of image hits for the query.
 * @property[totalHits] Total number of images accessible through the API.
 */
data class ImageResponse(
    val hits: List<ImageResult>,
    val total: Int,
    val totalHits: Int
)
```

- We create an `ImageResult` data class to hold image objects.

```kotlin
// ImageResult.kt
package com.example.testapplication.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Representation of a image in the Pixabay API response.
 *
 * Images are full width and height, or large with a
 * maximum width or height of 1280px, or preview
 * with a maximum width or height of 150px, or
 * profile picture with dimensions of 250px, or
 * web format with a variable maximum width and
 * height that can be requested for 24 hours.
 *
 * @property[comments] Number of comments.
 * @property[downloads] Number of downloads.
 * @property[likes] Number of likes.
 * @property[views] Number of views.
 * @property[id] UID for the image.
 * @property[tags] Comma separated string of tags for this image.
 * @property[type] Type of image.
 * @property[imageHeight] Height of the image.
 * @property[imageSize] Size of time image.
 * @property[imageWidth] Width of the image.
 * @property[imageURL] URL for the image.
 * @property[largeImageURL] URL for the large version of the image.
 * @property[pageUrl] Source page on Pixabay.
 * @property[previewHeight] Height of the preview image.
 * @property[previewWidth] Width of the preview image.
 * @property[previewURL] URL for the preview image.
 * @property[user] Name of the image contributor on Pixabay.
 * @property[userId] User ID of the image contributor on Pixabay.
 * @property[userImageURL] URL for the profile picture (250px) image.
 * @property[webFormatHeight] Height of the web format image.
 * @property[webFormatWidth] Width of the web format image.
 * @property[webformatURL] URL for the web format image.
 */
@Suppress("SpellCheckingInspection")
data class ImageResult(
    val comments: Int,
    val downloads: Int,
    val likes: Int,
    val views: Int,
    val id: Int,
    val tags: String,
    val type: String,
    val imageHeight: Int,
    val imageSize: Int,
    val imageWidth: Int,
    val imageURL: String,
    val largeImageURL: String,
    val pageUrl: String,
    val previewHeight: Int,
    val previewWidth: Int,
    val previewURL: String,
    val user: String,
    @Suppress("HardCodedStringLiteral") @SerializedName("user_id")
    val userId: Int,
    val userImageURL: String,
    val webFormatHeight: Int,
    val webFormatWidth: Int,
    val webformatURL: String
)
```

## Pixabay API retrofit interface

- We create a file `app/src/main/java/com/example/testapplication/data/remote/PixabayAPI.kt` for retrofit API interface.
- We add a function to search the Pixabay API for images.

```kotlin
// PixabayAPI.kt
@file:Suppress("HardCodedStringLiteral")

package com.example.testapplication.data.remote

import com.example.testapplication.BuildConfig
import com.example.testapplication.Constants.IMAGE_TYPE
import com.example.testapplication.data.remote.response.ImageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface to interact with the Pixabay API through Retrofit.
 */
interface PixabayAPI {
    /**
     * Method to search the Pixabay API for images.
     *
     * @param[searchQuery] The query to search for.
     * @param[key] Pixabay API key.
     * @param[imageType] Type of image to search for.
     *
     * @return[ImageResponse]
     */
    @GET("/api/")
    suspend fun searchImages(
        @Query("q") searchQuery: String,
        @Query("key") key: String = BuildConfig.PIXABAY_API_KEY,
        @Query("image_type") imageType: String = IMAGE_TYPE
    ): Response<ImageResponse>
}
```

- We create a file `app/src/main/java/com/example/testapplication/Constants.kt` to hold constants for the entire application. This will be an object, and thus a lazy initialized singleton.

```kotlin
package com.example.testapplication

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[IMAGE_TYPE] Type of image expected from the Pixabay API. It is used as a query
 * parameter. Accepted values: "all", "photo", "illustration", "vector". Default: "all"
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val IMAGE_TYPE: String = "photo"
}
```

## Setup DI with dagger hilt

- Create an `Application` class.

```kotlin
// ShoppingApplication.kt
package com.example.testapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@Suppress("KDocMissingDocumentation")
@HiltAndroidApp
class ShoppingApplication : Application()

```

- Declare `ShoppingApplication` in the manifest.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".ShoppingApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TestApplication">
        <activity
            android:name=".ui.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- Create a file `app/src/main/java/com/example/testapplication/AppModule.kt`.
- We annotate it with `@Module` to add it to the object graph.
- We annotate it with `@InstallIn(SingletonComponent::class)` to tie the lifecycle of the object to the entire application.
- We create a method `provideShoppingListDatabase` to provide the database and we create a method `provideShoppingDao` to provide the DAO. This will setup our Room database for the app.
- We create a method `providePixabayAPI` to setup Retrofit for the Pixabay domain.

```kotlin
package com.example.testapplication

import android.content.Context
import androidx.room.Room
import com.example.testapplication.Constants.DATABASE_NAME
import com.example.testapplication.Constants.PIXABAY_BASE_URL
import com.example.testapplication.data.local.ShoppingDao
import com.example.testapplication.data.local.ShoppingListDatabase
import com.example.testapplication.data.remote.PixabayAPI
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

}
```

- We add constants.

```kotlin
package com.example.testapplication

import com.example.testapplication.Constants.DATABASE_NAME
import com.example.testapplication.Constants.IMAGE_TYPE
import com.example.testapplication.Constants.PIXABAY_BASE_URL


/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[IMAGE_TYPE] Type of image expected from the Pixabay API. It is used as a query
 * parameter. Accepted values: "all", "photo", "illustration", "vector". Default: "all".
 * @property[DATABASE_NAME] Name of the Room database for the application.
 * @property[PIXABAY_BASE_URL] Base Pixabay API URL.
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val IMAGE_TYPE: String = "photo"
    const val DATABASE_NAME: String = "shopping_db"
    const val PIXABAY_BASE_URL: String = "https://pixabay.com"
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
