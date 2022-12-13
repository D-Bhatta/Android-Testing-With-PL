# Notes: Android Testing with Phillip Lackner

Course URL: [Android Testing with PL](https://www.youtube.com/playlist?list=PLQkwcJG4YTCSYJ13G4kVIJ10X5zisB2Lqen)

<!-- markdownlint-disable MD010 -->

## Sections

- [Notes: Android Testing with Phillip Lackner](#notes-android-testing-with-phillip-lackner)
  - [Sections](#sections)
  - [Notes](#notes)
  - [Setup initial project](#setup-initial-project)
  - [Add project dependencies](#add-project-dependencies)
  - [Add resources files](#add-resources-files)
  - [Define styles](#define-styles)
  - [Change main activity to a single activity architecture](#change-main-activity-to-a-single-activity-architecture)
  - [Add data layer](#add-data-layer)
  - [Add layouts](#add-layouts)
  - [Additional Information](#additional-information)
  - [Errors](#errors)
    - [Course](#course)
    - [Screenshots](#screenshots)
    - [Links](#links)
  - [Notes template](#notes-template)

## Notes

## Setup initial project

## Add project dependencies

- In the app level `build.gradle` file, add the following dependencies:

```python
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.devtools.ksp' version '1.7.20-1.0.7'
    id 'kotlin-kapt'
    id 'com.google.dagger.hilt.android'
    id 'androidx.navigation.safeargs.kotlin'
}

android {
    compileSdk 33

    defaultConfig {
        applicationId "com.example.testapplication"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    namespace 'com.example.testapplication'
}

dependencies {

    implementation 'androidx.core:core-ktx:1.9.0'
    implementation 'androidx.appcompat:appcompat:1.5.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // Material Design
    implementation 'com.google.android.material:material:1.4.0'

    // Dependency injection with Dagger-Hilt
    implementation "com.google.dagger:hilt-android:2.44"
    kapt "com.google.dagger:hilt-compiler:2.44"
    kapt 'androidx.hilt:hilt-compiler:1.0.0'
    implementation "androidx.hilt:hilt-navigation-compose:1.0.0"
    implementation 'androidx.hilt:hilt-navigation-fragment:1.0.0'
    implementation 'androidx.hilt:hilt-work:1.0.0'

    // Data storage with SQLite: Room
    implementation "androidx.room:room-runtime:2.4.3"
    ksp "androidx.room:room-compiler:2.4.3"
    implementation "androidx.room:room-ktx:2.4.3"
    testImplementation "androidx.room:room-testing:2.4.3"

    // Network transmission with Retrofit2
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    //     Use GSON for serialization
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    //     Use Moshi for serialization
    // implementation 'com.squareup.retrofit2:converter-moshi:2.9.0'

    // Navigation Components
    implementation "androidx.navigation:navigation-fragment-ktx:2.5.3"
    implementation "androidx.navigation:navigation-ui-ktx:2.5.3"
    androidTestImplementation "androidx.navigation:navigation-testing:2.5.3"

    // Glide: Image loading
    implementation 'com.github.bumptech.glide:glide:4.14.2'
    ksp 'com.github.bumptech.glide:ksp:4.14.2'

    // Timber: Logging
    implementation 'com.jakewharton.timber:timber:5.0.1'

    // Activity KTX for viewModels()
    implementation "androidx.activity:activity-ktx:1.6.1"

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

    // AndroidX
    //
    // Lifecycle
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.5.1"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.5.1"

    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'

    // Truth library for help with assertions
    testImplementation 'com.google.truth:truth:1.1.3'
    androidTestImplementation 'com.google.truth:truth:1.1.3'

    // Efficiently testing coroutines
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4'
    androidTestImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4"

    // Hamcrest: making assertions
    testImplementation "org.hamcrest:hamcrest-all:1.3"

    // Robolectric: unit testing framework
    testImplementation "org.robolectric:robolectric:4.9"

    // Mockito: mocking framework for unit tests
    testImplementation "org.mockito:mockito-core:4.8.1"
    testImplementation "org.mockito.kotlin:mockito-kotlin:4.0.0"
    testImplementation 'org.mockito:mockito-inline:4.8.1'
    androidTestImplementation "org.mockito:mockito-core:4.8.1"
    androidTestImplementation "com.linkedin.dexmaker:dexmaker-mockito:2.28.3"


}

// Allow references to generated code
kapt {
    correctErrorTypes true
}

```

- In the project level `build.gradle` file, add the following dependencies:

```python
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        def nav_version = "2.5.3"
        classpath "androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version"
    }
}

plugins {
    id 'com.android.application' version '7.3.1' apply false
    id 'com.android.library' version '7.3.1' apply false
    id 'org.jetbrains.kotlin.android' version '1.7.20' apply false
    id 'com.google.dagger.hilt.android' version '2.44' apply false
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

- This will add the project dependencies.

## Add resources files

- Add the following resources files we will use:

- `app/src/main/res/drawable-v24/ic_add.xml`: A `+` icon.
- `app/src/main/res/drawable-v24/ic_image.xml`: An image placeholder icon.
- `app/src/main/res/font/poppins_regular.ttf`: A good font.

## Define styles

- Modify colors to suit the needs of the app.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorPrimary">#6200EE</color>
    <color name="colorPrimaryDark">#3700B3</color>
    <color name="colorAccent">#03DAC5</color>
</resources>
```

- Update base application theme for both light and dark mode. Add a new text style.

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Theme.TestApplication" parent="Theme.MaterialComponents">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
        <item name="android:textViewStyle">@style/PoppinsTextStyle</item>
    </style>

    <style name="PoppinsTextStyle" parent="android:Widget.TextView">
        <item name="fontFamily">@font/poppins_regular</item>
        <item name="android:textColor">@android:color/white</item>
    </style>

</resources>
```

## Change main activity to a single activity architecture

- Create a file navigation graph using Android studio or by creating `res/navigation/nav_graph.xml`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/nav_graph">

</navigation>
```

- Update `activity_main.xml` to act as the container for Single Activity Architecture and as the `NavHostFragment`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/rootLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:defaultNavHost="true"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:navGraph="@navigation/nav_graph" />
</androidx.constraintlayout.widget.ConstraintLayout>

```

## Add data layer

- Create a package `data/local` under `main/java/com/example/testapplication`.
- Create a database with Room as `ShoppingListDatabase.kt`.

```kotlin
// ShoppingListDatabase.kt
package com.example.testapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ShoppingItem::class], version = 1
)
abstract class ShoppingListDatabase : RoomDatabase() {

    abstract fun shoppingDao(): ShoppingDao

}

```

- Create a Kotlin file `ShoppingListItem.kt` to define what a single shopping list item looks like locally.

```kotlin
// ShoppingItem.kt
package com.example.testapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    var name: String,
    var amount: Int,
    var price: Double,
    var imageURL: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)

```

- Create a DAO for the Database operations with `ShoppingListDao.kt`.

```kotlin
package com.example.testapplication.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShoppingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(shoppingItem: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem)

    @Query("SELECT * FROM shopping_items")
    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>

    @Query("SELECT SUM(price * amount) FROM shopping_items")
    fun observeTotalPrice(): LiveData<Double>
}

```

- Add the following to the bottom of app level `build.gradle` file to generate schemas. This will help in migrations.

```gradle
ksp {
    arg('room.schemaLocation', "$projectDir/schemas")
}
```

- Add the following to add the generated schemas as a source set for testing:

```gradle
android {
    compileSdk 33

    defaultConfig {
        applicationId "com.example.testapplication"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }

    sourceSets {
        androidTest.assets.srcDirs += files("$projectDir/schemas".toString())
    }

    namespace 'com.example.testapplication'
}
```

- The schemas file will be generated at `app\schemas\com.example.testapplication.data.local.ShoppingListDatabase\1.json`.

## Add layouts

- Create fragments for adding a shopping item (`fragment_add_shopping_item.xml`), shopping (`fragment_shopping.xml`), and picking an image (`fragment_image_pick.xml`) for a created shopping item.
- Create `item_shopping.xml` to define what a shopping item will look like.
- Create `item_images.xml` to contain shopping item images that will be selected in the `fragment_image_pick.xml` recycler view.
- Suppress line too long warning in `activity_main.xml`.
- Annotate values in comments in `strings.xml` with their usage.

Files:

- `fragment_shopping.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/rootLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/constraintLayoutShoppingItems"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerViewShoppingItems"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            app:layout_constraintBottom_toTopOf="@id/textViewShoppingItemPrice"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:listitem="@layout/item_shopping" />

        <com.google.android.material.textview.MaterialTextView
            android:id="@+id/textViewShoppingItemPrice"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:layout_marginStart="16dp"
            android:textSize="28sp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAddShoppingItem"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/add_shopping_item"
        app:backgroundTint="@color/colorAccent"
        app:fabSize="normal"
        app:layout_constraintBottom_toBottomOf="@id/constraintLayoutShoppingItems"
        app:layout_constraintEnd_toEndOf="@id/constraintLayoutShoppingItems"
        app:layout_constraintRight_toRightOf="@id/constraintLayoutShoppingItems"
        app:srcCompat="@drawable/ic_add" />

</androidx.constraintlayout.widget.ConstraintLayout>

```

- `item_shopping.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp">

    <ImageView
        android:id="@+id/imageViewShoppingImage"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:contentDescription="@string/shopping_item_image"
        android:scaleType="centerCrop"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textview.MaterialTextView
        android:id="@+id/textViewShoppingItemQuantity"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:text="@string/shopping_item_default_quantity"
        android:textSize="28sp"
        app:layout_constraintStart_toEndOf="@id/imageViewShoppingImage"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textview.MaterialTextView
        android:id="@+id/textViewShoppingItemName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:text="@string/shopping_item_default_name"
        android:textSize="28sp"
        app:layout_constraintStart_toEndOf="@id/textViewShoppingItemQuantity"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textview.MaterialTextView
        android:id="@+id/textViewShoppingItemPrice"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:text="@string/shopping_item_price"
        android:textSize="28sp"
        app:layout_constraintStart_toEndOf="@id/imageViewShoppingImage"
        app:layout_constraintTop_toBottomOf="@id/textViewShoppingItemQuantity" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- `fragment_add_shopping_item.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/imageViewShoppingImage"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_margin="16dp"
        android:contentDescription="@string/shopping_item_image"
        android:scaleType="centerCrop"
        android:src="@drawable/ic_image"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/shopping_item_name"
        app:layout_constraintBottom_toBottomOf="@id/imageViewShoppingImage"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/imageViewShoppingImage"
        app:layout_constraintTop_toTopOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextShoppingItemName"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:inputType="textAutoComplete" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutQuantity"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/shopping_item_quantity"
        app:layout_constraintEnd_toStartOf="@id/textInputLayoutPrice"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/imageViewShoppingImage">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextShoppingItemQuantity"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:inputType="number" />

    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutPrice"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/shopping_item_price"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toEndOf="@id/textInputLayoutQuantity"
        app:layout_constraintTop_toBottomOf="@id/imageViewShoppingImage">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextShoppingItemPrice"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:inputType="numberDecimal" />
    </com.google.android.material.textfield.TextInputLayout>


</androidx.constraintlayout.widget.ConstraintLayout>
```

- `fragment_image_pick.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/textInputLayoutSearchImage"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="@string/search_for_images"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editTextSearchImage"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

    </com.google.android.material.textfield.TextInputLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewImages"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_margin="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textInputLayoutSearchImage"
        tools:listitem="@layout/item_image" />

    <ProgressBar
        android:id="@+id/progressBarSearchImage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.5"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textInputLayoutSearchImage" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- `item_images.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--suppress LongLine -->
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <ImageView
        android:id="@+id/imageViewShoppingImage"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:contentDescription="@string/shopping_item_image"
        android:scaleType="centerCrop"
        android:src="@drawable/ic_image"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- `strings.xml`

```xml
<resources>
    <string name="app_name">TestApplication</string>

    <!--  content description: add a shopping item from the floating action button.  -->
    <string name="add_shopping_item">Add a shopping item</string>

    <!--  content description: image of the shopping item.  -->
    <string name="shopping_item_image">Shopping Item Image</string>

    <!--  default values for a shopping item that will be overridden with corresponding values.  -->
    <string name="shopping_item_default_name">Item name</string>
    <string name="shopping_item_default_quantity">3x</string>
    <string name="shopping_item_default_price">Price: $3</string>

    <!--  text input hints: for creating a shopping item.  -->
    <string name="shopping_item_name">name</string>
    <string name="shopping_item_quantity">quantity</string>
    <string name="shopping_item_price">price</string>

    <!--  text input hint: search for images for shopping items.  -->
    <string name="search_for_images">Search for images</string>
</resources>
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
