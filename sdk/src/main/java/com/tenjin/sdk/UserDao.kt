package com.tenjin.sdk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUsername(user: User)

    @Query("SELECT * FROM users WHERE id = 1")
    suspend fun getUsername(): User?

    @Query("DELETE FROM users WHERE id = 1")
    suspend fun removeUsername()
}
