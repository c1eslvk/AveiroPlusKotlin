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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.aveiroplus.components.Event
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


import com.google.firebase.auth.FirebaseAuth

fun registerUser(eventId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    currentUser?.uid?.let { userId ->
        val userRef = db.collection("users").document(userId)
        val eventRef = db.collection("events").document(eventId)

        db.runTransaction { transaction ->
            val eventSnapshot = transaction.get(eventRef)
            val userSnapshot = transaction.get(userRef)

            val registeredUserIds = eventSnapshot.get("registeredUserIds") as? List<String> ?: emptyList()
            val registeredEventIds = userSnapshot.get("registeredEventIds") as? List<String> ?: emptyList()

            if (registeredUserIds.contains(userId)) {
                throw Exception("User already registered for this event")
            }

            if (registeredUserIds.size >= (eventSnapshot.getLong("availablePlaces") ?: 0)) {
                throw Exception("No available places")
            }

            transaction.update(eventRef, "registeredUserIds", FieldValue.arrayUnion(userId))
            transaction.update(userRef, "registeredEventIds", FieldValue.arrayUnion(eventId))
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    } ?: run {
        onFailure(Exception("User not authenticated"))
    }
}

@Composable
fun EventDetailScreen(navController: NavController, eventName: String) {
    var event by remember { mutableStateOf<Event?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

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
                errorMessage = "Failed to load event details"
            }
    }

    val currentUser = auth.currentUser

    currentUser?.let {

        event?.let {
            val availablePlaces = it.availablePlaces - it.registeredUsersIds.size

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display event details
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
                    text = "Available Places: $availablePlaces",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = it.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (availablePlaces > 0) {
                    Button(onClick = {
                        registerUser(it.eventId, {
                            // On Success: Update UI or show success message
                            // Optionally update event details after registration
                            val db = FirebaseFirestore.getInstance()
                            db.collection("events")
                                .whereEqualTo("eventName", eventName)
                                .get()
                                .addOnSuccessListener { result ->
                                    event = result.documents.firstOrNull()?.toObject(Event::class.java)
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Failed to fetch updated event details"
                                }
                        }, { e ->
                            // On Failure: Show error message
                            errorMessage = e.message
                        })
                    }) {
                        Text(text = "Register")
                    }
                } else {
                    Text(text = "Event is full", color = Color.Red)
                }
                Button(onClick = { navController.popBackStack() }) {
                    Text(text = "Back")
                }
                errorMessage?.let { error ->
                    Text(text = error, color = Color.Red)
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
    } ?: run {
        Text(text = "User not authenticated", style = MaterialTheme.typography.bodyMedium)
    }
}
