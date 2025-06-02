package com.itza2k.privacyninja.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.provider.Settings
import java.net.NetworkInterface

object VpnUtils {

    // List of common VPN package names
    private val KNOWN_VPN_PACKAGES = listOf(
        "com.nordvpn.android",
        "com.expressvpn.vpn",
        "com.privateinternetaccess.android",
        "com.surfshark.vpnclient",
        "com.protonvpn.android",
        "org.torproject.android",
        "com.tunnelbear.android"
    )

    fun openVpnSettings(context: Context) {
        val intent = Intent(Settings.ACTION_VPN_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun prepareVpn(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    fun isVpnAppInstalled(context: Context): Boolean {
        val packageManager = context.packageManager

        for (packageName in KNOWN_VPN_PACKAGES) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue checking others
            }
        }

        return false
    }

    fun getInstalledVpnApps(context: Context): List<String> {
        val packageManager = context.packageManager
        val installedVpns = mutableListOf<String>()

        for (packageName in KNOWN_VPN_PACKAGES) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                installedVpns.add(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not there
            }
        }

        return installedVpns
    }

    fun openVpnApp(context: Context): Boolean {
        val installedVpns = getInstalledVpnApps(context)

        if (installedVpns.isNotEmpty()) {
            // Open the first installed VPN app
            val packageName = installedVpns.first()
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return true
            }
        }

        // If no VPN app is installed or couldn't be launched, open VPN settings
        openVpnSettings(context)
        return false
    }

    fun isVpnActive(context: Context): Boolean {
        // Method 1: Check using NetworkInterface
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

        //  6+ check
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun getRecommendedVpnApp(context: Context): String? {
        val installedVpns = getInstalledVpnApps(context)
        return installedVpns.firstOrNull()
    }
}
