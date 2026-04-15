package com.yourapp.spendwise.mail

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.yourapp.spendwise.data.SettingsStore
import java.util.Locale

class SparkNotificationTriggerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        if (notification.packageName != SPARK_PACKAGE_NAME) return

        val settings = SettingsStore(applicationContext)
        if (!settings.isSparkMailTriggerEnabled()) return
        if (settings.getAxisEmailAccount().isBlank()) return
        if (!NotificationAccessHelper.hasNotificationAccess(applicationContext)) return
        if (!looksAxisRelated(notification.notification)) return

        val now = System.currentTimeMillis()
        val lastTrigger = settings.getSparkMailTriggerLastSyncMs()
        if (now - lastTrigger < TRIGGER_DEBOUNCE_MS) return

        settings.setSparkMailTriggerLastSyncMs(now)
        GmailAxisSyncManager.enqueueImmediateSync(
            context = applicationContext,
            trigger = AxisEmailSyncTrigger.SPARK_NOTIFICATION
        )
    }

    private fun looksAxisRelated(notification: Notification): Boolean {
        val extras = notification.extras ?: return false
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val haystack = listOf(title, text, subText, summaryText, bigText)
            .joinToString(" ")
            .lowercase(Locale.ENGLISH)

        return "axis" in haystack || "alerts@axis.bank.in" in haystack || "axis bank" in haystack
    }

    companion object {
        private const val SPARK_PACKAGE_NAME = "com.readdle.spark"
        private const val TRIGGER_DEBOUNCE_MS = 90_000L
    }
}
