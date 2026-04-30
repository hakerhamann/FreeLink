package com.freelink.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelink.feature.settings.ui.components.SettingsActionRow
import com.freelink.feature.settings.ui.components.SettingsSectionCard
import com.freelink.feature.settings.ui.components.SettingsSwitchRow
import com.freelink.feature.settings.ui.components.WhoCanMessageChips
import com.freelink.feature.settings.ui.model.WhoCanMessageUiOption

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onRefresh: () -> Unit,
    onHiddenModeChanged: (Boolean) -> Unit,
    onBiometricLockChanged: (Boolean) -> Unit,
    onDisappearingMessagesChanged: (Boolean) -> Unit,
    onLinkPreviewChanged: (Boolean) -> Unit,
    onWhoCanMessageChanged: (WhoCanMessageUiOption) -> Unit,
    onOpenDevices: () -> Unit,
    onOpenArchive: () -> Unit,
    onLogout: () -> Unit
) {
    val controlsEnabled = !state.isLoading && !state.isSaving && !state.isLoggingOut

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Меню",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Настройки приватности и защиты",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                )
            }

            PrivacyLevelChip(label = state.privacyLevelLabel())
        }

        SettingsSectionCard(
            title = state.profileTitle(),
            subtitle = state.profileSubtitle()
        ) {
            Text(
                text = if (state.isSessionAvailable) {
                    "Защищённая сессия активна на этом устройстве."
                } else {
                    "Войдите, чтобы синхронизировать устройства и приватность."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            SettingsActionRow(
                title = "Активные устройства",
                subtitle = "Управление сессиями и отзыв старых устройств",
                trailing = "Открыть",
                enabled = controlsEnabled,
                onClick = onOpenDevices
            )
        }

        SettingsSectionCard(
            title = "Приватность и шифрование",
            subtitle = "Базовые настройки защиты безопасного MVP"
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            SettingsSwitchRow(
                title = "Скрытый режим",
                subtitle = "Скрывать контент в недавних приложениях и системных превью",
                checked = state.settings.hiddenModeEnabled,
                enabled = controlsEnabled,
                onCheckedChange = onHiddenModeChanged
            )
            SettingsSwitchRow(
                title = "Биометрическая блокировка",
                subtitle = "Требовать биометрию при повторном открытии приложения",
                checked = state.settings.biometricLockRequired,
                enabled = controlsEnabled,
                onCheckedChange = onBiometricLockChanged
            )
            SettingsSwitchRow(
                title = "Исчезающие сообщения",
                subtitle = "Удалять новые сообщения через 24 часа",
                checked = state.settings.disappearingMessagesEnabled,
                enabled = controlsEnabled,
                onCheckedChange = onDisappearingMessagesChanged
            )
            SettingsSwitchRow(
                title = "Предпросмотр ссылок",
                subtitle = "Показывать превью для отправленных ссылок",
                checked = state.settings.linkPreviewEnabled,
                enabled = controlsEnabled,
                onCheckedChange = onLinkPreviewChanged
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Кто может писать",
                    style = MaterialTheme.typography.titleSmall
                )
                WhoCanMessageChips(
                    selected = state.settings.whoCanMessageMe,
                    enabled = controlsEnabled,
                    onSelected = onWhoCanMessageChanged
                )
            }

            if (state.isSaving) {
                Text(
                    text = "Сохраняем настройки...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        SettingsSectionCard(
            title = "Приложение и данные",
            subtitle = "Ближайшие разделы настроек из следующих итераций"
        ) {
            SettingsActionRow(
                title = "Уведомления",
                subtitle = "Звуки, вибрация и счётчики",
                trailing = "Выйти",
                enabled = false
            )
            SettingsActionRow(
                title = "Внешний вид",
                subtitle = "Тема, атмосфера и оформление чатов",
                trailing = "Выйти",
                enabled = false
            )
            SettingsActionRow(
                title = "Выйти",
                subtitle = "Архивные чаты и защищённая история",
                trailing = "Открыть",
                enabled = controlsEnabled,
                onClick = onOpenArchive
            )
            SettingsActionRow(
                title = "Медиатека",
                subtitle = "Фото, файлы и голосовые вложения",
                trailing = "Спринт 5",
                enabled = false
            )
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = controlsEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Обновить настройки")
        }

        Button(
            onClick = onLogout,
            enabled = controlsEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoggingOut) "Выходим..." else "Выйти")
        }
    }
}

@Composable
private fun PrivacyLevelChip(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Приватность: $label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
