package com.example.aveiroplus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun EventDetailsScreen(navController: NavController, eventId: String) {
    var event by remember { mutableStateOf<Event?>(null) }
    var user by remember { mutableStateOf<UserProfile?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var debugMessage by remember { mutableStateOf<String?>(null) }
    var isRegistered by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(eventId) {
        try {
            debugMessage = "Fetching event with ID: $eventId"

            // Fetch event details
            val eventSnapshot = db.collection("events").document(eventId).get().await()
            if (eventSnapshot.exists()) {
                event = eventSnapshot.toObject(Event::class.java)
                debugMessage = "Event loaded: $event"
            } else {
                errorMessage = "Event not found"
                debugMessage = "Event not found with ID: $eventId"
                return@LaunchedEffect
            }

            // Fetch user details
            val userSnapshot = db.collection("users").document(currentUser?.uid ?: "").get().await()
            if (userSnapshot.exists()) {
                user = userSnapshot.toObject(UserProfile::class.java)
                debugMessage += "\nUser loaded: $user"
            } else {
                errorMessage = "User not found"
                debugMessage += "\nUser not found with UID: ${currentUser?.uid}"
                return@LaunchedEffect
            }

            // Check if user is registered for the event
            isRegistered = user?.registeredEventsIds?.contains(eventId) ?: false
            debugMessage += "\nUser is registered: $isRegistered"
        } catch (e: Exception) {
            errorMessage = "Failed to load data: ${e.message}"
            debugMessage = "Failed to load data: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        } else if (event == null) {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            // Event details UI
            Image(
                painter = rememberAsyncImagePainter(event?.imageUrl),
                contentDescription = event?.eventName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = event?.eventName ?: "",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Available places: ${event?.availablePlaces}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event?.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isRegistered) {
                Text(
                    text = "You are registered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (event?.availablePlaces == 0) {
                Text(
                    text = "Event is full",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Button(onClick = {
                    // Register user for the event
                    registerForEvent(event!!, user!!, db) { success, error ->
                        if (success) {
                            isRegistered = true
                            event = event?.copy(availablePlaces = event!!.availablePlaces - 1)
                        } else {
                            errorMessage = error
                        }
                    }
                }) {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}

fun registerForEvent(event: Event, user: UserProfile, db: FirebaseFirestore, callback: (Boolean, String?) -> Unit) {
    val eventRef = db.collection("events").document(event.eventId)
    val userRef = db.collection("users").document(user.uid)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(eventRef)
        val currentEvent = snapshot.toObject(Event::class.java)

        if (currentEvent == null || currentEvent.availablePlaces == 0) {
            throw Exception("No available places")
        }

        // Update event
        val newAvailablePlaces = currentEvent.availablePlaces - 1
        val newRegisteredUsersIds = currentEvent.registeredUsersIds + user.uid
        transaction.update(eventRef, "availablePlaces", newAvailablePlaces)
        transaction.update(eventRef, "registeredUsersIds", newRegisteredUsersIds)

        // Update user
        val newRegisteredEventsIds = user.registeredEventsIds + event.eventId
        transaction.update(userRef, "registeredEventsIds", newRegisteredEventsIds)
    }.addOnSuccessListener {
        callback(true, null)
    }.addOnFailureListener { exception ->
        callback(false, exception.message)
    }
}
