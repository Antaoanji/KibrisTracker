package com.example.pushuptracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.data.repo.PushupRepo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class PushupWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun settingsManager(): SettingsManager
        fun pushupRepo(): PushupRepo
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val settingsManager = entryPoint.settingsManager()

        provideContent {
            val remaining by settingsManager.remainingPushupsFlow.collectAsState(initial = 0.0)
            WidgetUI(remaining.toInt())
        }
    }

    @Composable
    private fun WidgetUI(remaining: Int) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(com.example.pushuptracker.R.drawable.rounded_widget_bg))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KALAN",
                    style = TextStyle(
                        color = ColorProvider(Color.Gray),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = remaining.toString(),
                    style = TextStyle(
                        color = ColorProvider(Color.Cyan),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(GlanceModifier.width(8.dp))

            // Ekle Butonu
            Button(
                text = "+5",
                onClick = actionRunCallback<UpdatePushupAction>(
                    actionParametersOf(ActionParameters.Key<Int>("amount") to 5)
                ),
                modifier = GlanceModifier.width(60.dp)
            )
            
            Spacer(GlanceModifier.width(8.dp))
            
            // Tamamla Butonu
            Button(
                text = "✓",
                onClick = actionRunCallback<UpdatePushupAction>(
                    actionParametersOf(ActionParameters.Key<Int>("amount") to -5)
                ),
                modifier = GlanceModifier.width(60.dp)
            )
        }
    }
}

class UpdatePushupAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val amount = parameters[ActionParameters.Key<Int>("amount")] ?: return
        
        val entryPoint = EntryPointAccessors.fromApplication(context, PushupWidget.WidgetEntryPoint::class.java)
        val settingsManager = entryPoint.settingsManager()
        val pushupRepo = entryPoint.pushupRepo()

        val currentRemaining = settingsManager.remainingPushupsFlow.first()

        if (amount > 0) {
            settingsManager.saveRemainingPushups(currentRemaining + amount)
        } else {
            val toComplete = (-amount).toDouble()
            settingsManager.saveRemainingPushups((currentRemaining - toComplete).coerceAtLeast(0.0))
            pushupRepo.addPushups(toComplete)
        }

        // Tüm widget'ları güncelle
        PushupWidget().updateAll(context)
    }
}

class PushupWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PushupWidget()
}
