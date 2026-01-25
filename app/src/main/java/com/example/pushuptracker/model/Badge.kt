package com.example.pushuptracker.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
            // --- STREAKS (13 ADET) ---
            Badge("streak_2", R.string.badge_streak_2_title, R.string.badge_streak_2_desc, Icons.Default.Whatshot, Color(0xFFFFCC80)),
            Badge("streak_3", R.string.badge_streak_3_title, R.string.badge_streak_3_desc, Icons.Default.Whatshot, Color(0xFFFFB74D)),
            Badge("streak_5", R.string.badge_streak_5_title, R.string.badge_streak_5_desc, Icons.Default.Whatshot, Color(0xFFFFA726)),
            Badge("streak_7", R.string.badge_streak_7_title, R.string.badge_streak_7_desc, Icons.Default.Whatshot, Color(0xFFFF9800)),
            Badge("streak_10", R.string.badge_streak_10_title, R.string.badge_streak_10_desc, Icons.Default.Whatshot, Color(0xFFFB8C00)),
            Badge("streak_14", R.string.badge_streak_14_title, R.string.badge_streak_14_desc, Icons.Default.Whatshot, Color(0xFFF57C00)),
            Badge("streak_30", R.string.badge_streak_30_title, R.string.badge_streak_30_desc, Icons.Default.Whatshot, Color(0xFFEF6C00)),
            Badge("streak_60", R.string.badge_streak_60_title, R.string.badge_streak_60_desc, Icons.Default.Whatshot, Color(0xFFE65100)),
            Badge("streak_90", R.string.badge_streak_90_title, R.string.badge_streak_90_desc, Icons.Default.LocalFireDepartment, Color(0xFFFF3D00)),
            Badge("streak_100", R.string.badge_streak_100_title, R.string.badge_streak_100_desc, Icons.Default.LocalFireDepartment, Color(0xFFFF3D00)),
            Badge("streak_180", R.string.badge_streak_180_title, R.string.badge_streak_180_desc, Icons.Default.AutoAwesome, Color(0xFFD50000)),
            Badge("streak_200", R.string.badge_streak_200_title, R.string.badge_streak_200_desc, Icons.Default.AutoAwesome, Color(0xFFD50000)),
            Badge("streak_365", R.string.badge_streak_365_title, R.string.badge_streak_365_desc, Icons.Default.EmojiEvents, Color(0xFFFFD700)),

            // --- PUSHUP VOLUME (14 ADET) ---
            Badge("total_100", R.string.badge_total_100_title, R.string.badge_total_100_desc, Icons.Default.FitnessCenter, Color(0xFFC8E6C9)),
            Badge("total_250", R.string.badge_total_250_title, R.string.badge_total_250_desc, Icons.Default.FitnessCenter, Color(0xFFA5D6A7)),
            Badge("total_500", R.string.badge_total_500_title, R.string.badge_total_500_desc, Icons.Default.FitnessCenter, Color(0xFF81C784)),
            Badge("total_750", R.string.badge_total_750_title, R.string.badge_total_750_desc, Icons.Default.FitnessCenter, Color(0xFF66BB6A)),
            Badge("total_1000", R.string.badge_total_1000_title, R.string.badge_total_1000_desc, Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
            Badge("total_2500", R.string.badge_total_2500_title, R.string.badge_total_2500_desc, Icons.Default.FitnessCenter, Color(0xFF43A047)),
            Badge("total_5000", R.string.badge_total_5000_title, R.string.badge_total_5000_desc, Icons.Default.MilitaryTech, Color(0xFF388E3C)),
            Badge("total_7500", R.string.badge_total_7500_title, R.string.badge_total_7500_desc, Icons.Default.MilitaryTech, Color(0xFF2E7D32)),
            Badge("total_10000", R.string.badge_total_10000_title, R.string.badge_total_10000_desc, Icons.Default.MilitaryTech, Color(0xFF1B5E20)),
            Badge("total_25000", R.string.badge_total_25000_title, R.string.badge_total_25000_desc, Icons.Default.WorkspacePremium, Color(0xFFFFD600)),
            Badge("total_50000", R.string.badge_total_50000_title, R.string.badge_total_50000_desc, Icons.Default.WorkspacePremium, Color(0xFFFFAB00)),
            Badge("total_100000", R.string.badge_total_100000_title, R.string.badge_total_100000_desc, Icons.Default.AutoGraph, Color(0xFFAA00FF)),
            Badge("single_50", R.string.badge_single_50_title, R.string.badge_single_50_desc, Icons.Default.Speed, Color(0xFF64FFDA)),
            Badge("single_100", R.string.badge_single_100_title, R.string.badge_single_100_desc, Icons.Default.FlashOn, Color(0xFF00BFA5)),

            // --- WALKING (13 ADET) ---
            Badge("walk_1", R.string.badge_walk_1_title, R.string.badge_walk_1_desc, Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFFB2EBF2)),
            Badge("walk_10", R.string.badge_walk_10_title, R.string.badge_walk_10_desc, Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFF80DEEA)),
            Badge("walk_25", R.string.badge_walk_25_title, R.string.badge_walk_25_desc, Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFF4DD0E1)),
            Badge("walk_50", R.string.badge_walk_50_title, R.string.badge_walk_50_desc, Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFF26C6DA)),
            Badge("walk_75", R.string.badge_walk_75_title, R.string.badge_walk_75_desc, Icons.AutoMirrored.Filled.DirectionsWalk, Color(0xFF00BCD4)),
            Badge("walk_100", R.string.badge_walk_100_title, R.string.badge_walk_100_desc, Icons.Default.Star, Color(0xFF00ACC1)),
            Badge("walk_33", R.string.badge_walk_33_title, R.string.badge_walk_33_desc, Icons.Default.Timer, Color(0xFF0097A7)),
            Badge("walk_1000", R.string.badge_walk_1000_title, R.string.badge_walk_1000_desc, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF00838F)),
            Badge("walk_2500", R.string.badge_walk_2500_title, R.string.badge_walk_2500_desc, Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF006064)),
            Badge("walk_5000", R.string.badge_walk_5000_title, R.string.badge_walk_5000_desc, Icons.Default.EmojiEvents, Color(0xFFFFD600)),
            Badge("walk_10000", R.string.badge_walk_10000_title, R.string.badge_walk_10000_desc, Icons.Default.AutoAwesome, Color(0xFFFFAB00)),
            Badge("walk_morning", R.string.badge_walk_morning_title, R.string.badge_walk_morning_desc, Icons.Default.LightMode, Color(0xFFFFEB3B)),
            Badge("walk_night", R.string.badge_walk_night_title, R.string.badge_walk_night_desc, Icons.Default.DarkMode, Color(0xFF3F51B5)),

            // --- LYMPHATIC (10 ADET) ---
            Badge("lymphatic_1", R.string.badge_lymphatic_1_title, R.string.badge_lymphatic_1_desc, Icons.Default.Spa, Color(0xFFE1BEE7)),
            Badge("lymphatic_7", R.string.badge_lymphatic_7_title, R.string.badge_lymphatic_7_desc, Icons.Default.Spa, Color(0xFFCE93D8)),
            Badge("lymphatic_14", R.string.badge_lymphatic_14_title, R.string.badge_lymphatic_14_desc, Icons.Default.Spa, Color(0xFFBA68C8)),
            Badge("lymphatic_30", R.string.badge_lymphatic_30_title, R.string.badge_lymphatic_30_desc, Icons.Default.Healing, Color(0xFFAB47BC)),
            Badge("lymphatic_50", R.string.badge_lymphatic_50_title, R.string.badge_lymphatic_50_desc, Icons.Default.Healing, Color(0xFF9C27B0)),
            Badge("lymphatic_100", R.string.badge_lymphatic_100_title, R.string.badge_lymphatic_100_desc, Icons.Default.AutoAwesome, Color(0xFF8E24AA)),
            Badge("lymphatic_morning", R.string.badge_lymphatic_morning_title, R.string.badge_lymphatic_morning_desc, Icons.Default.LightMode, Color(0xFF7B1FA2)),
            Badge("lymphatic_streak_3", R.string.badge_lymphatic_streak_3_title, R.string.badge_lymphatic_streak_3_desc, Icons.Default.Whatshot, Color(0xFF6A1B9A)),
            Badge("lymphatic_streak_7", R.string.badge_lymphatic_streak_7_title, R.string.badge_lymphatic_streak_7_desc, Icons.Default.Whatshot, Color(0xFF4A148C)),
            Badge("lymphatic_total_70", R.string.badge_lymphatic_total_70_title, R.string.badge_lymphatic_total_70_desc, Icons.Default.Timer, Color(0xFFEA80FC)),

            // --- TONAGE & WEIGHT (10 ADET) ---
            Badge("ton_1", R.string.badge_ton_1_title, R.string.badge_ton_1_desc, Icons.Default.FitnessCenter, Color(0xFFCFD8DC)),
            Badge("ton_5", R.string.badge_ton_5_title, R.string.badge_ton_5_desc, Icons.Default.FitnessCenter, Color(0xFFB0BEC5)),
            Badge("ton_10", R.string.badge_ton_10_title, R.string.badge_ton_10_desc, Icons.Default.FitnessCenter, Color(0xFF90A4AE)),
            Badge("ton_25", R.string.badge_ton_25_title, R.string.badge_ton_25_desc, Icons.Default.FitnessCenter, Color(0xFF78909C)),
            Badge("ton_50", R.string.badge_ton_50_title, R.string.badge_ton_50_desc, Icons.Default.FitnessCenter, Color(0xFF607D8B)),
            Badge("ton_75", R.string.badge_ton_75_title, R.string.badge_ton_75_desc, Icons.Default.FitnessCenter, Color(0xFF546E7A)),
            Badge("ton_100", R.string.badge_ton_100_title, R.string.badge_ton_100_desc, Icons.Default.Stars, Color(0xFF455A64)),
            Badge("ton_250", R.string.badge_ton_250_title, R.string.badge_ton_250_desc, Icons.Default.Stars, Color(0xFF37474F)),
            Badge("ton_500", R.string.badge_ton_500_title, R.string.badge_ton_500_desc, Icons.Default.AutoGraph, Color(0xFF263238)),
            Badge("whale", R.string.badge_whale_title, R.string.badge_whale_desc, Icons.Default.Anchor, Color(0xFF03A9F4)),

            // --- CALORIE (10 ADET) ---
            Badge("cal_500", R.string.badge_cal_500_title, R.string.badge_cal_500_desc, Icons.Default.LocalFireDepartment, Color(0xFFFFCCBC)),
            Badge("cal_1000", R.string.badge_cal_1000_title, R.string.badge_cal_1000_desc, Icons.Default.LocalFireDepartment, Color(0xFFFFAB91)),
            Badge("cal_2500", R.string.badge_cal_2500_title, R.string.badge_cal_2500_desc, Icons.Default.LocalFireDepartment, Color(0xFFFF8A65)),
            Badge("cal_5000", R.string.badge_cal_5000_title, R.string.badge_cal_5000_desc, Icons.Default.LocalFireDepartment, Color(0xFFFF7043)),
            Badge("cal_10000", R.string.badge_cal_10000_title, R.string.badge_cal_10000_desc, Icons.Default.LocalFireDepartment, Color(0xFFFF5722)),
            Badge("cal_25000", R.string.badge_cal_25000_title, R.string.badge_cal_25000_desc, Icons.Default.LocalFireDepartment, Color(0xFFF4511E)),
            Badge("cal_50000", R.string.badge_cal_50000_title, R.string.badge_cal_50000_desc, Icons.Default.LocalFireDepartment, Color(0xFFE64A19)),
            Badge("cal_75000", R.string.badge_cal_75000_title, R.string.badge_cal_75000_desc, Icons.Default.LocalFireDepartment, Color(0xFFD84315)),
            Badge("cal_100000", R.string.badge_cal_100000_title, R.string.badge_cal_100000_desc, Icons.Default.AutoAwesome, Color(0xFFBF360C)),
            Badge("cal_200000", R.string.badge_cal_200000_title, R.string.badge_cal_200000_desc, Icons.Default.AutoAwesome, Color(0xFFFFD600)),

            // --- EXPLORER (8 ADET) ---
            Badge("explore_1", R.string.badge_explore_1_title, R.string.badge_explore_1_desc, Icons.Default.Explore, Color(0xFFE3F2FD)),
            Badge("explore_5", R.string.badge_explore_5_title, R.string.badge_explore_5_desc, Icons.Default.Explore, Color(0xFFBBDEFB)),
            Badge("explore_10", R.string.badge_explore_10_title, R.string.badge_explore_10_desc, Icons.Default.Explore, Color(0xFF90CAF9)),
            Badge("explore_15", R.string.badge_explore_15_title, R.string.badge_explore_15_desc, Icons.Default.Explore, Color(0xFF64B5F6)),
            Badge("explore_20", R.string.badge_explore_20_title, R.string.badge_explore_20_desc, Icons.Default.Explore, Color(0xFF42A5F5)),
            Badge("explore_30", R.string.badge_explore_30_title, R.string.badge_explore_30_desc, Icons.Default.Explore, Color(0xFF2196F3)),
            Badge("explore_50", R.string.badge_explore_50_title, R.string.badge_explore_50_desc, Icons.Default.Explore, Color(0xFF1E88E5)),
            Badge("body_engineer", R.string.badge_body_engineer_title, R.string.badge_body_engineer_desc, Icons.Default.Build, Color(0xFF9C27B0)),

            // --- EXERCISE MASTERS (15 ADET) ---
            Badge("master_dips", R.string.badge_master_dips_title, R.string.badge_master_dips_desc, Icons.Default.Bolt, Color(0xFFFFEB3B)),
            Badge("master_pullup", R.string.badge_master_pullup_title, R.string.badge_master_pullup_desc, Icons.Default.VerticalAlignTop, Color(0xFF673AB7)),
            Badge("master_squat", R.string.badge_master_squat_title, R.string.badge_master_squat_desc, Icons.Default.AirlineSeatLegroomExtra, Color(0xFFE91E63)),
            Badge("master_plank", R.string.badge_master_plank_title, R.string.badge_master_plank_desc, Icons.Default.AccessibilityNew, Color(0xFF4CAF50)),
            Badge("master_pushup_200", R.string.badge_master_pushup_200_title, R.string.badge_master_pushup_200_desc, Icons.Default.Speed, Color(0xFFFF5722)),
            Badge("master_bench_10", R.string.badge_master_bench_10_title, R.string.badge_master_bench_10_desc, Icons.Default.Settings, Color(0xFF607D8B)),
            Badge("master_bench_50", R.string.badge_master_bench_50_title, R.string.badge_master_bench_50_desc, Icons.Default.Settings, Color(0xFF607D8B)),
            Badge("master_bench_100", R.string.badge_master_bench_100_title, R.string.badge_master_bench_100_desc, Icons.Default.Settings, Color(0xFF607D8B)),
            Badge("master_deadlift_10", R.string.badge_master_deadlift_10_title, R.string.badge_master_deadlift_10_desc, Icons.Default.Hardware, Color(0xFF795548)),
            Badge("master_deadlift_50", R.string.badge_master_deadlift_50_title, R.string.badge_master_deadlift_50_desc, Icons.Default.Hardware, Color(0xFF795548)),
            Badge("master_shoulder_10", R.string.badge_master_shoulder_10_title, R.string.badge_master_shoulder_10_desc, Icons.Default.ControlPoint, Color(0xFF3F51B5)),
            Badge("master_shoulder_50", R.string.badge_master_shoulder_50_title, R.string.badge_master_shoulder_50_desc, Icons.Default.ControlPoint, Color(0xFF3F51B5)),
            Badge("master_lunges_50", R.string.badge_master_lunges_50_title, R.string.badge_master_lunges_50_desc, Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFFFF4081)),
            Badge("master_burpee_50", R.string.badge_master_burpee_50_title, R.string.badge_master_burpee_50_desc, Icons.Default.FlashOn, Color(0xFFFFC107)),
            Badge("master_burpee_250", R.string.badge_master_burpee_250_title, R.string.badge_master_burpee_250_desc, Icons.Default.FlashOn, Color(0xFFFFC107)),

            // --- SPECIAL TIMES (6 ADET) ---
            Badge("early_bird", R.string.badge_early_bird_title, R.string.badge_early_bird_desc, Icons.Default.LightMode, Color(0xFFFFEB3B)),
            Badge("night_owl", R.string.badge_night_owl_title, R.string.badge_night_owl_desc, Icons.Default.DarkMode, Color(0xFF3F51B5)),
            Badge("lunch_warrior", R.string.badge_lunch_warrior_title, R.string.badge_lunch_warrior_desc, Icons.Default.Restaurant, Color(0xFFFF9800)),
            Badge("weekend_warrior", R.string.badge_weekend_warrior_title, R.string.badge_weekend_warrior_desc, Icons.Default.Event, Color(0xFFF44336)),
            Badge("monday_king", R.string.badge_monday_king_title, R.string.badge_monday_king_desc, Icons.Default.CalendarMonth, Color(0xFF4CAF50)),
            Badge("new_year", R.string.badge_new_year_title, R.string.badge_new_year_desc, Icons.Default.Celebration, Color(0xFFFFD700)),

            // --- WATER (8 ADET) ---
            Badge("water_1", R.string.badge_water_1_title, R.string.badge_water_1_desc, Icons.Default.LocalDrink, Color(0xFFE1F5FE)),
            Badge("water_3", R.string.badge_water_3_title, R.string.badge_water_3_desc, Icons.Default.LocalDrink, Color(0xFFB3E5FC)),
            Badge("water_7", R.string.badge_water_7_title, R.string.badge_water_7_desc, Icons.Default.LocalDrink, Color(0xFF81D4FA)),
            Badge("water_14", R.string.badge_water_14_title, R.string.badge_water_14_desc, Icons.Default.LocalDrink, Color(0xFF4FC3F7)),
            Badge("water_30", R.string.badge_water_30_title, R.string.badge_water_30_desc, Icons.Default.WaterDrop, Color(0xFF29B6F6)),
            Badge("water_60", R.string.badge_water_60_title, R.string.badge_water_60_desc, Icons.Default.WaterDrop, Color(0xFF03A9F4)),
            Badge("water_100", R.string.badge_water_100_title, R.string.badge_water_100_desc, Icons.Default.Waves, Color(0xFF039BE5)),
            Badge("water_200", R.string.badge_water_200_title, R.string.badge_water_200_desc, Icons.Default.Waves, Color(0xFF0288D1)),

            // --- META & LEVELS (11 ADET) ---
            Badge("level_1", R.string.badge_level_1_title, R.string.badge_level_1_desc, Icons.Default.EmojiEvents, Color(0xFFBDBDBD)),
            Badge("level_5", R.string.badge_level_5_title, R.string.badge_level_5_desc, Icons.Default.EmojiEvents, Color(0xFF9E9E9E)),
            Badge("level_bronze", R.string.badge_level_bronze_title, R.string.badge_level_bronze_desc, Icons.Default.EmojiEvents, Color(0xFFCD7F32)),
            Badge("level_20", R.string.badge_level_20_title, R.string.badge_level_20_desc, Icons.Default.EmojiEvents, Color(0xFF757575)),
            Badge("level_silver", R.string.badge_level_silver_title, R.string.badge_level_silver_desc, Icons.Default.EmojiEvents, Color(0xFFC0C0C0)),
            Badge("level_40", R.string.badge_level_40_title, R.string.badge_level_40_desc, Icons.Default.EmojiEvents, Color(0xFF616161)),
            Badge("level_gold", R.string.badge_level_gold_title, R.string.badge_level_gold_desc, Icons.Default.EmojiEvents, Color(0xFFFFD700)),
            Badge("level_70", R.string.badge_level_70_title, R.string.badge_level_70_desc, Icons.Default.Diamond, Color(0xFFB2DFDB)),
            Badge("level_80", R.string.badge_level_80_title, R.string.badge_level_80_desc, Icons.Default.Diamond, Color(0xFF4DB6AC)),
            Badge("level_platinum", R.string.badge_level_platinum_title, R.string.badge_level_platinum_desc, Icons.Default.Diamond, Color(0xFFE5E4E2)),
            Badge("legend", R.string.badge_legend_title, R.string.badge_legend_desc, Icons.Default.AutoGraph, Color(0xFF9C27B0))
        )
    }
}
