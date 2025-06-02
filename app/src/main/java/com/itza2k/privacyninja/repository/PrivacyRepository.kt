package com.itza2k.privacyninja.repository

import android.content.Context
import com.itza2k.privacyninja.model.AppPermission
import com.itza2k.privacyninja.model.NetworkStatus
import com.itza2k.privacyninja.model.PrivacyRecommendation
import com.itza2k.privacyninja.model.PrivacyStatus
import com.itza2k.privacyninja.model.RecommendationType
import com.itza2k.privacyninja.model.SecurityLevel
import com.itza2k.privacyninja.model.WifiNetwork
import com.itza2k.privacyninja.service.NetworkMonitorService
import com.itza2k.privacyninja.service.PermissionMonitorService
import com.itza2k.privacyninja.service.StealthModeService
import com.itza2k.privacyninja.util.VpnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrivacyRepository(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Services
    private val networkMonitorService = NetworkMonitorService(context)
    private val permissionMonitorService = PermissionMonitorService(context)
    private val stealthModeService = StealthModeService(context, permissionMonitorService)

    // State flows
    private val _privacyStatus = MutableStateFlow(PrivacyStatus())
    val privacyStatus: StateFlow<PrivacyStatus> = _privacyStatus.asStateFlow()

    private val _privacyRecommendations = MutableStateFlow<List<PrivacyRecommendation>>(emptyList())
    val privacyRecommendations: StateFlow<List<PrivacyRecommendation>> = _privacyRecommendations.asStateFlow()

    // Combined flows
    val networkStatus: Flow<NetworkStatus> = networkMonitorService.networkStatus
    val wifiNetworks: Flow<List<WifiNetwork>> = networkMonitorService.wifiNetworks
    val suspiciousApps: Flow<List<AppPermission>> = permissionMonitorService.suspiciousApps
    val isStealthModeEnabled: Flow<Boolean> = stealthModeService.isStealthModeEnabled
    val nonEssentialPermissions: Flow<List<AppPermission>> = stealthModeService.nonEssentialPermissions

    init {
        // Combine flows to update privacy status
        coroutineScope.launch {
            combine(
                networkMonitorService.networkStatus,
                permissionMonitorService.suspiciousApps,
                stealthModeService.isStealthModeEnabled
            ) { networkStatus, suspiciousApps, isStealthModeEnabled ->
                PrivacyStatus(
                    networkStatus = networkStatus,
                    suspiciousApps = suspiciousApps,
                    isStealthModeEnabled = isStealthModeEnabled
                )
            }.collect { status ->
                _privacyStatus.update { status }
            }
        }

        // Generate recommendations based on privacy status
        coroutineScope.launch {
            combine(
                networkStatus,
                suspiciousApps,
                isStealthModeEnabled
            ) { networkStatus, suspiciousApps, isStealthModeEnabled ->
                generateRecommendations(networkStatus, suspiciousApps, isStealthModeEnabled)
            }.collect { recommendations ->
                _privacyRecommendations.update { recommendations }
            }
        }
    }

    fun updatePrivacyData() {
        coroutineScope.launch {
            networkMonitorService.updateNetworkStatus()
            permissionMonitorService.updateAppPermissions()
            updatePrivacyRecommendations()
        }
    }

    private fun updatePrivacyRecommendations() {
        coroutineScope.launch {
            val currentNetworkStatus = networkStatus.first()
            val currentSuspiciousApps = suspiciousApps.first()
            val currentStealthMode = isStealthModeEnabled.first()

            val recommendations = generateRecommendations(
                currentNetworkStatus,
                currentSuspiciousApps,
                currentStealthMode
            )

            _privacyRecommendations.update { recommendations }
        }
    }

    private fun generateRecommendations(
        networkStatus: NetworkStatus,
        suspiciousApps: List<AppPermission>,
        isStealthModeEnabled: Boolean
    ): List<PrivacyRecommendation> {
        val recommendations = mutableListOf<PrivacyRecommendation>()

        // Network recommendations
        when (networkStatus.getSecurityLevel()) {
            SecurityLevel.INSECURE -> {
                recommendations.add(
                    PrivacyRecommendation(
                        type = RecommendationType.NETWORK,
                        title = "Insecure Network Detected",
                        description = "You're connected to an insecure network. Enable VPN for protection.",
                        actionLabel = "Enable VPN",
                        priority = 1,
                        actionType = "ENABLE_VPN"
                    )
                )
            }
            SecurityLevel.MODERATE -> {
                if (!networkStatus.isVpnActive) {
                    recommendations.add(
                        PrivacyRecommendation(
                            type = RecommendationType.NETWORK,
                            title = "Enhance Network Security",
                            description = "Your network is secure, but using a VPN would provide additional protection.",
                            actionLabel = "Use VPN",
                            priority = 3,
                            actionType = "ENABLE_VPN"
                        )
                    )
                }
            }
            else -> {}
        }

        // App permission recommendations
        if (suspiciousApps.isNotEmpty()) {
            recommendations.add(
                PrivacyRecommendation(
                    type = RecommendationType.PERMISSIONS,
                    title = "Suspicious App Permissions",
                    description = "You have ${suspiciousApps.size} apps with suspicious permissions.",
                    actionLabel = "Review Permissions",
                    priority = 2,
                    actionType = "REVIEW_PERMISSIONS"
                )
            )
        }

        // Stealth mode recommendations
        if (!isStealthModeEnabled) {
            recommendations.add(
                PrivacyRecommendation(
                    type = RecommendationType.STEALTH_MODE,
                    title = "Enable Stealth Mode",
                    description = "Stealth Mode can protect your privacy by disabling non-essential permissions and enabling VPN.",
                    actionLabel = "Enable Stealth Mode",
                    priority = 2,
                    actionType = "ENABLE_STEALTH_MODE"
                )
            )
        }

        // Password security recommendation
        recommendations.add(
            PrivacyRecommendation(
                type = RecommendationType.PASSWORD,
                title = "Secure Your Passwords",
                description = "Using a password manager helps protect your online accounts.",
                actionLabel = "Learn More",
                priority = 4,
                actionType = "PASSWORD_TIPS"
            )
        )

        // Data breach awareness
        recommendations.add(
            PrivacyRecommendation(
                type = RecommendationType.DATA_BREACH,
                title = "Check for Data Breaches",
                description = "Regularly check if your accounts have been involved in data breaches.",
                actionLabel = "Learn How",
                priority = 3,
                actionType = "DATA_BREACH_CHECK"
            )
        )

        return recommendations.sortedBy { it.priority }
    }

    fun enableStealthMode(): Boolean {
        return stealthModeService.enableStealthMode()
    }

    fun disableStealthMode(): Boolean {
        return stealthModeService.disableStealthMode()
    }

    fun getStealthModeStatus(): Pair<Boolean, String> {
        return stealthModeService.getStealthModeStatus()
    }

    fun openVpnSettings() {
        stealthModeService.openVpnSettings()
    }

    fun openPrivateDnsSettings() {
        stealthModeService.openPrivateDnsSettings()
    }

    fun openVpnApp(): Boolean {
        return VpnUtils.openVpnApp(context)
    }

    fun isVpnActive(): Boolean {
        return VpnUtils.isVpnActive(context)
    }

    fun getInstalledVpnApps(): List<String> {
        return VpnUtils.getInstalledVpnApps(context)
    }

    fun getRecommendedVpnApp(): String? {
        return VpnUtils.getRecommendedVpnApp(context)
    }

    fun revokePermission(packageName: String, permissionName: String): Boolean {
        return permissionMonitorService.revokePermission(packageName, permissionName)
    }

    fun getNonEssentialPermissionsCount(): Int {
        return stealthModeService.getNonEssentialPermissionsCount()
    }

    fun startWifiScan(): Boolean {
        return networkMonitorService.startWifiScan()
    }

    fun updateNetworkData() {
        networkMonitorService.updateNetworkData()
    }

    fun hasLocationPermission(): Boolean {
        return networkMonitorService.hasLocationPermission()
    }
}
