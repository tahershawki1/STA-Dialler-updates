package com.example.stadialler.model

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val extension: String? = null,
    val department: String = "General",
    val speedDialKey: Int? = null, // 1 to 9
    val isFavorite: Boolean = false,
    val email: String? = null
)
