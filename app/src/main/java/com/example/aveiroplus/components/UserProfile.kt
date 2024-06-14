package com.example.aveiroplus.components

data class UserProfile(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val role: String = "",
    val profileImageUrl: String = "",
    val registeredEventsIds: List<String> = emptyList()
)
