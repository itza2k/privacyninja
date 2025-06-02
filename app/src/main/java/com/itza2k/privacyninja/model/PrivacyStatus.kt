package com.itza2k.privacyninja.model

data class PrivacyStatus(
    val networkStatus: NetworkStatus = NetworkStatus(),
    val suspiciousApps: List<AppPermission> = emptyList(),
    val isStealthModeEnabled: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun calculatePrivacyScore(): Int {
        var score = 100
        when (networkStatus.getSecurityLevel()) {
            SecurityLevel.INSECURE -> score -= 30
            SecurityLevel.MODERATE -> score -= 10
            SecurityLevel.UNKNOWN -> score -= 15
            else -> {} // No deduction for SECURE
        }
        val suspiciousAppsCount = suspiciousApps.size
        score -= minOf(suspiciousAppsCount * 5, 40) // Max deduction of 40 points
        if (isStealthModeEnabled) {
            score += 10
        }

        return maxOf(0, minOf(score, 100)) // Ensure score is between 0 and 100
    }
}