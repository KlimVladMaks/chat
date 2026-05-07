package io.github.klimvladmaks.chat.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.klimvladmaks.chat.data.api.dto.MessageData
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Фабрика для создания экземпляра ChatApiService.
 * Содержит настройки Retrofit и Moshi.
 */
object NetworkModule {

    private const val BASE_URL = "https://faerytea.name/"

    /**
     * Создаёт и настраивает Moshi с поддержкой полиморфной сериализации sealed class MessageData.
     */
    fun createMoshi(): Moshi {
        val polymorphicAdapter = PolymorphicJsonAdapterFactory.of(MessageData::class.java, "type")
            .withSubtype(MessageData.TextMessage::class.java, "Text")
            .withSubtype(MessageData.ImageMessage::class.java, "Image")

        return Moshi.Builder()
            .add(polymorphicAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Создаёт OkHttpClient с таймаутами.
     */
    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        return builder.build()
    }
}
