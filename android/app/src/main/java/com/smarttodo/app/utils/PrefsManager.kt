package com.smarttodo.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PrefsManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "smart_todo_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val KEY_USER_NAME = "user_name"
        const val KEY_WHATSAPP_NUMBER = "whatsapp_number"
        const val KEY_TWILIO_SID = "twilio_sid"
        const val KEY_TWILIO_TOKEN = "twilio_token"
        const val KEY_TWILIO_FROM = "twilio_from"
        const val KEY_OPENROUTER_KEY = "openrouter_key"
        const val KEY_PICOVOICE_KEY = "picovoice_key"
        const val KEY_IS_ONBOARDED = "is_onboarded"
    }
}
