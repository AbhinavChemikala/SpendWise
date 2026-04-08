package com.yourapp.spendwise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yourapp.spendwise.sms.GeminiNanoAnalyzer
import com.yourapp.spendwise.sms.SpendWiseNotificationManager
import com.yourapp.spendwise.ui.DashboardScreen
import com.yourapp.spendwise.ui.MainViewModel
import com.yourapp.spendwise.ui.SpendWiseTheme
import com.yourapp.spendwise.background.DailyReminderScheduler
import android.content.Intent

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestSmsPermissions()
        SpendWiseNotificationManager.ensureChannel(this)
        DailyReminderScheduler.scheduleNext(this)

        GeminiNanoAnalyzer.ensureModelReady(
            onReady = {
                Log.d(TAG, "Gemini Nano is ready for SpendWise.")
            },
            onError = { throwable ->
                Log.w(TAG, "Gemini Nano warmup failed.", throwable)
            }
        )

        handleIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode) {
                "dark"  -> true
                "light" -> false
                else    -> systemDark   // "system"
            }
            SpendWiseTheme(isDark = isDark) {
                DashboardScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_ADD_TRANSACTION) {
            viewModel.openManualAddDialog()
        }
    }

    private fun requestSmsPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val SMS_PERMISSION_REQUEST_CODE = 101
        const val ACTION_ADD_TRANSACTION = "com.yourapp.spendwise.ACTION_ADD_TRANSACTION"
    }
}
