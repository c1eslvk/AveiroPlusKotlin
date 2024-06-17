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
import com.example.aveiroplus.components.UserProfile

class MapViewModel: ViewModel()  {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        getMapMarkers()
        getYourMarker()
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
}