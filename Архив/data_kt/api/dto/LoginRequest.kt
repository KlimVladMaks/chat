package io.github.klimvladmaks.chat.data.api.dto


/**
 * Тело запроса для логина.
 * @property name Имя пользователя
 * @property pwd Пароль
 */
data class LoginRequest(
    val name: String,
    val pwd: String
)
