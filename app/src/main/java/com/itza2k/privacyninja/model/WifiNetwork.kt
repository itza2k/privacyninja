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

    fun getPrivacyRating(): Int {
        var rating = 0
        rating += when {
            capabilities.contains("WPA3") -> 50
            capabilities.contains("WPA2") -> 40
            capabilities.contains("WPA") -> 30
            capabilities.contains("WEP") -> 10
            else -> 0
        }
        val lowerSsid = ssid.lowercase()
        rating -= when {
            lowerSsid.contains("public") || lowerSsid.contains("free") -> 20
            lowerSsid.contains("guest") -> 15
            lowerSsid.contains("cafe") || lowerSsid.contains("coffee") -> 10
            lowerSsid.contains("airport") || lowerSsid.contains("hotel") -> 15
            else -> 0
        }

        rating += getSignalStrengthPercent() / 5

        return rating.coerceIn(0, 100)
    }

    fun getPrivacyLevel(): PrivacyLevel {
        val rating = getPrivacyRating()
        return when {
            rating >= 70 -> PrivacyLevel.SECURE
            rating >= 40 -> PrivacyLevel.WARNING
            else -> PrivacyLevel.DANGER
        }
    }

    fun getChannel(): Int {
        // 2.4GHz: channel = (freq - 2407) / 5, 5GHz: channel = (freq - 5000) / 5
        return when {
            frequency in 2412..2484 -> (frequency - 2407) / 5
            frequency in 5170..5825 -> (frequency - 5000) / 5
            else -> -1 // Unknown
        }
    }
}

