package com.example.time.ui.activity

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.time.LifePieceApplication
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.screens.ModernTimeStatsScreen
import com.example.tiptime.ui.theme.TimeTheme

class ShowTimeActivity : ComponentActivity() {
    private val lifePieceViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory((application as LifePieceApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TimeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ModernTimeStatsScreen(
                        viewModel = lifePieceViewModel,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}
