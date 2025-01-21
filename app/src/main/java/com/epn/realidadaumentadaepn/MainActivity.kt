package com.epn.realidadaumentadaepn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.epn.realidadaumentadaepn.ui.screens.ARScreen
import com.epn.realidadaumentadaepn.ui.screens.HomeScreen
import com.epn.realidadaumentadaepn.ui.theme.RealidadAumentadaEPNTheme
import com.epn.realidadaumentadaepn.ui.navigation.NavRoutes
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealidadAumentadaEPNTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.HOME,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavRoutes.HOME) {
                            HomeScreen(navController)
                        }
                        composable(
                            route = "ar/{locationName}",
                            arguments = listOf(
                                navArgument("locationName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val locationName = backStackEntry.arguments?.getString("locationName")
                            if (locationName != null) {
                                ARScreen(navController, locationName)
                            }
                        }
                    }
                }
            }
        }
    }
}
