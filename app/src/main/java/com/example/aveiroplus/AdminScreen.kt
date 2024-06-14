package com.example.aveiroplus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.aveiroplus.components.Event
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminScreen(navController: NavController) {
    // State to hold events
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch events from Firestore
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .get()
            .addOnSuccessListener { result ->
                events = result.mapNotNull { it.toObject(Event::class.java) }
                errorMessage = null // Clear any previous error messages
            }
            .addOnFailureListener { exception ->
                errorMessage = "Failed to load events: ${exception.message}"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("new_event") }) {
            Text(text = "Add New Event")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessage != null) {
            Text(text = errorMessage!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        } else if (events.isEmpty()) {
            Text(text = "No events available.", style = MaterialTheme.typography.bodyMedium)
        } else {
            AdminContent(events = events, navController = navController)
        }
    }
}

@Composable
fun AdminContent(events: List<Event>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(events) { event ->
            EventItem(event = event, navController = navController)
        }
    }
}

@Composable
fun EventAdminItem(event: Event, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(8.dp))
            .shadow(4.dp)
            .clickable { navController.navigate("event_detail_admin/${event.eventName}") },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(event.imageUrl),
            contentDescription = event.eventName,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.eventName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(4.dp)
        )
        Text(
            text = event.description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(4.dp)
        )
    }
}