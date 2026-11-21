package org.com

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun getPlatformBaseUrl(): String = "http://10.0.2.2:8080"

actual fun openVideoPlayerModal(videoUrl: String): Boolean = false
