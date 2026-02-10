package com.banking.notification.banking_notification

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "transaction_voice"
    private var ttsManager: TTSManager? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        ttsManager = TTSManager(this)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "speak" -> {
                    val text = call.argument<String>("text")
                    if (text != null) {
                        val success = ttsManager?.speak(text) ?: false
                        if (success) {
                            result.success(null)
                        } else {
                            result.error("TTS_ERROR", "TTS not initialized or failed to speak", null)
                        }
                    } else {
                        result.error("INVALID_ARGUMENT", "Text is null", null)
                    }
                }
                "isNotificationAccessGranted" -> {
                    val enabledListeners = Settings.Secure.getString(
                        contentResolver, 
                        "enabled_notification_listeners"
                    )
                    val pkgName = packageName
                    val isEnabled = enabledListeners != null && enabledListeners.contains(pkgName)
                    result.success(isEnabled)
                }
                "openNotificationSettings" -> {
                    try {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        result.success(null)
                    } catch (e: Exception) {
                        result.error("SETTINGS_ERROR", "Failed to open settings: ${e.message}", null)
                    }
                }
                "rebindNotificationService" -> {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            NotificationListenerService.requestRebind(
                                ComponentName(this, BankNotificationService::class.java)
                            )
                            result.success(true)
                        } else {
                            // For older Android versions, toggle the service
                            result.success(false)
                        }
                    } catch (e: Exception) {
                        result.error("REBIND_ERROR", "Failed to rebind service: ${e.message}", null)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
    
    override fun onDestroy() {
        ttsManager?.shutdown()
        ttsManager = null
        super.onDestroy()
    }
}
