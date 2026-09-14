package com.govtech.landstack.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    
    var expandedThemeDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.text_preferences), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.text_enable_notifications), style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Theme Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.text_app_theme), style = MaterialTheme.typography.bodyLarge)
                    
                    Box {
                        TextButton(onClick = { expandedThemeDropdown = true }) {
                            Text(themeMode.replaceFirstChar { it.uppercase() })
                        }
                        DropdownMenu(
                            expanded = expandedThemeDropdown,
                            onDismissRequest = { expandedThemeDropdown = false }
                        ) {
                            listOf("system", "light", "dark").forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        expandedThemeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.text_about), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.text_version_100), style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = { /* TODO: Open Terms */ }, modifier = Modifier.padding(start = 0.dp)) {
                    Text(stringResource(R.string.text_terms_of_service))
                }
                
                TextButton(onClick = { /* TODO: Open Privacy Policy */ }) {
                    Text(stringResource(R.string.text_privacy_policy))
                }
            }
        }
    }
}
