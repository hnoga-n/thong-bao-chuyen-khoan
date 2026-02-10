package com.banking.notification.banking_notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.regex.Pattern
import kotlin.math.abs

class BankNotificationService : NotificationListenerService() {

    private lateinit var ttsManager: TTSManager
    private val TAG = "BankNotification"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BankNotificationService onCreate - Service is starting")
        ttsManager = TTSManager(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "BankNotificationService onDestroy - Service is stopping")
        ttsManager.shutdown()
        super.onDestroy()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService CONNECTED - Ready to receive notifications")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.e(TAG, "NotificationListenerService DISCONNECTED - Will not receive notifications")
        
        // Request rebind to reconnect the service automatically
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            requestRebind(android.content.ComponentName(this, BankNotificationService::class.java))
            Log.d(TAG, "Requested rebind for NotificationListenerService")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "onNotificationPosted called")
        if (sbn == null) {
            Log.d(TAG, "sbn is null, returning")
            return
        }

        val packageName = sbn.packageName
        Log.d(TAG, "Notification from package: $packageName")
        // Target packages
        if (packageName !in listOf("com.acb.acbapp", "com.vietcombank.vcb", "com.mservice.momotransfer")) {
            return
        }

        val extras = sbn.notification.extras
        Log.d(TAG, "Notification extras: $extras")
        
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        Log.d(TAG, "Package: $packageName, Title: $title, Text: $text")

        processTransaction(text)
    }

    private fun processTransaction(text: String) {
        // Regex for Amount: capture group 1
        // Supports: 1,500,000 VND, +500,000, -200,000
        val pattern = Pattern.compile("([+-]?\\d{1,3}(?:,\\d{3})*)\\s?(VND|đ|d|đồng)?", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            val amountRaw = matcher.group(1) ?: return
            // Remove commas
            val amountStr = amountRaw.replace(",", "")
            
            try {
                val amount = amountStr.toLong()
                
                val prefs = getSharedPreferences("FlutterSharedPreferences", MODE_PRIVATE)
                val isVoiceEnabled = prefs.getBoolean("flutter.voice_enabled", true)
                val minAmount = prefs.getLong("flutter.min_amount", 0L)

                if (!isVoiceEnabled) return
                if (abs(amount) < minAmount) return

                val speakText = if (amount >= 0) {
                     "Bạn vừa nhận ${amount} ngàn đồng"
                } else {
                     "Tài khoản vừa chi ${abs(amount)} ngàn đồng"
                }

                ttsManager.speak(speakText)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing transaction", e)
            }
        }
    }
}
