package com.ablomm.monthlygoalmanager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class GoalIcon(
    val name: String,
    val icon: ImageVector,
    val description: String
)

object GoalIcons {
    val allIcons = listOf(
        GoalIcon("EmojiEvents", Icons.Default.EmojiEvents, "トロフィー"),
        GoalIcon("Star", Icons.Default.Star, "星"),
        GoalIcon("TrendingUp", Icons.Default.TrendingUp, "成長"),
        GoalIcon("Lightbulb", Icons.Default.Lightbulb, "アイデア"),
        GoalIcon("School", Icons.Default.School, "学習"),
        GoalIcon("FitnessCenter", Icons.Default.FitnessCenter, "フィットネス"),
        GoalIcon("Work", Icons.Default.Work, "仕事"),
        GoalIcon("MonetizationOn", Icons.Default.MonetizationOn, "お金"),
        GoalIcon("Favorite", Icons.Default.Favorite, "健康・愛"),
        GoalIcon("Home", Icons.Default.Home, "家庭"),
        GoalIcon("Flight", Icons.Default.Flight, "旅行"),
        GoalIcon("MenuBook", Icons.Default.MenuBook, "読書"),
        GoalIcon("Palette", Icons.Default.Palette, "創作"),
        GoalIcon("Groups", Icons.Default.Groups, "人間関係"),
        GoalIcon("Psychology", Icons.Default.Psychology, "メンタル"),
        GoalIcon("Timer", Icons.Default.Timer, "時間管理"),
        GoalIcon("Language", Icons.Default.Language, "言語"),
        GoalIcon("SportsSoccer", Icons.Default.SportsSoccer, "スポーツ"),
        GoalIcon("MusicNote", Icons.Default.MusicNote, "音楽"),
        GoalIcon("CameraAlt", Icons.Default.CameraAlt, "写真・記録")
    )

    fun getIconByName(name: String): ImageVector {
        return allIcons.find { it.name == name }?.icon ?: Icons.Default.EmojiEvents
    }

    fun getIconDescription(name: String): String {
        return allIcons.find { it.name == name }?.description ?: "トロフィー"
    }
}
