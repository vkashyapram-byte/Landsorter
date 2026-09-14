package com.govtech.landstack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.govtech.landstack.theme.LandStackTheme
import com.govtech.landstack.ui.screens.CrashRecoveryScreen
import com.govtech.landstack.util.GlobalExceptionHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Set up global exception handler
    Thread.setDefaultUncaughtExceptionHandler(
        GlobalExceptionHandler(applicationContext, Thread.getDefaultUncaughtExceptionHandler())
    )

    val showErrorScreen = intent.getBooleanExtra("show_error_screen", false)
    val errorMessage = intent.getStringExtra("error_message") ?: "Unknown Error"

    enableEdgeToEdge()
    setContent {
      LandStackTheme { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
           if (showErrorScreen) {
               CrashRecoveryScreen(errorMessage = errorMessage)
           } else {
               MainNavigation()
           }
        } 
      }
    }
  }
}
