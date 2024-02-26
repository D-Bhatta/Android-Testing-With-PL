# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Setup AddShoppingItemFragment](#setup-addshoppingitemfragment)
  - [Test ShoppingItem insertion](#test-shoppingitem-insertion)
  - [Additional Information](#additional-information)
    - [Errors](#errors)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Setup AddShoppingItemFragment

- In `app/src/main/java/com/example/testapplication/ui/ShoppingFragmentFactory.kt`, add a Glide `RequestManager` argument in the constructor.
- Return an instance of `AddShoppingItemFragment` with the glide instance in the constructor.

```kotlin
// FragmentFactory.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class ShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter,
    private val glide: RequestManager
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter)
            AddShoppingItemFragment::class.java.name -> AddShoppingItemFragment(glide)
            else -> return super.instantiate(classLoader, className)
        }
    }
}

```

- In `app/src/main/res/layout/fragment_add_shopping_item.xml` add a `MaterialButton` with an ID of `addShoppingItemBtn`.

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/imageViewShoppingImage"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_margin="16dp"
        android:contentDescription="@string/shopping_item_image"
        android:scaleType="centerCrop"
        android:src="@drawable/ic_image"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/shopping_item_name"
        app:layout_constraintBottom_toBottomOf="@id/imageViewShoppingImage"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/imageViewShoppingImage"
        app:layout_constraintTop_toTopOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextShoppingItemName"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:inputType="textAutoComplete" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutQuantity"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/shopping_item_quantity"
        app:layout_constraintEnd_toStartOf="@id/textInputLayoutPrice"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/imageViewShoppingImage">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextShoppingItemQuantity"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:inputType="number" />

    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutPrice"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/shopping_item_price"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toEndOf="@id/textInputLayoutQuantity"
        app:layout_constraintTop_toBottomOf="@id/imageViewShoppingImage">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextShoppingItemPrice"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:inputType="numberDecimal" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/addShoppingItemBtn"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:layout_marginBottom="16dp"
        android:text="@string/add"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textInputLayoutPrice"
        app:layout_constraintStart_toStartOf="@id/textInputLayoutPrice"/>


</androidx.constraintlayout.widget.ConstraintLayout>
```

- In `app/src/main/java/com/example/testapplication/ui/AddShoppingItemFragment.kt`:
  - Inject the glide instance as a dependency in the constructor.
  - Create a function `subscribeToObservers` to connect the `ShoppingViewModel` to the fragment.
    - Load the image using glide into the `imageViewShoppingImage`.
    - Show status of success or error while inserting an item.
  - Call `subscribeToObservers` after the viewModel instantiation in `onViewCreated` to setup the observers.
  - Setup `setOnClickListener` on `addShoppingItemBtn` to insert an item.

```kotlin
// AddShoppingItemFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.testapplication.R
import com.example.testapplication.Status
import com.example.testapplication.databinding.FragmentAddShoppingItemBinding
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

@Suppress("KDocMissingDocumentation")
class AddShoppingItemFragment @Inject constructor(
    val glide: RequestManager
) : Fragment(R.layout.fragment_add_shopping_item) {

    lateinit var viewModel: ShoppingViewModel

    private var fragmentAddShoppingItemBinding: FragmentAddShoppingItemBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddShoppingItemBinding.bind(view)
        fragmentAddShoppingItemBinding = binding

        viewModel = ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]

        subscribeToObservers()

        binding.imageViewShoppingImage.setOnClickListener {
            findNavController().navigate(
                AddShoppingItemFragmentDirections.actionAddShoppingItemFragmentToImagePickFragment()
            )
        }
        binding.addShoppingItemBtn.setOnClickListener {
            viewModel.insertShoppingItem(
                binding.editTextShoppingItemName.text.toString(),
                binding.editTextShoppingItemQuantity.text.toString(),
                binding.editTextShoppingItemPrice.text.toString()
            )
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.setCurrentImageUrl("")
                findNavController().popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    override fun onDestroyView() {
        fragmentAddShoppingItemBinding = null
        super.onDestroyView()
    }

    private fun subscribeToObservers() {
        viewModel.currentImageUrl.observe(viewLifecycleOwner, Observer { url ->
            fragmentAddShoppingItemBinding?.let {
                glide.load(url).into(it.imageViewShoppingImage)
            }
        })

        viewModel.insertShoppingItemStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfUnintercepted()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.shopping_item_added),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Status.ERROR -> {
                        Snackbar.make(
                            requireView(),
                            result.message?.toString()
                                ?: getString(R.string.error_adding_shopping_item),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Status.LOADING -> {
                        /* NO-OP */
                    }
                }
            }
        })
    }
}

```

- Strings in `strings.xml` added:

```xml
...
    <!--  text input hint: add a shopping item.  -->
    <string name="shopping_item_added">Shopping Item added</string>
    <string name="error_adding_shopping_item">Error adding Shopping Item</string>
    <string name="add">Add</string>
```

## Test ShoppingItem insertion

- Inject `ShoppingFragmentFactory` into the test.
- Ensure that all tests having `launchFragmentInHiltContainer<AddShoppingItemFragment>` in `app/src/androidTest/java/com/example/testapplication/ui/AddShoppingItemFragmentTest.kt` use the fragment factory.

- Create a test function `pressAddShoppingItemBtn_insertShoppingItem` in `app/src/androidTest/java/com/example/testapplication/ui/AddShoppingItemFragmentTest.kt` that:
  - sets up a `viewModel` instance that uses fake data sources to speed up the test.
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - sets the `viewModel` to the test `viewModel` instance.
  - inserts test text into item name, price, and quantity fields with `Espresso.onView(withId(...)).perform(replaceText(....toString()))`.
  - clicks on the `addShoppingItemBtn` with `Espresso.onView(withId(...)).perform(click())`.
  - asserts that the inserted `ShoppingItem` is in the list of shopping items in the test `viewModel` instance.

```kotlin
// AddShoppingItemFragmentTest.kt

package com.example.testapplication.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.testapplication.R
import com.example.testapplication.data.local.FakeLocalDataSourceAndroidTest
import com.example.testapplication.data.local.ShoppingItem
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

@Suppress("HardCodedStringLiteral")
@MediumTest
@HiltAndroidTest
class AddShoppingItemFragmentTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: ShoppingFragmentFactory

    @Before
    fun setUp() {
        hiltAndroidRule.inject()
    }

    @Test
    fun clickImageViewShoppingImage_navigateToImagePickFragment() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<AddShoppingItemFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.imageViewShoppingImage)).perform(click())

        verify(navController).navigate(
            AddShoppingItemFragmentDirections.actionAddShoppingItemFragmentToImagePickFragment()
        )
    }

    @Test
    fun pressBackButton_popBackStack() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<AddShoppingItemFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
        }

        pressBack()

        verify(navController).popBackStack()
    }

    @Test
    fun pressBackButton_setBlankImageUrl() {
        val testViewModel = ShoppingViewModel(
            DefaultShoppingRepository(
                FakeRemoteDataSourceAndroidTest(), FakeLocalDataSourceAndroidTest()
            )
        )
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<AddShoppingItemFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
        }

        pressBack()

        assertThat(testViewModel.currentImageUrl.getOrAwaitValue()).isEqualTo("")
    }

    @Test
    fun pressAddShoppingItemBtn_insertShoppingItem() {
        val itemName = "test_shopping_item_name"
        val itemPrice = 5.0
        val itemQuantity = 10
        val testViewModel = ShoppingViewModel(
            DefaultShoppingRepository(
                FakeRemoteDataSourceAndroidTest(), FakeLocalDataSourceAndroidTest()
            )
        )
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<AddShoppingItemFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
        }

        onView(
            withId(R.id.editTextShoppingItemName)
        ).perform(replaceText(itemName))
        onView(
            withId(R.id.editTextShoppingItemPrice)
        ).perform(replaceText(itemPrice.toString()))
        onView(
            withId(R.id.editTextShoppingItemQuantity)
        ).perform(replaceText(itemQuantity.toString()))

        onView((withId(R.id.addShoppingItemBtn))).perform(click())

        assertThat(
            testViewModel.shoppingItems.getOrAwaitValue()
        ).contains(
            ShoppingItem(
                itemName, itemQuantity, itemPrice, ""
            )
        )

    }
}

```

- In `app/src/androidTest/java/com/example/testapplication/HiltFragmentTestingSetupTest.kt` change the fragment to `ShoppingFragment` from `AddShoppingItemFragment`.

```kotlin
// HiltFragmentTestingSetupTest.kt
package com.example.testapplication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.testapplication.ui.AddShoppingItemFragment
import com.example.testapplication.ui.ShoppingFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("TestMethodWithoutAssertion")
@SmallTest
@HiltAndroidTest
class HiltFragmentTestingSetupTest {

    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun testLaunchFragmentInHiltContainer() {
        launchFragmentInHiltContainer<ShoppingFragment> {}
    }
}

```

## Additional Information

### Errors

### Screenshots

### Links

- .

## Notes template

```kotlin

```

```xml

```

![Text](./static/img/name.jpg)

[y]
[y]: <https://link.to.thing> "link title"

[link title](https://link.to.thing)
