package com.smarttodo.app.ai

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun parseTodo(
        @Header("Authorization") token: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
}

data class OpenRouterRequest(
    val model: String = "z-ai/glm-4.5-air:free",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class ParsedTodoResult(
    @SerializedName("title") val title: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("raw_transcript") val rawTranscript: String,
    @SerializedName("confidence") val confidence: Float
)
