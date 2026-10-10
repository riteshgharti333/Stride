package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyStepEntity
import java.time.LocalDate

data class DayChartData(
    val date: LocalDate,
    val dayLabel: String, // "Mon", "Tue", ...
    val steps: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val rawEntity: DailyStepEntity?
)

@Composable
fun MinimalWeeklyBarChart(
    days: List<DayChartData>,
    dailyGoal: Int,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryBlue = MaterialTheme.colorScheme.primary
    val accentRed = MaterialTheme.colorScheme.tertiary
    val barInactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val goalLineColor = accentRed.copy(alpha = 0.7f)
    val maxSteps = maxOf(dailyGoal, days.maxOfOrNull { it.steps } ?: dailyGoal, 1000)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart")
    ) {
        // Goal Legend Subtitle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MON — SUN",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentRed)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "GOAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
                Text(
                    text = "  ${String.format("%,d", dailyGoal)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = accentRed
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Bars with Goal Reference Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Draw Goal Reference Line in Red
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val goalRatio = (dailyGoal.toFloat() / maxSteps).coerceIn(0f, 1f)
                val goalY = size.height * (1f - goalRatio)
                drawLine(
                    color = goalLineColor,
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // 7 Interactive Bars Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    val isSelected = index == selectedIndex
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onSelectDay(index)
                            }
                            .testTag("bar_day_${day.dayLabel}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Bar Canvas
                        val ratio = (day.steps.toFloat() / maxSteps).coerceIn(0.04f, 1f)
                        val barColor = when {
                            day.isToday -> primaryBlue
                            day.steps >= dailyGoal -> primaryBlue
                            isSelected -> primaryBlue.copy(alpha = 0.5f)
                            else -> barInactiveColor
                        }

                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val barWidth = (size.width * 0.45f).coerceIn(12f, 28.dp.toPx())
                            val barHeight = size.height * ratio
                            val xOffset = (size.width - barWidth) / 2f
                            val yOffset = size.height - barHeight

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(xOffset, yOffset),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )

                            // If goal met, draw small subtle red indicator pip on top of the bar
                            if (day.steps >= dailyGoal) {
                                drawCircle(
                                    color = accentRed,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(size.width / 2f, yOffset + 5.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label below bar
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = when {
                                day.isToday -> primaryBlue
                                isSelected -> MaterialTheme.colorScheme.onBackground
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        // Subtle selection or today indicator bar
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(primaryBlue)
                                        .fillMaxWidth(0.3f)
                                        .height(2.5.dp)
                                )
                            } else if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(accentRed)
                                        .fillMaxWidth(0.2f)
                                        .height(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
