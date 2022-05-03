# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Setup](#setup)
    - [testImplementaion and androidTestImplementation](#testimplementaion-and-androidtestimplementation)
    - [Adding the truth library to project](#adding-the-truth-library-to-project)
    - [Final Dependencies in app level `build.gradle`](#final-dependencies-in-app-level-buildgradle)
  - [Create a `RegistrationUtil.kt` object](#create-a-registrationutilkt-object)
  - [Create test file `RegistrationUtilTest.kt` under the `test` directory](#create-test-file-registrationutiltestkt-under-the-test-directory)
    - [Add tests for `RegistrationUtil.kt` in `RegistrationUtilTest.kt`](#add-tests-for-registrationutilkt-in-registrationutiltestkt)
  - [Write logic to satisfy `RegistrationUtilTest.kt` tests in `RegistrationUtil.kt`](#write-logic-to-satisfy-registrationutiltestkt-tests-in-registrationutilkt)
  - [Create `ResourceComparer.kt` class to compare a string resource with a string](#create-resourcecomparerkt-class-to-compare-a-string-resource-with-a-string)
  - [Create a test class `ResourceComparerTest.kt`](#create-a-test-class-resourcecomparertestkt)
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
- `testImplementaion` is for tests in the `test` directory.
- `androidTestImplementation` is for tests in the `androidTest` directory.

test-directories.png

### Adding the truth library to project

- We use the `truth` library because it allows us to write much more readable assertions.

### Final Dependencies in app level `build.gradle`

```shell
dependencies {

    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.6.0'
    implementation 'androidx.appcompat:appcompat:1.3.0'
    implementation 'com.google.android.material:material:1.4.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'

    testImplementation "com.google.truth:truth:1.1.3"
    androidTestImplementation "com.google.truth:truth:1.1.3"
}
```

## Create a `RegistrationUtil.kt` object

- This will be used as an object to test.
- It acts as an utility function that checks if registration data is valid or not.
- Add assumptions in doc string.
- Add a variable to hold existing users.

```kotlin
package com.example.testapplication

object RegistrationUtil {

    private val existingUsers =
        listOf("Peter", "R Kesh", "Li Fong", "RakeshDeshmukh", "UsrExisting")

    /**
     * * The input is not valid if...
     * ...the username/password is empty
     * ...the username is already taken
     * ...the confirmed password is not the same as the real password
     * ...the password is less than 12 characters in length
     */

    fun validateUserInput(
        username: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        return true
    }
}
```

## Create test file `RegistrationUtilTest.kt` under the `test` directory

- Create test file

```kotlin
package com.example.testapplication

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RegistrationUtilTest{
    @Test
    fun `empty username returns false`(){
        val result = RegistrationUtil.validateUserInput(
            "",
            "123456789101112",
            "123456789101112"
        )
        assertThat(result).isFalse()
    }
}
```

- Change `assert` to `assertThat` method from `truth` library.
- Use `@Test` annotation to indicate a test that will be run by junit.
- Write descriptive test name within backticks.
- Backticks only allowed for writing test method names.
- Run the test method or the test class.
- It should fail.

### Add tests for `RegistrationUtil.kt` in `RegistrationUtilTest.kt`

- Add the tests

```kotlin
package com.example.testapplication

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RegistrationUtilTest{

    @Test
    fun `empty username returns false`(){
        val result = RegistrationUtil.validateUserInput(
            "",
            "123456789101112",
            "123456789101112"
        )
        assertThat(result).isFalse()
    }

    @Test
    fun `valid username and correctly repeated password returns true`(){
        val result = RegistrationUtil.validateUserInput(
            "DBhatta",
            "123456789101112",
            "123456789101112"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `username already exists returns false`(){
        val result = RegistrationUtil.validateUserInput(
            "UsrExisting",
            "123456789101112",
            "123456789101112"
        )
        assertThat(result).isFalse()
    }

    // empty password
    @Test
    fun `empty password returs false`(){
        val result = RegistrationUtil.validateUserInput(
            "DBhatta",
            "",
            ""
        )
        assertThat(result).isFalse()
    }
    // password repeated incorrectly
    @Test
    fun `password and repeated password do not match returns false`(){
        val result = RegistrationUtil.validateUserInput(
            "DBhatta",
            "123456789101113",
            "123456789101112"
        )
        assertThat(result).isFalse()
    }
    // password contains less than 12 characters
    @Test
    fun `password less than 12 characters returns false`(){
        val result = RegistrationUtil.validateUserInput(
            "DBhatta",
            "123456789",
            "123456789"
        )
        assertThat(result).isFalse()
    }

}
```

- Run the tests and make sure they all fail.

## Write logic to satisfy `RegistrationUtilTest.kt` tests in `RegistrationUtil.kt`

- Write the code and run the tests until all the tests pass

```kotlin
package com.example.testapplication

object RegistrationUtil {

    private val existingUsers =
        listOf("Peter", "R Kesh", "Li Fong", "RakeshDeshmukh", "UsrExisting")

    /**
     * * The input is not valid if...
     * ...the username/password is empty
     * ...the username is already taken
     * ...the confirmed password is not the same as the real password
     * ...the password is less than 12 characters in length
     */

    fun validateUserInput(
        username: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        if (username.isEmpty() || password.isEmpty()) {
            return false
        }
        if (username in existingUsers) {
            return false
        }
        if (confirmedPassword != password) {
            return false
        }
        if (password.length < 12) {
            return false
        }
        return true
    }
}
```

## Create `ResourceComparer.kt` class to compare a string resource with a string

```kotlin
package com.example.testapplication

import android.content.Context

/*
* * Checks if the string resource at `resId` is equal to `string`
*/
class ResourceComparer {
    fun isEqual(context: Context, resId: Int, string: String): Boolean{
        return true
    }
}
```

## Create a test class `ResourceComparerTest.kt`

```kotlin
package com.example.testapplication

import org.junit.Assert.*

class ResourceComparerTest{
    
}
```

- Write a test case, and change the `assertThat` function import to the `truth` library.

```kotlin
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
        val result = resourceComparer.isEqual(context, R.string.app_name, "TestApplication")
        assertThat(result).isTrue()
    }

    @Test
    fun stringResourceSameAsGivenString_returnsFalse() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context, R.string.app_name, "HellWorld")
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
