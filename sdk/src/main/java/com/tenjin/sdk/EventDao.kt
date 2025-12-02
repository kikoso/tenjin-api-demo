package com.tenjin.sdk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: Event)

    @Query("SELECT * FROM events ORDER BY timestamp ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events ORDER BY timestamp ASC")
    suspend fun getEvents(): List<Event>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventsCount(): Int

    @Query("DELETE FROM events WHERE id IN (:eventIds)")
    suspend fun deleteEvents(eventIds: List<Long>)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)
}
