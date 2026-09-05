package org.com.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.com.model.ApiResponse

object ApiClient {

    /*
     * IMPORTANT:
     * http://10.0.2.2:8080/api/
     * Keep the trailing slash.
     *
     * Base URL:
     * http://localhost:8080/
     *
     * Endpoints:
     * api/rooms
     * api/auth/login
     * api/bookings
     *
     * Result:
     * http://localhost:8080/api/rooms
     */

    const val MEDIA_BASE_URL = "http://192.168.100.3:8080"
    private const val BASE_URL = "$MEDIA_BASE_URL/api/"
    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken

        println(
            "ApiClient: token ${if (newToken != null) "SET" else "CLEARED"}"
        )
    }

    fun getToken(): String? {
        return token
    }

    fun clearToken() {
        token = null
        println("ApiClient: token CLEARED")
    }

    val client = HttpClient {

        install(ContentNegotiation) {

            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                    explicitNulls = false
                }
            )
        }

        install(Logging) {

            logger =
                object : Logger {

                    override fun log(
                        message: String
                    ) {
                        println("Ktor: $message")
                    }
                }

            level = LogLevel.BODY
        }

        defaultRequest {

            url(BASE_URL)

            contentType(
                ContentType.Application.Json
            )

            accept(
                ContentType.Application.Json
            )

            token?.let { jwt ->

                header(
                    HttpHeaders.Authorization,
                    "Bearer $jwt"
                )
            }
        }
    }


    // ============================================================
    // GET
    // ============================================================

    suspend inline fun <reified T> get(
        endpoint: String
    ): T {

        println(
            "ApiClient: GET $endpoint"
        )

        return client
            .get {
                url(endpoint)
            }
            .body()
    }


    // ============================================================
    // POST
    // ============================================================

    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any?
    ): T {

        println(
            "ApiClient: POST $endpoint"
        )

        return client
            .post {
                url(endpoint)

                if (body != null) {
                    setBody(body)
                }
            }
            .body()
    }


    // ============================================================
    // PUT
    // ============================================================

    suspend inline fun <reified T> put(
        endpoint: String,
        body: Any?
    ): T {

        println(
            "ApiClient: PUT $endpoint"
        )

        return client
            .put {
                url(endpoint)

                if (body != null) {
                    setBody(body)
                }
            }
            .body()
    }


    // ============================================================
    // DELETE
    // ============================================================

    suspend inline fun <reified T> delete(
        endpoint: String
    ): T {

        println(
            "ApiClient: DELETE $endpoint"
        )

        return client
            .delete {
                url(endpoint)
            }
            .body()
    }


    // ============================================================
    // CLOSE
    // ============================================================

    fun close() {
        client.close()
    }
}