package com.example.aveiroplus.uiStates

import com.example.aveiroplus.components.Event
import com.example.aveiroplus.components.MapMarker
import com.example.aveiroplus.components.UserProfile

data class MapUiState(
    val markers: List<MapMarker> = emptyList(),
    val yourMarker: MapMarker = MapMarker(),
    val userToShow: UserProfile = UserProfile(),
    val isInfoVisible: Boolean = false,
    val eventsToShow: List<Event> = emptyList(),
    val isMapReady: Boolean = false,
    val yourEvents: List<Event> = emptyList(),
    val eventToShow: Event = Event(),
    val eventVisibility: Boolean = false
) {
}