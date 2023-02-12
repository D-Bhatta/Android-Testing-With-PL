// Resource.kt

package com.example.testapplication

/**
 * Resource class holds a resource value, it's operational status, and an optional message.
 *
 * @param[status] Can be one of the [Status] values.
 * @param[data] Nullable resource value. Even if the [status] is [Status.SUCCESS], the value might
 * still be null. So recommend checking it before using.
 * @param[message] [Message] holds a message value.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: Message?) {
    companion object {
        /**
         * Call this method to return a [Resource] object on successful operation.
         *
         * @param[data] Nullable resource value. Even if the [status] is [Status.SUCCESS], the value
         * might still be null. So recommend checking it before using.
         */
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        /**
         * Call this method to return a [Resource] object after encountering an error during an
         * operation. This error might represent an error in the business domain, as well as errors
         * encountered while performing any operation on the data.
         *
         * @param[message] [Message] holds a message value.
         * @param[data] Nullable resource value. The value might still be null. So recommend
         * checking it before using.
         */
        fun <T> error(message: Message, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        /**
         * Call this method to return a [Resource] value that signals it is involved in an ongoing
         * operation.
         *
         * @param[data] Nullable resource value. The value might still be null. So recommend
         * checking it before using.
         */
        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}


@Suppress("KDocMissingDocumentation")
data class Message(val message: String)

@Suppress("KDocMissingDocumentation")
enum class Status {
    SUCCESS, ERROR, LOADING
}
