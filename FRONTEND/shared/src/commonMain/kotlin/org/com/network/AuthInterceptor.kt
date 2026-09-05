package org.com.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class AuthClient(
    private val tokenManager: TokenManager
) {

    fun createClient(): HttpClient {
        return HttpClient {
            install(DefaultRequest) {
                val token = tokenManager.getToken()

                if (!token.isNullOrBlank()) {
                    header(
                        HttpHeaders.Authorization,
                        "Bearer $token"
                    )
                }
            }
        }
    }
}