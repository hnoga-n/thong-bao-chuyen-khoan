package com.banking.notification.banking_notification

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.*
import android.media.AudioManager
import android.os.Bundle

class TTSManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val TAG = "TTSManager"
    private val appContext = context.applicationContext // Sử dụng applicationContext để tránh memory leak
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { textToSpeech ->
                    // Thiết lập listener để theo dõi trạng thái
                    textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            Log.d(TAG, "TTS started speaking: $utteranceId")
                        }

                        override fun onDone(utteranceId: String?) {
                            Log.d(TAG, "TTS completed: $utteranceId")
                            // Abandon audio focus when done
                            audioManager.abandonAudioFocus(null)
                        }

                        override fun onError(utteranceId: String?) {
                            Log.e(TAG, "TTS error (deprecated): $utteranceId")
                            audioManager.abandonAudioFocus(null)
                        }
                        
                        override fun onError(utteranceId: String?, errorCode: Int) {
                            val errorMsg = when (errorCode) {
                                TextToSpeech.ERROR_SYNTHESIS -> "ERROR_SYNTHESIS"
                                TextToSpeech.ERROR_SERVICE -> "ERROR_SERVICE"
                                TextToSpeech.ERROR_OUTPUT -> "ERROR_OUTPUT"
                                TextToSpeech.ERROR_NETWORK -> "ERROR_NETWORK"
                                TextToSpeech.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
                                TextToSpeech.ERROR_INVALID_REQUEST -> "ERROR_INVALID_REQUEST"
                                TextToSpeech.ERROR_NOT_INSTALLED_YET -> "ERROR_NOT_INSTALLED_YET"
                                else -> "UNKNOWN_ERROR ($errorCode)"
                            }
                            Log.e(TAG, "TTS error: $utteranceId, errorCode: $errorMsg")
                            audioManager.abandonAudioFocus(null)
                        }
                    })

                    // Log available voices for debugging
                    val voices = textToSpeech.voices
                    Log.d(TAG, "Available voices count: ${voices?.size ?: 0}")
                    voices?.filter { it.locale.language == "vi" }?.forEach { voice ->
                        Log.d(TAG, "Vietnamese voice: ${voice.name}, locale: ${voice.locale}, quality: ${voice.quality}, features: ${voice.features}")
                    }
                    
                    // Log default engine
                    Log.d(TAG, "Default TTS engine: ${textToSpeech.defaultEngine}")
                    
                    // Thử thiết lập tiếng Việt
                    val locale = Locale("vi", "VN")
                    val result = textToSpeech.setLanguage(locale)
                    Log.d(TAG, "TTS language set result: $result (0=LANG_AVAILABLE, 1=LANG_COUNTRY_AVAILABLE, 2=LANG_COUNTRY_VAR_AVAILABLE, -1=MISSING_DATA, -2=NOT_SUPPORTED)")
                    
                    when (result) {
                        TextToSpeech.LANG_MISSING_DATA -> {
                            Log.e(TAG, "Vietnamese language data is missing")
                            isInitialized = false
                        }
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            Log.e(TAG, "Vietnamese language is not supported")
                            // Fallback sang English nếu không hỗ trợ tiếng Việt
                            val enResult = textToSpeech.setLanguage(Locale.US)
                            if (enResult == TextToSpeech.LANG_AVAILABLE || 
                                enResult == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                                Log.w(TAG, "Fallback to English")
                                isInitialized = true
                            } else {
                                isInitialized = false
                            }
                        }
                        else -> {
                            Log.d(TAG, "TTS initialized successfully for Vietnamese")
                            // Thiết lập các tham số TTS
                            textToSpeech.setPitch(1.0f) // Cao độ giọng nói
                            textToSpeech.setSpeechRate(1.0f) // Tốc độ nói
                            isInitialized = true
                        }
                    }
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
                isInitialized = false
            }
        }
    }

    fun speak(text: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized, cannot speak")
            return false
        }
        
        // Check and log current TTS state
        Log.d(TAG, "TTS isSpeaking: ${tts?.isSpeaking}")
        
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        Log.d(TAG, "Current volume: $currentVolume / $maxVolume")

        return try {
            // Request audio focus before speaking
            @Suppress("DEPRECATION")
            val focusResult = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            Log.d(TAG, "Audio focus request result: $focusResult (1=GRANTED, 0=FAILED)")
            
            val utteranceId = "TTS_ID_${System.currentTimeMillis()}"
            
            // Set audio stream to STREAM_MUSIC to ensure proper audio output
            val params = Bundle()
            params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            Log.d(TAG, "TTS speak called with result: $result (0=SUCCESS, -1=ERROR)")
            when (result) {
                TextToSpeech.SUCCESS -> {
                    Log.d(TAG, "Successfully queued text for speech: $text")
                    true
                }
                TextToSpeech.ERROR -> {
                    Log.e(TAG, "Error queuing text for speech")
                    false
                }
                else -> {
                    Log.e(TAG, "Unknown TTS result: $result")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while speaking: ${e.message}")
            false
        }
    }
    
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            Log.d(TAG, "TTS shutdown successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during TTS shutdown: ${e.message}")
        }
    }
    
    fun isReady(): Boolean = isInitialized
}
