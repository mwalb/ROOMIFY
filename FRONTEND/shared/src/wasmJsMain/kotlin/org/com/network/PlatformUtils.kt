package org.com.network

import kotlinx.browser.window

actual fun getPlatformBaseUrl(): String {
    val hostname = window.location.hostname

    return if (hostname == "localhost" || hostname == "127.0.0.1") {
        "http://localhost:8080"
    } else {
        "https://roomifytz-backend.fly.dev"
    }
}
