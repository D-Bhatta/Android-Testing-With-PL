# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lq)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Testing Android components](#testing-android-components)
  - [Testing Fragments](#testing-fragments)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Testing Android components

- We use Hilt to create mocks and fakes for testing in a central place in an `AppModule` for testing, and use that in all the places through injection.
- This prevents the need to create things like a database instance in multiple places, and we can just inject the instances everywhere we need them.
- This, however, only works for instrumented tests. Since it uses Android components.
- We add the following to the `app/build.gradle.kts` file.

```kotlin
dependencies {
    // Dependency injection with Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    implementation("androidx.core:core-ktx:1.9.0")
    kapt("com.google.dagger:hilt-compiler:2.44")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptTest("com.google.dagger:hilt-android-compiler:2.44")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
}
```

- We now create our own test runner in the `app/src/androidTest/java/com/example/testapplication` package as `CustomTestRunnerHilt.kt` and change the `className` to `HiltTestApplication::class.java.name`. This creates an `Application` object for hilt to use, using the prebuilt `HiltTestApplication`.
- This hilt compatible test runner will be used for all instrumented tests.

```kotlin
// CustomTestRunnerHilt
package com.example.testapplication

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class CustomTestRunnerHilt: AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
```

- We set this test runner as the `testInstrumentationRunner` in `app/build.gradle.kts` under `defaultConfig`.

```kotlin
defaultConfig {
        applicationId = "com.example.testapplication"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "PIXABAY_API_KEY", apiKeyProperties)

        testInstrumentationRunner = "com.example.testapplication.CustomTestRunnerHilt"
    }
```

- In `ShoppingDaoTest.kt` we remove the `@RunWith(AndroidJUnit4::class)` annotation from `ShoppingDaoTest` and instead annotate it with `@HiltAndroidTest` to let Hilt know that we want to inject dependencies in the test class, and hilt will generate the components for the test.
- We add the `HiltAndroidRule` with the test instance as `this` to manage the components state and inject them into the test.
- We remove `private` from the `database` variable, since we cannot inject into a private variable, and annotate it with `@Inject`. We use `@Named` to name it `androidTestInMemoryRoomDatabase` to differentiate it from the component in our `app/src/main/java/com/example/testapplication/AppModule.kt`.
- In the `setup` function, we use `inject` method of the `HiltAndroidRule` object to initialize all variables. Any previous initializations that conflict with hilt should be removed.

```kotlin
// ShoppingDaoTest.kt
package com.example.testapplication.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.testapplication.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class ShoppingDaoTest {

    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("androidTestInMemoryRoomDatabase")
    lateinit var database: ShoppingListDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        dao = database.shoppingDao()
    }

    @After
    fun teardown() {
        database.close()
    }
    ...
}
```

- We create a module object in `app/src/androidTest/java/com/example/testapplication` as `AndroidTestAppModule`. We make it a singleton component for our application.

```kotlin
// AndroidTestAppModule
package com.example.testapplication

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AndroidTestAppModule {
}

```

- Unlike our production application, we do not want our components to be singletons and instead want them to be recreated between tests. So we do not annotate them as singletons.
- We create a function to provide a Room database instance for testing.
- We use the same `@Named` annotation here that we used in the `ShoppingDaoTest` class to let Hilt know that this is the `androidTestInMemoryRoomDatabase` we want injected into the `database` variable.

```kotlin
// AndroidTestAppModule
package com.example.testapplication

import android.content.Context
import androidx.room.Room
import com.example.testapplication.data.local.ShoppingListDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AndroidTestAppModule {

    @Provides
    @Named("androidTestInMemoryRoomDatabase")
    fun provideInMemoryDatabase(@ApplicationContext context: Context) =
        Room.inMemoryDatabaseBuilder(context, ShoppingListDatabase::class.java)
            .allowMainThreadQueries().build()
}

```

## Testing Fragments

When we test fragments, we use a `FragmentScenario` which is an empty `Activity` that we test our fragment inside.

This doesn't work very well with Hilt because when we want to inject dependencies in our fragments, we need to annotate these fragments with `AndroidEntryPoint` AND we need to annotate the empty `Activities` with the same. Since `FragmentScenarios` launch with an empty activity that doesn't have this annotation, this crashes with Hilt.

To solve this, we create a custom activity that we annotate with `AndroidEntryPoint` and attach our fragment to this.

- Add dependencies in `app/build.gradle.kts`.

```kotlin
dependencies {
...
    // AndroidX
    //
...

    // Fragments
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
...

}
```

- Create a `debug` sourceset under `app/src` and create the following directory structure as `app/src/debug/java/com/example/testapplication`.

```terminal
src/debug
└───java
    └───com
        └───example
            └───testapplication
```

- Create an empty activity as `HiltTestActivity` as `app/src/debug/java/com/example/testapplication/HiltTestActivity.kt` for testing fragments with Hilt and annotate it with `@AndroidEntryPoint`.

```kotlin
// HiltTestActivity
package com.example.testapplication

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Empty Activity for testing Fragments with Hilt.
 */
@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity()

```

- We create a manifest file as `app/src/main/AndroidManifest.xml`. We set `android:exported="false"` to ensure that this activity can only be accessed from this package.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:icon="@mipmap/ic_launcher">
        <activity
            android:name=".HiltTestActivity"
            android:exported="false"/>
    </application>

</manifest>
```

- We create an extension function to launch fragments in activities annotated with `@AndroidEntryPoint`.
- We copy this from the [android/architecture-components-samples repository](https://github.com/android/architecture-components-samples).
- The `inline` function means that the compiler will insert the code of the function inline from where it is called, instead of creating and initiating another function object. This makes the code more efficient while maintaining readability.
- The `T` means that it is a generic function that inherits from `Fragment`.
- The `reified` keyword allows us to access the class information of the generic `T` even after it has been erased at runtime. This allows us to work with `T` as if it were a normal class. Using it with a `inline` function lets the compiler know that when copying the bytecode of the `inline` function it should replace all generics with the actual class type of the argument.[1] [2] [3]
- `fragmentArgs` is any args we might want to pass to the `Fragment` represented by `T`.
- We set the theme to the default theme used by the `FragmentScenarioEmptyFragmentActivityTheme` during testing a fragment.
- We can add a `fragmentFactory: FragmentFactory? = null` argument,it allows us to attach a fragment factory to that, which in turn gives us the capability to perform constructor injection into our fragments.
- `action` is used to gain an reference to the fragment launched in the HIlt container. It is a lambda function. The [`crossinline` keyword][4] is used to designate any non-local returns in an inlined lambda function originating from another execution context as forbidden.
- We create the intent that starts our `HiltTestActivity`. Since it will be the `MainActivity` here as the only activity, we use the `Intent.makeMainActivity()` method to create it. We attach the theme to it.
- We launch the `HiltTestActivity` with `ActivityScenario`.
- We can set the `FragmentFactory` we passed as an argument to the as the `supportFragmentManager.fragmentFactory`.
- We instantiate the fragment, and attach arguments to it.
- We use `beginTransaction` to launch our fragment with no tag.
- We call the lambda function.

```kotlin

/*
 * Taken from the android/architecture-components-samples repository
 * under the Apache License, Version 2.0, January 2004
 * hosted at
 *      https://github.com/android/architecture-components-samples
 * Modifications copyright (C) 2023 Debabrata Bhattacharya:
 *      package declaration on line 233
 *      import on line 257
 *      argument addition on line 259
 *      code addition on line 276
 */

// ! The line immediately below this line has been modified
package com.example.testapplication

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.core.internal.deps.guava.base.Preconditions

/**
 * launchFragmentInContainer from the androidx.fragment:fragment-testing library
 * is NOT possible to use right now as it uses a hardcoded Activity under the hood
 * (i.e. [EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint.
 *
 * As a workaround, use this function that is equivalent. It requires you to add
 * [HiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
 * as can be found in this project.
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    // ! The line immediately below this line has been modified
    @StyleRes themeResId: Int = androidx.fragment.testing.R.style.FragmentScenarioEmptyFragmentActivityTheme,
    // ! The line immediately below this line has been modified
    fragmentFactory: FragmentFactory? = null,
    crossinline action: Fragment.() -> Unit = {}
) {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )
    ).putExtra(
        "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
        themeResId
    )

    ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        // ! The line immediately below this line has been modified
        fragmentFactory?.let { activity.supportFragmentManager.fragmentFactory = it }
        val fragment: Fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )
        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()

        fragment.action()
    }
}

```

- We write a test to check that this flaky setup works at `app/src/androidTest/java/com/example/testapplication/HiltFragmentTestingSetupTest.kt`. If the test errors out with `Can not perform this action after onSaveInstanceState` and you are running this on a real device, try keeping the screen turned on and the device unlocked while the test runs.

```kotlin
// HiltFragmentTestingSetupTest.kt
package com.example.testapplication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.testapplication.ui.AddShoppingItemFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("TestMethodWithoutAssertion")
@SmallTest
@HiltAndroidTest
class HiltFragmentTestingSetupTest {

    @get:Rule
    var hiltAndroidRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }

    @Test
    fun testLaunchFragmentInHiltContainer() {
        launchFragmentInHiltContainer<AddShoppingItemFragment> {}
    }
}

```

## Additional Information

## Errors

### Course

### Screenshots

### Links

- [Inline functions](https://kotlinlang.org/docs/inline-functions.html)
- [Reified Functions in Kotlin](https://www.baeldung.com/kotlin/reified-functions)
- [How does the reified keyword in Kotlin work?](https://stackoverflow.com/questions/45949584/how-does-the-reified-keyword-in-kotlin-work)
- [Non-local returns](https://kotlinlang.org/docs/inline-functions.html#non-local-returns)

[1]: <https://kotlinlang.org/docs/inline-functions.html> "Inline functions"
[2]: <https://www.baeldung.com/kotlin/reified-functions> "Reified Functions in Kotlin"
[3]: <https://stackoverflow.com/questions/45949584/how-does-the-reified-keyword-in-kotlin-work> "How does the reified keyword in Kotlin work?"
[4]: <https://kotlinlang.org/docs/inline-functions.html#non-local-returns> "Non-local returns"

## Notes template

```kotlin

```

```xml

```

![Text](./static/img/name.jpg)

[y]
[y]: <https://link.to.thing> "link title"

[link title](https://link.to.thing)
