package com.itza2k.privacyninja.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.format.Formatter
import androidx.core.content.ContextCompat
import com.itza2k.privacyninja.model.NetworkStatus
import com.itza2k.privacyninja.model.WifiNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.NetworkInterface

class NetworkMonitorService(private val context: Context) {

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatus = MutableStateFlow(NetworkStatus())
    val networkStatus: Flow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _wifiNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val wifiNetworks: Flow<List<WifiNetwork>> = _wifiNetworks.asStateFlow()

    private var isReceiverRegistered = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)

                if (success) {
                    processScanResults()
                }
            }
        }
    }

    init {
        // Register the receiver
        registerWifiScanReceiver()
        registerNetworkCallback()
    }

    private fun registerWifiScanReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)
            isReceiverRegistered = true
        }
    }

    fun unregisterWifiScanReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(wifiScanReceiver)
                isReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                updateNetworkStatus()
            }

            override fun onLost(network: android.net.Network) {
                updateNetworkStatus()
            }

            override fun onCapabilitiesChanged(
                network: android.net.Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateNetworkStatus()
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }
    private fun unregisterNetworkCallback() {
        try {
            if (networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
                networkCallback = null
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun processScanResults() {
        if (!hasLocationPermission()) {
            _wifiNetworks.update { emptyList() }
            return
        }

        try {
            val scanResults = wifiManager.scanResults ?: emptyList()
            val currentSsid = getCurrentSsid()

            val networks = scanResults.map { scanResult ->
                convertScanResultToWifiNetwork(scanResult, currentSsid == scanResult.SSID)
            }.sortedByDescending { it.level } // Sort by signal strength

            _wifiNetworks.update { networks }
        } catch (e: SecurityException) {
            _wifiNetworks.update { emptyList() }
        }
    }

    private fun convertScanResultToWifiNetwork(scanResult: ScanResult, isConnected: Boolean): WifiNetwork {
        return WifiNetwork(
            ssid = scanResult.SSID.ifEmpty { "<Hidden Network>" },
            bssid = scanResult.BSSID,
            capabilities = scanResult.capabilities,
            level = scanResult.level,
            frequency = scanResult.frequency,
            isConnected = isConnected
        )
    }

    private fun getCurrentSsid(): String {
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo?.ssid?.removeSurrounding("\"") ?: ""
    }

    fun startWifiScan(): Boolean {
        if (!hasLocationPermission()) {
            return false
        }

        registerWifiScanReceiver()
        try {
            return wifiManager.startScan()
        } catch (e: SecurityException) {
            // Handle security exception (permission denied)
            return false
        }
    }

    fun updateNetworkData() {
        updateNetworkStatus()
        startWifiScan()
    }

    fun updateNetworkStatus() {
        val currentNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork)

        if (networkCapabilities == null) {
            _networkStatus.update { NetworkStatus() } // Not connected
            return
        }

        val isWifiConnected = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        if (!isWifiConnected) {
            _networkStatus.update {
                NetworkStatus(
                    isConnected = false,
                    isVpnActive = isVpnActive()
                )
            }
            return
        }

        // Get Wi-Fi details
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.ssid.removeSurrounding("\"")
        val ipAddress = Formatter.formatIpAddress(wifiInfo.ipAddress)
        val signalStrength = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5) // 0-4 scale

        // Check security
        val isSecure = isNetworkSecure(wifiInfo)
        val securityType = getSecurityType(wifiInfo)

        _networkStatus.update {
            NetworkStatus(
                isConnected = true,
                ssid = ssid,
                isSecure = isSecure,
                securityType = securityType,
                signalStrength = signalStrength,
                ipAddress = ipAddress,
                isVpnActive = isVpnActive()
            )
        }
    }

    private fun isNetworkSecure(wifiInfo: WifiInfo): Boolean {
        val securityType = getSecurityType(wifiInfo)
        return securityType.contains("WPA2") || securityType.contains("WPA3")
    }

    private fun getSecurityType(wifiInfo: WifiInfo): String {
        return "WPA2"
    }


    private fun isVpnActive(): Boolean {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                if (networkInterface.name.startsWith("tun") || networkInterface.name.startsWith("ppp")) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun cleanup() {
        unregisterNetworkCallback()
        unregisterWifiScanReceiver()
    }
}