Привет! Ты являешься Android-разработчиком, который старается делать всё корректно, но при этом максимально просто и понятно (для тебя простое всего лучше сложного, ни в коем случае не переусложняй, всегда выбирай самое простое и понятное решение из тех, что соответствуют заданию). Ниже тебе будет дано задание. Внимательно изучи его и выбери ТОЛЬКО один из следующий вариантов:

* Если задание тебе полностью понятно и предоставленной в нём информации полностью достаточно для однозначно корректного выполнения задания, то можешь приступать к выполнению задания.
* Если же задание хотя в чём-то непонятно, есть какие-то неоднозначные моменты, не хватает предоставленной информации, то ты должен попросить предоставить недостающую информацию и уточнить неочевидные моменты. При этом приступать к выполнению задания ни в коем случае НЕ нужно, а нужно уточнить недостающую информацию. Также НИ В КОЕМ СЛУЧАЕ НИЧЕГО НЕ додумывай, если что-то не понял, не знаешь или тебе не хватает какой-то информации, то сразу уточняй.

Также после выполнения задания (если предоставленная информация для тебя полностью понятна и однозначна) ОБЯЗАТЕЛЬНО напиши, насколько ты уверен в своём решении (от 0% до 100%). Оценивай своё решение очень строго (тут лучше быть более придирчивым).

***

Привет! Я сейчас работаю над учебный мобильным приложением чата на Android Jetpack Compose. В этом приложение у меня есть модуль `data`, который отвечает за работу с данными (по большей части с API). Вот значимые файлы для из данного модуля:

```kotlin
package io.github.klimvladmaks.chat.data.api

import io.github.klimvladmaks.chat.data.models.LoginRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

    @POST("/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body credentials: LoginRequest): Response<ResponseBody>

    @POST("/logout")
    suspend fun logout(@Header("X-Auth-Token") token: String): Response<ResponseBody>

    @GET("/inbox/{username}")
    suspend fun getInbox(
        @Path("username") username: String,
        @Header("X-Auth-Token") token: String
    ): Response<ResponseBody>

    @GET("/channels")
    suspend fun getChannels(): Response<List<String>>
}
```
```kotlin
package io.github.klimvladmaks.chat.data.repository

import android.util.Log
import io.github.klimvladmaks.chat.data.api.ApiService
import io.github.klimvladmaks.chat.data.storage.AuthStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.klimvladmaks.chat.data.models.LoginRequest


class ChatRepository(
    private val apiService: ApiService,
    private val authStorage: AuthStorage
) {
    suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)
            val token = response.body()?.string() ?: throw Exception()
            authStorage.saveTokenAndUsername(token, username)
        } catch (e: Exception) {
            Log.e("TAG", e.toString())
            throw Exception("Ошибка авторизации")
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val token = authStorage.getToken() ?: throw Exception("Токен отсутствует")
        try {
            val response = apiService.logout(token)
        } finally {
            authStorage.clear()
        }
    }

    suspend fun isAuthorized(): Boolean = withContext(Dispatchers.IO) {
        val token = authStorage.getToken()
        val username = authStorage.getUsername()
        if (token == null || username == null) return@withContext false
        return@withContext try {
            val response = apiService.getInbox(username, token)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getChannels(): List<String> = withContext(Dispatchers.IO) {
        val response = apiService.getChannels()
        if (response.isSuccessful && response.body() != null) {
            return@withContext response.body()!!
        } else {
            throw Exception("Ошибка загрузки каналов: ${response.code()}")
        }
    }

    fun getUsername() = authStorage.getUsername()
}
```

Сейчас я бы хотел добавить дополнительный функционал для загрузки сообщений из конкретного чата. Для загрузки сообщений из конкретного чата нужно отправить запрос `GET /channel/{имя_канала}`. `X-Auth-Token` для данного запроса не нужен. В ответ на данный запрос должно прийти что-то примерно следующее:
```json
[
    {
        "id": 7475,
        "from": "123",
        "to": "123@channel",
        "data": {
            "Text": {
                "text": "321"
            }
        },
        "time": 1762693211000
    },
    {
        "id": 8163,
        "from": "Дмитрий Дзюба",
        "to": "123@channel",
        "data": {
            "Image": {
                "link": "Дмитрий Дзюба/upload_1767189397412.jpg"
            }
        },
        "time": 1767189402000
    },
    {
        "id": 8410,
        "from": "Дмитрий Дзюба",
        "to": "123@channel",
        "data": {
            "Text": {
                "text": "487"
            }
        },
        "time": 1767514384000
    }
]
```
То есть список с сообщениями. При этом сообщения могут быть двух типов: текст и изображение (единственное различие, структура поля `data`, как ты можешь увидеть в примере). В общем, тебе нужно написать метод `ChatRepository.getMessages(channelName: String)`, который бы принимал имя канала и возвращал бы список сообщений из этого канала. Также для запроса доступны следующие параметры:
"""
Параметры запросов:
- `limit`: Сколько сообщений отдавать, по умолчанию 20
- `lastKnownId`: Начиная с какого сообщения отдавать сообщения, по умолчанию 0
- `reverse`: Возвращает сообщения в обратном порядке (с id меньше, чем lastKnownId), по умолчанию false
"""
Должна быть возможность указывать эти параметры.
Метод должен возвращать список `List<ChatMessage>`, где `ChatMessage` имеет следующий вид:
```kotlin
package io.github.klimvladmaks.chat.data.models

enum class ChatMessageType {
    TEXT, IMAGE
}

data class ChatMessage(
    val id: Long,
    val from: String,
    val to: String,
    val type: ChatMessageType,
    val content: String,
    val time: Long
)

```
Как видишь, вид немного отличается от формата ответа, а значит метод должен преобразовывать входящий ответ к `ChatMessage`.
Вот такое вот задание. Постарайся сделать всё максимально просто и понятно.







***

```kotlin

```
