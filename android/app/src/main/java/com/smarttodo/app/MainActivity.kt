package com.smarttodo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smarttodo.app.ui.theme.SmartTodoTheme
import com.smarttodo.app.utils.PrefsManager
import com.smarttodo.app.db.TodoEntity
import com.smarttodo.app.db.TranscriptEntity
import com.smarttodo.app.ui.OnboardingScreen
import com.smarttodo.app.ui.HomeScreen
import com.smarttodo.app.ui.AllTodosScreen
import com.smarttodo.app.ui.HistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = PrefsManager(this)
        val isOnboarded = prefs.getBoolean(PrefsManager.KEY_IS_ONBOARDED, false)
        
        setContent {
            SmartTodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startDestination = if (isOnboarded) "home" else "onboarding")
                }
            }
        }
    }
}

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            val context = LocalContext.current
            val prefs = remember { PrefsManager(context) }
            OnboardingScreen(navController, prefs)
        }
        composable("home") {
            val context = LocalContext.current
            val prefs = remember { PrefsManager(context) }
            val userName = prefs.getString(PrefsManager.KEY_USER_NAME, "Sweetheart")
            // In a real app, we'd collect this from the Room DB
            val dummyTodos = listOf(
                TodoEntity(id=1, title="Call Mom 📞", date="2024-04-11", time="18:00", priority="high", status="pending", rawTranscript="Call mom in the evng"),
                TodoEntity(id=2, title="Submit Report 📄", date="2024-04-11", time="14:00", priority="medium", status="pending", rawTranscript="Finish report abt noon")
            )
            HomeScreen(navController, userName, dummyTodos)
        }
        composable("all_todos") {
            val dummyTodos = listOf(
                TodoEntity(id=1, title="Call Mom 📞", date="2024-04-11", time="18:00", priority="high", status="pending", rawTranscript="Call mom in the evng"),
                TodoEntity(id=2, title="Submit Report 📄", date="2024-04-11", time="14:00", priority="medium", status="pending", rawTranscript="Finish report abt noon")
            )
            AllTodosScreen(navController, dummyTodos)
        }
        composable("history") {
            val dummyTranscripts = listOf(
                TranscriptEntity(id=1, rawText="Call mom in the evng", parsedTodoId=1, languageDetected="English", wasEnglish=true, confidence=0.9f),
                TranscriptEntity(id=2, rawText="Finish report abt noon", parsedTodoId=2, languageDetected="English", wasEnglish=true, confidence=0.85f)
            )
            HistoryScreen(navController, dummyTranscripts)
        }
        composable("settings") {
            val context = LocalContext.current
            val prefs = remember { PrefsManager(context) }
            SettingsScreen(navController, prefs)
        }
    }
}
