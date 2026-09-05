package org.com.ui

import kotlinx.serialization.Serializable
@Serializable
data class SplashConfig(
    val appName: String,
    val tagline: String,
    val primaryColor: String,
    val successColor: String,
    val duration: Int )