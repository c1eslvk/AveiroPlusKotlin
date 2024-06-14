package com.example.aveiroplus

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun YourEventsScreen(firestore: FirebaseFirestore, navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val userProfile = remember { mutableStateOf<UserProfile?>(null) }
    val events = remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Fetch user profile
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            try {
                val userId = it.uid
                val documentSnapshot = firestore.collection("users").document(userId).get().await()
                val name = documentSnapshot.getString("name") ?: "N/A"
                val surname = documentSnapshot.getString("surname") ?: "N/A"
                val email = documentSnapshot.getString("email") ?: "N/A"
                val profileImageUrl = documentSnapshot.getString("profileImageUrl") ?: ""
                val registeredEventsIds = documentSnapshot.get("registeredEventsIds") as? List<String> ?: emptyList()

                userProfile.value = UserProfile(
                    name = name,
                    surname = surname,
                    email = email,
                    profileImageUrl = profileImageUrl,
                    registeredEventsIds = registeredEventsIds
                )

                // Fetch events based on registeredEventsIds
                events.value = loadEvents(userProfile.value!!, firestore)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to fetch user profile and events: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Events",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (events.value.isEmpty()) {
            Text(
                text = "No registered events",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            events.value.forEach { event ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(event.imageUrl),
                        contentDescription = "Event Image",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = event.eventName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
    Button(onClick = { navController.popBackStack() }) {
        Text(text = "Back")
    }
}


private suspend fun loadEvents(userProfile: UserProfile, firestore: FirebaseFirestore): List<Event> {
    val events = mutableListOf<Event>()

    for (eventId in userProfile.registeredEventsIds) {
        try {
            val eventDocument = firestore.collection("events").document(eventId).get().await()
            val eventName = eventDocument.getString("eventName") ?: "N/A"
            val imageUrl = eventDocument.getString("imageUrl") ?: ""
            // Add other fields as per your Event data class

            val event = Event(
                eventId = eventId,
                eventName = eventName,
                imageUrl = imageUrl
                // Add other fields as per your Event data class
            )
            events.add(event)
        } catch (e: Exception) {
            // Handle exceptions if needed
        }
    }

    return events
}
