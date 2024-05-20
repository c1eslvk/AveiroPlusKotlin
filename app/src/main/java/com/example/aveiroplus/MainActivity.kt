package com.example.aveiroplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aveiroplus.ui.theme.AveiroPlusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AveiroPlusTheme {
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

    Scaffold (
        bottomBar = { BottomNavigationBar(navController = navController) }
    ){ innerPadding ->
       NavHost(
           navController = navController,
           startDestination = "home",
           modifier = Modifier.padding(innerPadding)
       ) {
           composable("home") { HomeScreen() }
           composable("map") { MapScreen() }
           composable("profile") { ProfileScreen() }
       }
    }

}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("home", "Home", ImageVector.vectorResource(id = R.drawable.ic_home)),
            BottomNavItem("map", "Map", ImageVector.vectorResource(id = R.drawable.ic_map)),
            BottomNavItem("profile", "Profile", ImageVector.vectorResource(id = R.drawable.ic_profile))
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = false,
                label = { Text(item.title)},
                icon = { Icon(item.icon, contentDescription = item.title) },
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen() {
    Text(text = "Home Screen")
}

@Composable
fun MapScreen() {
    Text(text = "Map Screen")
}

@Composable
fun ProfileScreen() {
    Text(text = "Profile Screen")
}

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)
