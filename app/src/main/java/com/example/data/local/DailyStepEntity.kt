package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents daily step count and derived metrics for a specific calendar date.
 * Date format is ISO-8601 string: YYYY-MM-DD (e.g. "2026-10-09")
 */
@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val goal: Int = 8000,
    val distanceKm: Float = 0f,
    val calories: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
