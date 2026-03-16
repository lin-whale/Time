/**
 * SettingsActivity - 设置页面 Activity
 * 
 * 功能：
 * - 显示设置界面
 * - 处理返回导航
 * - 显示帮助文档
 */
package com.example.time.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.time.ui.settings.SettingsScreen
import com.example.time.ui.timeRecord.IntroductionDialog
import com.example.tiptime.ui.theme.TimeTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showHelpDialog by remember { mutableStateOf(false) }
                    
                    SettingsScreen(
                        onNavigateBack = { finish() },
                        onShowHelp = { showHelpDialog = true }
                    )
                    
                    if (showHelpDialog) {
                        IntroductionDialog(
                            onCancel = { showHelpDialog = false }
                        )
                    }
                }
            }
        }
    }
}
