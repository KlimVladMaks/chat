package io.github.klimvladmaks.chat.data.api

import io.github.klimvladmaks.chat.data.api.dto.LoginRequest
import io.github.klimvladmaks.chat.data.api.dto.LoginResponse
import io.github.klimvladmaks.chat.data.api.dto.MessageDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part


/**
 * Retrofit интерфейс для работы с API чат-сервера.
 * Все методы suspend.
 */
interface ChatApiService {

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("channels")
    suspend fun getChannels(@Header("X-Auth-Token") token: String): List<String>

    @GET("channel/{channelName}")
    suspend fun getMessages(
        @Header("X-Auth-Token") token: String,
        @Path("channelName") channelName: String,
        @Query("limit") limit: Int,
        @Query("lastKnownId") lastKnownId: Int
    ): List<MessageDto>

    @POST("messages")
    @Headers("Content-Type: application/json")
    suspend fun sendTextMessage(
        @Header("X-Auth-Token") token: String,
        @Body message: MessageDto
    ): Response<Unit>

    @Multipart
    @POST("messages")
    suspend fun sendImageMessage(
        @Header("X-Auth-Token") token: String,
        @Part("msg") msg: MultipartBody.Part,
        @Part picture: MultipartBody.Part
    ): Response<Unit>

    @POST("logout")
    suspend fun logout(@Header("X-Auth-Token") token: String): Response<Unit>
}
