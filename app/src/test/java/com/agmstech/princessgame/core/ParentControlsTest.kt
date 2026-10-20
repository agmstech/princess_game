package com.agmstech.princessgame.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentControlsTest {

    @Test
    fun passwordIsCheckedAndNeverStoredInPlainText() {
        val store = MemoryStore()
        val lock = ParentLock(store, iterations = 1_000)
        assertFalse(lock.hasPassword())
        assertFalse(lock.verify("1234"))

        lock.setPassword("1234")
        assertTrue(lock.hasPassword())
        assertTrue(lock.verify("1234"))
        assertFalse(lock.verify("4321"))
        assertFalse(lock.verify(""))
        assertFalse(store.data.values.any { it.contains("1234") })

        // A fresh instance reads the stored hash.
        assertTrue(ParentLock(store).verify("1234"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsShortPasswords() {
        ParentLock(MemoryStore()).setPassword("12")
    }

    @Test
    fun dailyLimitIsClampedAndPersisted() {
        val store = MemoryStore()
        val s = ParentSettings(store)
        assertEquals(ParentSettings.DEFAULT_MINUTES, s.dailyLimitMinutes)
        s.dailyLimitMinutes = 45
        assertEquals(45, ParentSettings(store).dailyLimitMinutes)
        s.dailyLimitMinutes = 0
        assertEquals(ParentSettings.MIN_MINUTES, s.dailyLimitMinutes)
        s.dailyLimitMinutes = 10_000
        assertEquals(ParentSettings.MAX_MINUTES, s.dailyLimitMinutes)
        assertTrue(s.soundEffects)
        s.soundEffects = false
        assertFalse(ParentSettings(store).soundEffects)
    }
}
