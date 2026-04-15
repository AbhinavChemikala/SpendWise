package com.yourapp.spendwise.mail

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationAccessHelper {
    fun hasNotificationAccess(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        val component = ComponentName(context, SparkNotificationTriggerService::class.java).flattenToString()
        return enabled.split(':').any { it.equals(component, ignoreCase = true) }
    }
}
