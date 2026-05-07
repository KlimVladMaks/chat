package io.github.klimvladmaks.chat.data.api.dto


/**
 * Ответ сервера на успешный логин.
 * @property token Токен доступа
 */
data class LoginResponse(
    val token: String
)
