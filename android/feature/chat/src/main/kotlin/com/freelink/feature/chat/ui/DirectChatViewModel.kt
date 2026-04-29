package com.freelink.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.core.model.domain.MediaAttachment
import com.freelink.core.model.domain.Message
import com.freelink.core.network.messages.ws.DirectChatWsEvent
import com.freelink.feature.chat.data.DirectChatRepository
import com.freelink.feature.chat.data.DirectChatResult
import com.freelink.feature.chat.ui.mapper.toDirectUiModel
import com.freelink.feature.chat.ui.mapper.toUiModel
import com.freelink.feature.chat.ui.model.ReplyTargetUiModel
import kotlinx.coroutines.Job
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
    private var pendingAttachment: MediaAttachment? = null
    private val deliveryOverrides = LinkedHashMap<String, String>()
    private var wsJob: Job? = null

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
        startRealtimeSync()
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshMessages(isInitial = false)
        }
    }

    fun attachSampleMedia() {
        if (_uiState.value.isAttaching) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAttaching = true, errorMessage = null) }
            when (val result = repository.uploadSampleAttachment(chatId)) {
                is DirectChatResult.Success -> {
                    pendingAttachment = result.value
                    _uiState.update {
                        it.copy(
                            isAttaching = false,
                            pendingAttachment = result.value.toUiModel()
                        )
                    }
                }
                is DirectChatResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isAttaching = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun removePendingAttachment() {
        pendingAttachment = null
        _uiState.update { it.copy(pendingAttachment = null, isAttaching = false) }
    }

    fun sendMessage() {
        val text = _uiState.value.draft.trim()
        val attachment = pendingAttachment
        if (text.isEmpty() && attachment == null) {
            return
        }
        val replyTargetId = _uiState.value.replyTarget?.messageId

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            when (
                val result = repository.sendMessage(
                    chatId = chatId,
                    body = text,
                    attachment = attachment,
                    replyToMessageId = replyTargetId
                )
            ) {
                is DirectChatResult.Success -> {
                    pendingAttachment = null
                    _uiState.update {
                        it.copy(
                            draft = "",
                            pendingAttachment = null,
                            replyTarget = null
                        )
                    }
                    refreshMessages(isInitial = false)
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

    private fun startRealtimeSync() {
        wsJob?.cancel()
        wsJob = viewModelScope.launch {
            repository.observeRealtimeEvents(chatId) { event ->
                handleRemoteEvent(event)
            }
        }
    }

    private suspend fun handleRemoteEvent(event: DirectChatWsEvent) {
        when (event) {
            is DirectChatWsEvent.MessageCreated -> refreshMessages(isInitial = false)
            is DirectChatWsEvent.TypingStarted -> _uiState.update { it.copy(isPeerTyping = true) }
            is DirectChatWsEvent.TypingStopped -> _uiState.update { it.copy(isPeerTyping = false) }
            is DirectChatWsEvent.ReceiptDelivered -> applyRemoteReceipt(event.messageId, "delivered")
            is DirectChatWsEvent.ReceiptRead -> applyRemoteReceipt(event.messageId, "read")
        }
    }

    private fun applyRemoteReceipt(messageId: String?, deliveryState: String) {
        val targetId = messageId
            ?: latestMessages.lastOrNull { it.senderUserId == currentUserId }?.id
            ?: return
        deliveryOverrides[targetId] = deliveryState
        applyLocalDeliveryOverrides()
    }

    fun onReplyRequested(messageId: String) {
        val target = latestMessages.firstOrNull { it.id == messageId } ?: return
        val snippet = target.body.trim().ifBlank { target.attachment?.fileName.orEmpty() }.take(80)
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
        wsJob?.cancel()
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
