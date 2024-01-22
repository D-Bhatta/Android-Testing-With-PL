package com.example.testapplication

import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.testapplication.Constants.DATABASE_NAME
import com.example.testapplication.Constants.PIXABAY_BASE_URL
import com.example.testapplication.data.local.DefaultLocalDataSource
import com.example.testapplication.data.local.LocalDataSource
import com.example.testapplication.data.local.ShoppingDao
import com.example.testapplication.data.local.ShoppingListDatabase
import com.example.testapplication.data.remote.DefaultRemoteDataSource
import com.example.testapplication.data.remote.PixabayAPI
import com.example.testapplication.data.remote.RemoteDataSource
import com.example.testapplication.repository.DefaultShoppingRepository
import com.example.testapplication.repository.ShoppingRepository
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
    @Singleton
    @Provides
    fun providePixabayAPI(): PixabayAPI {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(
            PIXABAY_BASE_URL
        ).build().create(PixabayAPI::class.java)
    }

    /**
     * Provides the [RemoteDataSource] implementation instance.
     */
    @Singleton
    @Provides
    fun provideRemoteDataSource(
        pixabayAPI: PixabayAPI
    ): RemoteDataSource {
        return DefaultRemoteDataSource(pixabayAPI)
    }

    /**
     * Provides the [LocalDataSource] implementation instance.
     */
    @Singleton
    @Provides
    fun provideLocalDataSource(
        shoppingDao: ShoppingDao
    ): LocalDataSource {
        return DefaultLocalDataSource(shoppingDao)
    }

    /**
     * Provides an [ShoppingRepository] implementation in [DefaultShoppingRepository].
     */
    @Singleton
    @Provides
    fun provideDefaultShoppingRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource
    ): ShoppingRepository {
        return DefaultShoppingRepository(remoteDataSource, localDataSource)
    }

    /**
     * Provides a [Glide] instance [RequestManager] configured to load images.
     */
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ): RequestManager = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
    )
}
