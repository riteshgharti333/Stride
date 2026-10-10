package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.StepTrackingService
import com.example.ui.MainViewModel
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeeklyScreen
import com.example.ui.theme.StrideTheme

enum class StrideTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TODAY(
        title = "Today",
        selectedIcon = Icons.Filled.DirectionsWalk,
        unselectedIcon = Icons.Outlined.DirectionsWalk,
        testTag = "tab_today"
    ),
    WEEKLY(
        title = "Weekly",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "tab_weekly"
    ),
    HISTORY(
        title = "History",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarToday,
        testTag = "tab_history"
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Filled.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        testTag = "tab_settings"
    )
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeModeFlow.collectAsStateWithLifecycle()

            StrideTheme(themePreference = themeMode) {
                MainContent(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Only start background service if permission is granted and feature is enabled
        if (StepTrackingService.hasRequiredPermissions(this) && viewModel.backgroundServiceFlow.value) {
            StepTrackingService.start(this)
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(StrideTab.TODAY) }

    // Observe State
    val todaySteps by viewModel.todayStepsFlow.collectAsStateWithLifecycle()
    val stepGoal by viewModel.stepGoalFlow.collectAsStateWithLifecycle()
    val strideLengthCm by viewModel.strideLengthFlow.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeModeFlow.collectAsStateWithLifecycle()
    val isBackgroundServiceEnabled by viewModel.backgroundServiceFlow.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.weeklySummaryFlow.collectAsStateWithLifecycle()
    val selectedDayIndex by viewModel.selectedDayIndexFlow.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistoryFlow.collectAsStateWithLifecycle()
    val diagnostics by viewModel.sensorDiagnosticsFlow.collectAsStateWithLifecycle()

    // Permission state
    var hasActivityPermission by remember {
        mutableStateOf(StepTrackingService.hasRequiredPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val activityGranted = results[Manifest.permission.ACTIVITY_RECOGNITION] ?: hasActivityPermission
        hasActivityPermission = activityGranted
        if (activityGranted && isBackgroundServiceEnabled) {
            StepTrackingService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Back button pops back to Today screen if on a sub-screen
    BackHandler(enabled = selectedTab != StrideTab.TODAY) {
        selectedTab = StrideTab.TODAY
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                StrideTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(
                targetState = selectedTab,
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    StrideTab.TODAY -> {
                        HomeScreen(
                            steps = todaySteps,
                            goal = stepGoal,
                            strideLengthCm = strideLengthCm,
                            isSensorActive = diagnostics.isListening,
                            onAddSteps = { viewModel.addManualSteps(it) }
                        )
                    }

                    StrideTab.WEEKLY -> {
                        WeeklyScreen(
                            weeklySummary = weeklySummary,
                            dailyGoal = stepGoal,
                            strideLengthCm = strideLengthCm,
                            selectedDayIndex = selectedDayIndex,
                            onSelectDay = { viewModel.setSelectedDayIndex(it) },
                            onPreviousWeek = { viewModel.previousWeek() },
                            onNextWeek = { viewModel.nextWeek() }
                        )
                    }

                    StrideTab.HISTORY -> {
                        HistoryScreen(
                            historyList = allHistory,
                            dailyGoal = stepGoal
                        )
                    }

                    StrideTab.SETTINGS -> {
                        SettingsScreen(
                            stepGoal = stepGoal,
                            strideLengthCm = strideLengthCm,
                            themeMode = themeMode,
                            isBackgroundServiceEnabled = isBackgroundServiceEnabled,
                            diagnostics = diagnostics,
                            hasActivityPermission = hasActivityPermission,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
                                    )
                                }
                            },
                            onStepGoalChange = { viewModel.setStepGoal(it) },
                            onStrideLengthChange = { viewModel.setStrideLengthCm(it) },
                            onThemeChange = { viewModel.setThemeMode(it) },
                            onToggleBackgroundService = { viewModel.toggleBackgroundService(it) },
                            onResetTodaySteps = { viewModel.resetTodaySteps() }
                        )
                    }
                }
            }
        }
    }
}
