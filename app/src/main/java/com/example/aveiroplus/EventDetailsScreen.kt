package com.example.aveiroplus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.aveiroplus.components.Event
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EventDetailScreen(navController: NavController, eventName: String) {
    var event by remember { mutableStateOf<Event?>(null) }

    // Fetch the event details from Firestore
    LaunchedEffect(eventName) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .whereEqualTo("eventName", eventName)
            .get()
            .addOnSuccessListener { result ->
                event = result.documents.firstOrNull()?.toObject(Event::class.java)
            }
            .addOnFailureListener {
                // Handle the error
            }
    }

    event?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(it.imageUrl),
                    contentDescription = it.eventName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = it.eventName,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Available Places: ${it.availablePlaces}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = it.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Back")
            }
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}