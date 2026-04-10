package com.smarttodo.app.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.smarttodo.app.MainActivity
import com.smarttodo.app.ai.TodoParser
import com.smarttodo.app.db.AppDatabase
import com.smarttodo.app.db.TodoEntity
import com.smarttodo.app.db.TranscriptEntity
import com.smarttodo.app.reminder.ReminderScheduler
import com.smarttodo.app.utils.PrefsManager
import com.smarttodo.app.utils.LanguageDetector
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import org.vosk.android.RecognitionListener
import java.io.IOException

class VoiceListenerService : Service(), RecognitionListener {

    private var speechService: SpeechService? = null
    private var model: Model? = null
    private val CHANNEL_ID = "SmartTodoServiceChannel"
    private val okHttpClient = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        initVosk()
    }

    private fun initVosk() {
        StorageService.unpack(this, "model", "model",
            { model: Model ->
                this.model = model
                startListening()
            },
            { exception: IOException ->
                Log.e("VoskService", "Failed to unpack model: ${exception.message}")
            })
    }

    private fun startListening() {
        try {
            val rec = Recognizer(model, 16000.0f, "[\"hey todo\", \"[any]\"]")
            speechService = SpeechService(rec, 16000.0f)
            speechService?.startListening(this)
            Log.d("VoskService", "Vosk is listening...")
        } catch (e: Exception) {
            Log.e("VoskService", "Failed to start listening: ${e.message}")
        }
    }

    override fun onResult(hypothesis: String) {
        if (hypothesis.contains("hey todo")) {
            val transcript = hypothesis.substringAfter("\"text\" : \"").substringBefore("\"")
            handleParsedTranscript(transcript.replace("hey todo", "").trim())
        }
    }

    override fun onPartialResult(hypothesis: String) {}
    override fun onFinalResult(hypothesis: String) {}
    override fun onError(exception: Exception) {}
    override fun onTimeout() {
        speechService?.stop()
        startListening()
    }

    private fun handleParsedTranscript(transcript: String) {
        if (transcript.isBlank()) return
        
        val prefs = PrefsManager(this)
        val parser = TodoParser(prefs)
        val decoder = LanguageDetector()
        val db = AppDatabase.getDatabase(this)
        val scheduler = ReminderScheduler(this)

        GlobalScope.launch {
            val langCode = decoder.detectLanguage(transcript)
            val isEnglish = decoder.isEnglish(langCode)

            if (!isEnglish) {
                triggerSpeakEnglishCall(prefs)
                db.transcriptDao().insertTranscript(TranscriptEntity(
                    rawText = transcript,
                    languageDetected = langCode,
                    wasEnglish = false,
                    confidence = 0f
                ))
                return@launch
            }

            val result = parser.parseTranscript(transcript)
            if (result != null) {
                val todo = TodoEntity(
                    title = result.title,
                    date = result.date,
                    time = result.time,
                    priority = result.priority,
                    status = "pending",
                    rawTranscript = transcript
                )
                val id = db.todoDao().insertTodo(todo)
                scheduler.scheduleReminder(todo.copy(id = id.toInt()))
                
                db.transcriptDao().insertTranscript(TranscriptEntity(
                    rawText = transcript,
                    parsedTodoId = id.toInt(),
                    languageDetected = "English",
                    wasEnglish = true,
                    confidence = result.confidence
                ))
            }
        }
    }

    private fun triggerSpeakEnglishCall(prefs: PrefsManager) {
        val twilioSid = prefs.getString(PrefsManager.KEY_TWILIO_SID)
        val twilioToken = prefs.getString(PrefsManager.KEY_TWILIO_TOKEN)
        val twilioFrom = prefs.getString(PrefsManager.KEY_TWILIO_FROM)
        val userPhone = prefs.getString(PrefsManager.KEY_WHATSAPP_NUMBER)
        val userName = prefs.getString(PrefsManager.KEY_USER_NAME)
        
        val backendUrl = "https://your-smarttodo-backend.railway.app"

        val json = """
            {
                "to": "$userPhone",
                "userName": "$userName",
                "twilioSid": "$twilioSid",
                "twilioToken": "$twilioToken",
                "twilioFrom": "$twilioFrom",
                "reason": "non_english"
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/trigger-scold")
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("VoskService", "Failed to trigger scold call: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {}
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "SmartTodo Listening Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartTodo is listening...")
            .setContentText("Say 'Hey Todo' to add a task")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        super.onDestroy()
        speechService?.stop()
        speechService?.shutdown()
    }
}
