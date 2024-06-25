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
