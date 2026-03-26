package com.devx.flashtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.devx.flashtrack.ui.navigation.FlashTrackNavGraph
import com.devx.flashtrack.ui.navigation.Screen
import com.devx.flashtrack.ui.theme.FlashTrackTheme
import com.devx.flashtrack.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // TODO: read theme pref from DataStore; hardcoded dark for now
            FlashTrackTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: MainViewModel = hiltViewModel()

                    // For first-run we could show onboarding; skip directly to Home for now
                    FlashTrackNavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        startDestination = Screen.Home.route
                    )
                }
            }
        }
    }
}
