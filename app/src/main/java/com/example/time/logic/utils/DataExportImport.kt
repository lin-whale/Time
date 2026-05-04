/**
 * DataExportImport - 数据导入导出工具
 * 
 * 功能说明：
 * - 将时间记录导出为 ZIP 压缩包（包含 JSON + 图片）
 * - 从 ZIP 压缩包导入时间记录
 * - 支持增量导入和覆盖导入
 * 
 * 数据格式版本：v2
 * - ZIP 包含 data.json 和 images/ 目录
 * - 图片文件直接打包，支持跨设备迁移
 * 
 * 开发原则：
 * - 所有数据仅在本地处理，不上传网络
 * - 支持数据无缝迁移
 */
package com.example.time.logic.utils

import android.content.Context
import android.net.Uri
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 数据导出格式版本
 * 用于未来的数据迁移兼容性
 */
const val DATA_FORMAT_VERSION = 2

/**
 * 数据导出结果
 */
data class ExportResult(
    val success: Boolean,
    val message: String,
    val filePath: String? = null,
    val imageCount: Int = 0
)

/**
 * 数据导入结果
 */
data class ImportResult(
    val success: Boolean,
    val message: String,
    val timePieces: List<TimePiece> = listOf(),
    val lifePieces: List<LifePiece> = listOf(),
    val imageCount: Int = 0
)

/**
 * 导出数据包
 */
data class ExportData(
    val version: Int,
    val exportTime: Long,
    val timePieces: List<TimePiece>,
    val lifePieces: List<LifePiece>
)

/**
 * 导出数据为 ZIP 压缩包（包含 JSON + 图片）
 * 
 * @param context 上下文
 * @param uri 目标文件 Uri
 * @param timePieces 时间片段列表
 * @param lifePieces 生活片段标签列表
 * @return 导出结果
 */
fun exportToZip(
    context: Context,
    uri: Uri,
    timePieces: List<TimePiece>,
    lifePieces: List<LifePiece>
): ExportResult {
    return try {
        var imageCount = 0
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. 写入 data.json
                val jsonData = exportToJson(timePieces, lifePieces)
                val dataEntry = ZipEntry("data.json")
                zipOut.putNextEntry(dataEntry)
                zipOut.write(jsonData.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
                
                // 2. 写入图片文件
                val imageDir = File(context.filesDir, "images")
                val copiedImages = mutableSetOf<String>() // 避免重复复制同一图片
                
                timePieces.forEach { piece ->
                    piece.getMediaList().forEach { path ->
                        if (path !in copiedImages) {
                            val imageFile = File(path)
                            if (imageFile.exists()) {
                                val imageEntry = ZipEntry("images/${imageFile.name}")
                                zipOut.putNextEntry(imageEntry)
                                imageFile.inputStream().use { input ->
                                    input.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                                copiedImages.add(path)
                                imageCount++
                            }
                        }
                    }
                }
            }
        }
        
        ExportResult(
            success = true,
            message = "导出成功",
            imageCount = imageCount
        )
    } catch (e: Exception) {
        e.printStackTrace()
        ExportResult(
            success = false,
            message = "导出失败: ${e.message}"
        )
    }
}

/**
 * 从 ZIP 压缩包导入数据
 * 
 * @param context 上下文
 * @param uri ZIP 文件 Uri
 * @return 导入结果
 */
fun importFromZip(context: Context, uri: Uri): ImportResult {
    return try {
        var jsonData: String? = null
        val importedImages = mutableMapOf<String, String>() // 原文件名 -> 新路径
        var imageCount = 0
        
        // 确保图片目录存在
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()
        
        val exportData: ExportData?
        val importedImages = mutableMapOf<String, String>() // 原文件名 -> 新路径
        var imageCount = 0
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                
                while (entry != null) {
                    when {
                        entry.name == "data.json" -> {
                            // 读取 JSON 数据
                            jsonData = zipIn.readBytes().toString(Charsets.UTF_8)
                        }
                        entry.name.startsWith("images/") -> {
                            // 解压图片到应用目录
                            val fileName = entry.name.substringAfter("images/")
                            if (fileName.isNotEmpty()) {
                                // 使用时间戳避免文件名冲突
                                val newFileName = "${System.currentTimeMillis()}_$fileName"
                                val destFile = File(imageDir, newFileName)
                                destFile.outputStream().use { output ->
                                    zipIn.copyTo(output)
                                }
                                importedImages[fileName] = destFile.absolutePath
                                imageCount++
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
        }
        
        if (jsonData == null) {
            return ImportResult(
                success = false,
                message = "ZIP 文件中没有找到 data.json"
            )
        }
        
        // 解析 JSON 数据
        exportData = parseFromJson(jsonData!!)
            ?: return ImportResult(
                success = false,
                message = "JSON 数据解析失败"
            )
        
        // 更新图片路径（将旧文件名映射到新路径）
        exportData.timePieces.forEach { piece ->
            val newPaths = piece.getMediaList().map { oldPath ->
                val oldFileName = File(oldPath).name
                // 查找是否有导入的图片匹配
                importedImages[oldFileName] ?: oldPath
            }
            piece.setMediaList(newPaths)
        }
        
        ImportResult(
            success = true,
            message = "导入成功",
            timePieces = exportData.timePieces,
            lifePieces = exportData.lifePieces,
            imageCount = imageCount
        )
    } catch (e: Exception) {
        e.printStackTrace()
        ImportResult(
            success = false,
            message = "导入失败: ${e.message}"
        )
    }
}

/**
 * 将时间记录数据导出为 JSON 格式
 * 
 * @param timePieces 时间片段列表
 * @param lifePieces 生活片段标签列表
 * @return JSON 字符串
 */
fun exportToJson(timePieces: List<TimePiece>, lifePieces: List<LifePiece>): String {
    val root = JSONObject()
    
    // 添加元数据
    root.put("version", DATA_FORMAT_VERSION)
    root.put("exportTime", System.currentTimeMillis())
    root.put("appName", "TimeApp")
    
    // 导出时间片段
    val timePiecesArray = JSONArray()
    timePieces.forEach { piece ->
        val pieceJson = JSONObject().apply {
            put("id", piece.id)
            put("timePoint", piece.timePoint)
            put("fromTimePoint", piece.fromTimePoint)
            put("emotion", piece.emotion)
            put("lastTimeRecord", piece.lastTimeRecord)
            put("mainEvent", piece.mainEvent)
            put("subEvent", piece.subEvent)
            // 导出媒体路径（图片附件）
            piece.mediaPaths?.let {
                put("mediaPaths", JSONArray(it))
            }
        }
        timePiecesArray.put(pieceJson)
    }
    root.put("timePieces", timePiecesArray)
    
    // 导出生活片段标签
    val lifePiecesArray = JSONArray()
    lifePieces.forEach { piece ->
        val pieceJson = JSONObject().apply {
            put("id", piece.id)
            put("lifePiece", piece.lifePiece)
        }
        lifePiecesArray.put(pieceJson)
    }
    root.put("lifePieces", lifePiecesArray)
    
    return root.toString(2) // 格式化输出，缩进2空格
}

/**
 * 从 JSON 字符串解析数据
 * 支持版本迁移
 * 
 * @param jsonString JSON 字符串
 * @return 解析后的数据，如果解析失败返回 null
 */
fun parseFromJson(jsonString: String): ExportData? {
    return try {
        val root = JSONObject(jsonString)
        
        // 读取版本号
        val version = root.optInt("version", 1)
        val exportTime = root.optLong("exportTime", 0)
        
        // 解析时间片段
        val timePieces = mutableListOf<TimePiece>()
        val timePiecesArray = root.optJSONArray("timePieces")
        if (timePiecesArray != null) {
            for (i in 0 until timePiecesArray.length()) {
                val pieceJson = timePiecesArray.getJSONObject(i)
                val piece = TimePiece(
                    id = pieceJson.optLong("id", 0),
                    timePoint = pieceJson.getLong("timePoint"),
                    fromTimePoint = pieceJson.getLong("fromTimePoint"),
                    emotion = pieceJson.getInt("emotion"),
                    lastTimeRecord = pieceJson.optString("lastTimeRecord", ""),
                    mainEvent = pieceJson.getString("mainEvent"),
                    subEvent = pieceJson.optString("subEvent", "")
                )
                // 导入媒体路径（图片附件）
                pieceJson.optJSONArray("mediaPaths")?.let { mediaArray ->
                    piece.mediaPaths = mediaArray.toString()
                }
                timePieces.add(piece)
            }
        }
        
        // 解析生活片段标签
        val lifePieces = mutableListOf<LifePiece>()
        val lifePiecesArray = root.optJSONArray("lifePieces")
        if (lifePiecesArray != null) {
            for (i in 0 until lifePiecesArray.length()) {
                val pieceJson = lifePiecesArray.getJSONObject(i)
                val piece = LifePiece(
                    id = pieceJson.optLong("id", 0),
                    lifePiece = pieceJson.getString("lifePiece")
                )
                lifePieces.add(piece)
            }
        }
        
        // 版本迁移处理
        when (version) {
            1 -> {
                // v1 格式，图片路径可能不存在，无需处理
            }
            2 -> {
                // v2 格式，当前版本
            }
        }
        
        ExportData(
            version = version,
            exportTime = exportTime,
            timePieces = timePieces,
            lifePieces = lifePieces
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 检测文件是否为 ZIP 格式
 */
fun isZipFile(context: Context, uri: Uri): Boolean {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            // ZIP 文件魔数：PK\x03\x04
            val header = ByteArray(4)
            input.read(header)
            header[0] == 'P'.code.toByte() && 
            header[1] == 'K'.code.toByte() &&
            header[2] == 0x03.toByte() &&
            header[3] == 0x04.toByte()
        } ?: false
    } catch (e: Exception) {
        false
    }
}

/**
 * 生成默认的导出文件名
 * 格式：TimeApp_备份_yyyyMMdd_HHmmss.zip
 */
fun generateExportFileName(): String {
    val timestamp = convertTimeFormat(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
    return "TimeApp_备份_$timestamp.zip"
}