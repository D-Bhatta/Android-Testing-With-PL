# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Setup Android Navigation components](#setup-android-navigation-components)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Setup Android Navigation components

- Create a nav graph in `app/src/main/res/navigation/nav_graph.xml` using the UI.

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/shoppingFragment">

    <fragment
        android:id="@+id/shoppingFragment"
        android:name="com.example.testapplication.ui.ShoppingFragment"
        android:label="ShoppingFragment" >
        <action
            android:id="@+id/action_shoppingFragment_to_licenseFragment"
            app:destination="@id/licenseFragment" />
        <action
            android:id="@+id/action_shoppingFragment_to_addShoppingItemFragment"
            app:destination="@id/addShoppingItemFragment" />
    </fragment>
    <fragment
        android:id="@+id/addShoppingItemFragment"
        android:name="com.example.testapplication.ui.AddShoppingItemFragment"
        android:label="AddShoppingItemFragment" >
        <action
            android:id="@+id/action_addShoppingItemFragment_to_imagePickFragment"
            app:destination="@id/imagePickFragment" />
    </fragment>
    <fragment
        android:id="@+id/imagePickFragment"
        android:name="com.example.testapplication.ui.ImagePickFragment"
        android:label="ImagePickFragment" />
    <fragment
        android:id="@+id/licenseFragment"
        android:name="com.example.testapplication.ui.LicenseFragment"
        android:label="LicenseFragment" />
</navigation>
```

- Modify `app/src/main/res/layout/activity_main.xml` to serve as the Navigation Host.

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/rootLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:defaultNavHost="true"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:navGraph="@navigation/nav_graph" />
</androidx.constraintlayout.widget.ConstraintLayout>

```

- Add navigation to `ShoppingFragment.kt`.

```kotlin
// ShoppingFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentShoppingBinding

class ShoppingFragment : Fragment(R.layout.fragment_shopping) {

    lateinit var viewModel: ShoppingViewModel

    private var fragmentShoppingBinding: FragmentShoppingBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentShoppingBinding.bind(view)
        fragmentShoppingBinding = binding

        viewModel = ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]

        binding.fabAddShoppingItem.setOnClickListener {
            findNavController().navigate(
                ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
            )
        }
    }

    override fun onDestroyView() {
        fragmentShoppingBinding = null
        super.onDestroyView()
    }
}

```

- Add navigation to `AddShoppingItemFragment.kt`.
- Add a callback to navigate back and remove any selected image urls.

```kotlin
// AddShoppingItemFragment.kt
package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentAddShoppingItemBinding

class AddShoppingItemFragment : Fragment(R.layout.fragment_add_shopping_item) {

    lateinit var viewModel: ShoppingViewModel

    private var fragmentAddShoppingItemBinding: FragmentAddShoppingItemBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddShoppingItemBinding.bind(view)
        fragmentAddShoppingItemBinding = binding

        viewModel = ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]

        binding.imageViewShoppingImage.setOnClickListener {
            findNavController().navigate(
                AddShoppingItemFragmentDirections.actionAddShoppingItemFragmentToImagePickFragment()
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
}

```

- Generate a test class for `ShoppingFragment.kt` at `app/src/androidTest/java/com/example/testapplication/ui/ShoppingFragmentTest.kt`.
- Create a test function `clickAddShoppingItemButton_navigateToAddShoppingItemFragment` that:
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - clicks on the floating action button with `Espresso.onView(withId(...)).perform(click())`.
  - verify that the navigation action succeeded with `Mockito.verify(navController).navigate(...)`.

```kotlin
// ShoppingFragmentTest.kt

package com.example.testapplication.ui

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.testapplication.R
import com.example.testapplication.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@MediumTest
@HiltAndroidTest
class ShoppingFragmentTest {
    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun clickAddShoppingItemButton_navigateToAddShoppingItemFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ShoppingFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.fabAddShoppingItem)).perform(click())

        verify(navController).navigate(
            ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
        )
    }
}

```

- Generate a test class for `AddShoppingItemFragment.kt` at `app/src/androidTest/java/com/example/testapplication/ui/AddShoppingItemFragmentTest.kt`.
- Create a test function `clickImageViewShoppingImage_navigateToImagePickFragment` that:
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - clicks on the floating action button with `Espresso.onView(withId(...)).perform(click())`.
  - verify that the navigation action succeeded with `Mockito.verify(navController).navigate(...)`.
- Create a test function `pressBackButton_popBackStack` that:
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - use `Espresso.pressBack` to press the back button to navigate back.
  - verify that the back navigation succeeded with `Mockito.verify(navController).popBackStack()`
- Setup the `InstantTaskExecutorRule` rule.
- Create a test function `pressBackButton_setBlankImageUrl` that:
  - sets up a `testViewModel` with fake data sources.
  - sets up a mock `NavController` with `Mockito.mock`.
  - sets the navigation controller with the mocked `NavController` object using `Navigation.setViewNavController`.
  - sets the `viewModel` of the `Fragment` to the `testViewModel` created.
  - use `Espresso.pressBack` to press the back button to navigate back.
  - assert that the url is blank.

```kotlin
// AddShoppingItemFragmentTest.kt

package com.example.testapplication.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
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

@MediumTest
@HiltAndroidTest
class AddShoppingItemFragmentTest {

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltAndroidRule.inject()
    }

    @Test
    fun clickImageViewShoppingImage_navigateToImagePickFragment() {
        val navController = mock(NavController::class.java)

        launchFragmentInHiltContainer<AddShoppingItemFragment> {
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

        launchFragmentInHiltContainer<AddShoppingItemFragment> {
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

        launchFragmentInHiltContainer<AddShoppingItemFragment> {
            Navigation.setViewNavController(requireView(), navController)
            viewModel = testViewModel
        }

        pressBack()

        assertThat(testViewModel.currentImageUrl.getOrAwaitValue()).isEqualTo("")
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

```kotlin

```

```xml

```

![Text](./static/img/name.jpg)

[y]
[y]: <https://link.to.thing> "link title"

[link title](https://link.to.thing)
