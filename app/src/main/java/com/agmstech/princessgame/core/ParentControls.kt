package com.agmstech.princessgame.core

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Settings only a grown-up can change (behind the parent password). */
class ParentSettings(private val store: KeyValueStore) {

    var dailyLimitMinutes: Int
        get() = store.get(K_LIMIT)?.toIntOrNull()?.coerceIn(MIN_MINUTES, MAX_MINUTES) ?: DEFAULT_MINUTES
        set(value) = store.put(K_LIMIT, value.coerceIn(MIN_MINUTES, MAX_MINUTES).toString(), sync = true)

    var soundEffects: Boolean
        get() = store.get(K_SFX) != "off"
        set(value) = store.put(K_SFX, if (value) "on" else "off", sync = true)

    companion object {
        const val MIN_MINUTES = 1
        const val MAX_MINUTES = 240
        const val DEFAULT_MINUTES = 30
        private const val K_LIMIT = "parent.dailyLimitMinutes"
        private const val K_SFX = "parent.soundEffects"
    }
}

/** The parent password, stored only as a salted PBKDF2 hash. */
class ParentLock(private val store: KeyValueStore, private val iterations: Int = 20_000) {

    fun hasPassword(): Boolean = store.get(K_HASH) != null

    fun setPassword(password: String) {
        require(isAcceptable(password)) { "Password too short" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        store.put(
            mapOf(
                K_SALT to b64(salt),
                K_HASH to b64(hash(password, salt, iterations)),
                K_ITER to iterations.toString(),
            ),
            sync = true,
        )
    }

    fun verify(password: String): Boolean {
        val salt = store.get(K_SALT)?.let { unb64(it) } ?: return false
        val expected = store.get(K_HASH)?.let { unb64(it) } ?: return false
        val iter = store.get(K_ITER)?.toIntOrNull() ?: iterations
        return MessageDigest.isEqual(expected, hash(password, salt, iter))
    }

    companion object {
        const val MIN_LENGTH = 4
        private const val K_SALT = "parent.salt"
        private const val K_HASH = "parent.hash"
        private const val K_ITER = "parent.iterations"

        fun isAcceptable(password: String) = password.length >= MIN_LENGTH

        private fun hash(password: String, salt: ByteArray, iterations: Int): ByteArray {
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            } finally {
                spec.clearPassword()
            }
        }

        private fun b64(b: ByteArray) = Base64.getEncoder().encodeToString(b)
        private fun unb64(s: String) = Base64.getDecoder().decode(s)
    }
}
