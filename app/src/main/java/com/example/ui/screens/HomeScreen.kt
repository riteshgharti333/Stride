package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MinimalProgressRing
import com.example.ui.components.MinimalStatTile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    steps: Int,
    goal: Int,
    strideLengthCm: Int,
    isSensorActive: Boolean,
    onAddSteps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val distanceKm = (steps * strideLengthCm) / 100000f
    val calories = (steps * 0.04f).toInt()
    val progress = if (goal > 0) steps.toFloat() / goal else 0f
    val remainingSteps = maxOf(0, goal - steps)
    val isGoalAchieved = steps >= goal && goal > 0

    val primaryBlue = MaterialTheme.colorScheme.primary
    val accentRed = MaterialTheme.colorScheme.tertiary

    val todayFormatted = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .testTag("home_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Date & Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryBlue
                )
                Text(
                    text = todayFormatted,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Low Power Hardware Sensor Badge (White surface with Blue/Red status)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSensorActive) primaryBlue else accentRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSensorActive) "HW SENSOR" else "STANDBY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Hero Minimalist Progress Ring (Cobalt Blue Arc + White Center + Red Pip)
        MinimalProgressRing(
            progress = progress,
            steps = steps,
            goal = goal,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Goal Status Subtext & Achievement Indicator
        if (isGoalAchieved) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f))
                        .border(
                            1.dp,
                            accentRed.copy(alpha = 0.5f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("goal_achieved_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Goal reached",
                            tint = accentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily goal completed!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${String.format("%,d", remainingSteps)} steps remaining",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("steps_remaining_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Metric Tiles Grid (Distance in Blue, Energy in Red, Goal in Blue, Done in Red)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MinimalStatTile(
                label = "Distance",
                value = String.format(Locale.US, "%.2f", distanceKm),
                unit = "km",
                icon = Icons.Outlined.Route,
                accentColor = primaryBlue,
                modifier = Modifier.weight(1f),
                testTag = "tile_distance"
            )

            MinimalStatTile(
                label = "Energy",
                value = String.format("%,d", calories),
                unit = "kcal",
                icon = Icons.Outlined.LocalFireDepartment,
                accentColor = accentRed,
                modifier = Modifier.weight(1f),
                testTag = "tile_calories"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MinimalStatTile(
                label = "Daily Goal",
                value = String.format("%,d", goal),
                unit = "steps",
                icon = Icons.Outlined.FitnessCenter,
                accentColor = primaryBlue,
                modifier = Modifier.weight(1f),
                testTag = "tile_goal"
            )

            MinimalStatTile(
                label = "Completion",
                value = "${(progress * 100).toInt()}%",
                unit = "done",
                icon = Icons.Outlined.ElectricBolt,
                accentColor = if (isGoalAchieved) accentRed else primaryBlue,
                modifier = Modifier.weight(1f),
                testTag = "tile_completion"
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Subtle Emulator / Quick Test Step Counter Buttons
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SIMULATE STRIDE (FOR TESTING & DEMO)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistChip(
                        onClick = { onAddSteps(50) },
                        label = { Text("+50 Steps") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.DirectionsWalk,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = primaryBlue
                        ),
                        modifier = Modifier.testTag("btn_sim_50")
                    )

                    AssistChip(
                        onClick = { onAddSteps(500) },
                        label = { Text("+500 Steps") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.DirectionsWalk,
                                contentDescription = null,
                                tint = accentRed,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = accentRed
                        ),
                        modifier = Modifier.testTag("btn_sim_500")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
