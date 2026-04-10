package com.smarttodo.app.ai

import com.google.gson.Gson
import com.smarttodo.app.utils.PrefsManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TodoParser(private val prefs: PrefsManager) {
    private val gson = Gson()
    
    private val api: OpenRouterApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OpenRouterApi::class.java)
    }

    private val systemPrompt = """
        You are a smart todo parser. The user speaks casually and may use abbreviations, slang, or broken English.
        Your job is to extract a structured todo from their speech.

        Rules:
        - "evng" or "evning" = evening = 18:00
        - "mrng" or "morn" = morning = 08:00
        - "aftn" or "aftrn" = afternoon = 14:00
        - "nght" or "nite" = night = 21:00
        - "tmrw" or "tomoro" = tomorrow
        - If no time is given, default to the next logical time slot
        - If no date is given, assume today or tomorrow based on context

        Return ONLY a JSON object:
        {
          "title": "Clean, readable todo title",
          "date": "YYYY-MM-DD",
          "time": "HH:MM",
          "priority": "high | medium | low",
          "raw_transcript": "original words the user said",
          "confidence": 0.0 to 1.0
        }
    """.trimIndent()

    suspend fun parseTranscript(transcript: String): ParsedTodoResult? {
        val apiKey = prefs.getString(PrefsManager.KEY_OPENROUTER_KEY)
        if (apiKey.isBlank()) return null

        val request = OpenRouterRequest(
            messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = transcript)
            )
        )

        return try {
            val response = api.parseTodo("Bearer $apiKey", request)
            if (response.isSuccessful) {
                val jsonContent = response.body()?.choices?.getOrNull(0)?.message?.content
                jsonContent?.let {
                    // Extract JSON if it's wrapped in markdown backticks
                    val cleanJson = it.substringAfter("```json").substringBefore("```").trim()
                        .ifBlank { it.substringAfter("{").substringBeforeLast("}").let { content -> "{$content}" } }
                    gson.fromJson(cleanJson, ParsedTodoResult::class.java)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
