// Constants.kt
package com.example.testapplication

/**
 * Object instance that encapsulates constant values
 * throughout the application.
 *
 * @property[DATABASE_NAME] Name of the Room database for the application.
 * @property[PIXABAY_BASE_URL] Base Pixabay API URL.
 * @property[SHOPPING_ITEM_NAME_LENGTH] Max length of name field in `ShoppingItem`.
 * @property[SHOPPING_ITEM_PRICE_LENGTH] Max length of price field in `ShoppingItem`.
 */
@Suppress("HardCodedStringLiteral", "MemberVisibilityCanBePrivate")
object Constants {
    const val DATABASE_NAME: String = "shopping_db"
    const val PIXABAY_BASE_URL: String = "https://pixabay.com"
    const val SHOPPING_ITEM_NAME_LENGTH: Int = 25
    const val SHOPPING_ITEM_PRICE_LENGTH: Int = 10
}
