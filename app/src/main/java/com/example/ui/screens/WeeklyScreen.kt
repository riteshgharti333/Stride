package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WeeklySummary
import com.example.ui.components.MinimalStatTile
import com.example.ui.components.MinimalWeeklyBarChart
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyScreen(
    weeklySummary: WeeklySummary,
    dailyGoal: Int,
    strideLengthCm: Int,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val primaryBlue = MaterialTheme.colorScheme.primary
    val accentRed = MaterialTheme.colorScheme.tertiary

    val selectedDay = weeklySummary.days.getOrNull(selectedDayIndex)
    val selectedDaySteps = selectedDay?.steps ?: 0
    val selectedDayDistance = (selectedDaySteps * strideLengthCm) / 100000f
    val selectedDayCalories = (selectedDaySteps * 0.04f).toInt()
    val isSelectedDayGoalReached = selectedDaySteps >= dailyGoal && dailyGoal > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .testTag("weekly_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Week Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier.testTag("btn_prev_week")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous week",
                    tint = primaryBlue
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "WEEKLY OVERVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryBlue
                )
                Text(
                    text = weeklySummary.weekRangeLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = onNextWeek,
                enabled = weeklySummary.canGoForward,
                modifier = Modifier.testTag("btn_next_week")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next week",
                    tint = if (weeklySummary.canGoForward) primaryBlue
                    else MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Minimal 7-Day Bar Chart
        MinimalWeeklyBarChart(
            days = weeklySummary.days,
            dailyGoal = dailyGoal,
            selectedIndex = selectedDayIndex,
            onSelectDay = onSelectDay
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Selected Day Details Card
        if (selectedDay != null) {
            val dateFormatted = selectedDay.date.format(
                DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
                    .testTag("selected_day_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormatted.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSelectedDayGoalReached) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GOAL REACHED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = accentRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format("%,d", selectedDaySteps),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryBlue
                        )
                        Text(
                            text = " steps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distance: ${String.format(Locale.US, "%.2f", selectedDayDistance)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Energy: ${String.format("%,d", selectedDayCalories)} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Key Weekly Metrics
        Text(
            text = "SUMMARY METRICS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MinimalStatTile(
                label = "Week Total",
                value = String.format("%,d", weeklySummary.totalSteps),
                unit = "steps",
                icon = Icons.Outlined.Equalizer,
                accentColor = primaryBlue,
                modifier = Modifier.weight(1f),
                testTag = "tile_week_total"
            )

            MinimalStatTile(
                label = "Daily Avg",
                value = String.format("%,d", weeklySummary.averageStepsPerDay),
                unit = "steps",
                icon = Icons.Outlined.Speed,
                accentColor = primaryBlue,
                modifier = Modifier.weight(1f),
                testTag = "tile_daily_avg"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MinimalStatTile(
                label = "Peak Day",
                value = weeklySummary.mostActiveDayLabel,
                unit = "",
                icon = Icons.Outlined.Leaderboard,
                accentColor = accentRed,
                modifier = Modifier.weight(1f),
                testTag = "tile_peak_day"
            )

            MinimalStatTile(
                label = "Goals Met",
                value = "${weeklySummary.daysGoalMet}/7",
                unit = "days",
                icon = Icons.Outlined.CheckCircle,
                accentColor = accentRed,
                modifier = Modifier.weight(1f),
                testTag = "tile_goals_met"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
