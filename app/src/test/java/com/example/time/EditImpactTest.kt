package com.example.time

import org.junit.Test
import org.junit.Assert.*

/**
 * 模拟 TimePiece 数据类（仅用于测试）
 */
data class TimePiece(
    val id: Int = 0,
    val fromTimePoint: Long,  // 开始时间
    val timePoint: Long,       // 结束时间
    val mainEvent: String = "测试事件"
)

/**
 * 编辑影响分析结果
 */
data class EditImpactAnalysis(
    val piecesToDelete: List<TimePiece>,
    val piecesToAdjust: List<Pair<TimePiece, TimePiece>>,  // (原记录, 调整后)
    val hasImpact: Boolean
)

/**
 * 分析编辑操作对其他记录的影响
 * 这是从 TimePieceList.kt 复制的函数，用于测试
 */
fun analyzeEditImpact(
    original: TimePiece,
    updated: TimePiece,
    allPieces: List<TimePiece>,
    pieceIndex: Int
): EditImpactAnalysis {
    val toDelete = mutableListOf<TimePiece>()
    val toAdjust = mutableListOf<Pair<TimePiece, TimePiece>>()
    
    // 1. 分析开始时间提前的影响（影响之前的记录）
    if (updated.fromTimePoint < original.fromTimePoint) {
        for (i in (pieceIndex + 1) until allPieces.size) {
            val earlierPiece = allPieces[i]
            
            if (earlierPiece.timePoint <= updated.fromTimePoint) {
                break
            } else if (earlierPiece.fromTimePoint >= updated.fromTimePoint) {
                toDelete.add(earlierPiece)
            } else {
                val adjusted = earlierPiece.copy(timePoint = updated.fromTimePoint)
                toAdjust.add(earlierPiece to adjusted)
                break
            }
        }
    }
    
    // 2. 分析结束时间延后的影响（影响之后的记录）
    if (updated.timePoint > original.timePoint) {
        for (i in (pieceIndex - 1) downTo 0) {
            val laterPiece = allPieces[i]
            
            if (laterPiece.fromTimePoint >= updated.timePoint) {
                break
            } else if (laterPiece.timePoint <= updated.timePoint) {
                toDelete.add(laterPiece)
            } else {
                val adjusted = laterPiece.copy(fromTimePoint = updated.timePoint)
                toAdjust.add(laterPiece to adjusted)
                break
            }
        }
    }
    
    return EditImpactAnalysis(
        piecesToDelete = toDelete,
        piecesToAdjust = toAdjust,
        hasImpact = toDelete.isNotEmpty() || toAdjust.isNotEmpty()
    )
}

/**
 * 验证调整后的记录是否合法（结束时间 > 开始时间）
 */
fun validateAdjustedPieces(analysis: EditImpactAnalysis): List<String> {
    val errors = mutableListOf<String>()
    
    for ((original, adjusted) in analysis.piecesToAdjust) {
        if (adjusted.timePoint <= adjusted.fromTimePoint) {
            errors.add("记录 '${original.mainEvent}' 调整后时间非法: " +
                "开始=${adjusted.fromTimePoint}, 结束=${adjusted.timePoint}")
        }
    }
    
    return errors
}

/**
 * 辅助函数：创建时间（小时:分钟 -> 毫秒时间戳）
 */
fun time(hour: Int, minute: Int): Long = (hour * 60 + minute) * 60 * 1000L

class EditImpactTest {

    /**
     * 测试1: 简单的开始时间提前，部分覆盖前一条记录
     * 
     * 原始数据（列表倒序，index 小 = 时间晚）：
     * [0] D: 10:00 → 11:00  (当前编辑)
     * [1] C: 09:30 → 10:00
     * [2] B: 09:00 → 09:30
     * [3] A: 08:00 → 09:00
     * 
     * 操作：D 的开始时间从 10:00 改为 09:45
     * 预期：C 的结束时间调整为 09:45
     */
    @Test
    fun test1_startTimeEarlier_partialOverlap() {
        val pieces = listOf(
            TimePiece(id = 4, fromTimePoint = time(10, 0), timePoint = time(11, 0), mainEvent = "D"),
            TimePiece(id = 3, fromTimePoint = time(9, 30), timePoint = time(10, 0), mainEvent = "C"),
            TimePiece(id = 2, fromTimePoint = time(9, 0), timePoint = time(9, 30), mainEvent = "B"),
            TimePiece(id = 1, fromTimePoint = time(8, 0), timePoint = time(9, 0), mainEvent = "A")
        )
        
        val original = pieces[0]  // D
        val updated = original.copy(fromTimePoint = time(9, 45))  // 开始时间提前到 09:45
        
        val result = analyzeEditImpact(original, updated, pieces, 0)
        
        println("测试1: 开始时间提前，部分覆盖")
        println("删除: ${result.piecesToDelete.map { it.mainEvent }}")
        println("调整: ${result.piecesToAdjust.map { (o, a) -> "${o.mainEvent}: ${o.timePoint} -> ${a.timePoint}" }}")
        
        // 验证
        assertEquals("应该没有删除", 0, result.piecesToDelete.size)
        assertEquals("应该有1条调整", 1, result.piecesToAdjust.size)
        assertEquals("C 应该被调整", "C", result.piecesToAdjust[0].first.mainEvent)
        assertEquals("C 的结束时间应该是 09:45", time(9, 45), result.piecesToAdjust[0].second.timePoint)
        
        // 验证调整后的记录是否合法
        val errors = validateAdjustedPieces(result)
        assertTrue("调整后的记录应该合法: $errors", errors.isEmpty())
    }

    /**
     * 测试2: 开始时间大幅提前，完全覆盖多条记录
     * 
     * 操作：D 的开始时间从 10:00 改为 08:30
     * 预期：B、C 被删除，A 的结束时间调整为 08:30
     */
    @Test
    fun test2_startTimeEarlier_fullOverlapMultiple() {
        val pieces = listOf(
            TimePiece(id = 4, fromTimePoint = time(10, 0), timePoint = time(11, 0), mainEvent = "D"),
            TimePiece(id = 3, fromTimePoint = time(9, 30), timePoint = time(10, 0), mainEvent = "C"),
            TimePiece(id = 2, fromTimePoint = time(9, 0), timePoint = time(9, 30), mainEvent = "B"),
            TimePiece(id = 1, fromTimePoint = time(8, 0), timePoint = time(9, 0), mainEvent = "A")
        )
        
        val original = pieces[0]
        val updated = original.copy(fromTimePoint = time(8, 30))  // 开始时间提前到 08:30
        
        val result = analyzeEditImpact(original, updated, pieces, 0)
        
        println("\n测试2: 开始时间大幅提前，完全覆盖多条")
        println("删除: ${result.piecesToDelete.map { it.mainEvent }}")
        println("调整: ${result.piecesToAdjust.map { (o, a) -> "${o.mainEvent}: ${o.timePoint} -> ${a.timePoint}" }}")
        
        // 验证
        assertEquals("应该删除2条 (B, C)", 2, result.piecesToDelete.size)
        assertTrue("B 应该被删除", result.piecesToDelete.any { it.mainEvent == "B" })
        assertTrue("C 应该被删除", result.piecesToDelete.any { it.mainEvent == "C" })
        assertEquals("应该有1条调整 (A)", 1, result.piecesToAdjust.size)
        assertEquals("A 的结束时间应该是 08:30", time(8, 30), result.piecesToAdjust[0].second.timePoint)
        
        val errors = validateAdjustedPieces(result)
        assertTrue("调整后的记录应该合法: $errors", errors.isEmpty())
    }

    /**
     * 测试3: ⚠️ BUG 场景 - 开始时间提前导致前一条记录时间非法
     * 
     * 原始数据：
     * [0] B: 09:30 → 10:00  (当前编辑)
     * [1] A: 09:00 → 09:30
     * 
     * 操作：B 的开始时间从 09:30 改为 09:00（与 A 的开始时间相同）
     * 预期：A 应该被删除（因为调整后 A 变成 09:00→09:00，时长为0）
     * 实际（BUG）：A 被调整为 09:00→09:00，时间非法！
     */
    @Test
    fun test3_BUG_startTimeEarlier_invalidAdjustment() {
        val pieces = listOf(
            TimePiece(id = 2, fromTimePoint = time(9, 30), timePoint = time(10, 0), mainEvent = "B"),
            TimePiece(id = 1, fromTimePoint = time(9, 0), timePoint = time(9, 30), mainEvent = "A")
        )
        
        val original = pieces[0]  // B
        val updated = original.copy(fromTimePoint = time(9, 0))  // 开始时间提前到 09:00
        
        val result = analyzeEditImpact(original, updated, pieces, 0)
        
        println("\n测试3: ⚠️ BUG场景 - 开始时间提前到与前一条开始时间相同")
        println("删除: ${result.piecesToDelete.map { it.mainEvent }}")
        println("调整: ${result.piecesToAdjust.map { (o, a) -> "${o.mainEvent}: from=${a.fromTimePoint}, to=${a.timePoint}" }}")
        
        val errors = validateAdjustedPieces(result)
        if (errors.isNotEmpty()) {
            println("❌ 发现非法调整: $errors")
        }
        
        // 这个测试会失败，暴露 BUG
        assertTrue("调整后的记录应该合法（当前有BUG会失败）: $errors", errors.isEmpty())
    }

    /**
     * 测试4: ⚠️ BUG 场景 - 开始时间提前导致前一条记录结束时间早于开始时间
     * 
     * 原始数据：
     * [0] B: 09:30 → 10:00  (当前编辑)
     * [1] A: 09:15 → 09:30
     * 
     * 操作：B 的开始时间从 09:30 改为 09:00（比 A 的开始时间还早）
     * 预期：A 应该被删除
     * 实际（BUG）：A 被调整为 09:15→09:00，结束时间早于开始时间！
     */
    @Test
    fun test4_BUG_startTimeEarlier_endBeforeStart() {
        val pieces = listOf(
            TimePiece(id = 2, fromTimePoint = time(9, 30), timePoint = time(10, 0), mainEvent = "B"),
            TimePiece(id = 1, fromTimePoint = time(9, 15), timePoint = time(9, 30), mainEvent = "A")
        )
        
        val original = pieces[0]  // B
        val updated = original.copy(fromTimePoint = time(9, 0))  // 开始时间提前到 09:00
        
        val result = analyzeEditImpact(original, updated, pieces, 0)
        
        println("\n测试4: ⚠️ BUG场景 - 开始时间提前到比前一条开始时间还早")
        println("删除: ${result.piecesToDelete.map { it.mainEvent }}")
        println("调整: ${result.piecesToAdjust.map { (o, a) -> "${o.mainEvent}: from=${a.fromTimePoint}, to=${a.timePoint}" }}")
        
        val errors = validateAdjustedPieces(result)
        if (errors.isNotEmpty()) {
            println("❌ 发现非法调整: $errors")
        }
        
        // 这个测试会失败，暴露 BUG
        assertTrue("调整后的记录应该合法（当前有BUG会失败）: $errors", errors.isEmpty())
    }

    /**
     * 测试5: 结束时间延后，部分覆盖后一条记录
     */
    @Test
    fun test5_endTimeLater_partialOverlap() {
        val pieces = listOf(
            TimePiece(id = 2, fromTimePoint = time(11, 0), timePoint = time(12, 0), mainEvent = "E"),
            TimePiece(id = 1, fromTimePoint = time(10, 0), timePoint = time(11, 0), mainEvent = "D")
        )
        
        val original = pieces[1]  // D
        val updated = original.copy(timePoint = time(11, 30))  // 结束时间延后到 11:30
        
        val result = analyzeEditImpact(original, updated, pieces, 1)
        
        println("\n测试5: 结束时间延后，部分覆盖后一条")
        println("删除: ${result.piecesToDelete.map { it.mainEvent }}")
        println("调整: ${result.piecesToAdjust.map { (o, a) -> "${o.mainEvent}: from ${o.fromTimePoint} -> ${a.fromTimePoint}" }}")
        
        assertEquals("应该没有删除", 0, result.piecesToDelete.size)
        assertEquals("应该有1条调整", 1, result.piecesToAdjust.size)
        assertEquals("E 的开始时间应该是 11:30", time(11, 30), result.piecesToAdjust[0].second.fromTimePoint)
        
        val errors = validateAdjustedPieces(result)
        assertTrue("调整后的记录应该合法: $errors", errors.isEmpty())
    }

    /**
     * 测试6: ⚠️ BUG 场景 - 结束时间延后导致后一条记录时间非法
     */
    @Test
    fun test6_BUG_endTimeLater_invalidAdjustment() {
        val pieces = listOf(
            TimePiece(id = 2, fromTimePoint = time(11, 0), timePoint = time(11, 30), mainEvent = "E"),
            TimePiece(id = 1, fromTimePoint = time(10, 0), timePoint = time(11, 0), mainEvent = "D")
        )
        
        val original = pieces[1]  // D
        val updated = original.copy(timePoint = time(11, 30))  // 结束时间延后到 11:30（等于 E 的结束时间）
        
        val result = analyzeEditImpact(original, updated, pieces, 1)
        
        println("\n测试6: ⚠️ BUG场景 - 结束时间延后到与后一条结束时间相同")
        println("删除: ${result.piecesToDelete.map { it.mainEvent }}")
        println("调整: ${result.piecesToAdjust.map { (o, a) -> "${o.mainEvent}: from=${a.fromTimePoint}, to=${a.timePoint}" }}")
        
        val errors = validateAdjustedPieces(result)
        if (errors.isNotEmpty()) {
            println("❌ 发现非法调整: $errors")
        }
        
        // 这个测试可能会失败
        assertTrue("调整后的记录应该合法: $errors", errors.isEmpty())
    }

    /**
     * 运行所有测试并汇总结果
     */
    @Test
    fun runAllTestsWithSummary() {
        println("=" .repeat(60))
        println("EditImpact 函数测试")
        println("=".repeat(60))
        
        var passed = 0
        var failed = 0
        
        try { test1_startTimeEarlier_partialOverlap(); passed++ } 
        catch (e: AssertionError) { failed++; println("测试1 失败: ${e.message}") }
        
        try { test2_startTimeEarlier_fullOverlapMultiple(); passed++ } 
        catch (e: AssertionError) { failed++; println("测试2 失败: ${e.message}") }
        
        try { test3_BUG_startTimeEarlier_invalidAdjustment(); passed++ } 
        catch (e: AssertionError) { failed++; println("测试3 失败: ${e.message}") }
        
        try { test4_BUG_startTimeEarlier_endBeforeStart(); passed++ } 
        catch (e: AssertionError) { failed++; println("测试4 失败: ${e.message}") }
        
        try { test5_endTimeLater_partialOverlap(); passed++ } 
        catch (e: AssertionError) { failed++; println("测试5 失败: ${e.message}") }
        
        try { test6_BUG_endTimeLater_invalidAdjustment(); passed++ } 
        catch (e: AssertionError) { failed++; println("测试6 失败: ${e.message}") }
        
        println("\n" + "=".repeat(60))
        println("测试结果: 通过 $passed / 失败 $failed")
        println("=".repeat(60))
    }
}
