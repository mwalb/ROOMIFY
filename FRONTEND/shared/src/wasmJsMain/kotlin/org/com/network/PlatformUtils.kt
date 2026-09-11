package org.com.network

actual fun getPlatformHost(): String {
    // For Web, localhost is correct if the backend is on the same machine
    return "localhost"
}
