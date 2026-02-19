package com.banking.notification.banking_notification

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Boot completed - checking notification listener permission")
            
            // Check if notification access is granted
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            val isEnabled = enabledListeners != null && 
                           enabledListeners.contains(context.packageName)
            
            if (isEnabled) {
                Log.d(TAG, "Notification access granted - requesting rebind")
                
                // Request rebind for the notification listener service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        NotificationListenerService.requestRebind(
                            ComponentName(context, BankNotificationService::class.java)
                        )
                        Log.d(TAG, "Rebind requested successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to request rebind", e)
                    }
                }
            } else {
                Log.d(TAG, "Notification access not granted - skipping rebind")
            }
        }
    }
}
