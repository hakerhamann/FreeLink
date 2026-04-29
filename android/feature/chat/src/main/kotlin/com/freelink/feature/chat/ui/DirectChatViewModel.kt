package com.freelink.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.feature.chat.ui.model.DirectMessageUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class DirectChatViewModel(
    chatId: String,
    chatTitle: String
) : ViewModel() {
    private val peerUserId = "peer"
    private var receiveJob: Job? = null

    private val _uiState = MutableStateFlow(
        DirectChatUiState(
            chatId = chatId,
            chatTitle = chatTitle.ifBlank { "Direct chat" },
            messages = seedMessages()
        )
    )
    val uiState: StateFlow<DirectChatUiState> = _uiState.asStateFlow()

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) {
            return
        }

        val outgoing = DirectMessageUiModel(
            id = UUID.randomUUID().toString(),
            text = text,
            isOutgoing = true,
            timeLabel = nowTimeLabel(),
            deliveryStateLabel = "sent"
        )

        _uiState.update { state ->
            state.copy(
                draft = "",
                isPeerTyping = true,
                messages = state.messages + outgoing
            )
        }

        receiveJob?.cancel()
        receiveJob = viewModelScope.launch {
            delay(1_200)
            val incoming = DirectMessageUiModel(
                id = UUID.randomUUID().toString(),
                text = "Принято: $text",
                isOutgoing = false,
                timeLabel = nowTimeLabel()
            )
            _uiState.update { state ->
                state.copy(
                    isPeerTyping = false,
                    messages = state.messages + incoming
                )
            }
        }
    }

    override fun onCleared() {
        receiveJob?.cancel()
        super.onCleared()
    }

    private fun seedMessages(): List<DirectMessageUiModel> {
        return listOf(
            DirectMessageUiModel(
                id = "seed-1",
                text = "Привет! На связи.",
                isOutgoing = false,
                timeLabel = "18:24"
            ),
            DirectMessageUiModel(
                id = "seed-2",
                text = "Привет! Как дела?",
                isOutgoing = true,
                timeLabel = "18:25",
                deliveryStateLabel = "read"
            )
        )
    }

    private fun nowTimeLabel(): String {
        return Instant.now()
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(timeFormatter)
    }

    companion object {
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun factory(chatId: String, chatTitle: String): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    DirectChatViewModel(
                        chatId = chatId,
                        chatTitle = chatTitle
                    )
                }
            }
        }
    }
}
