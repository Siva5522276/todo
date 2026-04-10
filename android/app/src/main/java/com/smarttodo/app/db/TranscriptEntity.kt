package com.smarttodo.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcripts")
data class TranscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawText: String,
    val parsedTodoId: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val languageDetected: String,
    val wasEnglish: Boolean,
    val confidence: Float
)
