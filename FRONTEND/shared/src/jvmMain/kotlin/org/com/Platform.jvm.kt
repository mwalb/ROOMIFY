package org.com

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun getPlatformBaseUrl(): String = "http://localhost:8080"

actual fun openVideoPlayerModal(videoUrl: String): Boolean = false

actual fun syncWebMapLayer(isMapRoute: Boolean) {}

actual fun showNativeWebMapToast(message: String): Boolean = false


