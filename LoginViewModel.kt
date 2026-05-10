package io.github.klimvladmaks.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.klimvladmaks.chat.data.repository.ChatRepository
import io.github.klimvladmaks.chat.data.repository.UnauthorizedException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LoginViewModel(
    private val repository: ChatRepository,
    private val mainViewModel: MainViewModel
) : ViewModel() {
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateUsername(newUsername: String) {
        _username.value = newUsername
        clearError()
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        clearError()
    }

    fun login() {
        val currentUsername = _username.value
        val currentPassword = _password.value

        if (currentUsername.isBlank() || currentPassword.isBlank()) {
            _errorMessage.value = "Заполните логин и пароль"
            return
        }

        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.login(currentUsername, currentPassword)
                mainViewModel.onLoginSuccess()
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("401") == true -> "Неверный логин / пароль"
                    else -> e.message ?: "Ошибка при авторизации"
                }
                _errorMessage.value = message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
