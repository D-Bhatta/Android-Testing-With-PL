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
