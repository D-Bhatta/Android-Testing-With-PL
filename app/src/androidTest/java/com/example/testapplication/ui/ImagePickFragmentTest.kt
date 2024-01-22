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
