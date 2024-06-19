package com.example.aveiroplus.viewModels

import androidx.lifecycle.ViewModel
import com.example.aveiroplus.components.MapMarker
import com.example.aveiroplus.uiStates.MapUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.UserProfile
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MapViewModel: ViewModel()  {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        _uiState.update {
            it.copy(isMapReady = false)
        }
        runBlocking {
            getMapMarkers()
            getYourMarker()
            getYourEvents()
        }
        _uiState.update {
            it.copy(isMapReady = true)
        }
    }
    fun getMapMarkers() {
        var newMarkers by mutableStateOf<List<MapMarker>>(emptyList())
        firestore.collection("location").get()
            .addOnSuccessListener { result ->
                newMarkers = result.mapNotNull { it.toObject(MapMarker::class.java) }
                _uiState.update {
                    it.copy(markers = newMarkers)
                }
            }

    }

    fun getYourEvents() {
        firestore.collection("users").document(firebaseAuth.currentUser?.uid?: "")
            .get().addOnSuccessListener { result ->
                val user = result.toObject(UserProfile::class.java)
                runBlocking {
                    val events = mutableListOf<Event>()

                    for (eventId in user?.registeredEventsIds ?: emptyList()) {
                        try {
                            val eventDocument =
                                firestore.collection("events").document(eventId).get().await()
                            val eventName = eventDocument.getString("eventName") ?: "N/A"
                            val imageUrl = eventDocument.getString("imageUrl") ?: ""
                            val location = eventDocument.getString("location") ?: ""
                            val lat = eventDocument.getDouble("lat") ?: 0.0
                            val long = eventDocument.getDouble("long") ?: 0.0

                            val event = Event(
                                eventId = eventId,
                                eventName = eventName,
                                imageUrl = imageUrl,
                                location = location,
                                lat = lat,
                                long = long
                            )
                            events.add(event)
                        } catch (e: Exception) {
                        }
                    }
                    _uiState.update {
                        it.copy(yourEvents = events)
                    }
                }
            }
    }
    suspend fun getUserEvents(eventsIds: List<String>){
        val events = mutableListOf<Event>()

        for (eventId in eventsIds) {
            try {
                val eventDocument = firestore.collection("events").document(eventId).get().await()
                val eventName = eventDocument.getString("eventName") ?: "N/A"
                val imageUrl = eventDocument.getString("imageUrl") ?: ""
                val location = eventDocument.getString("location") ?: ""
                val lat = eventDocument.getDouble("lat") ?: 0.0
                val long = eventDocument.getDouble("long") ?: 0.0

                val event = Event(
                    eventId = eventId,
                    eventName = eventName,
                    imageUrl = imageUrl,
                    location = location,
                    lat = lat,
                    long = long
                )
                events.add(event)
            } catch (e: Exception) { }
        }
        _uiState.update {
            it.copy(eventsToShow = events)
        }
    }

    fun getYourMarker() {
        val currentUser = firebaseAuth.currentUser

        firestore.collection("location").document(currentUser?.uid?:"").get()
            .addOnSuccessListener {result ->
                val yourNewMarker = result.toObject(MapMarker::class.java)
                _uiState.update {
                    it.copy(yourMarker = yourNewMarker?: MapMarker())
                }
            }
    }

    fun assignUserToView(usr: UserProfile) {
        _uiState.update {
            it.copy(userToShow = usr)
        }
    }


    fun changeUserVisibility(visibility: Boolean) {
        _uiState.update {
            it.copy(isInfoVisible = !visibility)
        }
    }

    fun assignEventToView(event: Event) {
        _uiState.update {
            it.copy(eventToShow = event)
        }
    }


    fun changeEventVisibility(visibility: Boolean) {
        _uiState.update {
            it.copy(isInfoVisible = !visibility)
        }
    }
}