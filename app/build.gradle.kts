// file:noinspection HardCodedStringLiteral
// file:noinspection LongLine
@file:Suppress("HardCodedStringLiteral")

import java.util.Properties
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
}
val properties = File(rootDir, "apikey.properties").inputStream().use {
    Properties().apply { load(it) }
}
val apiKeyProperties = properties.getValue("PIXABAY_API_KEY") as String

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.testapplication"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "PIXABAY_API_KEY", apiKeyProperties)

        testInstrumentationRunner = "com.example.testapplication.CustomTestRunnerHilt"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    sourceSets {
        getByName("androidTest").assets.srcDirs(files("${projectDir}/schemas"))
    }

    namespace = "com.example.testapplication"

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Design
    implementation("com.google.android.material:material:1.4.0")

    // Dependency injection with Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.core:core-ktx:1.9.0")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    testImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspTest("com.google.dagger:hilt-android-compiler:2.51.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")

    // Data storage with SQLite: Room
    implementation("androidx.room:room-runtime:2.4.3")
    ksp("androidx.room:room-compiler:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
    testImplementation("androidx.room:room-testing:2.4.3")

    // Network transmission with Retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //     Use GSON for serialization
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //     Use Moshi for serialization
    // implementation "com.squareup.retrofit2:converter-moshi:2.9.0"

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    androidTestImplementation("androidx.navigation:navigation-testing:2.5.3")

    // Glide: Image loading
    implementation("com.github.bumptech.glide:glide:4.14.2")
    ksp("com.github.bumptech.glide:ksp:4.14.2")

    // Timber: Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Activity KTX for viewModels()
    implementation("androidx.activity:activity-ktx:1.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // AndroidX
    //
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")

    // Fragments
    debugImplementation("androidx.fragment:fragment-testing:1.5.0")

    // Testing Core
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:3.1.2")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")

    // Truth library for help with assertions
    testImplementation("com.google.truth:truth:1.1.3")
    androidTestImplementation("com.google.truth:truth:1.1.3")

    // Efficiently testing coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    // Hamcrest: making assertions
    testImplementation("org.hamcrest:hamcrest-all:1.3")

    // Robolectric: unit testing framework
    testImplementation("org.robolectric:robolectric:4.9")

    // Mockito: mocking framework for unit tests
    testImplementation("org.mockito:mockito-core:4.8.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.8.1")
    androidTestImplementation("org.mockito:mockito-core:4.8.1")
    androidTestImplementation("com.linkedin.dexmaker:dexmaker-mockito:2.28.3")


}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

