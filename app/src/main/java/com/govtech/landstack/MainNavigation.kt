package com.govtech.landstack

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.govtech.landstack.feature.auth.AuthScreen
import com.govtech.landstack.feature.dashboard.DashboardScreen
import com.govtech.landstack.feature.gismap.MapScreen
import com.govtech.landstack.feature.parceldetail.ParcelDetailScreen
import com.govtech.landstack.feature.gnss.GnssScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    var currentRole by remember { mutableStateOf("") }

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(onLoginSuccess = { role ->
                currentRole = role
                navController.navigate("dashboard") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable("dashboard") {
            DashboardScreen(
                role = currentRole,
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToServices = { /* Navigate to Services */ }
            )
        }
        composable("map") {
            MapScreen(
                onParcelClick = { ulpin -> navController.navigate("parcel/$ulpin") },
                onNavigateToGnss = { navController.navigate("gnss") }
            )
        }
        composable("parcel/{ulpin}") { backStackEntry ->
            val ulpin = backStackEntry.arguments?.getString("ulpin") ?: ""
            ParcelDetailScreen(ulpin = ulpin, role = currentRole, onBack = { navController.popBackStack() })
        }
        composable("gnss") {
            GnssScreen(onBack = { navController.popBackStack() })
        }
    }
}
