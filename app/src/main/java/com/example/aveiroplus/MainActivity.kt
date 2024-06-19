package com.example.aveiroplus

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aveiroplus.components.BottomNavigationBar
import com.example.aveiroplus.components.TopBar
import com.example.aveiroplus.services.ForegroundLocationService
import com.example.aveiroplus.ui.theme.AveiroPlusTheme
import com.example.aveiroplus.viewModels.SharedPreferenceUtil
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.android.material.snackbar.Snackbar

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private lateinit var sharedPreferences: SharedPreferences

class MainActivity : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener{

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val enabled = sharedPreferences.getBoolean(
            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)

        val locationService = ForegroundLocationService(applicationContext)



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
                    MainScreen(locationService, requestForegroundPermissions = ::requestForegroundPermissions, foregroundPermissionApproved = ::foregroundPermissionApproved)
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        if (provideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_main),
                "Location permission needed for core functionality",
                Snackbar.LENGTH_LONG
            )
                .setAction("OK") {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d("LOCATI", "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }


}

@Composable
fun MainScreen(locationService: ForegroundLocationService, requestForegroundPermissions: () ->Unit, foregroundPermissionApproved: () -> Boolean) {
    val navController = rememberNavController()
    var userRole by remember { mutableStateOf<String?>(null) }
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(firebaseAuth.currentUser) {
        firebaseAuth.currentUser?.let { user ->
            try {
                val documentSnapshot =
                    firestore.collection("users").document(user.uid).get().await()
                userRole = documentSnapshot.getString("role")
            } catch (e: Exception) {

            }
        }
    }

    if (userRole != null) {
        Scaffold(
            topBar = { TopBar() },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    userRole = userRole!!
                )
            },
            contentWindowInsets = WindowInsets.systemBars,
        ) {
            innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (userRole == "ADMIN") "admin" else "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HomeScreen(
                            navController,
                            sharedPreferences,
                            locationService,
                            requestForegroundPermissions,
                            foregroundPermissionApproved
                        )
                    }
                }
                composable("admin") { AdminScreen(navController) }
                composable("map") { MapScreen(navController, locationService, requestForegroundPermissions, foregroundPermissionApproved, sharedPreferences) }
                composable("profile") { ProfileContent(navController) }
                composable("new_event") { NewEventScreen(navController = navController) }
                composable(
                    route = "event_detail/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString("eventId") ?: return@composable
                    EventDetailsScreen(navController = navController, eventId = eventId)
                }
                composable(
                    route = "event_detail_admin/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId =
                        backStackEntry.arguments?.getString("eventId") ?: return@composable
                    EventDetailAdminScreen(navController = navController, eventId = eventId)
                }
                composable("your_events") { YourEventsScreen(firestore, navController) }
            }
        }
        }
    }




