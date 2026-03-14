package com.example.time.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.time.ui.screens.ThemeSelectionScreen
import com.example.tiptime.ui.theme.TimeTheme

class ThemeSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TimeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ThemeSelectionScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}
