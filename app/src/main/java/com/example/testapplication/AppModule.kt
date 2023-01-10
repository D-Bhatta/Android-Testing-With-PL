package com.example.testapplication

import android.content.Context
import androidx.room.Room
import com.example.testapplication.Constants.DATABASE_NAME
import com.example.testapplication.Constants.PIXABAY_BASE_URL
import com.example.testapplication.data.local.ShoppingDao
import com.example.testapplication.data.local.ShoppingListDatabase
import com.example.testapplication.data.remote.PixabayAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Dependency injection module for objects that will live for the entire lifetime of the
 * application.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the app database [ShoppingListDatabase].
     */
    @Singleton
    @Provides
    fun provideShoppingListDatabase(
        @ApplicationContext context: Context
    ): ShoppingListDatabase =
        Room.databaseBuilder(context, ShoppingListDatabase::class.java, DATABASE_NAME).build()

    /**
     * Provides the DAO [ShoppingDao] for the [ShoppingListDatabase]
     */
    @Singleton
    @Provides
    fun provideShoppingDao(
        database: ShoppingListDatabase
    ): ShoppingDao = database.shoppingDao()

    /**
     * Provides the [PixabayAPI] Retrofit instance.
     */
    fun providePixabayAPI(): PixabayAPI {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(
            PIXABAY_BASE_URL
        ).build().create(PixabayAPI::class.java)
    }

}