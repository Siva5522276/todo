package com.smarttodo.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smarttodo.app.utils.PrefsManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ReminderReceiver : BroadcastReceiver() {
    private val client = OkHttpClient()

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra("todo_id", -1)
        val todoTitle = intent.getStringExtra("todo_title") ?: "Something important"
        
        val prefs = PrefsManager(context)
        val userName = prefs.getString(PrefsManager.KEY_USER_NAME)
        val whatsappTo = prefs.getString(PrefsManager.KEY_WHATSAPP_NUMBER)
        val twilioSid = prefs.getString(PrefsManager.KEY_TWILIO_SID)
        val twilioToken = prefs.getString(PrefsManager.KEY_TWILIO_TOKEN)
        val twilioFrom = prefs.getString(PrefsManager.KEY_TWILIO_FROM)
        
        // This is the URL of your Railway backend
        val backendUrl = "https://your-smarttodo-backend.railway.app"

        val json = """
            {
                "to": "$whatsappTo",
                "title": "$todoTitle",
                "userName": "$userName",
                "twilioSid": "$twilioSid",
                "twilioToken": "$twilioToken",
                "twilioFrom": "$twilioFrom"
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$backendUrl/trigger-reminder")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ReminderReceiver", "Failed to trigger reminder: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("ReminderReceiver", "Reminder triggered successfully")
                } else {
                    Log.e("ReminderReceiver", "Reminder trigger error: ${response.code}")
                }
            }
        })
    }
}
