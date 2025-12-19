package com.example.pushuptracker.model

data class ExerciseProgram(
    val id: String,
    val title: String,
    val description: String,
    val durationInWeeks: Int,
    val daysPerWeek: Int,
    val dailyWorkouts: List<ProgramDay>
)

data class ProgramDay(
    val day: Int,
    val title: String,
    val description: String,
)

// A static data source for now.
object ProgramsRepo {
    val programs = listOf(
        ExerciseProgram(
            id = "beginner_pushup_1",
            title = "Yeni Başlayanlar için Şınav Gücü",
            description = "Sıfırdan başlayarak ilk 20 şınavınıza ulaşmanızı sağlayacak 4 haftalık program.",
            durationInWeeks = 4,
            daysPerWeek = 3,
            dailyWorkouts = listOf(
                ProgramDay(day = 1, title = "Day 1: Temel Güç", description = "5 Set, Maksimum Tekrar (Hedef: 5-8)"),
                ProgramDay(day = 2, title = "Day 2: Dayanıklılık", description = "3 Set, Maksimum Tekrar (Hedef: 10-12, dizler yerde)"),
                ProgramDay(day = 3, title = "Day 3: Form Odaklı", description = "4 Set, 5 Tekrar (Yavaş ve kontrollü)"),
            )
        ),
        ExerciseProgram(
            id = "intermediate_core_2",
            title = "Orta Seviye Karın Kası Programı",
            description = "Karın kaslarınızı güçlendirmek ve plank sürenizi artırmak için 2 haftalık yoğun program.",
            durationInWeeks = 2,
            daysPerWeek = 4,
            dailyWorkouts = listOf(
                ProgramDay(day = 1, title = "Day 1: Plank ve Mekik", description = "Plank: 3 Set, 45sn | Mekik: 3 Set, 15 Tekrar"),
                ProgramDay(day = 2, title = "Day 2: Bacak Kaldırma", description = "Yatarak Bacak Kaldırma: 4 Set, 12 Tekrar"),
                ProgramDay(day = 3, title = "Day 3: Dinamik Karın", description = "Russian Twist: 3 Set, 20 Tekrar (10 sağ, 10 sol)"),
                ProgramDay(day = 4, title = "Day 4: Tam Vücut Direnci", description = "Plank: 3 Set, 60sn | Dağ Tırmanışı: 3 Set, 30sn"),
            )
        )
    )
}