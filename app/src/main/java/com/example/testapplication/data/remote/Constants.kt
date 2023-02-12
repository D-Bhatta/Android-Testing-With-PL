package com.example.testapplication.data.remote

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[IMAGE_TYPE] Type of image expected from the Pixabay API. It is used as a query
 * parameter. Accepted values: "all", "photo", "illustration", "vector". Default: "all".
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val IMAGE_TYPE: String = "photo"
}