package com.example.pushuptracker.di

import com.example.pushuptracker.ai.GenerativeAiService
import com.example.pushuptracker.ai.GeminiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideGenerativeAiService(geminiApi: GeminiApiService): GenerativeAiService {
        return GenerativeAiService(geminiApi)
    }
}
