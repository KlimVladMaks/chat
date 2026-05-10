```kotlin
package io.github.klimvladmaks.chat.data.repository

class UnauthorizedException(message: String) : Exception(message)
```
```kotlin
package io.github.klimvladmaks.chat.data.repository

import io.github.klimvladmaks.chat.data.api.ApiService
import io.github.klimvladmaks.chat.data.storage.AuthStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.klimvladmaks.chat.data.api.dto.LoginRequest
import io.github.klimvladmaks.chat.data.api.dto.Message
import io.github.klimvladmaks.chat.data.api.dto.MessageData
import okhttp3.ResponseBody


class ChatRepository(
    private val apiService: ApiService,
    private val authStorage: AuthStorage
) {
    suspend fun login(username: String, password: String): String = withContext(Dispatchers.IO) {
        val request = LoginRequest(username, password)
        val response = apiService.login(request)
        if (response.isSuccessful) {
            val token = response.body() ?: throw Exception("Empty token")
            authStorage.saveTokenAndUsername(token, username)
            token
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Login failed"
            throw Exception(errorMsg)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
        } finally {
            authStorage.clear()
        }
    }

    suspend fun getChannels(): List<String> = withContext(Dispatchers.IO) {
        safeApiCall { apiService.getChannels() }
    }

    suspend fun getMessages(
        channel: String,
        limit: Int = 20,
        lastKnownId: Long? = null
    ): List<Message> = withContext(Dispatchers.IO) {
        safeApiCall {
            apiService.getMessages(
                channelName = channel,
                limit = limit,
                lastKnownId = lastKnownId
            )
        }
    }

    suspend fun sendTextMessage(channel: String, text: String) = withContext(Dispatchers.IO) {
        val username = authStorage.getUsername()
            ?: throw Exception("No logged in user")
        val message = Message(
            from = username,
            to = channel,
            data = MessageData.Text(text)
        )
        val response = apiService.sendTextMessage(message)
        if (!response.isSuccessful) {
            handleErrorResponse(response)
        }
    }

    suspend fun getFullImage(path: String): ResponseBody = safeApiCall {
        apiService.getFullImage(path)
    }

    suspend fun getThumbImage(path: String): ResponseBody = safeApiCall {
        apiService.getThumbImage(path)
    }

    private suspend fun <T> safeApiCall(block: suspend () -> retrofit2.Response<T>): T {
        val response = block()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            handleErrorResponse(response)
        }
    }

    private suspend fun handleErrorResponse(response: retrofit2.Response<*>): Nothing {
        if (response.code() == 401) {
            authStorage.clear()
            throw UnauthorizedException("Unauthorized: invalid or expired token")
        }
        val errorMessage = response.errorBody()?.string() ?: response.message()
        throw Exception(errorMessage)
    }
}
```
```kotlin
package io.github.klimvladmaks.chat.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AuthStorage(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveTokenAndUsername(token: String, username: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putString(KEY_USERNAME, username)
        }
    }

    fun hasToken(): Boolean = getToken() != null

    fun hasUsername(): Boolean = getUsername() != null

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        private const val PREFS_NAME = "chat_auth"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "user_name"
    }
}
```
