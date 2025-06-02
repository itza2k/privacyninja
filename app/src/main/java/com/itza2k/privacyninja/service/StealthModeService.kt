package com.itza2k.privacyninja.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.itza2k.privacyninja.model.AppPermission
import com.itza2k.privacyninja.model.PermissionCategory
import com.itza2k.privacyninja.util.VpnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class StealthModeService(
    private val context: Context,
    private val permissionMonitorService: PermissionMonitorService
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _isStealthModeEnabled = MutableStateFlow(false)
    val isStealthModeEnabled: Flow<Boolean> = _isStealthModeEnabled.asStateFlow()

    val nonEssentialPermissions = permissionMonitorService.appPermissions
        .map { appPermissions ->
            appPermissions.filter { app ->
                app.permissions.any { permission ->
                    isNonEssentialPermission(permission.category) && permission.isGranted
                }
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun enableStealthMode(): Boolean {
        val permissions = runBlocking { nonEssentialPermissions.first() }
        var allPermissionsRevoked = true

        for (app in permissions) {
            for (permission in app.permissions) {
                if (permission.isGranted && isNonEssentialPermission(permission.category)) {
                    val revoked = permissionMonitorService.revokePermission(app.packageName, permission.name)
                    if (!revoked) {
                        allPermissionsRevoked = false
                    }
                }
            }
        }

        // Automatically enable VPN
        val isVpnActive = VpnUtils.isVpnActive(context)
        val vpnEnabled = if (!isVpnActive) {
            VpnUtils.openVpnApp(context) // Attempt to open a VPN app
        } else {
            true
        }

        _isStealthModeEnabled.update { allPermissionsRevoked && vpnEnabled }
        return _isStealthModeEnabled.value
    }

    fun disableStealthMode(): Boolean {
        _isStealthModeEnabled.update { false }
        return true
    }

    fun getStealthModeStatus(): Pair<Boolean, String> {
        val isEnabled = _isStealthModeEnabled.value
        val isVpnActive = VpnUtils.isVpnActive(context)

        return when {
            isEnabled && isVpnActive -> Pair(true, "Fully enabled with VPN protection")
            isEnabled && !isVpnActive -> Pair(true, "Partially enabled (VPN inactive)")
            !isEnabled && isVpnActive -> Pair(false, "Disabled (but VPN is active)")
            else -> Pair(false, "Disabled")
        }
    }

    private fun isNonEssentialPermission(category: PermissionCategory): Boolean {
        return when (category) {
            PermissionCategory.LOCATION,
            PermissionCategory.CAMERA,
            PermissionCategory.MICROPHONE,
            PermissionCategory.CONTACTS,
            PermissionCategory.STORAGE,
            PermissionCategory.ACTIVITY_RECOGNITION -> true
            else -> false
        }
    }

    fun getNonEssentialPermissionsCount(): Int = runBlocking {
        nonEssentialPermissions.first().size
    }

    fun openVpnSettings() {
        val intent = Intent(Settings.ACTION_VPN_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun openPrivateDnsSettings() {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
