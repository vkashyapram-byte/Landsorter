package com.govtech.landstack.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandStackTopAppBar(
    title: String,
    onNavigationIconClick: () -> Unit
) {
    var showProfileMenu by remember { mutableStateOf(false) }

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
            Box {
                IconButton(onClick = { showProfileMenu = true }) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Profile Menu")
                }
                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile") },
                        onClick = { showProfileMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { showProfileMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Language") },
                        onClick = { showProfileMenu = false }
                    )
                }
            }
        }
    )
}
