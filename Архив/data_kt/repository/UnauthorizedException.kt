package io.github.klimvladmaks.chat.data.repository


/**
 * Исключение, выбрасываемое репозиторием при отсутствии токена
 * или получении HTTP 401 от сервера.
 */
class UnauthorizedException : Exception("Authentication token is missing or expired")
