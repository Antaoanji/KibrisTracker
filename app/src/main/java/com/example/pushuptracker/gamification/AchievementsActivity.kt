package com.example.pushuptracker.gamification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pushuptracker.R
import com.example.pushuptracker.SettingsManager
import com.example.pushuptracker.ui.theme.PushupTrackerTheme

data class Badge(val id: String, val title: String, val description: String, val icon: ImageVector)

val allBadges = listOf(
    Badge(
        id = "streak_7_day",
        title = "Azim Abidesi",
        description = "7 gün üst üste hedefe ulaştın.",
        icon = Icons.Default.WorkspacePremium
    )
    // TODO: Add more badges here
)

class AchievementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PushupTrackerTheme {
                AchievementsScreen(onNavigateUp = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val unlockedBadges by settingsManager.unlockedBadgesFlow.collectAsStateWithLifecycle(initialValue = emptySet())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.achievements)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allBadges) { badge ->
                val isUnlocked = unlockedBadges.contains(badge.id)
                ListItem(
                    modifier = Modifier.alpha(if (isUnlocked) 1f else 0.5f),
                    headlineContent = { Text(badge.title) },
                    supportingContent = { Text(badge.description) },
                    leadingContent = {
                        Icon(
                            badge.icon, 
                            contentDescription = badge.title,
                            tint = if (isUnlocked) MaterialTheme.colorScheme.secondary else LocalContentColor.current
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
