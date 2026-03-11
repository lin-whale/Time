/**
 * DataManageDialog - 数据导入导出管理对话框
 * 
 * 功能说明：
 * - 导出所有时间记录到 JSON 文件
 * - 从 JSON 文件导入时间记录
 * - 支持增量导入和覆盖导入
 * 
 * 开发原则：
 * - 所有数据仅在本地处理，不上传网络
 * - 支持版本间无缝迁移
 */
package com.example.time.ui.timeRecord

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.time.logic.model.TimePiece
import com.example.time.logic.model.LifePiece
import com.example.time.logic.utils.*
import com.example.time.ui.TimeViewModel

/**
 * 数据管理对话框
 * 
 * @param viewModel TimeViewModel 实例
 * @param onDismiss 关闭回调
 */
@Composable
fun DataManageDialog(
    viewModel: TimeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allTimePieces = viewModel.allTimePieces.observeAsState(listOf())
    val allLifePieces = viewModel.allLifePieces.observeAsState(listOf())
    
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportData by remember { mutableStateOf<ExportData?>(null) }
    var importMode by remember { mutableStateOf("append") } // append 或 replace
    
    // 导出文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            // 导出数据
            val jsonData = exportToJson(
                allTimePieces.value ?: listOf(),
                allLifePieces.value ?: listOf()
            )
            val success = writeToUri(context, it, jsonData)
            if (success) {
                Toast.makeText(context, "✅ 导出成功！", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "❌ 导出失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 导入文件选择器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val jsonString = readFromUri(context, it)
            if (jsonString != null) {
                val data = parseFromJson(jsonString)
                if (data != null) {
                    pendingImportData = data
                    showImportConfirm = true
                } else {
                    Toast.makeText(context, "❌ 文件格式错误", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "❌ 读取文件失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📦 数据管理",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "当前共 ${allTimePieces.value?.size ?: 0} 条记录",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 导出按钮
                Button(
                    onClick = {
                        val fileName = generateExportFileName()
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📤 导出数据到文件")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 导入按钮
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📥 从文件导入数据")
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 说明文字
                Text(
                    text = "💡 导出的 JSON 文件可用于：\n• 备份数据\n• 迁移到新设备\n• 版本升级后恢复数据",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        }
    }
    
    // 导入确认对话框
    if (showImportConfirm && pendingImportData != null) {
        AlertDialog(
            onDismissRequest = { 
                showImportConfirm = false
                pendingImportData = null
            },
            title = { Text("确认导入") },
            text = {
                Column {
                    Text("发现 ${pendingImportData!!.timePieces.size} 条时间记录")
                    Text("发现 ${pendingImportData!!.lifePieces.size} 个标签")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("导入方式：", fontWeight = FontWeight.Medium)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = importMode == "append",
                            onClick = { importMode = "append" }
                        )
                        Text("追加到现有数据")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = importMode == "replace",
                            onClick = { importMode = "replace" }
                        )
                        Text("替换现有数据（清空后导入）")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val data = pendingImportData!!
                        val clearExisting = importMode == "replace"
                        
                        viewModel.importTimePieces(data.timePieces, clearExisting)
                        viewModel.importLifePieces(data.lifePieces, clearExisting)
                        
                        Toast.makeText(
                            context, 
                            "✅ 导入成功！共 ${data.timePieces.size} 条记录", 
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        showImportConfirm = false
                        pendingImportData = null
                    }
                ) {
                    Text("确认导入")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirm = false
                        pendingImportData = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
