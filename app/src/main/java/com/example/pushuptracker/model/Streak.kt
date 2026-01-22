package com.example.pushuptracker.model

data class Streak(
    val count: Int,
    val isCompletedToday: Boolean,
    val type: Type
) {
    enum class Type {
        PUSHUP,
        WATER,
        WORKOUT,
        WALKING // NEW: Japon Yürüyüşü için tip eklendi
    }
}
