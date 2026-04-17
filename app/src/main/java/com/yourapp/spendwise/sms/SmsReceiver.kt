package com.yourapp.spendwise.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.yourapp.spendwise.data.LocationCache
import com.yourapp.spendwise.data.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { message ->
            val sender = message.displayOriginatingAddress ?: message.originatingAddress.orEmpty()
            val body = message.messageBody.orEmpty().trim()
            val timestamp = message.timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()

            if (body.isBlank()) {
                return@forEach
            }

            CoroutineScope(Dispatchers.IO).launch {
                // ── Snapshot location FIRST at detection time ──────────────────
                // We cache coords immediately so there's zero timing gap between
                // the SMS arrival and the GPS read. The cache entry is consumed
                // when the transaction is confirmed, or evicted if discarded.
                val location = LocationHelper.getLocation(context.applicationContext)
                if (location != null) {
                    LocationCache.put(body, timestamp, location)
                }
                // ──────────────────────────────────────────────────────────────
                val outcome = SmsIntakeManager.ingest(
                    context = context.applicationContext,
                    sender = sender,
                    body = body,
                    timestamp = timestamp
                )
                // If a new item was queued for AI review, kick off background processing
                if (outcome is SmsIntakeOutcome.Pending) {
                    AiProcessingService.start(context)
                }
            }
        }
    }
}
