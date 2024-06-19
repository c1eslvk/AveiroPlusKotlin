package com.example.aveiroplus.components

import java.util.UUID

data class Event(
    val eventId: String = UUID.randomUUID().toString(),
    val imageUrl: String = "",
    val eventName: String = "",
    val description: String = "",
    val availablePlaces: Int = 0,
    val registeredUsersIds: List<String> = emptyList(),
    val eventDate: Long = 0,
    val location: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val price: Double = 0.0
)

