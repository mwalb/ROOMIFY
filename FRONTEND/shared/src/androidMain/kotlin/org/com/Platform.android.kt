package org.com

import android.os.Build
import org.com.config.AppConfig

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun getPlatformBaseUrl(): String = AppConfig.REMOTE_API_BASE_URL

actual fun openVideoPlayerModal(videoUrl: String): Boolean = false

actual fun syncWebMapLayer(isMapRoute: Boolean) {}

actual fun showNativeWebMapToast(message: String): Boolean = false


