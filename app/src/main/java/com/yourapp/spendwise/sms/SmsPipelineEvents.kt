package com.yourapp.spendwise.sms

import com.yourapp.spendwise.data.db.PendingSmsEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SmsPipelineEvent {
    data object PendingQueued : SmsPipelineEvent()

    // Emitted by AiProcessingService when it starts draining the queue
    data class ProcessingStarted(val total: Int) : SmsPipelineEvent()

    // Emitted for every message the service processes
    data class ProcessingProgress(
        val processed: Int,
        val total: Int,
        val current: PendingSmsEntity?
    ) : SmsPipelineEvent()

    // Emitted when the service has finished draining the entire queue
    data object ProcessingComplete : SmsPipelineEvent()
}

object SmsPipelineEvents {
    private val _events = MutableSharedFlow<SmsPipelineEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val events = _events.asSharedFlow()

    fun notifyPendingQueued() {
        _events.tryEmit(SmsPipelineEvent.PendingQueued)
    }

    fun notifyProcessingStarted(total: Int) {
        _events.tryEmit(SmsPipelineEvent.ProcessingStarted(total))
    }

    fun notifyProcessingProgress(processed: Int, total: Int, current: PendingSmsEntity?) {
        _events.tryEmit(SmsPipelineEvent.ProcessingProgress(processed, total, current))
    }

    fun notifyProcessingComplete() {
        _events.tryEmit(SmsPipelineEvent.ProcessingComplete)
    }
}
