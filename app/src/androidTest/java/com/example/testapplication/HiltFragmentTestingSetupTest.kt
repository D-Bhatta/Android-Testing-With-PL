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
