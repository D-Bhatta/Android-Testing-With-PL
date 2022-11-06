// ResourceComparerTest.kt
package com.example.testapplication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class ResourceComparerTest {
    private lateinit var resourceComparer: ResourceComparer

    @Before
    fun setup() {
        resourceComparer = ResourceComparer()
    }

    @After
    fun teardown() {
    }

    @Test
    fun stringResourceSameAsGivenString_returnsTrue() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isStringEqual(context, R.string.app_name, "TestApplication")
        assertThat(result).isTrue()
    }

    @Test
    fun stringResouceDiffernentFromGivenString_returnsFalse() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isStringEqual(context, R.string.app_name, "AnyString")
        assertThat(result).isFalse()
    }
}