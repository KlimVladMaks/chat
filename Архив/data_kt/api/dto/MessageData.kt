package io.github.klimvladmaks.chat.data.api.dto

sealed class MessageData {
    data class TextMessage(val text: String) : MessageData()
    data class ImageMessage(val link: String? = null) : MessageData()
}
