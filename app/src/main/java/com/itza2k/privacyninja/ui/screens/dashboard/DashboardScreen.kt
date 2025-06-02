package com.itza2k.privacyninja.ui.screens.dashboard

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.itza2k.privacyninja.R
import com.itza2k.privacyninja.model.AppPermission
import com.itza2k.privacyninja.model.PrivacyLevel
import com.itza2k.privacyninja.model.SecurityLevel
import com.itza2k.privacyninja.repository.PrivacyRepository
import com.itza2k.privacyninja.ui.theme.DangerRed
import com.itza2k.privacyninja.ui.theme.NinjaAccent
import com.itza2k.privacyninja.ui.theme.NinjaBlack
import com.itza2k.privacyninja.ui.theme.NinjaDarkGrey
import com.itza2k.privacyninja.ui.theme.SecureGreen
import com.itza2k.privacyninja.ui.theme.TextPrimary
import com.itza2k.privacyninja.ui.theme.TextSecondary
import com.itza2k.privacyninja.ui.theme.WarningYellow

@Composable
fun DashboardScreen(
    privacyRepository: PrivacyRepository,
    onNavigateToPermissionPatrol: () -> Unit = {}
) {
    val privacyStatus by privacyRepository.privacyStatus.collectAsState()
    val networkStatus by privacyRepository.networkStatus.collectAsState(initial = null)
    val suspiciousApps by privacyRepository.suspiciousApps.collectAsState(initial = emptyList())
    val isStealthModeEnabled by privacyRepository.isStealthModeEnabled.collectAsState(initial = false)
    val allApps by privacyRepository.nonEssentialPermissions.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showAllApps by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        privacyRepository.updatePrivacyData()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NinjaBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween<Float>(700)) + scaleIn(tween<Float>(700))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextSecondary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showAllApps) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Apps and Permissions",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hide Apps",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = NinjaAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showAllApps = false }
                            .padding(8.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(allApps) { app ->
                        AppPermissionCard(app, privacyRepository)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    PrivacyScoreCard(
                        privacyStatus.calculatePrivacyScore(),
                        privacyLevel = privacyStatus.getPrivacyLevel(),
                        privacyRepository,
                        context
                    )
                    PrivacyStatsCard(
                        suspiciousAppsCount = suspiciousApps.size,
                        totalAppsCount = allApps.size,
                        isVpnActive = networkStatus?.isVpnActive ?: false,
                        isStealthModeEnabled = isStealthModeEnabled
                    )
                    networkStatus?.let {
                        NetworkStatusCard(it.getSecurityLevel(), it.ssid, it.isVpnActive)
                    }
                    SuspiciousAppsCard(suspiciousApps.size)
                    AppsOverviewCard(suspiciousApps.size, allApps.size) {
                        onNavigateToPermissionPatrol()
                    }
                    StealthModeCard(isStealthModeEnabled)
                }
            }
        }
    }
}

@Composable
fun AppsOverviewCard(suspiciousAppsCount: Int, totalAppsCount: Int, onViewAllClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = null,
                    tint = NinjaAccent,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "App Permissions",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Total Apps: $totalAppsCount",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = TextSecondary
            )
            Text(
                text = "Suspicious Apps: $suspiciousAppsCount",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = if (suspiciousAppsCount > 0) DangerRed else SecureGreen
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "View All Apps",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = NinjaAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onViewAllClick() }
                    .padding(12.dp)
                    .fillMaxWidth()
                    .background(NinjaDarkGrey.copy(alpha = 0.3f)),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AppPermissionCard(app: AppPermission, privacyRepository: PrivacyRepository) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    app.appIcon?.let { drawable ->
                        androidx.compose.foundation.Image(
                            bitmap = drawable.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NinjaBlack)
                        )
                    } ?: Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NinjaAccent.copy(alpha = 0.15f))
                            .padding(8.dp),
                        tint = NinjaAccent
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${app.permissions.size} permissions",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = TextSecondary
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = NinjaAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween<Float>(300)) + slideInVertically(tween<IntOffset>(300)),
                exit = fadeOut(tween<Float>(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = NinjaBlack.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    app.permissions.forEach { permission ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (permission.isSuspicious) DangerRed else NinjaAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = permission.name.split(".").last(),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                    fontWeight = if (permission.isSuspicious) FontWeight.SemiBold else FontWeight.Normal,
                                    color = TextPrimary
                                )
                                if (permission.description.isNotEmpty()) {
                                    Text(
                                        text = permission.description,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyScoreCard(
    score: Int,
    privacyLevel: PrivacyLevel,
    privacyRepository: PrivacyRepository,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            when (privacyLevel) {
                                PrivacyLevel.SECURE -> SecureGreen.copy(alpha = 0.3f)
                                PrivacyLevel.WARNING -> WarningYellow.copy(alpha = 0.3f)
                                PrivacyLevel.DANGER -> DangerRed.copy(alpha = 0.3f)
                            },
                            NinjaDarkGrey.copy(alpha = 0.95f)
                        ),
                        radius = 600f
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.privacy_score),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(NinjaBlack.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.size(120.dp),
                        color = when (privacyLevel) {
                            PrivacyLevel.SECURE -> SecureGreen
                            PrivacyLevel.WARNING -> WarningYellow
                            PrivacyLevel.DANGER -> DangerRed
                        },
                        trackColor = NinjaBlack,
                        strokeWidth = 10.dp
                    )
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        fontWeight = FontWeight.Bold,
                        color = when (privacyLevel) {
                            PrivacyLevel.SECURE -> SecureGreen
                            PrivacyLevel.WARNING -> WarningYellow
                            PrivacyLevel.DANGER -> DangerRed
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        when (privacyLevel) {
                            PrivacyLevel.SECURE -> R.string.status_secure
                            PrivacyLevel.WARNING -> R.string.status_warning
                            PrivacyLevel.DANGER -> R.string.status_danger
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = when (privacyLevel) {
                        PrivacyLevel.SECURE -> SecureGreen
                        PrivacyLevel.WARNING -> WarningYellow
                        PrivacyLevel.DANGER -> DangerRed
                    },
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                val recommendation = when {
                    score < 40 -> "ENABLE_VPN"
                    score < 60 -> "REVIEW_PERMISSIONS"
                    score < 80 -> "ENABLE_STEALTH_MODE"
                    else -> null
                }
                recommendation?.let { action ->
                    Button(
                        onClick = { handleRecommendationAction(action, privacyRepository, context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NinjaAccent, NinjaAccent.copy(alpha = 0.7f))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NinjaAccent,
                            contentColor = TextPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = when (action) {
                                "ENABLE_VPN" -> "Activate VPN"
                                "REVIEW_PERMISSIONS" -> "Review App Permissions"
                                "ENABLE_STEALTH_MODE" -> "Enable Stealth Mode"
                                else -> "Take Action"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkStatusCard(securityLevel: SecurityLevel, ssid: String, isVpnActive: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Wifi,
                contentDescription = null,
                tint = when (securityLevel) {
                    SecurityLevel.SECURE -> SecureGreen
                    SecurityLevel.MODERATE -> WarningYellow
                    SecurityLevel.INSECURE -> DangerRed
                    SecurityLevel.UNKNOWN -> TextSecondary
                },
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.network_ninja_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (securityLevel != SecurityLevel.UNKNOWN) {
                    Text(
                        text = ssid,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = when (securityLevel) {
                            SecurityLevel.SECURE -> stringResource(R.string.wifi_secure)
                            SecurityLevel.MODERATE -> stringResource(R.string.wifi_secure)
                            SecurityLevel.INSECURE -> stringResource(R.string.wifi_unsecure)
                            SecurityLevel.UNKNOWN -> stringResource(R.string.wifi_not_connected)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = when (securityLevel) {
                            SecurityLevel.SECURE -> SecureGreen
                            SecurityLevel.MODERATE -> WarningYellow
                            SecurityLevel.INSECURE -> DangerRed
                            SecurityLevel.UNKNOWN -> TextSecondary
                        }
                    )
                    if (isVpnActive) {
                        Text(
                            text = "VPN Active",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = SecureGreen
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.wifi_not_connected),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SuspiciousAppsCard(suspiciousAppsCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = if (suspiciousAppsCount > 0) DangerRed else SecureGreen,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.permission_patrol_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = if (suspiciousAppsCount > 0) {
                        stringResource(R.string.suspicious_apps) + ": $suspiciousAppsCount"
                    } else {
                        stringResource(R.string.no_suspicious_apps)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = if (suspiciousAppsCount > 0) DangerRed else SecureGreen
                )
            }
        }
    }
}

@Composable
fun StealthModeCard(isEnabled: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = if (isEnabled) SecureGreen else TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.stealth_mode_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = if (isEnabled) {
                        stringResource(R.string.stealth_mode_enabled)
                    } else {
                        stringResource(R.string.stealth_mode_disabled)
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    color = if (isEnabled) SecureGreen else TextSecondary
                )
            }
        }
    }
}

@Composable
fun PrivacyStatsCard(
    suspiciousAppsCount: Int,
    totalAppsCount: Int,
    isVpnActive: Boolean,
    isStealthModeEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Privacy Stats",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = null,
                        tint = if (suspiciousAppsCount > 0) WarningYellow else SecureGreen,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$totalAppsCount",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Apps",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = TextSecondary
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = if (suspiciousAppsCount > 0) DangerRed else SecureGreen,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$suspiciousAppsCount",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (suspiciousAppsCount > 0) DangerRed else SecureGreen
                    )
                    Text(
                        text = "Suspicious",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = TextSecondary
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = if (isVpnActive) SecureGreen else WarningYellow,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isVpnActive) "On" else "Off",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isVpnActive) SecureGreen else WarningYellow
                    )
                    Text(
                        text = "VPN",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = TextSecondary
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (isStealthModeEnabled) SecureGreen else WarningYellow,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isStealthModeEnabled) "On" else "Off",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isStealthModeEnabled) SecureGreen else WarningYellow
                    )
                    Text(
                        text = "Stealth",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private fun handleRecommendationAction(
    actionType: String,
    privacyRepository: PrivacyRepository,
    context: Context
) {
    when (actionType) {
        "ENABLE_VPN" -> {
            if (!privacyRepository.openVpnApp()) {
                privacyRepository.openVpnSettings()
            }
        }
        "REVIEW_PERMISSIONS" -> {
            Toast.makeText(context, "Navigate to Permission Patrol", Toast.LENGTH_SHORT).show()
        }
        "ENABLE_STEALTH_MODE" -> {
            privacyRepository.enableStealthMode()
        }
        "PASSWORD_TIPS" -> {
            Toast.makeText(
                context,
                "Use a password manager and unique passwords for each account",
                Toast.LENGTH_LONG
            ).show()
        }
        "DATA_BREACH_CHECK" -> {
            Toast.makeText(
                context,
                "Check haveibeenpwned.com to see if your accounts have been compromised",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}