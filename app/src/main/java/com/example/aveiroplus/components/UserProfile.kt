package com.example.aveiroplus.components

import java.util.UUID

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val role: String = "",
    val profileImageUrl: String = "",
    val registeredEventsIds: List<String> = emptyList(),
    val paidEventsIds: List<String> = emptyList()
)
