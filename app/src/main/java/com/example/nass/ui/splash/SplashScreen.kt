package com.example.nass.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nass.data.local.SessionStore
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onRouteToLogin: () -> Unit,
    onRouteToDashboard: (role: String) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val store = SessionStore.from(context)
        delay(600) // small visual pause for the splash
        val token = store.currentToken()
        val role = store.currentRole()

        if (token.isNullOrBlank() || role.isNullOrBlank()) {
            onRouteToLogin()
        } else {
            onRouteToDashboard(role)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nass",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Second-hand clothing, reimagined",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}