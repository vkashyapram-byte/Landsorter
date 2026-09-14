package com.govtech.landstack

import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.govtech.landstack.feature.auth.AuthScreen
import com.govtech.landstack.feature.auth.AuthViewModel
import com.govtech.landstack.feature.dashboard.DashboardScreen
import com.govtech.landstack.feature.gismap.MapScreen
import com.govtech.landstack.feature.parceldetail.ParcelDetailScreen
import com.govtech.landstack.feature.gnss.GnssScreen
import com.govtech.landstack.data.model.RoleAccess
import com.govtech.landstack.feature.myproperty.MyPropertyScreen
import com.govtech.landstack.feature.transactions.TransactionsScreen
import com.govtech.landstack.feature.records.RecordsScreen
import com.govtech.landstack.feature.tax.TaxScreen
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    var currentRole by remember { mutableStateOf("") }
    val authViewModel: AuthViewModel = hiltViewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val gesturesEnabled = currentRole.isNotEmpty()

    LaunchedEffect(gesturesEnabled) {
        if (gesturesEnabled && drawerState.isOpen) {
            drawerState.snapTo(DrawerValue.Closed)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            if (gesturesEnabled) {
                ModalDrawerSheet {
                    Spacer(Modifier.height(32.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentRole.isNotEmpty()) currentRole.take(1).uppercase() else "U",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if ("Citizen".equals(currentRole, ignoreCase = true)) "John Doe" else "Officer Smith", // Placeholder name
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.govtech.landstack.ui.components.StatusBadge(status = currentRole)
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))
                    
                    if ("Citizen".equals(currentRole, ignoreCase = true)) {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Dashboard") },
                            selected = navController.currentDestination?.route == "dashboard",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("dashboard") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Search") },
                            selected = navController.currentDestination?.route == "parcel_list",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("parcel_list") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("My property") },
                            selected = navController.currentDestination?.route == "my_property",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("my_property") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            label = { Text("Register property") },
                            selected = navController.currentDestination?.route == "parcel/new",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("parcel/new") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            label = { Text("Transactions") },
                            selected = navController.currentDestination?.route == "transactions",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("transactions") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            label = { Text("Records") },
                            selected = navController.currentDestination?.route == "records",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("records") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                            label = { Text("TAX") },
                            selected = navController.currentDestination?.route == "tax",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("tax") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Build, contentDescription = null) },
                            label = { Text("Service req") },
                            selected = navController.currentDestination?.route == "service_requests",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("service_requests") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Face, contentDescription = null) }, // Placeholder icon for AI
                            label = { Text("AI Assistant") },
                            selected = navController.currentDestination?.route == "ai_assistant",
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("ai_assistant") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                    } else {
                        NavigationDrawerItem(
                            icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = null) },
                            label = { Text("Dashboard") },
                            selected = navController.currentDestination?.route == "dashboard",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("dashboard") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                        
                        if (RoleAccess.canEdit(currentRole, RoleAccess.CREATE_PARCEL)) {
                            NavigationDrawerItem(
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = null) },
                                label = { Text("Create Parcel") },
                                selected = navController.currentDestination?.route == "parcel/new",
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    navController.navigate("parcel/new") { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                            )
                        }
                        
                        NavigationDrawerItem(
                            icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, contentDescription = null) },
                            label = { Text("My Profile") },
                            selected = false,
                            onClick = { 
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )

                        NavigationDrawerItem(
                            icon = { Icon(androidx.compose.material.icons.Icons.Default.Face, contentDescription = null) }, // Placeholder icon for AI
                            label = { Text("AI Assistant") },
                            selected = navController.currentDestination?.route == "ai_assistant",
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("ai_assistant") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )
                    }
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = { 
                            scope.launch { drawerState.close() }
                            authViewModel.logout(onComplete = {
                                currentRole = ""
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            })
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "auth") {
            composable("auth") {
                AuthScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        currentRole = role
                        navController.navigate("dashboard") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    role = currentRole,
                    onNavigateToMap = { navController.navigate("map") },
                    onNavigateToList = { navController.navigate("parcel_list") },
                    onNavigateToServices = { navController.navigate("service_requests") },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable("parcel_list") {
                com.govtech.landstack.feature.parcellist.ParcelListScreen(
                    role = currentRole,
                    onBack = { navController.popBackStack() },
                    onNavigateToParcel = { ulpin -> navController.navigate("parcel/$ulpin") }
                )
            }
            composable("map") {
                MapScreen(
                    role = currentRole,
                    onNavigateToParcel = { ulpin -> navController.navigate("parcel/$ulpin") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("parcel/{ulpin}") { backStackEntry ->
                val ulpin = backStackEntry.arguments?.getString("ulpin") ?: ""
                ParcelDetailScreen(ulpin = ulpin, role = currentRole, onBack = { navController.popBackStack() })
            }
            composable("gnss") {
                GnssScreen(onBack = { navController.popBackStack() })
            }
            composable("service_requests") {
                com.govtech.landstack.feature.servicerequest.ServiceRequestScreen(onBack = { navController.popBackStack() })
            }
            composable("ai_assistant") {
                com.govtech.landstack.feature.aiassistant.AIAssistantScreen(onBack = { navController.popBackStack() })
            }
            composable("my_property") {
                MyPropertyScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToParcel = { ulpin -> navController.navigate("parcel/$ulpin") }
                )
            }
            composable("transactions") {
                TransactionsScreen(onBack = { navController.popBackStack() })
            }
            composable("records") {
                RecordsScreen(onBack = { navController.popBackStack() })
            }
            composable("tax") {
                TaxScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
