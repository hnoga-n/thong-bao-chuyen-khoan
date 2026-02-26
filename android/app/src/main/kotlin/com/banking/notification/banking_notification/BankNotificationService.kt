package com.banking.notification.banking_notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern
import kotlin.math.abs

class BankNotificationService : NotificationListenerService() {

    companion object {
        @Volatile
        var isServiceConnected: Boolean = false
            private set
    }

    private lateinit var ttsManager: TTSManager
    private val TAG = "BankNotification"
    private val CHANNEL_ID = "banking_notification_channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BankNotificationService onCreate - Service is starting")
        ttsManager = TTSManager(this)
        createNotificationChannel()
    }

    override fun onDestroy() {
        Log.d(TAG, "BankNotificationService onDestroy - Service is stopping")
        isServiceConnected = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        ttsManager.shutdown()
        super.onDestroy()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.d(TAG, "NotificationListenerService CONNECTED - Ready to receive notifications")
        startForegroundService()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
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
        if (packageName !in listOf("mobile.acb.com.vn", "com.VCB", "com.mservice.momotransfer","com.mbmobile","com.vnpay.Agribank3g")) {
            return
        }

        val extras = sbn.notification.extras
        Log.d(TAG, "Notification extras: $extras")
        
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        Log.d(TAG, "Package: $packageName, Title: $title, Text: $text")

        processTransaction(text)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Banking Notification Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the banking notification listener active"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun startForegroundService() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang lắng nghe thông báo")
                .setContentText("Nhấn để mở ứng dụng")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            
            Log.d(TAG, "Foreground service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
        }
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
