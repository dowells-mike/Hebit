package com.hebit.app.data.remote.api

import com.hebit.app.data.local.TokenManager
import com.hebit.app.data.remote.dto.RefreshTokenRequest
import com.hebit.app.data.remote.dto.RefreshTokenResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class BooleanAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Boolean {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Unit>()
            return false
        }
        
        return when (reader.peek()) {
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.STRING -> reader.nextString().lowercase() == "true"
            JsonReader.Token.NUMBER -> reader.nextInt() != 0
            else -> {
                reader.skipValue()
                false
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: Boolean?) {
        writer.value(value)
    }
}

/**
 * Network module for setting up Retrofit and OkHttp
 */
object NetworkModule {
    
    private const val BASE_URL = "http://192.168.0.137:5000/api/" // Local network IP address
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(BooleanAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    fun provideHttpClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()
            
            if (token.isNullOrEmpty()) {
                println("DEBUG: No auth token available for request: ${originalRequest.url}")
                return@Interceptor chain.proceed(originalRequest)
            }
            
            println("DEBUG: Adding auth token to request: ${originalRequest.url}")
            
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            
            val authHeader = newRequest.header("Authorization")
            if (authHeader.isNullOrEmpty()) {
                println("DEBUG: Failed to add Authorization header to request")
            } else {
                println("DEBUG: Authorization header added successfully: ${authHeader.take(15)}...")
            }
            
            chain.proceed(newRequest)
        }
        
        val tokenAuthenticator = object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (response.request.url.encodedPath.endsWith("/auth/login") ||
                    response.request.url.encodedPath.endsWith("/auth/register") ||
                    response.request.url.encodedPath.endsWith("/auth/refresh-token")) {
                    return null
                }
                
                val refreshToken = tokenManager.getRefreshToken() ?: return null
                val userId = tokenManager.getUserId() ?: return null
                
                val tokenClient = OkHttpClient()
                
                val tempMoshi = provideMoshi()
                val tempRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(tokenClient)
                    .addConverterFactory(MoshiConverterFactory.create(tempMoshi))
                    .build()
                
                val tempApi = tempRetrofit.create(HebitApiService::class.java)
                
                return runBlocking {
                    try {
                        val refreshRequest = RefreshTokenRequest(refreshToken, userId)
                        val refreshResponse = tempApi.refreshToken(refreshRequest)
                        
                        if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                            val newToken = refreshResponse.body()!!.token
                            
                            tokenManager.saveToken(newToken)
                            
                            return@runBlocking response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                        } else {
                            tokenManager.clearAuthData()
                            return@runBlocking null
                        }
                    } catch (e: Exception) {
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
    
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    fun provideHebitApiService(retrofit: Retrofit): HebitApiService {
        return retrofit.create(HebitApiService::class.java)
    }
}
