package com.itza2k.privacyninja.service

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.itza2k.privacyninja.model.AppPermission
import com.itza2k.privacyninja.model.Permission
import com.itza2k.privacyninja.model.PermissionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class PermissionMonitorService(private val context: Context) {
    
    private val packageManager = context.packageManager
    private val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    private val _appPermissions = MutableStateFlow<List<AppPermission>>(emptyList())
    val appPermissions: Flow<List<AppPermission>> = _appPermissions.asStateFlow()
    
    private val _suspiciousApps = MutableStateFlow<List<AppPermission>>(emptyList())
    val suspiciousApps: Flow<List<AppPermission>> = _suspiciousApps.asStateFlow()

    fun updateAppPermissions() {
        val installedApps = getInstalledApps()
        val appPermissionsList = mutableListOf<AppPermission>()
        
        for (appInfo in installedApps) {
            val packageName = appInfo.packageName
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(appInfo)
            
            val permissions = getAppPermissions(packageName)
            val lastUsed = getAppLastUsedTime(packageName)
            
            val appPermission = AppPermission(
                packageName = packageName,
                appName = appName,
                appIcon = appIcon,
                permissions = permissions,
                lastUsed = lastUsed
            )
            
            appPermissionsList.add(appPermission)
        }
        
        _appPermissions.update { appPermissionsList }
        updateSuspiciousApps()
    }
    

    private fun updateSuspiciousApps() {
        val suspicious = _appPermissions.value.filter { it.hasSuspiciousPermissions() }
        _suspiciousApps.update { suspicious }
    }
    private fun getInstalledApps(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { !isSystemApp(it) }
    }
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    private fun getAppPermissions(packageName: String): List<Permission> {
        val permissions = mutableListOf<Permission>()
        
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val requestedPermissions = packageInfo.requestedPermissions ?: return permissions
            
            for (i in requestedPermissions.indices) {
                val permName = requestedPermissions[i]
                val permInfo = try {
                    packageManager.getPermissionInfo(permName, 0)
                } catch (e: Exception) {
                    continue
                }
                
                val isGranted = isPermissionGranted(packageName, permName)
                val category = categorizePermission(permName)
                val isSuspicious = isSuspiciousPermission(packageName, permName, category)
                
                permissions.add(
                    Permission(
                        name = permName,
                        description = permInfo.loadDescription(packageManager)?.toString() ?: "",
                        isGranted = isGranted,
                        isSuspicious = isSuspicious,
                        category = category
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return permissions
    }
    private fun isPermissionGranted(packageName: String, permissionName: String): Boolean {
        return try {
            packageManager.checkPermission(permissionName, packageName) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    private fun categorizePermission(permissionName: String): PermissionCategory {
        return when {
            permissionName.contains("LOCATION") -> PermissionCategory.LOCATION
            permissionName.contains("CAMERA") -> PermissionCategory.CAMERA
            permissionName.contains("MICROPHONE") || permissionName.contains("RECORD_AUDIO") -> PermissionCategory.MICROPHONE
            permissionName.contains("CONTACTS") -> PermissionCategory.CONTACTS
            permissionName.contains("STORAGE") || permissionName.contains("EXTERNAL_STORAGE") -> PermissionCategory.STORAGE
            permissionName.contains("PHONE") -> PermissionCategory.PHONE
            permissionName.contains("SMS") -> PermissionCategory.SMS
            permissionName.contains("CALENDAR") -> PermissionCategory.CALENDAR
            permissionName.contains("SENSOR") -> PermissionCategory.SENSORS
            permissionName.contains("ACTIVITY_RECOGNITION") -> PermissionCategory.ACTIVITY_RECOGNITION
            else -> PermissionCategory.OTHER
        }
    }

    private fun isSuspiciousPermission(packageName: String, permissionName: String, category: PermissionCategory): Boolean {
        if (packageName.contains("calculator") && category == PermissionCategory.LOCATION) {
            return true
        }
        if (packageName.contains("flashlight") && category == PermissionCategory.CONTACTS) {
            return true
        }
        
        return false
    }

    private fun getAppLastUsedTime(packageName: String): Long {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(7) // Last 7 days
        
        try {
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            
            for (stat in usageStats) {
                if (stat.packageName == packageName) {
                    return stat.lastTimeUsed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return 0
    }

    fun revokePermission(packageName: String, permissionName: String): Boolean {
        return true
    }
}