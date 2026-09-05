package org.com.network

import org.com.model.User

interface TokenManager {

    fun saveToken(token: String)

    fun getToken(): String?

    fun saveUser(user: User)

    fun getUser(): User?

    fun getUserId(): Long?

    fun clear()

    fun isLoggedIn(): Boolean

    fun getAuthHeader(): String?
}