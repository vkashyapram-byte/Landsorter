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
import androidx.compose.ui.res.stringResource
import com.govtech.landstack.R
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

import androidx.navigation.compose.currentBackStackEntryAsState
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
                            label = { Text(stringResource(R.string.text_dashboard)) },
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
                            label = { Text(stringResource(R.string.text_search)) },
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
                            label = { Text(stringResource(R.string.text_my_property)) },
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
                            label = { Text(stringResource(R.string.text_register_property)) },
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
                            label = { Text(stringResource(R.string.text_transactions)) },
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
                            label = { Text(stringResource(R.string.text_records)) },
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
                            label = { Text(stringResource(R.string.text_tax)) },
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
                            label = { Text(stringResource(R.string.text_service_request)) },
                            selected = navController.currentDestination?.route == "service_requests",
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("service_requests") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )

                    } else {
                        NavigationDrawerItem(
                            icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = null) },
                            label = { Text(stringResource(R.string.text_dashboard)) },
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
                                label = { Text(stringResource(R.string.text_create_parcel)) },
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
                            label = { Text(stringResource(R.string.text_my_profile)) },
                            selected = false,
                            onClick = { 
                                scope.launch { drawerState.close() }
                                navController.navigate("profile") { launchSingleTop = true }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                        )


                    }
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                        label = { Text(stringResource(R.string.text_logout)) },
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                        },
                        onProfileClick = { navController.navigate("profile") { launchSingleTop = true } },
                        onSettingsClick = { navController.navigate("settings") { launchSingleTop = true } }
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
                    ParcelDetailScreen(
                        ulpin = ulpin,
                        role = currentRole,
                        onBack = { navController.popBackStack() },
                        onProfileClick = { navController.navigate("profile") { launchSingleTop = true } },
                        onSettingsClick = { navController.navigate("settings") { launchSingleTop = true } }
                    )
                }
                composable("gnss") {
                    GnssScreen(onBack = { navController.popBackStack() })
                }
                composable("service_requests") {
                    com.govtech.landstack.feature.servicerequest.ServiceRequestScreen(onBack = { navController.popBackStack() })
                }
                composable("records") {
                    com.govtech.landstack.feature.records.RecordsScreen(
                        onBack = { navController.popBackStack() },
                        onProfileClick = { navController.navigate("profile") { launchSingleTop = true } },
                        onSettingsClick = { navController.navigate("settings") { launchSingleTop = true } }
                    )
                }
                composable(
                    "ai_assistant?ulpin={ulpin}",
                    arguments = listOf(androidx.navigation.navArgument("ulpin") { nullable = true })
                ) { backStackEntry ->
                    val ulpin = backStackEntry.arguments?.getString("ulpin")
                    com.govtech.landstack.feature.aiassistant.AIAssistantScreen(ulpin = ulpin, onBack = { navController.popBackStack() })
                }
                composable("my_property") {
                    MyPropertyScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToParcel = { ulpin -> navController.navigate("parcel/$ulpin") }
                    )
                }
                composable("profile") {
                    com.govtech.landstack.feature.profile.ProfileScreen(
                        onNavigateToLogin = {
                            currentRole = ""
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("settings") {
                    com.govtech.landstack.feature.settings.SettingsScreen()
                }
                composable("transactions") {
                    TransactionsScreen(onBack = { navController.popBackStack() })
                }

                composable("tax") {
                    TaxScreen(onBack = { navController.popBackStack() })
                }
            }

            val navBackStackEntry = navController.currentBackStackEntryAsState().value
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute != null && !currentRoute.startsWith("auth") && !currentRoute.startsWith("ai_assistant")) {
                FloatingActionButton(
                    onClick = {
                        val ulpinArg = navBackStackEntry.arguments?.getString("ulpin")
                        val aiRoute = if (ulpinArg != null && ulpinArg != "new") "ai_assistant?ulpin=$ulpinArg" else "ai_assistant"
                        navController.navigate(aiRoute) { launchSingleTop = true }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()), // handle nav bar overlaps if any
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Face, contentDescription = "AI Assistant")
                }
            }
        }
    }
}
