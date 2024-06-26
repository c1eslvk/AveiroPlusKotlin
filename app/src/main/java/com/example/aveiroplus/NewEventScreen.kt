package com.example.aveiroplus

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.uiStates.NewEventUiState
import com.example.aveiroplus.viewModels.NewEventViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

val newEventViewModel = NewEventViewModel()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEventScreen(navController: NavController) {
    val newEventUiState by newEventViewModel.uiState.collectAsState()

    var eventName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var availablePlaces by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    val context = LocalContext.current

    val defaultCameraPositionState = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPositionState
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        Toast.makeText(context, "Image selected", Toast.LENGTH_SHORT).show()
    }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Event Name") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                OutlinedTextField(
                    value = availablePlaces,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            availablePlaces = it
                        }
                    },
                    label = { Text("Available Places") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") }
                )
                mapComponent(viewModel = newEventViewModel, latitude = newEventUiState.latitude, longitude = newEventUiState.longitude, cameraPositionState)

                val state = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
                DatePicker(state = state, modifier = Modifier.padding(16.dp))
                eventDate = state.selectedDateMillis.toString()
                Button(onClick = { launcher.launch("image/*") }) {
                    Text(text = "Choose Image")
                }
                Button(
                    onClick = {
                        if (eventName.isNotEmpty() && description.isNotEmpty() && availablePlaces.isNotEmpty() && selectedImageUri != null && location.isNotEmpty() && eventDate.isNotEmpty()) {
                            uploadImageToFirebaseStorage(selectedImageUri!!, context) { downloadUrl ->
                                if (downloadUrl != null) {
                                    val newEvent = Event(
                                        eventId = UUID.randomUUID().toString(),  // Generate unique event ID
                                        eventName = eventName,
                                        description = description,
                                        availablePlaces = availablePlaces.toInt(),
                                        imageUrl = downloadUrl,
                                        location = location,
                                        eventDate = eventDate.toLong(),
                                        lat = newEventUiState.latitude,
                                        long = newEventUiState.longitude,
                                        price = price.toDouble()
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
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Add Event")
                }
            }
        }
    }
}

@Composable
fun mapComponent(viewModel: NewEventViewModel, latitude: Double, longitude: Double, cameraPositionState: CameraPositionState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .scrollable(
                state = rememberScrollState(),
                enabled = false,
                orientation = Orientation.Vertical
            )
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                viewModel.updateCoords(lat = it.latitude, long = it.longitude)
            }
        ) {
            Marker(
                state = MarkerState(position = LatLng(latitude, longitude)),
                draggable = true,
            )
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
        .document(event.eventId)  // Use eventId as the document ID
        .set(event)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to save event: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}
