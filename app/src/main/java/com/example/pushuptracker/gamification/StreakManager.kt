package com.example.pushuptracker.gamification

import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.PushupRepo
import com.example.pushuptracker.data.repo.WaterRepo
import com.example.pushuptracker.model.ActivityRecord
import com.example.pushuptracker.model.Streak
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakManager @Inject constructor(
    private val pushupRepo: PushupRepo,
    private val waterRepo: WaterRepo,
    private val settingsManager: SettingsManager
) {
    private val today: String get() = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun getStreaksFlow(): Flow<List<Streak>> {
        return combine(
            pushupRepo.getAllPushupRecords(),
            settingsManager.dailyGoalFlow,
            waterRepo.getAllRecords(),
            settingsManager.dailyWaterGoalFlow,
            pushupRepo.getRecordsByType("walking"),
            pushupRepo.getRecordsByType("lymphatic"),
            settingsManager.currentStreakFlow,
            settingsManager.lastWorkoutSummaryFlow
        ) { data ->
            @Suppress("UNCHECKED_CAST")
            val allPushups = data[0] as List<ActivityRecord>
            val dailyPushupGoal = data[1] as Int
            @Suppress("UNCHECKED_CAST")
            val allWater = data[2] as List<ActivityRecord>
            val dailyWaterGoal = data[3] as Int
            @Suppress("UNCHECKED_CAST")
            val allWalking = data[4] as List<ActivityRecord>
            @Suppress("UNCHECKED_CAST")
            val allLymphatic = data[5] as List<ActivityRecord>
            val workoutStreak = data[6] as Int
            val lastWorkoutSummary = data[7] as com.example.pushuptracker.model.WorkoutSummary?

            val achievedPushupDates = allPushups.filter { it.value >= dailyPushupGoal }.map { it.date }.toSet()
            val achievedWaterDates = allWater.filter { it.value >= dailyWaterGoal }.map { it.date }.toSet()
            val achievedWalkingDates = allWalking.filter { it.value >= 33.0 }.map { it.date }.toSet()
            val achievedLymphaticDates = allLymphatic.filter { it.value >= 7.0 }.map { it.date }.toSet()

            listOf(
                Streak(
                    count = calculateCurrentStreak(achievedPushupDates, isDaily = true),
                    isCompletedToday = achievedPushupDates.contains(today),
                    type = Streak.Type.PUSHUP
                ),
                Streak(
                    count = calculateCurrentStreak(achievedWaterDates, isDaily = true),
                    isCompletedToday = achievedWaterDates.contains(today),
                    type = Streak.Type.WATER
                ),
                Streak(
                    count = calculateCurrentStreak(achievedWalkingDates, isDaily = true),
                    isCompletedToday = achievedWalkingDates.contains(today),
                    type = Streak.Type.WALKING
                ),
                Streak(
                    count = calculateCurrentStreak(achievedLymphaticDates, isDaily = true),
                    isCompletedToday = achievedLymphaticDates.contains(today),
                    type = Streak.Type.LYMPHATIC
                ),
                Streak(
                    count = workoutStreak,
                    isCompletedToday = isWorkoutCompletedToday(lastWorkoutSummary),
                    type = Streak.Type.WORKOUT
                )
            )
        }.flowOn(Dispatchers.Default)
    }

    private fun isWorkoutCompletedToday(summary: com.example.pushuptracker.model.WorkoutSummary?): Boolean {
        if (summary == null) return false
        val lastWorkoutDate = Instant.ofEpochMilli(summary.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return lastWorkoutDate == LocalDate.now()
    }

    suspend fun checkAndResetWorkoutStreak() {
        val lastWorkoutSummary = settingsManager.lastWorkoutSummaryFlow.first()
        if (lastWorkoutSummary != null) {
            val lastWorkoutDate = Instant.ofEpochMilli(lastWorkoutSummary.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val todayDate = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(lastWorkoutDate, todayDate)
            
            if (daysBetween > 1) {
                var checkDate = lastWorkoutDate.plusDays(1)
                var isStreakBroken = false
                while(checkDate.isBefore(todayDate)) {
                    val day = checkDate.dayOfWeek
                    val isRestDay = day == DayOfWeek.THURSDAY || day == DayOfWeek.SUNDAY
                    if (!isRestDay) {
                        isStreakBroken = true
                        break
                    }
                    checkDate = checkDate.plusDays(1)
                }
                if (isStreakBroken) {
                    settingsManager.saveCurrentStreak(0)
                }
            }
        } else {
            settingsManager.saveCurrentStreak(0)
        }
    }

    private fun calculateCurrentStreak(dates: Set<String>, isDaily: Boolean): Int {
        if (dates.isEmpty()) return 0
        var streak = 0
        var currentDate = LocalDate.now()

        // Eğer bugün tamamlandıysa seri bugünden başlar.
        // Tamamlanmadıysa düne bakarız.
        if (!dates.contains(currentDate.toString())) {
            currentDate = currentDate.minusDays(1)
        }

        // Geriye dönük sayıyoruz
        while (currentDate != null) {
            val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            if (dates.contains(dateStr)) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (!isDaily && isRestDay(currentDate)) {
                // Dinlenme günü ise seriyi bozma ama sayıya da ekleme (Workout için)
                currentDate = currentDate.minusDays(1)
            } else {
                // Seri bozuldu
                break
            }

            // Güvenlik sınırı (CPU'yu korumak için)
            if (streak > 5000) break
        }
        return streak
    }

    private fun isRestDay(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.THURSDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }
}
