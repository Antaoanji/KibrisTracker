package com.example.pushuptracker.di

import android.content.Context
import androidx.room.Room
import com.example.pushuptracker.data.local.PushupDao
import com.example.pushuptracker.data.local.WaterDao
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "activity_tracker_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun providePushupDao(appDatabase: AppDatabase): PushupDao {
        return appDatabase.pushupDao()
    }

    @Provides
    @Singleton
    fun provideWaterDao(appDatabase: AppDatabase): WaterDao {
        return appDatabase.waterDao()
    }

    @Provides
    @Singleton
    fun providePushupRepo(pushupDao: PushupDao): PushupRepo {
        return PushupRepo(pushupDao)
    }

    @Provides
    @Singleton
    fun provideWaterRepo(waterDao: WaterDao): WaterRepo {
        return WaterRepo(waterDao)
    }
}
