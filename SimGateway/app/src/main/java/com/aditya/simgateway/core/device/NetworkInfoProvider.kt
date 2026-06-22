package com.aditya.simgateway.core.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.aditya.simgateway.domain.model.NetworkInfo

class NetworkInfoProvider {

    fun getNetworkInfo(context: Context): NetworkInfo {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }

        val connected = capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val networkType = when {
            capabilities == null -> "NONE"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "OTHER"
        }

        val metered = connectivityManager.isActiveNetworkMetered

        return NetworkInfo(
            connected = connected,
            networkType = networkType,
            metered = metered
        )
    }
}
