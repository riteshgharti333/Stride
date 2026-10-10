package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepDao {
    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun getStepRecordFlow(date: String): Flow<DailyStepEntity?>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getStepRecord(date: String): DailyStepEntity?

    @Query("SELECT * FROM daily_steps WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getStepsBetweenDatesFlow(startDate: String, endDate: String): Flow<List<DailyStepEntity>>

    @Query("SELECT * FROM daily_steps WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getStepsBetweenDates(startDate: String, endDate: String): List<DailyStepEntity>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    fun getAllHistoryFlow(): Flow<List<DailyStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<DailyStepEntity>)

    @Query("SELECT COUNT(*) FROM daily_steps")
    suspend fun getCount(): Int

    @Query("DELETE FROM daily_steps")
    suspend fun clearAll()
}
