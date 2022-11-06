# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lqen)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Setup](#setup)
    - [testImplementaion and androidTestImplementation](#testimplementaion-and-androidtestimplementation)
    - [Adding the truth library to project](#adding-the-truth-library-to-project)
    - [Final Dependencies in app level `build.gradle` for now](#final-dependencies-in-app-level-buildgradle-for-now)
  - [Create a `RegistrationUtil.kt` object](#create-a-registrationutilkt-object)
  - [Create test file `RegistrationUtilTest.kt` under the `test` directory](#create-test-file-registrationutiltestkt-under-the-test-directory)
    - [Add tests for `RegistrationUtil.kt` in `RegistrationUtilTest.kt`](#add-tests-for-registrationutilkt-in-registrationutiltestkt)
  - [Write logic to satisfy `RegistrationUtilTest.kt` tests in `RegistrationUtil.kt`](#write-logic-to-satisfy-registrationutiltestkt-tests-in-registrationutilkt)
  - [Instrumented tests](#instrumented-tests)
  - [Create `ResourceComparer.kt` class to compare a string resource with a string](#create-resourcecomparerkt-class-to-compare-a-string-resource-with-a-string)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Setup

### testImplementaion and androidTestImplementation

- The `test` directory is for unit tests, and other tests which do not require android components to run.
- The `androidTest` directory is for integrated tests, which depend on parts of the android system to run, such as fragments, activiites, and such.
- Each of `testImplementaion` and `androidTestImplementation` is called a single source set. They live in the `src/test` and `src/androidTest` folders.
- `testImplementaion` is for tests in the `test` directory. These tests do not need part of the android framework to function.
- `androidTestImplementation` is for tests in the `androidTest` directory. These need part of the android framework to run. These are instrumented unit tests.

![Test directories](./static/img/test-directories.png)

### Adding the truth library to project

- We use the `truth` library because it allows us to write much more readable assertions.

### Final Dependencies in app level `build.gradle` for now

```gradle
dependencies {

    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.5.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.3'

    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'

    // Truth library for help with assertions
    testImplementation'com.google.truth:truth:1.1.3'
    androidTestImplementation 'com.google.truth:truth:1.1.3'
}
```

## Create a `RegistrationUtil.kt` object

- This will be used as an object to test.
- It acts as an utility function that checks if registration data (username, password, confirmed password) is valid depending upon the conditions.
- We add the conditions we assume the data to be valid under as a docstring.
- We create a `List<String>` to act as a mock database.

```kotlin
// RegistrationUtil.kt
package com.example.testapplication

object RegistrationUtil {

  private val users: List<String> = listOf(
    "Tom",
    "Phillip",
    "Sam",
    "Alisha",
    "Erin",
    "Stacey2358",
  )

  /* *
   * the input is not valid if ...
   * ... the username or password is empty
   * ... the password and confirmedPassword do not match
   * ... the username is already taken
   * ... the password length is at least 12 characters
   * ... the password contains at least 2 digits
   */
  fun validateRegistrationInput(
    username: String,
    password: String,
    confirmedPassword: String
  ): Boolean {
    return true
  }
}
```

## Create test file `RegistrationUtilTest.kt` under the `test` directory

- We create the test file by right clicking on the Object name and choosing `Generate...` and then `Test...`. We can also do this manually.
- We are using the `Junit4` library, since `JUnit5` compatibility might not be there yet.
- Since this is a unit test and not an instrumented test, that would require the android framework, we save this in the `test` directory.

```kotlin
package com.example.testapplication

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RegistrationUtilTest {

  @Test
  fun `empty username returns false`(){
    val result = RegistrationUtil.validateRegistrationInput(
      username = "",
      password = "za5la3Ich5cocuat",
      confirmedPassword = "za5la3Ich5cocuat"
    )
    assertThat(result).isFalse()
  }
}
```

- Change `assert` to `assertThat` method from `truth` library.
- Use `@Test` annotation to indicate a test that will be run by junit.
- Write descriptive test method name within backticks. This functionality is only available for unit tests in kotlin.
- Instrumented tests must have proper method names, no backticks.
- Run the test method or the test class.
- It should fail.

### Add tests for `RegistrationUtil.kt` in `RegistrationUtilTest.kt`

- Add the tests

```kotlin
package com.example.testapplication

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RegistrationUtilTest {

    @Test
    fun `empty username returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "za5la3Ich5cocuat"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `valid username and correctly repeated password returns true`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "za5la3Ich5cocuat"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `username already exists returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Tom",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "za5la3Ich5cocuat"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `empty password returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "",
            confirmedPassword = ""
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `password and confirmed password do not match returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "za5la3Ich5cocuat",
            confirmedPassword = "notmatchingpass"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `password length less than 12 characters returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "za5la",
            confirmedPassword = "za5la"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `password contains less than 2 digits returns false`() {
        val result = RegistrationUtil.validateRegistrationInput(
            username = "Rachel",
            password = "side1burns",
            confirmedPassword = "side1burns"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `number of digits in 541132 is 6`() {
        val result = RegistrationUtil.numberOfDigits("541132")
        assertThat(result).isEqualTo(6)
    }

    @Test
    fun `number of digits in 112david is 3`() {
        val result = RegistrationUtil.numberOfDigits("112david")
        assertThat(result).isEqualTo(3)
    }

    @Test
    fun `number of digits in red41riding is 2`() {
        val result = RegistrationUtil.numberOfDigits("red41riding")
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `number of digits in blue67one29pink is 4`() {
        val result = RegistrationUtil.numberOfDigits("blue67one29pink")
        assertThat(result).isEqualTo(4)
    }

    @Test
    fun `number of digits in tint91 is 2`() {
        val result = RegistrationUtil.numberOfDigits("tint91")
        assertThat(result).isEqualTo(2)
    }
}
```

- Run the tests and make sure they all fail.

## Write logic to satisfy `RegistrationUtilTest.kt` tests in `RegistrationUtil.kt`

- Write the code and run the tests until all the tests pass

```kotlin
// RegistrationUtil.kt
package com.example.testapplication

object RegistrationUtil {

    private val users: List<String> = listOf(
        "Tom",
        "Phillip",
        "Sam",
        "Alisha",
        "Erin",
        "Stacey2358",
    )

    /* *
     * the input is not valid if ...
     * ... the username or password is empty
     * ... the password and confirmedPassword do not match
     * ... the username is already taken
     * ... the password length is at least 12 characters
     * ... the password contains at least 2 digits
     */
    fun validateRegistrationInput(
        username: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        if (username.isEmpty() or password.isEmpty()) {
            return false
        }
        if (!(password.equals(confirmedPassword, ignoreCase = false))) {
            return false
        }
        if (users.contains(username)) {
            return false
        }
        if (password.length < 12) {
            return false
        }
        if (numberOfDigits(password) < 2) {
            return false
        }
        return true
    }

    fun numberOfDigits(string: String): Int {
        var count: Int = 0
        for (letter in string) {
            if (letter.isDigit()) {
                count += 1
            }
        }
        return count
    }
}
```

## Instrumented tests

- Instrumented test require the Android framework. This is because the code we are testing might depend on an Android component itself.

## Create `ResourceComparer.kt` class to compare a string resource with a string

- This test will depend on a string resource. All resources in Android require the `Context`. Thus, the test for this code will be an Instrumented test.
- In the main source set, we create a new kotlin class called `ResourceComparer.kt`. Inside it we create a function `isStringEqual` which takes a `Context` object, a resource ID of type `int`, and a `String`, and returns a `Boolean` on whether the strings are equal or not.

```kotlin
// ResourceComparer.kt.kt
package com.example.testapplication

import android.content.Context

class ResourceComparer {

    fun isStringEqual(context: Context, resId: Int, string: String): Boolean {
        return false
    }
}
```

- We right click on the class name and select `Generate...` or `Alt + Insert` on Android Studio and then `Test...`.
- We choose the name of the test, and then `OK`. Then we select the `androidTest` directory for the destination.
- This will generate the `ResourceComparerTest` file for us.

```kotlin

// ResourceComparerTest.kt
package com.example.testapplication

class ResourceComparerTest 
```

- We need to get the `Context` object for use in our test case. We also need to initialize the `ResourceComparer`.
- We could create a private variable as the global object, but then this object will not be unique to all the tests. This will mean that tests will share state between them, which is bad practice since one test can influence another.
- When naming tests in `androidTest` we cannot have function names inside backticks ` `` `. We use the naming convention of `WHEN_THEN`.

```kotlin
// ResourceComparerTest.kt
package com.example.testapplication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceComparerTest {
    private val resourceComparer: ResourceComparer = ResourceComparer()

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
```

- When we run the tests, it runs on the device.
- To reduce state, we could initialize the `ResouceComparer` objects inside the test methods. That way both the tests will have their own object with fresh state.

```kotlin
// ResourceComparerTest.kt
package com.example.testapplication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceComparerTest {
    private lateinit var resourceComparer: ResourceComparer

    @Test
    fun stringResourceSameAsGivenString_returnsTrue() {
        resourceComparer = ResourceComparer()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isStringEqual(context, R.string.app_name, "TestApplication")
        assertThat(result).isTrue()
    }

    @Test
    fun stringResouceDiffernentFromGivenString_returnsFalse() {
        resourceComparer = ResourceComparer()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isStringEqual(context, R.string.app_name, "AnyString")
        assertThat(result).isFalse()
    }
}
```

- The tests run fine again. However, this increases the verbosity of the test.
- Now, we use a `setup` function to initialize the dependencies for each test.

```kotlin
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
```

- The `lateinit var` is used to delay the initialization.
- The `@Before` annotation is used to signify a `setup` function that executes before each test case method.
- The `@After` annotation is used to do things after each test that executes, like close out DB connections.
- Connecting a device will run the test on a physical device instead of the emulator.

- Make sure the tests fail.
- Write out the app logic in `ResourceComparer.kt`

```kotlin
package com.example.testapplication

import android.content.Context

/*
* * Checks if the string resource at `resId` is equal to `string`
*/
class ResourceComparer {
    fun isEqual(context: Context, resId: Int, string: String): Boolean{
        return context.getString(resId) == string
    }
}
```

## Additional Information

## Errors

### Course

### Screenshots

### Links

- .

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
