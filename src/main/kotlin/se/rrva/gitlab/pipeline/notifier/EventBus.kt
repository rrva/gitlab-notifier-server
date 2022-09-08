package se.rrva.gitlab.pipeline.notifier

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val internalEvents = MutableSharedFlow<Event>(0, 10, BufferOverflow.DROP_OLDEST)
    val events = internalEvents.asSharedFlow()

    fun produceEvent(event: Event): Boolean {
        return internalEvents.tryEmit(event)
    }
}

data class Event(val payload: String)