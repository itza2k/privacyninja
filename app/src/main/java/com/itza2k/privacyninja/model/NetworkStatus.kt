package com.itza2k.privacyninja.model


data class NetworkStatus(
    val isConnected: Boolean = false,
    val ssid: String = "",
    val isSecure: Boolean = false,
    val securityType: String = "",
    val signalStrength: Int = 0,
    val ipAddress: String = "",
    val isVpnActive: Boolean = false
) {
    fun getSecurityLevel(): SecurityLevel {
        return when {
            !isConnected -> SecurityLevel.UNKNOWN
            isVpnActive -> SecurityLevel.SECURE
            isSecure -> SecurityLevel.MODERATE
            else -> SecurityLevel.INSECURE
        }
    }
}
enum class SecurityLevel {
    SECURE,
    MODERATE,
    INSECURE,
    UNKNOWN
}