package com.aditya.simgateway

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.aditya.simgateway.core.diagnostics.EventLogger
import com.aditya.simgateway.presentation.navigation.SimGatewayNavHost
import com.aditya.simgateway.services.GatewayForegroundService
import com.aditya.simgateway.ui.theme.SimGatewayTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        EventLogger.logInfo(
            source = "MainActivity",
            message = "Notification permission flow completed"
        )
        startGatewayService()
        requestRuntimePermissions()
    }

    private val runtimePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        EventLogger.logInfo(
            source = "MainActivity",
            message = "Runtime permission flow completed",
            payload = permissions.entries.joinToString { "${it.key}=${it.value}" }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionsAndStartService()

        setContent {
            SimGatewayTheme {
                SimGatewayNavHost()
            }
        }
    }

    private fun requestPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startGatewayService()
                requestRuntimePermissions()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startGatewayService()
            requestRuntimePermissions()
        }
    }

    private fun requestRuntimePermissions() {
        val missingPermissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
        ).filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            EventLogger.logInfo(
                source = "MainActivity",
                message = "Requesting runtime permissions",
                payload = missingPermissions.joinToString()
            )
            runtimePermissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startGatewayService() {
        val intent = Intent(this, GatewayForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
