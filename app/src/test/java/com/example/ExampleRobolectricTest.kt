package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DailyStepEntity
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Stride", appName)
    }

    @Test
    fun `user preferences defaults and updates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferences(context)

        assertEquals(8000, prefs.getStepGoal())
        prefs.setStepGoal(10000)
        assertEquals(10000, prefs.getStepGoal())

        assertEquals(74, prefs.getStrideLengthCm())
        prefs.setStrideLengthCm(80)
        assertEquals(80, prefs.getStrideLengthCm())
    }

    @Test
    fun `database inserts and queries step record`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val dao = db.dailyStepDao()

        val record = DailyStepEntity(
            date = "2026-10-09",
            steps = 6240,
            goal = 8000,
            distanceKm = 4.61f,
            calories = 249
        )
        dao.insertOrUpdate(record)

        val retrieved = dao.getStepRecord("2026-10-09")
        assertNotNull(retrieved)
        assertEquals(6240, retrieved?.steps)
        assertEquals(8000, retrieved?.goal)
    }
}
