package com.example.wanderly.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// PBKDF2-HMAC-SHA256 with 120k iterations + 16-byte random salt.
// Constant-time comparison via MessageDigest.isEqual to avoid timing leaks on login.
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun newSalt(): String {
        val bytes = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        val candidate = Base64.decode(hash(password, salt), Base64.NO_WRAP)
        val expected = Base64.decode(expectedHash, Base64.NO_WRAP)
        return MessageDigest.isEqual(candidate, expected)
    }
}
