package com.example.pushuptracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pushuptracker.gamification.GamificationManager
import com.example.pushuptracker.model.Badge
import com.example.pushuptracker.navigation.NavGraph
import com.example.pushuptracker.navigation.Screen
import com.example.pushuptracker.ui.programs.ProgramsViewModel
import com.example.pushuptracker.ui.theme.PushupTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var gamificationManager: GamificationManager

    private val programsViewModel: ProgramsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            PushupTrackerTheme {
                val context = LocalContext.current
                
                // İzin Durumları
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    } else {
                        mutableStateOf(true)
                    }
                }
                
                var hasAudioPermission by remember {
                    mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                }

                // İzin Başlatıcıları
                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { hasAudioPermission = it }
                )

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { hasNotificationPermission = it }
                )

                // İzin İstek Akışı
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (!hasAudioPermission) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(gamificationManager)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.action == Intent.ACTION_VIEW) {
                val programType = it.getStringExtra("programType")
                val dayName = it.getStringExtra("dayName")
                if (programType != null || dayName != null) {
                    programsViewModel.handleVoiceCommand(programType, dayName)
                }
            }
        }
    }
}

@Composable
fun MainScreen(gamificationManager: GamificationManager) {
    val navController = rememberNavController()
    val screens = remember { listOf(Screen.Home, Screen.Programs, Screen.Stats, Screen.Achievements, Screen.Profile) }

    // Global Badge State
    var activeBadge by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(Unit) {
        gamificationManager.newBadgeUnlocked.collect { badge ->
            activeBadge = badge
            delay(5000) // 5 saniye göster
            activeBadge = null
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    screen.icon?.let { icon ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = stringResource(id = screen.titleRes)) },
                            label = { Text(stringResource(id = screen.titleRes), style = MaterialTheme.typography.labelSmall) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)){
            NavGraph(navController = navController)

            // --- Global Achievement Notification UI ---
            AnimatedVisibility(
                visible = activeBadge != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).zIndex(100f)
            ) {
                activeBadge?.let { badge ->
                    AchievementToast(badge = badge)
                }
            }
        }
    }
}

@Composable
fun AchievementToast(badge: Badge) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradyan Arka Plan (Hafif Rozet Rengiyle)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(badge.color.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rozet İkonu
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = badge.color.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = badge.icon,
                        contentDescription = null,
                        tint = badge.color,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BAŞARIM KAZANILDI!",
                        style = MaterialTheme.typography.labelSmall,
                        color = badge.color,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stringResource(id = badge.title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = badge.color.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
