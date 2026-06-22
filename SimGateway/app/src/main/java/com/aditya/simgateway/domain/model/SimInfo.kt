package com.aditya.simgateway.domain.model

data class SimInfo(
    val slotIndex: Int,
    val carrierName: String,
    val countryIso: String,
    val subscriptionId: Int
)
