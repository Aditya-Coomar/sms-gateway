package com.aditya.simgateway.domain.model

data class DeviceInfo(
    val deviceId: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String
)
