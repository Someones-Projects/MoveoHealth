package com.example.moveohealth.ui



data class DataState<T>(
    var error: Event<StateError>? = null,
    var loading: Loading = Loading(false),
    var success: StateSuccess<T>? = null
) {

    companion object {

        fun <T> error(
            response: Response
        ): DataState<T> {
            return DataState(
                error = Event(
                    StateError(
                        response
                    )
                ),
                loading = Loading(false),
                success = null
            )
        }

        fun <T> loading(
            isLoading: Boolean,
            cachedData: T? = null
        ): DataState<T> {
            return DataState(
                error = null,
                loading = Loading(isLoading),
                success = StateSuccess(
                    Event.dataEvent(
                        cachedData
                    ), null
                )
            )
        }

        fun <T> success(
            data: T? = null,
            response: Response? = null
        ): DataState<T> {
            return DataState(
                error = null,
                loading = Loading(false),
                success = StateSuccess(
                    Event.dataEvent(data),
                    Event.responseEvent(response)
                )
            )
        }
    }
}