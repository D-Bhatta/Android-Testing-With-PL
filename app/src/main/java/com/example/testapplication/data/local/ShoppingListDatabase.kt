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
