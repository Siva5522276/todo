package com.smarttodo.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smarttodo.app.utils.PrefsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController, prefs: PrefsManager) {
    var name by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var picovoiceKey by remember { mutableStateOf("") }
    var openRouterKey by remember { mutableStateOf("") }
    var twilioSid by remember { mutableStateOf("") }
    var twilioToken by remember { mutableStateOf("") }
    var twilioFrom by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Welcome Sweetheart! 💛",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "I'm your SmartTodo mama. Let's get you set up so I can help you stay on track!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("What's your name?") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("WhatsApp Number (with country code)") },
            modifier = Modifier.fillMaxWidth()
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text("API Keys (Private & Local Only)", fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = picovoiceKey,
            onValueChange = { picovoiceKey = it },
            label = { Text("Picovoice Access Key") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = openRouterKey,
            onValueChange = { openRouterKey = it },
            label = { Text("OpenRouter API Key") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = twilioSid,
            onValueChange = { twilioSid = it },
            label = { Text("Twilio Account SID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = twilioToken,
            onValueChange = { twilioToken = it },
            label = { Text("Twilio Auth Token") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = twilioFrom,
            onValueChange = { twilioFrom = it },
            label = { Text("Twilio Outbound Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                prefs.saveString(PrefsManager.KEY_USER_NAME, name)
                prefs.saveString(PrefsManager.KEY_WHATSAPP_NUMBER, whatsapp)
                prefs.saveString(PrefsManager.KEY_PICOVOICE_KEY, picovoiceKey)
                prefs.saveString(PrefsManager.KEY_OPENROUTER_KEY, openRouterKey)
                prefs.saveString(PrefsManager.KEY_TWILIO_SID, twilioSid)
                prefs.saveString(PrefsManager.KEY_TWILIO_TOKEN, twilioToken)
                prefs.saveString(PrefsManager.KEY_TWILIO_FROM, twilioFrom)
                prefs.saveBoolean(PrefsManager.KEY_IS_ONBOARDED, true)
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = name.isNotBlank() && whatsapp.isNotBlank()
        ) {
            Text("Start My Journey 💛")
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
