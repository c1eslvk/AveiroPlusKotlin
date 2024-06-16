package com.example.aveiroplus

import android.os.Bundle
import android.view.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aveiroplus.components.MapMarker
import com.example.aveiroplus.components.UserProfile
import com.example.aveiroplus.databinding.MapLayoutBinding.inflate
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

val usr = UserProfile()
val mrkrs = listOf(
    MapMarker(lat = 1.35, long = 103.87, relatedUser = usr)
)
@Composable
fun MapScreen(){
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        for (marker in mrkrs) {
            CreateMarker(markerData = marker)
        }
    }
}

@Composable
fun CreateMarker(markerData: MapMarker) {
    return Marker(state = MarkerState(LatLng(markerData.lat, markerData.long)))
}
