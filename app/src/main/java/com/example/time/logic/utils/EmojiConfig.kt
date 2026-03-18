/**
 * 心情Emoji配置管理
 * 
 * 功能：
 * - 管理各心情等级对应的emoji和文字标签
 * - 支持用户自定义emoji和文字
 * - 提供默认配置
 */
package com.example.time.logic.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 心情Emoji配置管理器
 */
object EmojiConfig {
    
    // 默认emoji配置
    val DEFAULT_EMOJIS = listOf("😞", "😕", "😐", "😊", "😄")
    val DEFAULT_LABELS = listOf("很差", "较差", "一般", "较好", "很好")
    
    // 可选emoji列表（供用户选择）
    val AVAILABLE_EMOJIS = listOf(
        // 负面情绪
        "😞", "😢", "😭", "😤", "😠", "😡", "😔", "😟", "🙁", "😣",
        // 中性情绪
        "😐", "😶", "😑", "😬", "🤔", "😶‍🌫️",
        // 正面情绪
        "😊", "🙂", "😃", "😄", "😁", "😆", "🥰", "😍", "🤩", "😎",
        // 特殊表情
        "😴", "🥱", "😵", "🤯", "🤕", "🤒", "🥵", "🥶", "😱", "💀",
        "🤡", "👻", "👽", "🤖", "😺", "😸", "😻", "😽", "🙀", "😿"
    )
    
    private const val PREFS_NAME = "emoji_settings"
    
    /**
     * 获取SharedPreferences
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 获取指定心情等级的emoji
     * @param context 上下文
     * @param feeling 心情等级 (1-5)
     * @return 对应的emoji
     */
    fun getEmoji(context: Context, feeling: Int): String {
        val prefs = getPrefs(context)
        val defaultEmoji = DEFAULT_EMOJIS.getOrElse(feeling - 1) { "😐" }
        return prefs.getString("emoji_$feeling", defaultEmoji) ?: defaultEmoji
    }
    
    /**
     * 获取指定心情等级的文字标签
     * @param context 上下文
     * @param feeling 心情等级 (1-5)
     * @return 对应的文字标签
     */
    fun getLabel(context: Context, feeling: Int): String {
        val prefs = getPrefs(context)
        val defaultLabel = DEFAULT_LABELS.getOrElse(feeling - 1) { "一般" }
        return prefs.getString("label_$feeling", defaultLabel) ?: defaultLabel
    }
    
    /**
     * 获取所有心情等级的emoji列表
     * @param context 上下文
     * @return emoji列表 (索引0对应心情1)
     */
    fun getAllEmojis(context: Context): List<String> {
        return (1..5).map { feeling -> getEmoji(context, feeling) }
    }
    
    /**
     * 获取所有心情等级的文字标签列表
     * @param context 上下文
     * @return 标签列表 (索引0对应心情1)
     */
    fun getAllLabels(context: Context): List<String> {
        return (1..5).map { feeling -> getLabel(context, feeling) }
    }
    
    /**
     * 设置指定心情等级的emoji
     * @param context 上下文
     * @param feeling 心情等级 (1-5)
     * @param emoji 新的emoji
     */
    fun setEmoji(context: Context, feeling: Int, emoji: String) {
        getPrefs(context).edit().putString("emoji_$feeling", emoji).apply()
    }
    
    /**
     * 设置指定心情等级的文字标签
     * @param context 上下文
     * @param feeling 心情等级 (1-5)
     * @param label 新的文字标签
     */
    fun setLabel(context: Context, feeling: Int, label: String) {
        getPrefs(context).edit().putString("label_$feeling", label).apply()
    }
    
    /**
     * 重置所有emoji和标签为默认值
     * @param context 上下文
     */
    fun resetToDefault(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    /**
     * 根据平均分数值获取emoji索引（四舍五入）
     * @param avgScore 平均分 (1.0-5.0)
     * @return emoji索引 (0-4)
     */
    fun getEmojiIndexByAvgScore(avgScore: Float): Int {
        return (avgScore - 0.5f).toInt().coerceIn(0, 4)
    }
}
