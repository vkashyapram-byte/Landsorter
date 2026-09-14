package com.govtech.landstack.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandStackTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var showProfileMenu by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = { showLanguageDialog = true }) {
                Icon(Icons.Default.Language, contentDescription = "Language")
            }
            Box {
                IconButton(onClick = { showProfileMenu = true }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.text_profile)) },
                        onClick = { 
                            showProfileMenu = false
                            onProfileClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.text_settings)) },
                        onClick = { 
                            showProfileMenu = false
                            onSettingsClick()
                        }
                    )
                }
            }
        }
    )

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.text_select_language)) },
            text = {
                val languages = listOf(
                    "English" to "en",
                    "Assamese" to "as",
                    "Bengali" to "bn",
                    "Bodo" to "brx",
                    "Dogri" to "doi",
                    "Gujarati" to "gu",
                    "Hindi" to "hi",
                    "Kannada" to "kn",
                    "Kashmiri" to "ks",
                    "Konkani" to "kok",
                    "Maithili" to "mai",
                    "Malayalam" to "ml",
                    "Marathi" to "mr",
                    "Nepali" to "ne",
                    "Odia" to "or",
                    "Punjabi" to "pa",
                    "Sanskrit" to "sa",
                    "Sindhi" to "sd",
                    "Tamil" to "ta",
                    "Telugu" to "te",
                    "Urdu" to "ur"
                )
                LazyColumn {
                    items(languages) { lang ->
                        TextButton(
                            onClick = {
                                showLanguageDialog = false
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.second))
                            }
                        ) {
                            Text(lang.first)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.text_close))
                }
            }
        )
    }
}
