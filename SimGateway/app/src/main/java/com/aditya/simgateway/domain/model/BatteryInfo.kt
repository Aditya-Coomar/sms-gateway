package com.aditya.simgateway.domain.model

data class BatteryInfo(
    val level: Int,
    val charging: Boolean,
    val powerSource: String
)
