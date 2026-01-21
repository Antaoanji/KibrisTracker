package com.example.pushuptracker.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pushuptracker.R

data class Badge(
    val id: String,
    @get:StringRes val title: Int,
    @get:StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val color: Color,
    val progress: Float = 0f
) {
    companion object {
        val allBadges = listOf(
            // --- STREAKS (SERİLER) - Renk: Orange/Red ---
            Badge("streak_3", R.string.badge_streak_3_title, R.string.badge_streak_3_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_7", R.string.badge_streak_7_title, R.string.badge_streak_7_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_14", R.string.badge_streak_14_title, R.string.badge_streak_14_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_30", R.string.badge_streak_30_title, R.string.badge_streak_30_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_60", R.string.badge_streak_60_title, R.string.badge_streak_60_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_90", R.string.badge_streak_90_title, R.string.badge_streak_90_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_180", R.string.badge_streak_180_title, R.string.badge_streak_180_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),
            Badge("streak_365", R.string.badge_streak_365_title, R.string.badge_streak_365_desc, Icons.Default.Whatshot, Color(0xFFFF5722)),

            // --- PUSHUP VOLUME (ŞINAV HACMİ) - Renk: Green ---
            Badge("total_100", R.string.badge_total_100_title, R.string.badge_total_100_desc, Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
            Badge("total_500", R.string.badge_total_500_title, R.string.badge_total_500_desc, Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
            Badge("total_1000", R.string.badge_total_1000_title, R.string.badge_total_1000_desc, Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
            Badge("total_5000", R.string.badge_total_5000_title, R.string.badge_total_5000_desc, Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
            Badge("total_10000", R.string.badge_total_10000_title, R.string.badge_total_10000_desc, Icons.Default.MilitaryTech, Color(0xFFFFD700)),
            Badge("total_25000", R.string.badge_total_25000_title, R.string.badge_total_25000_desc, Icons.Default.MilitaryTech, Color(0xFFFFD700)),
            Badge("total_50000", R.string.badge_total_50000_title, R.string.badge_total_50000_desc, Icons.Default.WorkspacePremium, Color(0xFF9C27B0)),

            // --- CALISTHENICS - Renk: Blue ---
            Badge("cali_1", R.string.badge_cali_1_title, R.string.badge_cali_1_desc, Icons.Default.SelfImprovement, Color(0xFF2196F3)),
            Badge("cali_10", R.string.badge_cali_10_title, R.string.badge_cali_10_desc, Icons.Default.SelfImprovement, Color(0xFF2196F3)),
            Badge("cali_50", R.string.badge_cali_50_title, R.string.badge_cali_50_desc, Icons.Default.SelfImprovement, Color(0xFF2196F3)),
            Badge("cali_100", R.string.badge_cali_100_title, R.string.badge_cali_100_desc, Icons.Default.Star, Color(0xFFFFD700)),
            Badge("cali_dips", R.string.badge_cali_dips_title, R.string.badge_cali_dips_desc, Icons.Default.Bolt, Color(0xFFFFEB3B)),
            Badge("cali_pullup", R.string.badge_cali_pullup_title, R.string.badge_cali_pullup_desc, Icons.Default.VerticalAlignTop, Color(0xFF673AB7)),

            // --- MACHINE & WEIGHT - Renk: Grey/Steel ---
            Badge("weight_1", R.string.badge_weight_1_title, R.string.badge_weight_1_desc, Icons.Default.Settings, Color(0xFF607D8B)),
            Badge("weight_10", R.string.badge_weight_10_title, R.string.badge_weight_10_desc, Icons.Default.Settings, Color(0xFF607D8B)),
            Badge("weight_50", R.string.badge_weight_50_title, R.string.badge_weight_50_desc, Icons.Default.Settings, Color(0xFF607D8B)),
            Badge("weight_100", R.string.badge_weight_100_title, R.string.badge_weight_100_desc, Icons.Default.Stars, Color(0xFFFFD700)),
            Badge("weight_deadlift", R.string.badge_weight_deadlift_title, R.string.badge_weight_deadlift_desc, Icons.Default.Hardware, Color(0xFF795548)),
            Badge("weight_squat", R.string.badge_weight_squat_title, R.string.badge_weight_squat_desc, Icons.Default.AirlineSeatLegroomExtra, Color(0xFFE91E63)),

            // --- WATER - Renk: Cyan/Light Blue ---
            Badge("water_1", R.string.badge_water_1_title, R.string.badge_water_1_desc, Icons.Default.LocalDrink, Color(0xFF03A9F4)),
            Badge("water_7", R.string.badge_water_7_title, R.string.badge_water_7_desc, Icons.Default.LocalDrink, Color(0xFF03A9F4)),
            Badge("water_30", R.string.badge_water_30_title, R.string.badge_water_30_desc, Icons.Default.WaterDrop, Color(0xFF00BCD4)),
            Badge("water_100", R.string.badge_water_100_title, R.string.badge_water_100_desc, Icons.Default.Waves, Color(0xFF009688)),

            // --- PERFORMANCE & TIME - Renk: Purple/Amber ---
            Badge("single_50", R.string.badge_single_50_title, R.string.badge_single_50_desc, Icons.Default.Speed, Color(0xFFFF9800)),
            Badge("single_100", R.string.badge_single_100_title, R.string.badge_single_100_desc, Icons.Default.FlashOn, Color(0xFFFFC107)),
            Badge("early_bird", R.string.badge_early_bird_title, R.string.badge_early_bird_desc, Icons.Default.LightMode, Color(0xFFFFEB3B)),
            Badge("night_owl", R.string.badge_night_owl_title, R.string.badge_night_owl_desc, Icons.Default.DarkMode, Color(0xFF3F51B5)),
            Badge("weekend_warrior", R.string.badge_weekend_warrior_title, R.string.badge_weekend_warrior_desc, Icons.Default.Event, Color(0xFFF44336)),

            // --- SPECIAL & HIDDEN - Renk: Various ---
            Badge("full_week", R.string.badge_full_week_title, R.string.badge_full_week_desc, Icons.Default.DoneAll, Color(0xFF4CAF50)),
            Badge("consistency_100", R.string.badge_consistency_title, R.string.badge_consistency_desc, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF2196F3)),
            Badge("variety", R.string.badge_variety_title, R.string.badge_variety_desc, Icons.Default.Category, Color(0xFF9C27B0)),
            Badge("reborn", R.string.badge_reborn_title, R.string.badge_reborn_desc, Icons.Default.AutoAwesome, Color(0xFFCDDC39)),
            Badge("level_up", R.string.badge_level_up_title, R.string.badge_level_up_desc, Icons.Default.KeyboardDoubleArrowUp, Color(0xFFFF5722)),
            Badge("badge_hunter", R.string.badge_badge_hunter_title, R.string.badge_badge_hunter_desc, Icons.AutoMirrored.Filled.ManageSearch, Color(0xFF00BCD4)),
            Badge("legend", R.string.badge_legend_title, R.string.badge_legend_desc, Icons.Default.EmojiEvents, Color(0xFFFFD700))
        )
    }
}
