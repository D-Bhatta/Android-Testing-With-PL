package com.example.testapplication

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[DATABASE_NAME] Name of the Room database for the application.
 * @property[PIXABAY_BASE_URL] Base Pixabay API URL.
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val DATABASE_NAME: String = "shopping_db"
    const val PIXABAY_BASE_URL: String = "https://pixabay.com"
}