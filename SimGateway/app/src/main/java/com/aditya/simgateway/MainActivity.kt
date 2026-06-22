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
import com.aditya.simgateway.presentation.navigation.SimGatewayNavHost
import com.aditya.simgateway.services.GatewayForegroundService
import com.aditya.simgateway.ui.theme.SimGatewayTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Start the service regardless of permission result.
        // The foreground service notification is shown even without
        // POST_NOTIFICATIONS on Android 13+, but granting it ensures
        // full visibility in the notification shade.
        startGatewayService()
        requestPhoneStatePermission()
    }

    private val phoneStatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // SIM data will populate on next refresh cycle if granted.
        // No action needed on denial — SimInfoProvider gracefully returns empty list.
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
                requestPhoneStatePermission()
            } else {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        } else {
            // Pre-Android 13: no runtime permission needed for notifications
            startGatewayService()
            requestPhoneStatePermission()
        }
    }

    private fun requestPhoneStatePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            phoneStatePermissionLauncher.launch(
                Manifest.permission.READ_PHONE_STATE
            )
        }
    }

    private fun startGatewayService() {
        val intent = Intent(this, GatewayForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}