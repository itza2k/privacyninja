package com.itza2k.privacyninja.model

import android.graphics.drawable.Drawable

data class AppPermission(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable? = null,
    val permissions: List<Permission> = emptyList(),
    val lastUsed: Long = 0
) {
    fun hasSuspiciousPermissions(): Boolean {
        return permissions.any { it.isSuspicious }
    }

    fun getSuspiciousPermissions(): List<Permission> {
        return permissions.filter { it.isSuspicious }
    }
}

data class Permission(
    val name: String,
    val description: String,
    val isGranted: Boolean = false,
    val isSuspicious: Boolean = false,
    val category: PermissionCategory = PermissionCategory.OTHER
)
enum class PermissionCategory {
    LOCATION,
    CAMERA,
    MICROPHONE,
    CONTACTS,
    STORAGE,
    PHONE,
    SMS,
    CALENDAR,
    SENSORS,
    ACTIVITY_RECOGNITION,
    OTHER
}