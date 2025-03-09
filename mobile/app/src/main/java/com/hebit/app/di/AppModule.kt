package com.hebit.app.di

import android.content.Context
import com.hebit.app.data.local.TokenManager
import com.hebit.app.data.remote.api.HebitApiService
import com.hebit.app.data.remote.api.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
    
    @Provides
    @Singleton
    fun provideHebitApiService(tokenManager: TokenManager): HebitApiService {
        val okHttpClient = NetworkModule.provideHttpClient(tokenManager)
        val retrofit = NetworkModule.provideRetrofit(okHttpClient)
        return NetworkModule.provideHebitApiService(retrofit)
    }
}
