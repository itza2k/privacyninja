package com.itza2k.privacyninja.ui.screens.network

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itza2k.privacyninja.R
import com.itza2k.privacyninja.model.NetworkStatus
import com.itza2k.privacyninja.model.PrivacyLevel
import com.itza2k.privacyninja.model.SecurityLevel
import com.itza2k.privacyninja.model.WifiNetwork
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
fun NetworkNinjaScreen(privacyRepository: PrivacyRepository) {
    val networkStatus by privacyRepository.networkStatus.collectAsState(initial = NetworkStatus())
    val wifiNetworks by privacyRepository.wifiNetworks.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(privacyRepository.hasLocationPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        hasLocationPermission = allGranted
        if (allGranted) {
            isScanning = true
            privacyRepository.startWifiScan()
        }
    }

    LaunchedEffect(Unit) {
        privacyRepository.updateNetworkData()
    }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            kotlinx.coroutines.delay(2000)
            isScanning = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NinjaBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween<Float>(700)) + scaleIn(tween<Float>(700))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.network_ninja_title),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Text(
                            text = "Secure your network connections",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = TextSecondary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                                imageVector = if (networkStatus.isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                                contentDescription = if (networkStatus.isConnected) "Connected" else "Disconnected",
                                tint = when (networkStatus.getSecurityLevel()) {
                                    SecurityLevel.SECURE -> SecureGreen
                                    SecurityLevel.MODERATE -> WarningYellow
                                    SecurityLevel.INSECURE -> DangerRed
                                    SecurityLevel.UNKNOWN -> TextSecondary
                                },
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (networkStatus.isConnected) {
                                    val displaySsid = if (networkStatus.ssid.isBlank() || networkStatus.ssid.equals("<unknown ssid>", ignoreCase = true)) "Connected" else networkStatus.ssid
                                    displaySsid
                                } else "Not Connected",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        if (networkStatus.isConnected) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (networkStatus.isSecure) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                    contentDescription = if (networkStatus.isSecure) "Secure" else "Insecure",
                                    tint = if (networkStatus.isSecure) SecureGreen else DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = networkStatus.securityType,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                    color = TextSecondary
                                )
                                if (networkStatus.isVpnActive) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "VPN Active",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                        color = SecureGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { privacyRepository.openVpnSettings() },
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
                                    text = if (networkStatus.isVpnActive) stringResource(R.string.disable_vpn)
                                    else stringResource(R.string.enable_vpn),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearby Networks",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = NinjaAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasLocationPermission) {
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
                                text = "Location Permission Needed",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Allow location access to scan for WiFi networks.",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
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
                                    text = "Grant Permission",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Or go to Settings",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                modifier = Modifier.clickable {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", context.packageName, null)
                                    intent.data = uri
                                    context.startActivity(intent)
                                },
                                color = NinjaAccent
                            )
                        }
                    }
                } else if (wifiNetworks.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = NinjaDarkGrey.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = "No networks",
                                tint = TextSecondary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Networks Found",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap the refresh button to scan again.",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                lineHeight = 24.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(wifiNetworks) { index, network ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween<Float>(400)) + scaleIn(tween<Float>(400))
                            ) {
                                WifiNetworkCard(network)
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        isScanning = true
                        privacyRepository.startWifiScan()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .shadow(8.dp, CircleShape),
                containerColor = NinjaAccent,
                contentColor = TextPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh networks",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun WifiNetworkCard(network: WifiNetwork) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (network.getSecurityLevel()) {
                                    SecurityLevel.SECURE -> SecureGreen.copy(alpha = 0.15f)
                                    SecurityLevel.MODERATE -> WarningYellow.copy(alpha = 0.15f)
                                    SecurityLevel.INSECURE -> DangerRed.copy(alpha = 0.15f)
                                    SecurityLevel.UNKNOWN -> TextSecondary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = "Signal strength",
                            tint = when (network.getSecurityLevel()) {
                                SecurityLevel.SECURE -> SecureGreen
                                SecurityLevel.MODERATE -> WarningYellow
                                SecurityLevel.INSECURE -> DangerRed
                                SecurityLevel.UNKNOWN -> TextSecondary
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = network.ssid,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                            fontWeight = if (network.isConnected) FontWeight.Bold else FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (network.isConnected) "Connected" else network.getSecurityType(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = if (network.isConnected) SecureGreen else TextSecondary
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${network.getPrivacyRating()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = when (network.getPrivacyLevel()) {
                                PrivacyLevel.SECURE -> SecureGreen
                                PrivacyLevel.WARNING -> WarningYellow
                                PrivacyLevel.DANGER -> DangerRed
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = if (network.isSecure()) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = if (network.isSecure()) "Secure" else "Insecure",
                            tint = when (network.getPrivacyLevel()) {
                                PrivacyLevel.SECURE -> SecureGreen
                                PrivacyLevel.WARNING -> WarningYellow
                                PrivacyLevel.DANGER -> DangerRed
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween<Float>(300)) + slideInVertically(tween<IntOffset>(300)),
                exit = fadeOut(tween<Float>(300))
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Signal Strength",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        LinearProgressIndicator(
                            progress = { network.getSignalStrengthPercent() / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = when {
                                network.getSignalStrengthPercent() > 70 -> SecureGreen
                                network.getSignalStrengthPercent() > 40 -> WarningYellow
                                else -> DangerRed
                            },
                            trackColor = NinjaBlack.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "${network.getSignalStrengthPercent()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privacy Rating",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        LinearProgressIndicator(
                            progress = { network.getPrivacyRating() / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = when (network.getPrivacyLevel()) {
                                PrivacyLevel.SECURE -> SecureGreen
                                PrivacyLevel.WARNING -> WarningYellow
                                PrivacyLevel.DANGER -> DangerRed
                            },
                            trackColor = NinjaBlack.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "${network.getPrivacyRating()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailRow("Security Type", network.getSecurityType())
                    DetailRow("Frequency", "${network.frequency} MHz")
                    DetailRow("Channel", if (network.getChannel() > 0) network.getChannel().toString() else "Unknown")
                    DetailRow("BSSID", network.bssid)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Privacy Assessment",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (network.getPrivacyLevel()) {
                            PrivacyLevel.SECURE -> "This network uses ${network.getSecurityType()} encryption, providing strong protection."
                            PrivacyLevel.WARNING -> "This network uses ${network.getSecurityType()} encryption but may be vulnerable to certain attacks."
                            PrivacyLevel.DANGER -> if (network.isSecure()) {
                                "This network uses outdated ${network.getSecurityType()} encryption, which is not secure."
                            } else {
                                "This network has no encryption. Your data can be easily intercepted."
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )
                    if (network.getPrivacyLevel() != PrivacyLevel.SECURE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Recommendation: Use a VPN for added security.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = if (network.getPrivacyLevel() == PrivacyLevel.DANGER) DangerRed else WarningYellow
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = TextPrimary
        )
    }
}

