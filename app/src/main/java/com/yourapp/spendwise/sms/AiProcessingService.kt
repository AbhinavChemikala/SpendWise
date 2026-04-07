package com.yourapp.spendwise.sms

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.yourapp.spendwise.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Foreground service that drains the pending SMS queue through Gemini Nano,
 * even when the SpendWise UI is closed.
 *
 * Lifecycle:
 *  - Started by SmsReceiver whenever a new SMS is queued for AI review.
 *  - Started by MainViewModel when the app opens and pending items exist.
 *  - Stops itself automatically when the queue is fully drained.
 *  - Uses START_NOT_STICKY so Android does NOT restart it if killed (we
 *    trigger it explicitly on the next SMS or next app open instead).
 */
class AiProcessingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationManager get() = NotificationManagerCompat.from(this)

    companion object {
        private const val TAG = "AiProcessingService"

        /**
         * Process-level guard. When true, a drain coroutine is already running
         * inside this service instance. Any subsequent onStartCommand call will
         * be ignored so the existing drain is never interrupted or cancelled.
         */
        private val isDraining = AtomicBoolean(false)

        /** Start (or re-trigger) the service from any context. */
        fun start(context: Context) {
            val intent = Intent(context.applicationContext, AiProcessingService::class.java)
            context.applicationContext.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Ensure both notification channels exist before calling startForeground
        SpendWiseNotificationManager.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Must call startForeground immediately (within 5 seconds of service start)
        startForeground(
            SpendWiseNotificationManager.AI_PROCESSING_NOTIFICATION_ID,
            SpendWiseNotificationManager.buildAiProcessingNotification(
                context = this,
                processed = 0,
                total = 0
            )
        )

        val settingsStore = SettingsStore(this)
        if (!settingsStore.isAiReviewEnabled()) {
            Log.d(TAG, "AI review disabled — stopping service immediately.")
            stopSelf()
            return START_NOT_STICKY
        }

        // Guard: if a drain is already running, do NOT launch another one.
        // The running drain will handle all pending items, and its finally block
        // will call stopSelf() when done. Launching a second drain would race
        // and its finally block would call stopSelf() prematurely, cancelling
        // the still-running original drain via serviceScope.cancel().
        if (!isDraining.compareAndSet(false, true)) {
            Log.d(TAG, "Drain already in progress — ignoring duplicate start command.")
            return START_NOT_STICKY
        }

        serviceScope.launch {
            drainQueue()
        }

        return START_NOT_STICKY
    }

    private suspend fun drainQueue() {
        try {
            val processor = SmsProcessor(applicationContext)
            // Notify ViewModel that drain is starting so UI can show progress
            val initialCount = com.yourapp.spendwise.data.db.AppDatabase
                .getInstance(applicationContext)
                .pendingSmsDao()
                .getPendingCount()
            SmsPipelineEvents.notifyProcessingStarted(initialCount)

            processor.drainPendingQueue { processed, total, current ->
                // 1. Update the foreground notification
                val notification = SpendWiseNotificationManager.buildAiProcessingNotification(
                    context = this@AiProcessingService,
                    processed = processed,
                    total = total
                )
                notificationManager.notify(
                    SpendWiseNotificationManager.AI_PROCESSING_NOTIFICATION_ID,
                    notification
                )
                // 2. Broadcast progress so the ViewModel updates the UI
                //    even when the app is open and watching
                SmsPipelineEvents.notifyProcessingProgress(
                    processed = processed,
                    total = total,
                    current = current
                )
            }
            Log.d(TAG, "Queue fully drained — stopping service.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during queue drain", e)
        } finally {
            isDraining.set(false)  // Allow future starts to launch a new drain
            SmsPipelineEvents.notifyProcessingComplete()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
