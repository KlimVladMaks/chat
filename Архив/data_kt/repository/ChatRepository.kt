package io.github.klimvladmaks.chat.data.repository

import io.github.klimvladmaks.chat.data.api.ChatApiService
import io.github.klimvladmaks.chat.data.storage.AuthStorage
import io.github.klimvladmaks.chat.data.api.NetworkModule
import io.github.klimvladmaks.chat.data.api.dto.LoginRequest
import io.github.klimvladmaks.chat.data.api.dto.MessageData
import io.github.klimvladmaks.chat.data.api.dto.MessageDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File


/**
 * Репозиторий для работы с чатом.
 * Инкапсулирует сетевые вызовы и локальное хранилище.
 *
 * @param api Сетевой сервис Retrofit
 * @param authStorage Хранилище токена и имени пользователя
 */
class ChatRepository(
    private val api: ChatApiService,
    private val authStorage: AuthStorage
) {
    private val moshi by lazy { NetworkModule.createMoshi() }
    private val messageDtoAdapter = moshi.adapter(MessageDto::class.java)

    companion object {
        private const val BASE_URL = "https://faerytea.name/"
    }

    /**
     * Логин пользователя.
     * @param name Имя пользователя
     * @param password Пароль
     * @return Result.success(Unit) при успехе, иначе Result.failure
     */
    suspend fun login(name: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(name, password))
            authStorage.saveTokenAndUserName(response.token, name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Выход из системы: вызывает logout на сервере (если есть токен) и очищает локальное хранилище.
     * Ошибки сервера игнорируются – локальные данные всё равно очищаются.
     * @return Всегда Result.success(Unit)
     */
    suspend fun logout(): Result<Unit> {
        try {
            val token = authStorage.getToken()
            if (token != null) {
                api.logout(token)
            }
        } catch (_: Exception) {
            // ignore network errors
        } finally {
            authStorage.clear()
        }
        return Result.success(Unit)
    }

    /**
     * Получить список каналов.
     * @return Result.success со списком строк или Result.failure
     */
    suspend fun getChannels(): Result<List<String>> {
        val token = authStorage.getToken() ?: return Result.failure(UnauthorizedException())
        return try {
            val channels = api.getChannels(token)
            Result.success(channels)
        } catch (e: HttpException) {
            if (e.code() == 401) Result.failure(UnauthorizedException())
            else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получить сообщения из канала.
     * @param channelName Имя канала (например, "1@channel")
     * @param limit Количество сообщений (по умолчанию 20)
     * @param lastKnownId ID последнего известного сообщения (0 – с начала)
     * @return Result.success со списком MessageDto или Result.failure
     */
    suspend fun getMessages(
        channelName: String,
        limit: Int = 20,
        lastKnownId: Int = 0
    ): Result<List<MessageDto>> {
        val token = authStorage.getToken() ?: return Result.failure(UnauthorizedException())
        return try {
            val messages = api.getMessages(token, channelName, limit, lastKnownId)
            Result.success(messages)
        } catch (e: HttpException) {
            if (e.code() == 401) Result.failure(UnauthorizedException())
            else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Отправить текстовое сообщение в канал.
     * @param channelName Имя канала
     * @param text Текст сообщения
     * @return Result.success(Unit) или Result.failure
     */
    suspend fun sendTextMessage(channelName: String, text: String): Result<Unit> {
        val token = authStorage.getToken() ?: return Result.failure(UnauthorizedException())
        val userName = authStorage.getUserName() ?: return Result.failure(UnauthorizedException())
        val message = MessageDto(
            from = userName,
            to = channelName,
            data = MessageData.TextMessage(text)
        )
        return try {
            val response = api.sendTextMessage(token, message)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(HttpException(response))
        } catch (e: HttpException) {
            if (e.code() == 401) Result.failure(UnauthorizedException())
            else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Отправить сообщение с изображением в канал.
     * @param channelName Имя канала
     * @param imageFile Файл изображения
     * @param caption Опциональный текст (будет отправлен как текстовое сообщение вместе с картинкой)
     * @return Result.success(Unit) или Result.failure
     */
    suspend fun sendImageMessage(
        channelName: String,
        imageFile: File,
        caption: String? = null
    ): Result<Unit> {
        val token = authStorage.getToken() ?: return Result.failure(UnauthorizedException())
        val userName = authStorage.getUserName() ?: return Result.failure(UnauthorizedException())

        val messageData = if (!caption.isNullOrBlank()) {
            MessageData.TextMessage(caption)
        } else {
            MessageData.ImageMessage()
        }
        val message = MessageDto(
            from = userName,
            to = channelName,
            data = messageData
        )

        val jsonString = messageDtoAdapter.toJson(message)

        val msgPart = MultipartBody.Part.createFormData(
            "msg", null,
            jsonString.toRequestBody("application/json".toMediaType())
        )
        val picturePart = MultipartBody.Part.createFormData(
            "picture", imageFile.name,
            imageFile.asRequestBody("image/*".toMediaType())
        )

        return try {
            val response = api.sendImageMessage(token, msgPart, picturePart)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(HttpException(response))
        } catch (e: HttpException) {
            if (e.code() == 401) Result.failure(UnauthorizedException())
            else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Сформировать URL для изображения (не suspend).
     * @param path Относительный путь (например, "pic.jpg")
     * @param thumb true – уменьшенное (thumb), false – оригинал (img)
     * @return Полный URL
     */
    fun getImageUrl(path: String, thumb: Boolean): String {
        val endpoint = if (thumb) "thumb" else "img"
        return "$BASE_URL$endpoint/$path"
    }
}
