package org.com

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentTimeMillis(): Long

expect fun getPlatformBaseUrl(): String

expect fun openVideoPlayerModal(videoUrl: String): Boolean

expect fun syncWebMapLayer(isMapRoute: Boolean)

