package com.example.appbanco.data.database

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("UPDATE users SET username = :newUsername WHERE id = :userId")
    suspend fun updateUsername(userId: Int, newUsername: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity)
}
