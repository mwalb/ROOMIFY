package org.com.network

actual fun getPlatformHost(): String {
    // 10.0.2.2 is the special IP for Android emulators to reach the host's localhost
    return "10.0.2.2"
}
