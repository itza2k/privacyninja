package com.itza2k.privacyninja.ui.screens.permission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.itza2k.privacyninja.R
import com.itza2k.privacyninja.model.AppPermission
import com.itza2k.privacyninja.model.Permission
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
fun PermissionPatrolScreen(privacyRepository: PrivacyRepository) {
    val suspiciousApps by privacyRepository.suspiciousApps.collectAsState(initial = emptyList())
    val allApps by privacyRepository.nonEssentialPermissions.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        privacyRepository.updatePrivacyData()
    }

    // Merge suspiciousApps and allApps, removing duplicates by packageName
    val mergedApps = remember(suspiciousApps, allApps) {
        (suspiciousApps + allApps)
            .distinctBy { it.packageName }
    }

    val filteredApps = remember(mergedApps, searchQuery) {
        if (searchQuery.isEmpty()) mergedApps
        else mergedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NinjaBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(700)) + scaleIn(tween(700))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.permission_patrol_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Monitor and control app permissions",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextSecondary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .background(NinjaDarkGrey.copy(alpha = 0.95f), RoundedCornerShape(14.dp)),
                placeholder = { Text("Search apps", color = TextSecondary.copy(alpha = 0.6f)) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = NinjaAccent
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.clickable { searchQuery = "" }
                        )
                    }
                },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = NinjaDarkGrey,
                    unfocusedContainerColor = NinjaDarkGrey,
                    focusedIndicatorColor = NinjaAccent,
                    unfocusedIndicatorColor = TextSecondary.copy(alpha = 0.3f),
                    cursorColor = NinjaAccent,
                    focusedPlaceholderColor = TextSecondary,
                    unfocusedPlaceholderColor = TextSecondary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (filteredApps.isEmpty()) {
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
                            imageVector = Icons.Filled.Block,
                            contentDescription = "No apps found",
                            tint = TextSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Apps Found",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search term" else "No apps with permissions found",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredApps) { app ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(400)) + scaleIn(tween(400))
                        ) {
                            AppPermissionCard(app)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppPermissionCard(app: AppPermission) {
    var expanded by remember { mutableStateOf(false) }
    val hasSuspiciousPermissions = app.hasSuspiciousPermissions()
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) { expanded = !expanded },
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    app.appIcon?.let { drawable ->
                        androidx.compose.foundation.Image(
                            bitmap = drawable.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NinjaBlack)
                        )
                    } ?: Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = "App icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NinjaAccent.copy(alpha = 0.15f))
                            .padding(8.dp),
                        tint = NinjaAccent
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSuspiciousPermissions) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Suspicious",
                            tint = WarningYellow,
                            modifier = Modifier
                                .size(28.dp)
                                .background(WarningYellow.copy(alpha = 0.2f), CircleShape)
                                .padding(6.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = NinjaAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (app.permissions.isEmpty()) {
                        Text(
                            text = "No permissions found",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = TextSecondary,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        val permissionsByCategory = app.permissions.groupBy { it.category }
                        permissionsByCategory.forEach { (category, permissions) ->
                            Text(
                                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                            permissions.forEach { permission ->
                                PermissionItem(permission)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionItem(permission: Permission) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (permission.isSuspicious) Icons.Filled.Warning else Icons.Filled.Security,
            contentDescription = if (permission.isSuspicious) "Suspicious" else "Secure",
            tint = if (permission.isSuspicious) DangerRed else SecureGreen,
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
                    color = TextSecondary.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
            Text(
                text = if (permission.isGranted) "Granted" else "Not granted",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = if (permission.isGranted)
                    (if (permission.isSuspicious) DangerRed else SecureGreen)
                else TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

