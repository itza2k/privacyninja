package com.itza2k.privacyninja.ui.screens.stealth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itza2k.privacyninja.R
import com.itza2k.privacyninja.repository.PrivacyRepository
import com.itza2k.privacyninja.ui.theme.NinjaAccent
import com.itza2k.privacyninja.ui.theme.NinjaBlack
import com.itza2k.privacyninja.ui.theme.NinjaDarkGrey
import com.itza2k.privacyninja.ui.theme.SecureGreen
import com.itza2k.privacyninja.ui.theme.TextPrimary
import com.itza2k.privacyninja.ui.theme.TextSecondary

@Composable
fun StealthModeScreen(stealthModeService: PrivacyRepository) {
    val isStealthModeEnabled by stealthModeService.isStealthModeEnabled.collectAsState(initial = false)
    var showActivationAnimation by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NinjaBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { -50 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.stealth_mode_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Go invisible with cutting-edge privacy",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = TextSecondary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Enhanced Pulsating Circle with Glow
            val infiniteTransition = rememberInfiniteTransition()
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = { it * it * (3f - 2f * it) }),
                    repeatMode = RepeatMode.Reverse
                )
            )

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(tween(500)),
                exit = scaleOut(tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(if (isStealthModeEnabled && showActivationAnimation) 1.4f else pulseScale)
                        .clip(CircleShape)
                        .shadow(12.dp, CircleShape, ambientColor = NinjaAccent.copy(alpha = 0.3f))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    if (isStealthModeEnabled) SecureGreen else NinjaDarkGrey,
                                    NinjaBlack.copy(alpha = 0.9f)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showActivationAnimation = true
                            if (isStealthModeEnabled) {
                                stealthModeService.disableStealthMode()
                            } else {
                                stealthModeService.enableStealthMode()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isStealthModeEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = "Stealth mode toggle",
                        tint = if (isStealthModeEnabled) TextPrimary else NinjaAccent,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isStealthModeEnabled) stringResource(R.string.stealth_mode_enabled)
                else stringResource(R.string.stealth_mode_disabled),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                fontWeight = FontWeight.SemiBold,
                color = if (isStealthModeEnabled) SecureGreen else TextPrimary
            )

            Spacer(modifier = Modifier.height(40.dp))

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
                        text = stringResource(R.string.stealth_mode_description),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                        lineHeight = 26.sp
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
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Dns,
                            contentDescription = "DNS icon",
                            tint = NinjaAccent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Private DNS",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Set to Cloudflare (1.1.1.1) to encrypt DNS queries and block ads.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { stealthModeService.openPrivateDnsSettings() },
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
                            text = "Open DNS Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            Button(
                onClick = { stealthModeService.openVpnSettings() },
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
                    text = "VPN Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}