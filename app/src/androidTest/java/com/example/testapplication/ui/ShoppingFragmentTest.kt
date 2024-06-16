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
