package com.itza2k.privacyninja.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itza2k.privacyninja.model.PrivacyRecommendation
import com.itza2k.privacyninja.model.RecommendationType

@Composable
fun RecommendationCard(
    recommendation: PrivacyRecommendation,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on recommendation type
            Icon(
                imageVector = getIconForRecommendationType(recommendation.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { onActionClick(recommendation.actionType) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(recommendation.actionLabel)
                }
            }
        }
    }
}

@Composable
fun getIconForRecommendationType(type: RecommendationType): ImageVector {
    return when (type) {
        RecommendationType.NETWORK -> Icons.Filled.Wifi
        RecommendationType.PERMISSIONS -> Icons.Filled.Security
        RecommendationType.STEALTH_MODE -> Icons.Filled.Lock
        RecommendationType.PASSWORD -> Icons.Filled.Lock
        RecommendationType.DATA_BREACH -> Icons.Filled.Warning
        RecommendationType.GENERAL -> Icons.Filled.Security
    }
}