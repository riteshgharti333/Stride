package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensor.SensorDiagnostics
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    stepGoal: Int,
    strideLengthCm: Int,
    themeMode: String,
    isBackgroundServiceEnabled: Boolean,
    diagnostics: SensorDiagnostics,
    hasActivityPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStepGoalChange: (Int) -> Unit,
    onStrideLengthChange: (Int) -> Unit,
    onThemeChange: (String) -> Unit,
    onToggleBackgroundService: (Boolean) -> Unit,
    onResetTodaySteps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val primaryBlue = MaterialTheme.colorScheme.primary
    val accentRed = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PREFERENCES",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = primaryBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Settings & Device",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Daily Step Goal Setting
        SettingsCard(title = "DAILY STEP GOAL") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%,d", stepGoal),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryBlue,
                    modifier = Modifier.testTag("text_current_goal")
                )
                Text(
                    text = "steps / day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = stepGoal.toFloat(),
                onValueChange = { onStepGoalChange(((it / 500f).roundToInt() * 500).coerceIn(2000, 30000)) },
                valueRange = 2000f..30000f,
                steps = 55,
                colors = SliderDefaults.colors(
                    thumbColor = primaryBlue,
                    activeTrackColor = primaryBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("slider_step_goal")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "2,000",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Default: 8,000",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = primaryBlue
                )
                Text(
                    text = "30,000",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Stride Length Setting
        SettingsCard(title = "STRIDE LENGTH") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$strideLengthCm cm",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("text_current_stride")
                )
                Text(
                    text = "used for distance calculation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = strideLengthCm.toFloat(),
                onValueChange = { onStrideLengthChange(it.roundToInt().coerceIn(50, 100)) },
                valueRange = 50f..100f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = primaryBlue,
                    activeTrackColor = primaryBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("slider_stride_length")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "50 cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Default: 74 cm (approx. 29 in)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = primaryBlue
                )
                Text(
                    text = "100 cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Theme Mode Selector
        SettingsCard(title = "APPEARANCE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    label = "System",
                    icon = Icons.Outlined.SettingsBrightness,
                    isSelected = themeMode == "SYSTEM",
                    onClick = { onThemeChange("SYSTEM") },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_system"
                )
                ThemeOptionButton(
                    label = "Light (White)",
                    icon = Icons.Outlined.LightMode,
                    isSelected = themeMode == "LIGHT",
                    onClick = { onThemeChange("LIGHT") },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_light"
                )
                ThemeOptionButton(
                    label = "Dark",
                    icon = Icons.Outlined.DarkMode,
                    isSelected = themeMode == "DARK",
                    onClick = { onThemeChange("DARK") },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_dark"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Background Tracking Service Toggle
        SettingsCard(title = "CONTINUOUS BACKGROUND TRACKING") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Count steps when screen is off",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Maintains a silent low-power background service to ensure no steps are dropped during long walks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isBackgroundServiceEnabled,
                    onCheckedChange = onToggleBackgroundService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = primaryBlue
                    ),
                    modifier = Modifier.testTag("switch_background_service")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Battery Optimization & Privacy Architecture Card
        SettingsCard(title = "BATTERY ARCHITECTURE & PRIVACY") {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.BatteryChargingFull,
                    contentDescription = null,
                    tint = primaryBlue,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Zero GPS & Ultra-Low Power DSP",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stride taps exclusively into the device's hardware step-counting coprocessor (Sensor Hub DSP). It does not access GPS or perform continuous accelerometer matrix calculations, keeping power consumption under ~0.1% daily.\n\nAll data is stored strictly locally in your device's SQLite database. Zero network requests, zero accounts, zero analytics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Sensor Diagnostics & Permission Status
        SettingsCard(title = "HARDWARE SENSOR DIAGNOSTICS") {
            DiagnosticRow("Sensor Type", if (diagnostics.hasHardwareStepCounter) "Hardware Step Counter" else "Step Detector / Emulated")
            DiagnosticRow("Sensor Hardware", diagnostics.sensorName)
            DiagnosticRow("Vendor", diagnostics.vendor)
            DiagnosticRow("Power Draw", "${String.format("%.3f", diagnostics.powerMa)} mA")
            DiagnosticRow("Hardware FIFO Batching", if (diagnostics.isBatchingSupported) "Supported (60s batching)" else "Direct Event Delivery")
            DiagnosticRow(
                "Activity Permission",
                if (hasActivityPermission) "Granted" else "Requires Permission"
            )

            if (!hasActivityPermission) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accentRed.copy(alpha = 0.15f),
                        contentColor = accentRed
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("btn_request_permission")
                ) {
                    Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Activity Recognition Permission", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Reset / Maintenance Action
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = onResetTodaySteps,
                modifier = Modifier.testTag("btn_reset_today")
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = accentRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset Today's Step Baseline",
                    color = accentRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
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
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "theme_option"
) {
    val primaryBlue = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) primaryBlue.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) primaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) primaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) primaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
