package com.freelink.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.freelink.core.designsystem.theme.FreeLinkTheme
import com.freelink.core.navigation.FreeLinkAppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreeLinkTheme {
                FreeLinkAppNavHost()
            }
        }
    }
}
