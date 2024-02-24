# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Glide instance injection](#glide-instance-injection)
  - [Setup `ImagePickFragment.kt`](#setup-imagepickfragmentkt)
  - [Create an Adapter class](#create-an-adapter-class)
  - [Create a FragmentFactory](#create-a-fragmentfactory)
  - [Setup `ImagePickFragment`](#setup-imagepickfragment)
  - [Test `ImagePickFragment`](#test-imagepickfragment)
  - [Additional Information](#additional-information)
    - [Errors](#errors)
      - [Espresso tests fail if another area is in focus](#espresso-tests-fail-if-another-area-is-in-focus)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Glide instance injection

Create a function to provide a Glide instance through dependency injection.

- Add the `INTERNET` permission to the app manifest `app/src/main/AndroidManifest.xml` to allow Glide to load images from the internet.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />

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

- Create a `AppGlideModule` class.

```kotlin
// GlideModule.kt
package com.example.testapplication

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Used by Glide as a replacement for com.bumptech.glide.Glide in Applications that depend on
 * Glide's generated code.
 */
@GlideModule
class GlideModule : AppGlideModule()

```

- Create a `RequestManager` with default `RequestOptions` for placeholder and error states.

```kotlin
package com.example.testapplication

import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
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
    @Singleton
    @Provides
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

    /**
     * Provides a [Glide] instance [RequestManager] configured to load images.
     */
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ): RequestManager = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
    )
}


```

## Setup `ImagePickFragment.kt`

- Setup the `RecyclerView` and `Adapter` classes.

## Create an Adapter class

- Inject he glide instance in the constructor of the `RecyclerView.Adapter` class.
- Subclass a `ViewHolder` class with the `RecyclerView` item binding as an argument for `itemView`. Use `.root` on the binding to return the outermost view.
- Create a `DiffUtil.ItemCallback` object to calculate the item difference between versions of the list passed to `RecyclerView` to trigger minimal animations and updates for a performant UI. From [RecyclerView] documentation: "This approach requires that each list is represented in memory with immutable content, and relies on receiving updates as new instances of lists. This approach is also ideal if your UI layer doesn't implement sorting, it just presents the data in the order it's given." And from [DiffUtil] documentation: "new lists should be provided any time content changes".
- Create an [AsyncListDiffer] to help compute the `DiffUtil.ItemCallback` using a background thread.
- Create a list to store image urls, and set the `get` and `set` methods to use the `AsyncListDiffer` instance methods.
- Create a lambda function nullable variable `onItemClickListener` that takes a string as an argument. We create a function to set the listener from an adapter instance. This will allow us to set the listener from the UI layer.
- Override the `onCreateViewHolder`, `getItemCount`, `onBindViewHolder` functions as usual.
- In `onBindViewHolder` load the image from the url using Glide and set an on click listener.

```kotlin
// ImageAdapter.kt
@file:Suppress("KDocMissingDocumentation")

package com.example.testapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.testapplication.databinding.ItemImageBinding
import javax.inject.Inject

class ImageAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val itemImageBinding: ItemImageBinding) :
        RecyclerView.ViewHolder(itemImageBinding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var images: List<String>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = images[position]
        holder.itemView.apply {
            glide.load(url).into(holder.itemImageBinding.imageViewShoppingImage)
            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(url)
                }
            }
        }
    }
}

```

## Create a FragmentFactory

- We modify the constructor of `ImagePickFragment` to accept an `ImageAdapter` as a val.

```kotlin
// ImagePickFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testapplication.Constants.GRID_SPAN_COUNT
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentImagePickBinding
import javax.inject.Inject

class ImagePickFragment @Inject constructor(
    private val imageAdapter: ImageAdapter
) : Fragment(R.layout.fragment_image_pick)
```

- We create a `FragmentFactory` to perform constructor injection in our fragments. We do this instead of field injection to be able to easily create fragments with dependency injection, without having to set it up. This follows the dependency inversion principle. This also aids in testing fragments.
- We override the `instantiate` method to return a custom `ImagePickFragment` with an `ImageAdapter` in the constructor.

```kotlin
// FragmentFactory.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Inject

class ShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter)
            else -> return super.instantiate(classLoader, className)
        }
    }
}

```

## Setup `ImagePickFragment`

- Setup view binding.
- Create a constant `GRID_SPAN_COUNT` for `GridLayoutManager` spans.

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
    const val GRID_SPAN_COUNT: Int = 3
}

```

- Set `setOnItemClickListener` for the `ImageAdapter` instance to set an image url and navigate back.
- Create a private function `setupRecyclerView` and call it.

```kotlin
// ImagePickFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.testapplication.Constants.GRID_SPAN_COUNT
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentImagePickBinding
import javax.inject.Inject

class ImagePickFragment @Inject constructor(
    private val imageAdapter: ImageAdapter
) : Fragment(R.layout.fragment_image_pick) {

    lateinit var viewModel: ShoppingViewModel

    private var fragmentImagePickBinding: FragmentImagePickBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding: FragmentImagePickBinding = FragmentImagePickBinding.bind(view)
        fragmentImagePickBinding = binding

        viewModel = ViewModelProvider(requireActivity()).get(ShoppingViewModel::class.java)

        imageAdapter.setOnItemClickListener { url ->
            findNavController().popBackStack()
            viewModel.setCurrentImageUrl(url = url)
        }

        setupRecyclerView()
    }

    override fun onDestroyView() {
        fragmentImagePickBinding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        fragmentImagePickBinding?.recyclerViewImages?.apply {
            adapter = imageAdapter
            layoutManager = GridLayoutManager(requireContext(), GRID_SPAN_COUNT)
        }
    }

}

```

## Test `ImagePickFragment`

- Add the libraries `espresso-contrib` to `app/build.gradle.kts`.

```kotlin
    // Testing Core
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:3.1.2")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
```

- Generate `app/src/androidTest/java/com/example/testapplication/ui/ImagePickFragmentTest.kt`.
- Create a test function `clickImage_popBackStack` that:
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets the test url to an image that loads fast and passes it to the adapter.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - clicks on the first item in the recycler view with `Espresso.onView(withId(...)).perform(RecyclerViewActions.actionOnItemAtPosition<...>(0, click()))`.
  - verify that the navigation action succeeded with `Mockito.verify(navController).navigate(...)`.
- Create a test function `clickImage_setImageURL` that:
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets up a `viewModel` instance that uses fake data sources to speed up the test.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - sets the test url to an image that loads fast and passes it to the adapter.
  - sets the `viewModel` to the test `viewModel` instance.
  - clicks on the first item in the recycler view with `Espresso.onView(withId(...)).perform(RecyclerViewActions.actionOnItemAtPosition<...>(0, click()))`.
  - asserts that the `currentImageUrl` in the test `viewModel` instance has been set to the test url.

```kotlin
// ImagePickFragmentTest.kt

package com.example.testapplication.ui // JUnit4 Test Class.java.java
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.testapplication.R
import com.example.testapplication.data.local.FakeLocalDataSourceAndroidTest
import com.example.testapplication.data.remote.FakeRemoteDataSourceAndroidTest
import com.example.testapplication.getOrAwaitValue
import com.example.testapplication.launchFragmentInHiltContainer
import com.example.testapplication.repository.DefaultShoppingRepository
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
class ImagePickFragmentTest {
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: ShoppingFragmentFactory

    private val imageURL: String =
        "https://blog.cloudflare.com/content/images/2021/09/image1-10.png"

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun clickImage_popBackStack() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<ImagePickFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            imageAdapter.images = listOf(imageURL)
        }

        onView(withId(R.id.recyclerViewImages)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ImageAdapter.ImageViewHolder>(0, click())
        )

        verify(navController).popBackStack()
    }

    @Test
    fun clickImage_setImageURL() {
        val navController = mock(NavController::class.java)
        val testViewModel = ShoppingViewModel(
            DefaultShoppingRepository(
                FakeRemoteDataSourceAndroidTest(),
                FakeLocalDataSourceAndroidTest()
            )
        )

        launchFragmentInHiltContainer<ImagePickFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
            imageAdapter.images = listOf(imageURL)
        }

        onView(withId(R.id.recyclerViewImages)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ImageAdapter.ImageViewHolder>(0, click())
        )

        assertThat(testViewModel.currentImageUrl.getOrAwaitValue()).isEqualTo(imageURL)
    }
}

```

## Additional Information

### Errors

#### Espresso tests fail if another area is in focus

```terminal
clickAddShoppingItemButton_navigateToAddShoppingItemFragment(com.example.testapplication.ui.ShoppingFragmentTest)

androidx.test.espresso.base.RootViewPicker$RootViewWithoutFocusException: Waited for the root of the view hierarchy to have window focus and not request layout for 10 seconds. If you specified a non default root matcher, it may be picking a root that never takes focus. Root:
Root{application-window-token=android.view.ViewRootImpl$W@d5c5245, window-token=android.view.ViewRootImpl$W@d5c5245, has-window-focus=false, layout-params-type=1, layout-params-string={(0,0)(fillxfill) sim={forwardNavigation} ty=BASE_APPLICATION wanim=0x10302fe
fl=LAYOUT_IN_SCREEN LAYOUT_INSET_DECOR SPLIT_TOUCH HARDWARE_ACCELERATED DRAWS_SYSTEM_BAR_BACKGROUNDS
pfl=FORCE_DRAW_STATUS_BAR_BACKGROUND FIT_INSETS_CONTROLLED
fitSides=}, decor-view-string=DecorView{id=-1, visibility=VISIBLE, width=1080, height=2340, has-focus=false, has-focusable=true, has-window-focus=false, is-clickable=false, is-enabled=true, is-focused=false, is-focusable=false, is-layout-requested=false, is-selected=false, layout-params={(0,0)(fillxfill) sim={forwardNavigation} ty=BASE_APPLICATION wanim=0x10302fe
fl=LAYOUT_IN_SCREEN LAYOUT_INSET_DECOR SPLIT_TOUCH HARDWARE_ACCELERATED DRAWS_SYSTEM_BAR_BACKGROUNDS
pfl=FORCE_DRAW_STATUS_BAR_BACKGROUND FIT_INSETS_CONTROLLED
fitSides=}, tag=null, root-is-layout-requested=false, has-input-connection=false, x=0.0, y=0.0, child-count=3}}
```

### Screenshots

### Links

[RecyclerView]: <https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView#presenting-dynamic-data> "Presenting Dynamic Data"

[DiffUtil]: <https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/DiffUtil> "DiffUtil"

- [Presenting Dynamic Data:](https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView#presenting-dynamic-data)
- [DiffUtil:](https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/DiffUtil)

## Notes template

```kotlin

```

```xml

```

![Text](./static/img/name.jpg)

[y]
[y]: <https://link.to.thing> "link title"

[link title](https://link.to.thing)
