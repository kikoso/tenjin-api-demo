package com.tenjin.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tenjin.sdk.TenjinSDK
import com.tenjin.testapp.ui.theme.TenjinSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TenjinSDKTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TenjinTestApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenjinTestApp(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf<String?>(null) }
    var newUsername by remember { mutableStateOf("") }
    val events by TenjinSDK.getEvents().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        username = TenjinSDK.getUsername()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (username != null) {
            Text("Current Username: $username")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                TenjinSDK.removeUsername()
                username = null
            }) {
                Text("Remove Username")
            }
        } else {
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("Username") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (newUsername.isNotEmpty()) {
                    TenjinSDK.setUsername(newUsername)
                    username = newUsername
                }
            }) {
                Text("Set Username")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { TenjinSDK.sendEvent("event_a_clicked") }) {
            Text("Send Event A")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { TenjinSDK.sendEvent("event_b_clicked") }) {
            Text("Send Event B")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items( events) { event ->
                Text("Sending event ${event.eventName}...")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TenjinTestAppPreview() {
    TenjinSDKTheme {
        TenjinTestApp()
    }
}
