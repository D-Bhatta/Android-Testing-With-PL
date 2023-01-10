package com.example.testapplication

import com.example.testapplication.Constants.DATABASE_NAME
import com.example.testapplication.Constants.IMAGE_TYPE
import com.example.testapplication.Constants.PIXABAY_BASE_URL


/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[IMAGE_TYPE] Type of image expected from the Pixabay API. It is used as a query
 * parameter. Accepted values: "all", "photo", "illustration", "vector". Default: "all".
 * @property[DATABASE_NAME] Name of the Room database for the application.
 * @property[PIXABAY_BASE_URL] Base Pixabay API URL.
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val IMAGE_TYPE: String = "photo"
    const val DATABASE_NAME: String = "shopping_db"
    const val PIXABAY_BASE_URL: String = "https://pixabay.com"
}