package com.aditya.simgateway.core.device

import com.aditya.simgateway.domain.model.GatewayHealth

object GatewayHealthProvider {

    private var serviceRunning: Boolean = false
    private var lastStartedAt: Long = 0L

    fun onServiceStarted() {
        serviceRunning = true
        lastStartedAt = System.currentTimeMillis()
    }

    fun onServiceStopped() {
        serviceRunning = false
    }

    fun getHealth(): GatewayHealth {
        val uptimeSeconds = if (serviceRunning && lastStartedAt > 0) {
            (System.currentTimeMillis() - lastStartedAt) / 1000
        } else {
            0L
        }

        return GatewayHealth(
            serviceRunning = serviceRunning,
            uptimeSeconds = uptimeSeconds,
            lastStartedAt = lastStartedAt
        )
    }
}
