package com.example.moveohealth.util

import com.example.moveohealth.constants.Constants.Companion.APP_DEBUG
import retrofit2.Response
import timber.log.Timber


/**
 * Copied from Architecture components google sample:
 * https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/main/java/com/android/example/github/api/ApiResponse.kt
 */
@Suppress("unused") // T is used in extending classes
sealed class GenericApiResponse<T> {

    companion object {

        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(
                error.message ?: "unknown error"
            )
        }

        fun <T> create(response: Response<T>): GenericApiResponse<T> {
            Timber.tag(APP_DEBUG).d("GenericApiResponse: response: $response")
            Timber.tag(APP_DEBUG).d("GenericApiResponse: raw: ${response.raw()}")
            Timber.tag(APP_DEBUG).d("GenericApiResponse: headers: ${response.headers()}")
            Timber.tag(APP_DEBUG).d("GenericApiResponse:  message: ${response.message()}")

            if(response.isSuccessful) {
                val body = response.body()
                return if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else if (response.code() == 401) { // Asi: seems unreachable.. isSuccessful represent 200-299.
                    ApiErrorResponse("401 Unauthorized. Token may be invalid.")
                } else {
                    ApiSuccessResponse(body = body)
                }
            } else {
                val msg = response.errorBody()?.string()
                val errorMsg =
                    if (msg.isNullOrBlank()) {
                        response.message()
                    } else {
                        msg
                    }
                return ApiErrorResponse(errorMsg ?: "unknown error")
            }
        }
    }
}

/**
 * separate class for HTTP 204 responses so that we can make ApiSuccessResponse's body non-null.
 */
class ApiEmptyResponse<T> : GenericApiResponse<T>()

data class ApiSuccessResponse<T>(val body: T) : GenericApiResponse<T>() {}

data class ApiErrorResponse<T>(val errorMessage: String) : GenericApiResponse<T>()