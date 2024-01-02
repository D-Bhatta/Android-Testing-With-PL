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
