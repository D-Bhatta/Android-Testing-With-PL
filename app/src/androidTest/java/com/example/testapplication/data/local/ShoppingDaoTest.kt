// ShoppingDaoTest.kt
package com.example.testapplication.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.testapplication.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ShoppingDaoTest {

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ShoppingListDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), ShoppingListDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.shoppingDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertShoppingItem(): TestResult = runTest {
        @Suppress("HardCodedStringLiteral") val shoppingItem =
            ShoppingItem("test item", 1, 10.0, "url", 1)

        dao.insertShoppingItem(shoppingItem)

        val allShoppingItems = dao.observeAllShoppingItems().getOrAwaitValue()

        assertThat(allShoppingItems).contains(shoppingItem)
    }

    @Test
    fun deleteShoppingItem(): TestResult = runTest {
        @Suppress("HardCodedStringLiteral") val firstShoppingItem =
            ShoppingItem("test item", 1, 10.0, "url", 1)
        dao.insertShoppingItem(firstShoppingItem)

        @Suppress("HardCodedStringLiteral") val secondShoppingItem =
            ShoppingItem("second test item", 1, 10.0, "url", 2)
        dao.insertShoppingItem(secondShoppingItem)

        dao.deleteShoppingItem(firstShoppingItem)

        val allShoppingItems = dao.observeAllShoppingItems().getOrAwaitValue()

        assertThat(allShoppingItems).doesNotContain(firstShoppingItem)
        assertThat(allShoppingItems).contains(secondShoppingItem)
        assertThat(allShoppingItems.size).isEqualTo(1)
    }

    @Suppress("HardCodedStringLiteral")
    @Test
    fun observeTotalPrice(): TestResult = runTest {
        val shoppingItem1 = ShoppingItem("first item", 2, 10.0, "url/1")
        val shoppingItem2 = ShoppingItem("second item", 4, 5.5, "url/2")
        val shoppingItem3 = ShoppingItem("third item", 2, 3.6, "url/3")

        dao.insertShoppingItem(shoppingItem1)
        dao.insertShoppingItem(shoppingItem2)
        dao.insertShoppingItem(shoppingItem3)

        val totalPrice: Double = dao.observeTotalPrice().getOrAwaitValue()

        assertThat(totalPrice).isEqualTo(2 * 10.0 + 4 * 5.5 + 2 * 3.6)
    }
}