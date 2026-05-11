Привет! Ты являешься Android-разработчиком, который старается делать всё корректно, но при этом максимально просто и понятно (для тебя простое всего лучше сложного, ни в коем случае не переувлажняй, всегда выбирай самое простое и понятное решение из тех, что соответствуют заданию). Ниже тебе будет дано задание. Внимательно изучи его и выбери ТОЛЬКО один из следующий вариантов:

* Если задание тебе полностью понятно и предоставленной в нём информации полностью достаточно для однозначно корректного выполнения задания, то можешь приступать к выполнению задания.
* Если же задание хотя в чём-то непонятно, есть какие-то неоднозначные моменты, не хватает предоставленной информации, то ты должен попросить предоставить недостающую информацию и уточнить неочевидные моменты. При этом приступать к выполнению задания ни в коем случае НЕ нужно, а нужно уточнить недостающую информацию.

====

```kotlin
package io.github.klimvladmaks.chat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.klimvladmaks.chat.viewmodel.ChannelsViewModel


@Composable
fun ChannelsScreen(
    channelsViewModel: ChannelsViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by channelsViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogout()
            channelsViewModel.reset()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Список каналов")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { channelsViewModel.logout() },
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Выйти")
            }
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
```
```kotlin
package io.github.klimvladmaks.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.klimvladmaks.chat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ChannelsState(
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val error: String? = null
)


class ChannelsViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChannelsState())
    val state: StateFlow<ChannelsState> = _state.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.logout()
                _state.update { it.copy(isLoggedOut = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Ошибка выхода") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun reset() {
        _state.update { ChannelsState() }
    }
}
```
Я сейчас разрабатываю учебное мобильное приложение чата с использованием подхода Jetpack Compose. Сейчас я работаю над экраном `ChannelsScreen`. Пока на нём есть только заголовок и кнопка выхода из аккаунта. Однако я хотел бы добавить туда непосредственно список чатов. Пока я не буду подключать работу с API, так что хотелось бы сделать просто интерфейс (пока заполни его просто тестовыми данными). В итоге должен быть список чатов, который можно листать. Каждый чат должен быть представлен только названием (наверное, тебе придётся хранить список строк). При этом элемент чата в списке должен быть кликабельным, но пока без какого-то функционала (функционал добавим позже). А вверху должна быть небольшая панель с кнопка "Выйти" и именем пользователя (имя пользователя можно получить из `repository.getUsername()`). Постарайся сделать всё максимально просто и понятно.
