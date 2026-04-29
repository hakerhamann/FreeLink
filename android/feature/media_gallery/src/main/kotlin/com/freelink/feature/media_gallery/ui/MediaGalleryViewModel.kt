package com.freelink.feature.media_gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freelink.core.model.domain.AttachmentType
import com.freelink.feature.media_gallery.data.MediaGalleryRepository
import com.freelink.feature.media_gallery.data.MediaGalleryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaGalleryViewModel(
    private val repository: MediaGalleryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MediaGalleryUiState())
    val uiState: StateFlow<MediaGalleryUiState> = _uiState.asStateFlow()

    fun onFileNameChanged(value: String) {
        _uiState.update { it.copy(fileName = value) }
    }

    fun onMimeTypeChanged(value: String) {
        _uiState.update { it.copy(mimeType = value) }
    }

    fun onDigestChanged(value: String) {
        _uiState.update { it.copy(digestSha256 = value) }
    }

    fun onByteSizeChanged(value: String) {
        _uiState.update { it.copy(byteSizeText = value) }
    }

    fun onAttachmentTypeChanged(value: AttachmentType) {
        _uiState.update { it.copy(attachmentType = value) }
    }

    fun initUpload() {
        val state = _uiState.value
        val byteSize = state.byteSizeText.toLongOrNull()
        if (
            state.fileName.isBlank() ||
            state.mimeType.isBlank() ||
            state.digestSha256.isBlank() ||
            byteSize == null ||
            byteSize <= 0
        ) {
            _uiState.update { it.copy(errorMessage = "Fill all media fields with valid values.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, latestMedia = null) }
            when (
                val result = repository.initUpload(
                    fileName = state.fileName.trim(),
                    mimeType = state.mimeType.trim(),
                    digestSha256 = state.digestSha256.trim(),
                    byteSize = byteSize,
                    attachmentType = state.attachmentType
                )
            ) {
                is MediaGalleryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            latestSession = result.value
                        )
                    }
                }
                is MediaGalleryResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun completeUpload() {
        val uploadId = _uiState.value.latestSession?.uploadId.orEmpty()
        if (uploadId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Start an upload session first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = repository.completeUpload(uploadId)) {
                is MediaGalleryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            latestMedia = result.value
                        )
                    }
                }
                is MediaGalleryResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(repository: MediaGalleryRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    MediaGalleryViewModel(repository)
                }
            }
        }
    }
}
