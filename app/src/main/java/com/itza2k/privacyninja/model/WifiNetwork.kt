package com.itza2k.privacyninja.model


data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val capabilities: String,
    val level: Int,
    val frequency: Int,
    val isConnected: Boolean = false
) {
    fun getSecurityType(): String {
        return when {
            capabilities.contains("WPA3") -> "WPA3"
            capabilities.contains("WPA2") -> "WPA2"
            capabilities.contains("WPA") -> "WPA"
            capabilities.contains("WEP") -> "WEP"
            else -> "Open"
        }
    }

    fun isSecure(): Boolean {
        return capabilities.contains("WPA") || capabilities.contains("WEP")
    }

    fun getSecurityLevel(): SecurityLevel {
        return when {
            capabilities.contains("WPA3") -> SecurityLevel.SECURE
            capabilities.contains("WPA2") -> SecurityLevel.MODERATE
            capabilities.contains("WPA") -> SecurityLevel.MODERATE
            capabilities.contains("WEP") -> SecurityLevel.INSECURE
            else -> SecurityLevel.INSECURE
        }
    }

    fun getSignalStrengthPercent(): Int {
        // WifiManager.calculateSignalLevel normalized to 0-100 scale
        // Level ranges from -100 dBm to -55 dBm
        val minLevel = -100
        val maxLevel = -55
        val normalizedLevel = ((level - minLevel).coerceAtLeast(0) * 100) / (maxLevel - minLevel)
        return normalizedLevel.coerceAtMost(100)
    }

    fun getSignalStrengthBars(): Int {
        val percent = getSignalStrengthPercent()
        return when {
            percent >= 80 -> 4
            percent >= 60 -> 3
            percent >= 40 -> 2
            percent >= 20 -> 1
            else -> 0
        }
    }

}