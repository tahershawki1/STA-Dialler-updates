package com.example.stadialler.model

enum class CallType {
    OUTGOING,
    INCOMING,
    MISSED,
    BLOCKED
}

data class CallRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val number: String,
    val contactName: String? = null,
    val type: CallType,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val carrierOrLine: String = "STA-Secure Line 1",
    val notes: String? = null
)
