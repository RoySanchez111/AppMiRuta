package com.example.appbanco.logic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

object SecurityUtils {
    suspend fun hashPassword(password: String): String = withContext(Dispatchers.Default) {
        BCrypt.hashpw(password, BCrypt.gensalt())
    }

    suspend fun checkPassword(password: String, hashed: String): Boolean = withContext(Dispatchers.Default) {
        try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }
}
