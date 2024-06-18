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
import com.example.aveiroplus.uiStates.NewEventUiState
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await

class NewEventViewModel: ViewModel()  {
    private val _uiState = MutableStateFlow(NewEventUiState())
    val uiState: StateFlow<NewEventUiState> = _uiState.asStateFlow()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    fun updateCoords(lat: Double, long: Double) {
        _uiState.update {
            it.copy(latitude = lat, longitude = long)
        }
    }
}