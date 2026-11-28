package org.com.config

/**
 * SINGLE SOURCE OF TRUTH for Roomify Frontend Configuration.
 *
 * Changing [REMOTE_API_BASE_URL] here automatically updates:
 * 1. Kotlin/Wasm HTTP API & Media requests (ApiClient)
 * 2. Native Web Map data fetching & media resolution (roomify-map.js)
 * 3. Android and Desktop platform targets
 */
object AppConfig {
    const val REMOTE_API_BASE_URL: String = "https://roomify-api3.fly.dev"
}
