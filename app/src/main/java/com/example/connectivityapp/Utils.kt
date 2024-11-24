package com.example.connectivityapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun Context.observeConnectivity(): Flow<String> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val networkRequest =
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
    
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val connectionType = when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                else -> "No Connection"
            }
            trySend(connectionType).isSuccess
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            trySend("No Connection").isSuccess
        }
    }
    
    connectivityManager.registerNetworkCallback(networkRequest, callback)
    
    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
    
}

fun getConnectionType(context: Context): String {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val activeNetwork = connectivityManager.activeNetwork ?: return "No Connection"
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "No Connection"
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other Connection"
        }
    } else {
        @Suppress("DEPRECATION")
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return when (activeNetworkInfo?.type) {
            ConnectivityManager.TYPE_WIFI -> "Wi-Fi"
            ConnectivityManager.TYPE_MOBILE -> "Mobile Data"
            ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
            else -> "No Connection"
        }
    }
}

suspend fun getSignalStrengthForModernAndroid(context: Context): Int? {
    val deferredSignalStrength = CompletableDeferred<Int?>()
    
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val numberOfLevels = 5
    
    var signalStrength: Int? = null
    
    val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    
    connectivityManager.registerNetworkCallback(
        networkRequest,
        object : ConnectivityManager.NetworkCallback() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val wifiInfo =
                        networkCapabilities.transportInfo as? android.net.wifi.WifiInfo
                    wifiInfo?.let {
                        signalStrength = WifiManager.calculateSignalLevel(it.rssi, numberOfLevels)
                        deferredSignalStrength.complete(signalStrength)
                        
                    }
                }
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                deferredSignalStrength.complete(null)
            }
            
        }
    )
    return deferredSignalStrength.await()
}


suspend fun getWifiSignalStrength(context: Context): Int {
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val numberOfLevels = 5
    
    val wifiInfo: WifiInfo = wifiManager.getConnectionInfo()
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return getSignalStrengthForModernAndroid(context) ?: -1
    } else {
        // For older versions
        WifiManager.calculateSignalLevel(wifiInfo.rssi, numberOfLevels)
    }
    
}