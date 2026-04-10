package com.smarttodo.app.utils

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.tasks.await

class LanguageDetector {
    private val languageIdentifier: LanguageIdentifier = LanguageIdentification.getClient()

    suspend fun detectLanguage(text: String): String {
        return try {
            languageIdentifier.identifyLanguage(text).await()
        } catch (e: Exception) {
            "und" // Undetermined
        }
    }

    fun isEnglish(langCode: String): Boolean {
        return langCode == "en" || langCode == "und" // Assume en if unsure
    }
}
