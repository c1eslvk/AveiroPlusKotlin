package com.example.aveiroplus.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.MapMarker
import com.example.aveiroplus.components.UserProfile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ForegroundLocationService(
    private val context: Context
): Service(){

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.lastLocation?.let {
                runBlocking {
                    saveToDb(it)
                    Log.d("LOCATION", "Location sent")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Throws
    fun listenToLocation(){

    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .build()
        if (!hasLocationPermission()) throw NoPermissionsException


        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    suspend fun saveToDb(location: Location) {
        val user = firebaseAuth.currentUser
        val relatedUser: UserProfile
        val snapshot = firestore.collection("users").document(user?.uid ?: "").get().await()

        relatedUser = snapshot.toObject(UserProfile::class.java)?: UserProfile()

        val loc = MapMarker(user?.uid ?: "", lat = location.latitude, long = location.longitude, relatedUser)
        firestore.collection("location")
            .document(loc.markerId)  // Use eventId as the document ID
            .set(loc)
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    object NoPermissionsException : Exception()

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}