package com.aditya.simgateway.domain.model

data class NetworkInfo(
    val connected: Boolean,
    val networkType: String,
    val metered: Boolean
)
