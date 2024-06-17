package com.example.aveiroplus

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aveiroplus.components.BottomNavigationBar
import com.example.aveiroplus.services.ForegroundLocationService
import com.example.aveiroplus.components.TopBar
import com.example.aveiroplus.ui.theme.AveiroPlusTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val locationService = ForegroundLocationService(applicationContext)
        locationService.listenToLocation()
        setContent {
            AveiroPlusTheme {
                //check if system is in darkmode
                val isSystemInDarkMode = isSystemInDarkTheme()
                val systemController = rememberSystemUiController()

                SideEffect {
                    systemController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !isSystemInDarkMode
                    )
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }

    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var userRole by remember { mutableStateOf<String?>(null) }
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(firebaseAuth.currentUser) {
        firebaseAuth.currentUser?.let { user ->
            try {
                val documentSnapshot = firestore.collection("users").document(user.uid).get().await()
                userRole = documentSnapshot.getString("role")
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    if (userRole != null) {
        Scaffold(
            topBar = { TopBar() },
            bottomBar = { BottomNavigationBar(navController = navController, userRole = userRole!!) },
            contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (userRole == "ADMIN") "admin" else "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("admin") { AdminScreen(navController) }
                composable("map") { MapScreen() }
                composable("profile") { ProfileContent(navController) }
                composable("new_event") { NewEventScreen(navController = navController) }
                composable(
                    route = "event_detail/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    EventDetailsScreen(navController = navController, eventId = eventId)
                }
                composable(
                    route = "event_detail_admin/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    EventDetailAdminScreen(navController = navController, eventId = eventId)
                }
                composable("your_events") { YourEventsScreen(firestore, navController) }
            }
        }
    }
}

