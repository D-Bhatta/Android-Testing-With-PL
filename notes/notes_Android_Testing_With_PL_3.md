# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lqen)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Testing DAO functions](#testing-dao-functions)
  - [Setup the test class](#setup-the-test-class)
  - [Helper function for testing LiveData objects](#helper-function-for-testing-livedata-objects)
  - [Test the dao functions](#test-the-dao-functions)
    - [Insertion function](#insertion-function)
    - [Deletion function](#deletion-function)
    - [Total price function](#total-price-function)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Testing DAO functions

- We should keep to the same file structure as in the `src` package.
- Create `data/local` package in `androidTest/java/com/example/testapplication`.
- We create a new class `ShoppingDaoTest.kt`.

## Setup the test class

```kotlin
// ShoppingDaoTest.kt
package com.example.testapplication.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ShoppingDaoTest {
}
```

- We annotate with `ShoppingDaoTest` class with `@RunWith(AndroidJUnit4::class)`. JUnit normally expects to run inside a plain JVM. However, instead of a normal JVM, instrumented tests run inside an Android environment. This annotation is used to tell JUnit that it is an Android instrumented test.
- We annotate with `@SmallTest`. This is used to tell JUnit that this test qualifies as a small test by Google's testing conventions. Since the database is SQlite, we can load it into memory alone and it should run within 60 seconds, we can consider this a small test.
- We annotate with `ExperimentalCoroutinesApi` to since we will be using the `runTest` function. It helps with testing coroutines.
- We create a database and a dao variable that we will initialize before each test.
- We create a `setup` function with a `@Before` annotation, and initialize the database and DAO variables.
- We initialize the database as an in-memory database using `inMemoryDatabaseBuilder`.
- Usually database access, Room or otherwise, is restricted to the background thread for good reason. However, during testing, we want the database to run in the main thread on the android platform. This is because if it ran on a background thread, several test cases might manipulate it at the same time. Running it in the main thread ensures that only one test case has access to it at the same time. We use `allowMainThreadQueries` for this.
- We create a `teardown` function to close the database after each test case.

```kotlin
// ShoppingDaoTest.kt
package com.example.testapplication.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ShoppingDaoTest {

    private lateinit var database: ShoppingListDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShoppingListDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.shoppingDao()
    }

    @After
    fun teardown() {
        database.close()
    }
}
```

## Helper function for testing LiveData objects

- Google has a helper file what we can use in projects compatible with Apache V2 license.
- This file has a function that can be used to return data from `LiveData` objects during testing.
- To use this file, it is necessary to comply with the requirements of the Apache License.
- We store this file at `app\src\androidTest\java\com\example\testapplication\LiveDataTestUtil.kt`

## Test the dao functions

- To prevent concurrent execution of tests on the same database, we will use `runTest` to ensure the coroutine runs in a single thread, though it will not honor delays.

### Insertion function

- To test the insertion function, we will insert an item in the database, and then read it back from the database.
- We create a new `val shoppingItem` and we insert it into the database.
- We observe all shopping items in the database, and get the list of inserted items.
- We assert that the list contains the item.
- We want the result of the `LiveData` instantly, so we use the `InstantTaskExecutorRule`. This ensures that we do not encounter the errors `Cannot invoke observeForever on a background thread during test` or `Job has not completed yet`. It swaps the background executor so that tests execute instantly.

```gradle
// Core
testImplementation("junit:junit:4.13.2")
testImplementation("androidx.arch.core:core-testing:2.1.0")
androidTestImplementation("androidx.test.ext:junit:1.1.3")
androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
androidTestImplementation("androidx.arch.core:core-testing:2.1.0")

```

```kotlin
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
}
```

### Deletion function

- To test the deletion function, we will insert 2 items in the database and delete one of them immediately after.
- We observe all shopping items, and assert that there is no such item in the database.

```kotlin
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
```

### Total price function

- To test the total price function `observeTotalPrice`, we will insert 3 items and observe their total price.

```kotlin
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
```

## Additional Information

## Errors

### Course

### Screenshots

### Links

- Craig Russell: [Testing Android Coroutines using runTest](https://craigrussell.io/2021/12/testing-android-coroutines-using-runtest/)
- Google Blog: [Test Sizes](https://testing.googleblog.com/2010/12/test-sizes.html)
- kotlinx.coroutines: [runTest](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/kotlinx.coroutines.test/run-test.html)

## Notes template

```language

```

![Text](./static/img/name.jpg)

- [Text](Link)
- StackOverflow:
- Android Dev Docs ():
- AndroidX releases:
- ProAndroidDev
- Dagger Docs: [Hilt Application](https://dagger.dev/hilt/application.html)
- Howtodoandroid:
- Medium:
