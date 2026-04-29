package com.freelink.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.core.model.domain.Message
import com.freelink.feature.chat.data.DirectChatRepository
import com.freelink.feature.chat.data.DirectChatResult
import com.freelink.feature.chat.ui.mapper.toDirectUiModel
import com.freelink.feature.chat.ui.model.ReplyTargetUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DirectChatViewModel(
    private val chatId: String,
    chatTitle: String,
    private val repository: DirectChatRepository
) : ViewModel() {
    private var currentUserId: String? = null
    private var latestMessages: List<Message> = emptyList()
    private val deliveryOverrides = LinkedHashMap<String, String>()
    private val deliveryJobs = LinkedHashMap<String, Job>()
    private var typingJob: Job? = null

    private val _uiState = MutableStateFlow(
        DirectChatUiState(
            chatId = chatId,
            chatTitle = chatTitle.ifBlank { "Direct chat" }
        )
    )
    val uiState: StateFlow<DirectChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserId = repository.currentUserId()
            refreshMessages(isInitial = true)
        }
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshMessages(isInitial = false)
        }
    }

    fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) {
            return
        }
        val replyTargetId = _uiState.value.replyTarget?.messageId

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (
                val result = repository.sendMessage(
                    chatId = chatId,
                    body = text,
                    replyToMessageId = replyTargetId
                )
            ) {
                is DirectChatResult.Success -> {
                    _uiState.update { it.copy(draft = "", replyTarget = null) }
                    refreshMessages(isInitial = false)
                    scheduleDeliveryProgression(result.value.id)
                    schedulePeerTypingIndicator()
                }
                is DirectChatResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onReplyRequested(messageId: String) {
        val target = latestMessages.firstOrNull { it.id == messageId } ?: return
        val snippet = target.body.trim().take(80)
        _uiState.update {
            it.copy(
                replyTarget = ReplyTargetUiModel(
                    messageId = target.id,
                    snippet = snippet
                )
            )
        }
    }

    fun onReplyCancelled() {
        _uiState.update { it.copy(replyTarget = null) }
    }

    fun onReactionRequested(messageId: String, emoji: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (
                val result = repository.setReaction(
                    chatId = chatId,
                    messageId = messageId,
                    emoji = emoji
                )
            ) {
                is DirectChatResult.Success -> refreshMessages(isInitial = false)
                is DirectChatResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private suspend fun refreshMessages(isInitial: Boolean) {
        _uiState.update { state ->
            state.copy(
                isLoading = if (isInitial) true else state.isLoading,
                isRefreshing = !isInitial,
                errorMessage = null
            )
        }

        when (val result = repository.loadMessages(chatId)) {
            is DirectChatResult.Success -> {
                val sortedMessages = result.value.sortedBy { it.createdAtEpochMs }
                latestMessages = sortedMessages

                val bodyById = sortedMessages.associate { it.id to it.body.trim() }
                val mapped = sortedMessages.map { message ->
                    val replySnippet = message.replyToMessageId
                        ?.let(bodyById::get)
                        ?.take(80)
                    message.toDirectUiModel(
                        currentUserId = currentUserId,
                        replyToSnippet = replySnippet
                    )
                }.map { item ->
                    val overrideState = deliveryOverrides[item.id]
                    if (overrideState == null) item else item.copy(deliveryStateLabel = overrideState)
                }

                val validReplyTarget = _uiState.value.replyTarget?.takeIf { reply ->
                    sortedMessages.any { it.id == reply.messageId }
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        messages = mapped,
                        replyTarget = validReplyTarget,
                        errorMessage = null
                    )
                }
            }
            is DirectChatResult.Failure -> {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun scheduleDeliveryProgression(messageId: String) {
        deliveryJobs.remove(messageId)?.cancel()
        deliveryJobs[messageId] = viewModelScope.launch {
            delay(1_500)
            deliveryOverrides[messageId] = "delivered"
            applyLocalDeliveryOverrides()

            delay(1_800)
            deliveryOverrides[messageId] = "read"
            applyLocalDeliveryOverrides()
        }
    }

    private fun schedulePeerTypingIndicator() {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPeerTyping = true) }
            delay(1_300)
            _uiState.update { it.copy(isPeerTyping = false) }
        }
    }

    private fun applyLocalDeliveryOverrides() {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { item ->
                    val overrideState = deliveryOverrides[item.id]
                    if (overrideState == null) item else item.copy(deliveryStateLabel = overrideState)
                }
            )
        }
    }

    override fun onCleared() {
        typingJob?.cancel()
        deliveryJobs.values.forEach { it.cancel() }
        deliveryJobs.clear()
        super.onCleared()
    }

    companion object {
        fun factory(
            chatId: String,
            chatTitle: String,
            repository: DirectChatRepository
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    DirectChatViewModel(
                        chatId = chatId,
                        chatTitle = chatTitle,
                        repository = repository
                    )
                }
            }
        }
    }
}
