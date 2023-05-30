// Event.kt
package com.example.testapplication

import java.util.UUID

/**
 * Holds information about events.
 *
 * @param[content] Information stored in the event.
 *
 * @property[interceptionStatus] Status of event interception.
 * @property[eventStatus] Status of the event lifecycle.
 * @property[eventID] Unique identifier of the event.
 */
open class Event<out T>(private val content: T) {
    var interceptionStatus: InterceptionStatus = InterceptionStatus.UNINTERCEPTED
        private set

    var eventStatus: EventStatus = EventStatus.ONGOING

    val eventID: String = createId()

    private fun createId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Return the content if the [Event] has not been intercepted.
     */
    fun getContentIfUnintercepted(): T? {
        return if (interceptionStatus == InterceptionStatus.INTERCEPTED) {
            null
        } else {
            interceptionStatus = InterceptionStatus.INTERCEPTED
            content
        }
    }

    /**
     * Extract the content without affecting the [interceptionStatus]
     */
    fun peekContent(): T = content
}

/**
 * Status of the event.
 */
enum class EventStatus {
    /**
     * Event has been cancelled.
     */
    CANCELLED,

    /**
     * Event resource has been destroyed.
     */
    DESTROYED,

    /**
     * Event is currently in progress.
     */
    ONGOING,

    /**
     * Event resource has been created.
     */
    CREATED
}

@Suppress("KDocMissingDocumentation")
enum class InterceptionStatus {
    INTERCEPTED, UNINTERCEPTED
}
