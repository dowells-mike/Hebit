package com.hebit.app.data.remote.api

import com.hebit.app.data.local.TokenManager
import com.hebit.app.data.remote.dto.RefreshTokenRequest
import com.hebit.app.data.remote.dto.RefreshTokenResponse
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module for setting up Retrofit and OkHttp
 */
object NetworkModule {
    
    private const val BASE_URL = "http://192.168.0.137:5000/api/" // Local network IP address
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    fun provideHttpClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Add auth header to requests
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()
            
            if (token.isNullOrEmpty()) {
                return@Interceptor chain.proceed(originalRequest)
            }
            
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            
            chain.proceed(newRequest)
        }
        
        // Handle token refresh when we get 401 responses
        val tokenAuthenticator = object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                // Check if this is a 401 from a failed authentication request
                if (response.request.url.encodedPath.endsWith("/auth/login") ||
                    response.request.url.encodedPath.endsWith("/auth/register") ||
                    response.request.url.encodedPath.endsWith("/auth/refresh-token")) {
                    return null // Let the login/register flow handle these failures
                }
                
                // Get saved tokens and user ID
                val refreshToken = tokenManager.getRefreshToken() ?: return null
                val userId = tokenManager.getUserId() ?: return null
                
                // Create a new client for token refresh to avoid interceptors
                val tokenClient = OkHttpClient()
                
                // Create retrofit API to refresh token
                val tempRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(tokenClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                val tempApi = tempRetrofit.create(HebitApiService::class.java)
                
                // Attempt to get new token
                return runBlocking {
                    try {
                        val refreshRequest = RefreshTokenRequest(refreshToken, userId)
                        val refreshResponse = tempApi.refreshToken(refreshRequest)
                        
                        if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                            val newToken = refreshResponse.body()!!.token
                            
                            // Save the new token
                            tokenManager.saveToken(newToken)
                            
                            // Create new request with new token
                            return@runBlocking response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                        } else {
                            // If refresh fails, clear tokens and let user re-login
                            tokenManager.clearAuthData()
                            return@runBlocking null
                        }
                    } catch (e: Exception) {
                        // If refresh fails, clear tokens and let user re-login
                        tokenManager.clearAuthData()
                        return@runBlocking null
                    }
                }
            }
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }
    
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun provideHebitApiService(retrofit: Retrofit): HebitApiService {
        return retrofit.create(HebitApiService::class.java)
    }
}
