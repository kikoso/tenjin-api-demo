package com.tenjin.sdk

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventName: String,
    val timestamp: Long = System.currentTimeMillis()
)
