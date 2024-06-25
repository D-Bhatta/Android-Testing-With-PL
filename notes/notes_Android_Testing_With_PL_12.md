# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Setup `ShoppingFragment`](#setup-shoppingfragment)
  - [Additional Information](#additional-information)
    - [Errors](#errors)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Setup `ShoppingFragment`

- Create `app/src/main/java/com/example/testapplication/ui/ShoppingItemAdapter.kt`.

```kotlin
// ShoppingItemAdapter.kt
@file:Suppress("KDocMissingDocumentation")

package com.example.testapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.databinding.ItemShoppingBinding
import javax.inject.Inject

class ShoppingItemAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<ShoppingItemAdapter.ShoppingItemHolder>() {

    class ShoppingItemHolder(val itemShoppingBinding: ItemShoppingBinding) :
        RecyclerView.ViewHolder(itemShoppingBinding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<ShoppingItem>() {
        override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
            @Suppress(
                "MaxLineLength",
                "LongLine"
            ) return (oldItem.id == newItem.id) && (oldItem.name == newItem.name) && (oldItem.amount == newItem.amount) && (oldItem.price == newItem.price) && (oldItem.imageURL == newItem.imageURL)
        }

    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var shoppingItems: List<ShoppingItem>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemHolder {
        return ShoppingItemHolder(
            ItemShoppingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return shoppingItems.size
    }

    override fun onBindViewHolder(holder: ShoppingItemHolder, position: Int) {
        val shoppingItem = shoppingItems[position]
        holder.itemView.apply {
            glide.load(shoppingItem.imageURL)
                .into(holder.itemShoppingBinding.imageViewShoppingImage)
            holder.itemShoppingBinding.textViewShoppingItemName.text = shoppingItem.name
            holder.itemShoppingBinding.textViewShoppingItemPrice.text =
                shoppingItem.price.toString()
            holder.itemShoppingBinding.textViewShoppingItemQuantity.text =
                shoppingItem.amount.toString()
        }
    }
}
```

- In `app/src/main/java/com/example/testapplication/ui/ShoppingFragment.kt` add `ShoppingAdapter` and `ShoppingViewModel?` as dependencies.
- Add logic to use `ViewModelProvider` if null is passed for `ShoppingViewModel?`.
- Setup recycler view and observers.

```kotlin
// ShoppingFragment.kt
@file:Suppress("KDocMissingDocumentation")

package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentShoppingBinding
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ShoppingFragment @Inject constructor(
    val shoppingItemAdapter: ShoppingItemAdapter, var viewModel: ShoppingViewModel?
) : Fragment(R.layout.fragment_shopping) {

    private var fragmentShoppingBinding: FragmentShoppingBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentShoppingBinding.bind(view)
        fragmentShoppingBinding = binding

        viewModel = viewModel ?: ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]
        subscribeToObservers()
        setupRecyclerView()

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

    private fun setupRecyclerView() {
        fragmentShoppingBinding?.recyclerViewShoppingItems?.apply {
            adapter = shoppingItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToObservers() {
        viewModel?.shoppingItems?.observe(viewLifecycleOwner, Observer {
            shoppingItemAdapter.shoppingItems = it
        })

        viewModel?.totalPrice?.observe(viewLifecycleOwner, Observer {
            val price = it ?: 0f
            val priceText = getString(R.string.total_price) + price.toString()
            fragmentShoppingBinding?.textViewShoppingItemPrice?.text = priceText
        })
    }
}

```

- Create entry for `ShoppingFragment` in `app/src/main/java/com/example/testapplication/ui/ShoppingFragmentFactory.kt`.

```kotlin
// FragmentFactory.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import com.example.testapplication.data.remote.DefaultRemoteDataSource
import com.example.testapplication.repository.DefaultShoppingRepository
import javax.inject.Inject

/**
 * Instantiate fragments with constructors.
 */
class ShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter,
    private val glide: RequestManager,
    private val shoppingItemAdapter: ShoppingItemAdapter
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ShoppingFragment::class.java.name -> ShoppingFragment(shoppingItemAdapter, null)
            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter)
            AddShoppingItemFragment::class.java.name -> AddShoppingItemFragment(glide)
            else -> return super.instantiate(classLoader, className)
        }
    }
}

```

- Create `app/src/androidTest/java/com/example/testapplication/ui/TestShoppingFragmentFactory.kt` to pass a mock view model to `ShoppingFragment`.

```kotlin
// FragmentFactory.kt
package com.example.testapplication.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import com.example.testapplication.data.local.FakeLocalDataSourceAndroidTest
import com.example.testapplication.data.remote.FakeRemoteDataSourceAndroidTest
import com.example.testapplication.repository.DefaultShoppingRepository
import javax.inject.Inject

class TestShoppingFragmentFactory @Inject constructor(
    private val imageAdapter: ImageAdapter,
    private val glide: RequestManager,
    private val shoppingItemAdapter: ShoppingItemAdapter
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ShoppingFragment::class.java.name -> ShoppingFragment(
                shoppingItemAdapter, ShoppingViewModel(
                    DefaultShoppingRepository(
                        FakeRemoteDataSourceAndroidTest(), FakeLocalDataSourceAndroidTest()
                    )
                )
            )

            ImagePickFragment::class.java.name -> ImagePickFragment(imageAdapter)
            AddShoppingItemFragment::class.java.name -> AddShoppingItemFragment(glide)
            else -> return super.instantiate(classLoader, className)
        }
    }
}

```

- Fix `app/src/androidTest/java/com/example/testapplication/HiltFragmentTestingSetupTest.kt` and `app/src/androidTest/java/com/example/testapplication/ui/ShoppingFragmentTest.kt` test failures. Add the test shopping fragment there.

```kotlin
// HiltFragmentTestingSetupTest.kt
package com.example.testapplication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.testapplication.ui.AddShoppingItemFragment
import com.example.testapplication.ui.ShoppingFragment
import com.example.testapplication.ui.ShoppingFragmentFactory
import com.example.testapplication.ui.TestShoppingFragmentFactory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@Suppress("TestMethodWithoutAssertion")
@SmallTest
@HiltAndroidTest
class HiltFragmentTestingSetupTest {

    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var fragmentFactory: TestShoppingFragmentFactory

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun testLaunchFragmentInHiltContainer() {
        launchFragmentInHiltContainer<ShoppingFragment>(fragmentFactory = fragmentFactory) {}
    }
}

```

```kotlin
// ShoppingFragmentTest.kt

package com.example.testapplication.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
class ShoppingFragmentTest {
    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: TestShoppingFragmentFactory

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun clickAddShoppingItemButton_navigateToAddShoppingItemFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ShoppingFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.fabAddShoppingItem)).perform(click())

        verify(navController).navigate(
            ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
        )
    }
}

```

- Add a test `swipeOnRecyclerViewItem_DeleteShoppingItem` to test deletion of items on swipe.

```kotlin
// ShoppingFragmentTest.kt

package com.example.testapplication.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.testapplication.R
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.getOrAwaitValue
import com.example.testapplication.launchFragmentInHiltContainer
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
class ShoppingFragmentTest {
    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: TestShoppingFragmentFactory

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    private val imageURL: String =
        "https://blog.cloudflare.com/content/images/2021/09/image1-10.png"

    @Test
    fun clickAddShoppingItemButton_navigateToAddShoppingItemFragment() {
        val navController = mock(NavController::class.java)
        launchFragmentInHiltContainer<ShoppingFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.fabAddShoppingItem)).perform(click())

        verify(navController).navigate(
            ShoppingFragmentDirections.actionShoppingFragmentToAddShoppingItemFragment()
        )
    }

    @Test
    fun swipeOnRecyclerViewItem_DeleteShoppingItem(){
        val itemName = "test_shopping_item_name"
        val itemPrice = 5.0
        val itemQuantity = 10
        val shoppingItem = ShoppingItem(itemName, itemQuantity, itemPrice, imageURL)
        var testShoppingViewModel: ShoppingViewModel? = null

        launchFragmentInHiltContainer<ShoppingFragment>(fragmentFactory = fragmentFactory) {
            testShoppingViewModel = viewModel
            viewModel?.createShoppingItem(shoppingItem)
        }

        onView(withId(R.id.recyclerViewShoppingItems)).perform(
            RecyclerViewActions.actionOnItemAtPosition<ShoppingItemAdapter.ShoppingItemHolder>(
                0,
                swipeLeft()
            )
        )

        assertThat(testShoppingViewModel?.shoppingItems?.getOrAwaitValue()).isEmpty()

    }
}

```

- Add swipe to delete functionality in `ShoppingFragment`.
  - Create a `ItemTouchHelper.SimpleCallback` that on swipe deletes a `ShoppingItem`.
  - Create a `Snackbar` that shows deletion message, as well as set an undo functionality.
  - Attach callback to recyclerview.

```kotlin
// ShoppingFragment.kt
@file:Suppress("KDocMissingDocumentation")

package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.R
import com.example.testapplication.databinding.FragmentShoppingBinding
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class ShoppingFragment @Inject constructor(
    val shoppingItemAdapter: ShoppingItemAdapter, var viewModel: ShoppingViewModel?
) : Fragment(R.layout.fragment_shopping) {

    private var fragmentShoppingBinding: FragmentShoppingBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentShoppingBinding.bind(view)
        fragmentShoppingBinding = binding

        viewModel = viewModel ?: ViewModelProvider(requireActivity())[ShoppingViewModel::class.java]
        subscribeToObservers()
        setupRecyclerView()

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

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, LEFT or RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = shoppingItemAdapter.shoppingItems[pos]
            viewModel?.deleteShoppingItem(item)
            Snackbar.make(
                requireView(), getString(R.string.item_deleted_successfully), Snackbar.LENGTH_LONG
            ).apply {
                setAction("UNDO") {
                    viewModel?.createShoppingItem(item)
                }
                show()
            }
        }
    }

    private fun setupRecyclerView() {
        fragmentShoppingBinding?.recyclerViewShoppingItems?.apply {
            adapter = shoppingItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
    }

    private fun subscribeToObservers() {
        viewModel?.shoppingItems?.observe(viewLifecycleOwner, Observer {
            shoppingItemAdapter.shoppingItems = it
        })

        viewModel?.totalPrice?.observe(viewLifecycleOwner, Observer {
            val price = it ?: 0f
            val priceText = getString(R.string.total_price) + price.toString()
            fragmentShoppingBinding?.textViewShoppingItemPrice?.text = priceText
        })
    }
}

```

- Add strings

```xml
<resources>
...

    <!--  ShoppingFragment.kt BEGIN  -->
    <string name="item_deleted_successfully">Item deleted successfully</string>
    <string name="total_price">"Total Price: "</string>
    <!--  ShoppingFragment.kt END  -->
</resources>
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
