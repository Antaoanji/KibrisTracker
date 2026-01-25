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
        WALKING,
        LYMPHATIC // NEW: Çin Lenfatik Egzersizi için tip eklendi
    }
}
