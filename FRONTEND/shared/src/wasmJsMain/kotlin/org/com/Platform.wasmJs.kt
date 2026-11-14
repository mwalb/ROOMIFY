package org.com

import kotlin.js.ExperimentalWasmJsInterop

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun dateNow(): Double

actual fun currentTimeMillis(): Long = dateNow().toLong()

actual fun getPlatformBaseUrl(): String {
    val hostname = kotlinx.browser.window.location.hostname
    return if (hostname == "localhost" || hostname == "127.0.0.1") {
        "http://localhost:8080"
    } else {
        "https://roomifytz-backend.fly.dev"
    }
}
