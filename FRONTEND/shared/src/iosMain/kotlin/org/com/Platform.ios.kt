package org.com

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970() * 1000).toLong()

actual fun getPlatformBaseUrl(): String = "http://localhost:8080"

actual fun openVideoPlayerModal(videoUrl: String): Boolean = false

actual fun syncWebMapLayer(isMapRoute: Boolean) {}

actual fun showNativeWebMapToast(message: String): Boolean = false


