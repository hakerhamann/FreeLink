package com.freelink.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.freelink.core.datastore.privacy.PrivacySettingsStore
import com.freelink.core.designsystem.theme.FreeLinkTheme
import com.freelink.core.navigation.FreeLinkAppNavHost
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val privacySettingsStore by lazy {
        PrivacySettingsStore.create(applicationContext)
    }

    private var biometricLockRequired: Boolean = false
    private var biometricPromptInProgress: Boolean = false
    private var isUnlockedByBiometric by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreeLinkTheme {
                if (!biometricLockRequired || isUnlockedByBiometric) {
                    FreeLinkAppNavHost()
                } else {
                    BiometricLockedScreen(
                        onUnlockClick = ::promptBiometricUnlock
                    )
                }
            }
        }

        observePrivacySettings()
    }

    override fun onResume() {
        super.onResume()
        if (biometricLockRequired && !isUnlockedByBiometric) {
            promptBiometricUnlock()
        }
    }

    private fun observePrivacySettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                privacySettingsStore.settingsFlow.collect { settings ->
                    applyHiddenMode(settings.hiddenModeEnabled)
                    syncBiometricRequirement(settings.biometricLockRequired)
                }
            }
        }
    }

    private fun applyHiddenMode(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun syncBiometricRequirement(required: Boolean) {
        val requirementChanged = biometricLockRequired != required
        biometricLockRequired = required

        if (!required) {
            isUnlockedByBiometric = true
            biometricPromptInProgress = false
            return
        }

        if (requirementChanged) {
            isUnlockedByBiometric = false
        }

        if (!isUnlockedByBiometric) {
            promptBiometricUnlock()
        }
    }

    private fun promptBiometricUnlock() {
        if (!biometricLockRequired || isUnlockedByBiometric || biometricPromptInProgress) {
            return
        }

        val canAuthenticate = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            isUnlockedByBiometric = true
            return
        }

        biometricPromptInProgress = true
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    biometricPromptInProgress = false
                    isUnlockedByBiometric = true
                }

                override fun onAuthenticationFailed() {
                    isUnlockedByBiometric = false
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    biometricPromptInProgress = false
                    isUnlockedByBiometric = false
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock FreeLink")
            .setSubtitle("Confirm biometrics to continue")
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }
}

@Composable
private fun BiometricLockedScreen(
    onUnlockClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "App is locked",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Biometric lock is enabled. Confirm your identity to continue.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onUnlockClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unlock")
        }
    }
}
