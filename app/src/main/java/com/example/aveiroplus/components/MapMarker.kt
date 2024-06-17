package com.example.aveiroplus.components;

data class MapMarker(
    val markerId: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val relatedUser: UserProfile = UserProfile(),
) {}
