package com.example.aveiroplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aveiroplus.components.BottomNavigationBar
import com.example.aveiroplus.ui.theme.AveiroPlusTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            bottomBar = { BottomNavigationBar(navController = navController, userRole = userRole!!) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (userRole == "ADMIN") "admin" else "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen() }
                composable("admin") { AdminScreen() }
                composable("map") { MapScreen() }
                composable("profile") { ProfileContent() }
            }
        }
    }
}
