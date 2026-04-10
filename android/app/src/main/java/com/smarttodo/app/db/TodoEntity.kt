package com.smarttodo.app.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val priority: String, // high, medium, low
    val status: String, // pending, done, snoozed, missed
    val rawTranscript: String,
    val createdAt: Long = System.currentTimeMillis(),
    val whatsappSent: Boolean = false,
    val callMade: Boolean = false,
    val reminderAcknowledged: Boolean = false
)
