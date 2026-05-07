package io.github.klimvladmaks.chat.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


/**
 * Хранилище аутентификационных данных (токен и имя пользователя).
 * Использует SharedPreferences для сохранения между сессиями.
 *
 * @param context Контекст приложения (будет использован applicationContext)
 */
class AuthStorage(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Сохраняет токен и имя пользователя.
     * @param token Токен доступа
     * @param userName Имя пользователя
     */
    fun saveTokenAndUserName(token: String, userName: String) {
        prefs.edit {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_NAME, userName)
        }
    }

    /**
     * Возвращает сохранённый токен или null, если токен отсутствует.
     */
    fun getToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    /**
     * Возвращает сохранённое имя пользователя или null, если имя не сохранено.
     */
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    /**
     * Очищает все сохранённые данные (токен и имя пользователя).
     */
    fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        private const val PREFS_NAME = "chat_auth"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_NAME = "user_name"
    }
}
