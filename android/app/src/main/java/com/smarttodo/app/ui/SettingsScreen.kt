package com.smarttodo.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smarttodo.app.utils.PrefsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, prefs: PrefsManager) {
    var name by remember { mutableStateOf(prefs.getString(PrefsManager.KEY_USER_NAME)) }
    var whatsapp by remember { mutableStateOf(prefs.getString(PrefsManager.KEY_WHATSAPP_NUMBER)) }
    var openRouterKey by remember { mutableStateOf(prefs.getString(PrefsManager.KEY_OPENROUTER_KEY)) }
    var twilioSid by remember { mutableStateOf(prefs.getString(PrefsManager.KEY_TWILIO_SID)) }
    var twilioToken by remember { mutableStateOf(prefs.getString(PrefsManager.KEY_TWILIO_TOKEN)) }
    var twilioFrom by remember { mutableStateOf(prefs.getString(PrefsManager.KEY_TWILIO_FROM)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings ⚙️") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp/Phone Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = openRouterKey, onValueChange = { openRouterKey = it }, label = { Text("OpenRouter Key") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = twilioSid, onValueChange = { twilioSid = it }, label = { Text("Twilio SID") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = twilioToken, onValueChange = { twilioToken = it }, label = { Text("Twilio Token") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = twilioFrom, onValueChange = { twilioFrom = it }, label = { Text("Twilio From Number") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    prefs.saveString(PrefsManager.KEY_USER_NAME, name)
                    prefs.saveString(PrefsManager.KEY_WHATSAPP_NUMBER, whatsapp)
                    prefs.saveString(PrefsManager.KEY_OPENROUTER_KEY, openRouterKey)
                    prefs.saveString(PrefsManager.KEY_TWILIO_SID, twilioSid)
                    prefs.saveString(PrefsManager.KEY_TWILIO_TOKEN, twilioToken)
                    prefs.saveString(PrefsManager.KEY_TWILIO_FROM, twilioFrom)
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save Changes")
            }
        }
    }
}
