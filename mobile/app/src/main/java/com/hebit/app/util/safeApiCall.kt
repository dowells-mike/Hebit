package com.hebit.app.util

import com.hebit.app.domain.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * A utility function to safely make API calls and handle errors
 * 
 * @param apiCall The suspend function API call to execute
 * @return Resource object wrapping the API response or error
 */
suspend inline fun <T> safeApiCall(crossinline apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        withContext(Dispatchers.IO) {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorCode = response.code()
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                when (errorCode) {
                    401 -> Resource.Error("Authentication required")
                    403 -> Resource.Error("Not authorized to access this resource")
                    404 -> Resource.Error("Resource not found")
                    429 -> Resource.Error("Rate limit exceeded. Please try again later")
                    500, 501, 502, 503 -> Resource.Error("Server error. Please try again later")
                    else -> Resource.Error(errorMessage)
                }
            }
        }
    } catch (e: HttpException) {
        Resource.Error("Network error: ${e.message()}")
    } catch (e: IOException) {
        Resource.Error("Network error: Check your internet connection")
    } catch (e: Exception) {
        Resource.Error("Unexpected error: ${e.localizedMessage ?: "Unknown error occurred"}")
    }
} 