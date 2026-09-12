package com.govtech.landstack.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("Citizen") }
    val roles = listOf("Citizen", "Land Officer", "Admin")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "LandStack", style = MaterialTheme.typography.displayMedium)
        Text(text = "Unified Land Governance", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(48.dp))

        Text("Select Role to Login:")
        Spacer(modifier = Modifier.height(16.dp))

        roles.forEach { role ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = (role == selectedRole),
                    onClick = { selectedRole = role }
                )
                Text(text = role, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLoginSuccess(selectedRole) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Mock authentication for prototype.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
