package com.example.wanderly.repository

import com.example.wanderly.auth.PasswordHasher
import com.example.wanderly.local.users.UserDao
import com.example.wanderly.local.users.UserEntity

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    sealed interface Error : AuthResult { val message: String }
    data class InvalidCredentials(
        override val message: String = "Username or password is incorrect",
    ) : Error
    data class UsernameTaken(
        override val message: String = "That username is already taken",
    ) : Error
    data class ValidationError(override val message: String) : Error
    data class UnknownError(
        override val message: String = "Something went wrong, please try again",
    ) : Error
}

class AuthRepository(private val userDao: UserDao) {

    suspend fun signup(username: String, displayName: String, password: String): AuthResult {
        val cleanedUsername = username.trim().lowercase()
        val cleanedDisplayName = displayName.trim()
        validate(cleanedUsername, cleanedDisplayName, password)?.let {
            return AuthResult.ValidationError(it)
        }
        if (userDao.findByUsername(cleanedUsername) != null) {
            return AuthResult.UsernameTaken()
        }
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)
        return runCatching {
            val id = userDao.insert(
                UserEntity(
                    username = cleanedUsername,
                    displayName = cleanedDisplayName,
                    passwordHash = hash,
                    passwordSalt = salt,
                    createdAt = System.currentTimeMillis(),
                )
            )
            val user = userDao.findById(id) ?: return AuthResult.UnknownError()
            AuthResult.Success(user)
        }.getOrElse { AuthResult.UnknownError() }
    }

    suspend fun login(username: String, password: String): AuthResult {
        val cleaned = username.trim().lowercase()
        if (cleaned.isBlank() || password.isBlank()) {
            return AuthResult.ValidationError("Please fill in both fields")
        }
        val user = userDao.findByUsername(cleaned) ?: return AuthResult.InvalidCredentials()
        return if (PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
            AuthResult.Success(user)
        } else {
            AuthResult.InvalidCredentials()
        }
    }

    suspend fun findById(id: Long): UserEntity? = userDao.findById(id)

    private fun validate(username: String, displayName: String, password: String): String? = when {
        username.length < 3 -> "Username must be at least 3 characters"
        !username.matches(USERNAME_PATTERN) -> "Username can use letters, numbers, dot, underscore"
        displayName.isBlank() -> "Please enter a display name"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    private companion object {
        val USERNAME_PATTERN = Regex("^[a-z0-9_.]+$")
    }
}
