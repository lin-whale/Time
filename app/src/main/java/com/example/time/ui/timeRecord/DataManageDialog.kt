/**
 * DataManageDialog - 数据导入导出管理对话框
 * 
 * 功能说明：
 * - 导出所有时间记录到 ZIP 文件（包含 JSON + 图片）
 * - 从 ZIP 文件导入时间记录
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
    var pendingImportResult by remember { mutableStateOf<ImportResult?>(null) }
    var importMode by remember { mutableStateOf("append") } // append 或 replace
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    
    // 导出文件选择器 (ZIP 格式)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            isExporting = true
            Thread {
                val result = exportToZip(
                    context,
                    it,
                    allTimePieces.value ?: listOf(),
                    allLifePieces.value ?: listOf()
                )
                Thread.currentThread().interrupt()
                (context as? android.app.Activity)?.runOnUiThread {
                    isExporting = false
                    if (result.success) {
                        val msg = if (result.imageCount > 0) {
                            "✅ 导出成功！包含 ${result.imageCount} 张图片"
                        } else {
                            "✅ 导出成功！"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "❌ ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
    
    // 导入文件选择器 (支持 ZIP 和 JSON)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            isImporting = true
            Thread {
                // 检测文件类型
                val isZip = isZipFile(context, it)
                
                if (isZip) {
                    // ZIP 格式导入（新格式，包含图片）
                    val result = importFromZip(context, it)
                    Thread.currentThread().interrupt()
                    (context as? android.app.Activity)?.runOnUiThread {
                        isImporting = false
                        if (result.success) {
                            pendingImportResult = result
                            // 直接导入数据
                            val jsonData = exportToJson(
                                result.timePiecesCount.let { count ->
                                    // 从 ZIP 导入时，我们要真正的数据
                                    // 这里我们创建一个临时的 ImportResult，实际数据还需要解析
                                    listOf<TimePiece>()
                                },
                                listOf()
                            )
                            // 这里简化处理：ZIP 导入已经完成了数据解析
                            showImportConfirm = true
                        } else {
                            Toast.makeText(context, "❌ ${result.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // JSON 格式导入（旧格式兼容）
                    val jsonString = readFromUri(context, it)
                    Thread.currentThread().interrupt()
                    (context as? android.app.Activity)?.runOnUiThread {
                        isImporting = false
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
            }.start()
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
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isExporting && !isImporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出中...")
                    } else {
                        Text("📤 导出数据（含图片）")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 导入按钮
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isExporting && !isImporting
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导入中...")
                    } else {
                        Text("📥 导入数据")
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 说明文字
                Text(
                    text = "💡 导出的 ZIP 包含：\n• 所有时间记录 (JSON)\n• 所有图片附件\n• 可跨设备完整迁移",
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
    if (showImportConfirm && (pendingImportData != null || pendingImportResult != null)) {
        AlertDialog(
            onDismissRequest = { 
                showImportConfirm = false
                pendingImportData = null
                pendingImportResult = null
            },
            title = { Text("确认导入") },
            text = {
                Column {
                    Text("发现 ${pendingImportResult!!.timePieces.size} 条时间记录")
                    Text("发现 ${pendingImportResult!!.lifePieces.size} 个标签")
                    if (pendingImportResult!!.imageCount > 0) {
                        Text("包含 ${pendingImportResult!!.imageCount} 张图片", color = MaterialTheme.colorScheme.primary)
                    }
                    
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
                        val clearExisting = importMode == "replace"
                        
                        if (pendingImportResult != null) {
                            // ZIP 导入，数据已经在 ImportResult 中
                            viewModel.importTimePieces(pendingImportResult!!.timePieces, clearExisting)
                            viewModel.importLifePieces(pendingImportResult!!.lifePieces, clearExisting)
                        } else if (pendingImportData != null) {
                            viewModel.importTimePieces(pendingImportData!!.timePieces, clearExisting)
                            viewModel.importLifePieces(pendingImportData!!.lifePieces, clearExisting)
                        }
                        
                        val msg = if (pendingImportResult != null && pendingImportResult!!.imageCount > 0) {
                            "✅ 导入成功！包含 ${pendingImportResult!!.imageCount} 张图片"
                        } else {
                            "✅ 导入成功！"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        
                        showImportConfirm = false
                        pendingImportData = null
                        pendingImportResult = null
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
                        pendingImportResult = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}