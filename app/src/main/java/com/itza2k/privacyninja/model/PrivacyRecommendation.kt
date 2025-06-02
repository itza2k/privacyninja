package com.itza2k.privacyninja.model

data class PrivacyRecommendation(
    val title: String,
    val description: String,
    val actionLabel: String,
    val priority: Int = 3,
    val actionType: String = ""
)

