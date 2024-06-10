package com.example.aveiroplus

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aveiroplus.components.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

@Composable
fun NewEventScreen(navController: NavController) {
    var eventName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var availablePlaces by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") }
        )
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )
        TextField(
            value = availablePlaces,
            onValueChange = { availablePlaces = it },
            label = { Text("Available Places") }
        )
        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Choose Image")
        }
        Button(onClick = {
            if (eventName.isNotEmpty() && description.isNotEmpty() && availablePlaces.isNotEmpty() && selectedImageUri != null) {
                uploadImageToFirebaseStorage(selectedImageUri!!, context) { downloadUrl ->
                    if (downloadUrl != null) {
                        val newEvent = Event(
                            eventName = eventName,
                            description = description,
                            availablePlaces = availablePlaces.toInt(),
                            imageUrl = downloadUrl
                        )
                        saveEventToFirestore(newEvent, context) {
                            Toast.makeText(context, "Event created successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()  // Navigate back to the AdminScreen
                        }
                    } else {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Add Event")
        }
    }
}

fun uploadImageToFirebaseStorage(imageUri: Uri, context: android.content.Context, onComplete: (String?) -> Unit) {
    val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    val imageRef = storageReference.child("event_images/${UUID.randomUUID()}.jpg")

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onComplete(uri.toString())
            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                onComplete(null)
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
            onComplete(null)
        }
}

fun saveEventToFirestore(event: Event, context: android.content.Context, onSuccess: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("events")
        .add(event)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to save event: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}