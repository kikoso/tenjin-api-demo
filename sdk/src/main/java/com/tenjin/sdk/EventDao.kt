package com.tenjin.sdk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: Event)

    @Query("SELECT * FROM events ORDER BY timestamp ASC")
    suspend fun getAllEvents(): List<Event>

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: Long)
}
