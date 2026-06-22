package com.aditya.simgateway.domain.model

data class GatewayHealth(
    val serviceRunning: Boolean,
    val uptimeSeconds: Long,
    val lastStartedAt: Long
)
