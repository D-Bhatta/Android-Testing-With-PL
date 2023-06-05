// ShoppingViewModelTest.kt

package com.example.testapplication.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.testapplication.Constants
import com.example.testapplication.Message
import com.example.testapplication.Status
import com.example.testapplication.TestCoroutineRule
import com.example.testapplication.data.local.FakeLocalDataSource
import com.example.testapplication.data.local.ShoppingItem
import com.example.testapplication.data.remote.FakeRemoteDataSource
import com.example.testapplication.getOrAwaitValue
import com.example.testapplication.repository.DefaultShoppingRepository
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("HardCodedStringLiteral")
class ShoppingViewModelTest {
    private lateinit var viewModel: ShoppingViewModel

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        viewModel = ShoppingViewModel(
            DefaultShoppingRepository(FakeRemoteDataSource(), FakeLocalDataSource())
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `insert shopping item with empty field, returns error`() {
        viewModel.insertShoppingItem("", "1", "3.0")

        val valueEmptyName =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueEmptyName?.status).isEqualTo(Status.ERROR)
        assertThat(valueEmptyName?.message).isEqualTo(
            Message(Errors.INPUT_IS_EMPTY.errorMessage)
        )

        viewModel.insertShoppingItem("name", "", "3.0")

        val valueEmptyAmount =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueEmptyAmount?.status).isEqualTo(Status.ERROR)
        assertThat(valueEmptyAmount?.message).isEqualTo(
            Message(Errors.INPUT_IS_EMPTY.errorMessage)
        )

        viewModel.insertShoppingItem("name", "1", "")

        val valueEmptyPrice =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueEmptyPrice?.status).isEqualTo(Status.ERROR)
        assertThat(valueEmptyPrice?.message).isEqualTo(
            Message(Errors.INPUT_IS_EMPTY.errorMessage)
        )
    }

    @Test
    fun `insert shopping item with too long name, returns error`() {
        val tooLongNameString = buildString {
            for (i in 1..Constants.SHOPPING_ITEM_NAME_LENGTH + 1) {
                append(i)
            }
        }
        viewModel.insertShoppingItem(tooLongNameString, "3", "3.0")

        val valueTooLongNameString =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()
        assertThat(valueTooLongNameString?.status).isEqualTo(Status.ERROR)
        assertThat(valueTooLongNameString?.message).isEqualTo(Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.errorMessage))
    }

    @Test
    fun `insert shopping item with too long price, returns error`() {
        val tooLongPriceString = buildString {
            for (i in 1..Constants.SHOPPING_ITEM_PRICE_LENGTH) {
                append(i)
            }
        }

        viewModel.insertShoppingItem("name", "3", tooLongPriceString)

        val valueTooLongPriceString =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()

        assertThat(valueTooLongPriceString?.status).isEqualTo(Status.ERROR)
        assertThat(valueTooLongPriceString?.message).isEqualTo(Message(Errors.INPUT_EXCEEDS_CONSTRAINTS.errorMessage))
    }

    @Test
    fun `insert shopping item with too high amount, returns error`() {
        val tooHighAmount = "99999999999999999999999999999"

        viewModel.insertShoppingItem("name", tooHighAmount, "3.0")

        val valueTooHighAmount =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()

        assertThat(valueTooHighAmount?.status).isEqualTo(Status.ERROR)
        assertThat(valueTooHighAmount?.message).isEqualTo(Message(Errors.AMOUNT_NOT_VALID.errorMessage))
    }

    @Test
    fun `insert shopping item with valid input, returns success`() {
        viewModel.insertShoppingItem("validName", "3", "3.0")

        val valueValidInput =
            viewModel.insertShoppingItemStatus.getOrAwaitValue().getContentIfUnintercepted()

        assertThat(valueValidInput?.status).isEqualTo(Status.SUCCESS)
        assertThat(valueValidInput?.data).isEqualTo(ShoppingItem("validName", 3, 3.0, ""))
    }
}
