package io.github.klimvladmaks.chat.data.api.dto


/**
 * DTO для сообщения (отправка и получение).
 * @property id Уникальный идентификатор (опционально при запросе, всегда есть в ответе)
 * @property from Отправитель (должен совпадать с именем пользователя)
 * @property to Получатель (канал или пользователь, по умолчанию "1@channel")
 * @property data Содержимое сообщения (текст или изображение)
 * @property time Unix timestamp (опционально при запросе)
 */
data class MessageDto(
    val id: String? = null,
    val from: String,
    val to: String = "1@channel",
    val data: MessageData,
    val time: Long? = null
)
