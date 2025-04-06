package com.hebit.app.domain.model

/**
 * A generic class that holds a value with its loading status
 * Used for UI state management and for handling API responses
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String = "Unknown error occurred", data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
