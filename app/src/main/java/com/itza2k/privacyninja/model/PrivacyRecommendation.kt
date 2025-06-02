package com.itza2k.privacyninja.model

data class PrivacyRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val actionLabel: String,
    val priority: Int = 3,
    val actionType: String = ""
)

enum class RecommendationType {
    NETWORK,
    PERMISSIONS,
    STEALTH_MODE,
    PASSWORD,
    DATA_BREACH,
    GENERAL
}

