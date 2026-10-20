package com.agmstech.princessgame.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayTimeTrackerTest {
    private val store = MemoryStore()
    private var day = "2026-10-20"
    private var limit = 30

    private fun tracker() = PlayTimeTracker(store, { limit }, { day })

    private fun play(t: PlayTimeTracker, ms: Long) {
        var left = ms
        while (left > 0) {
            val step = minOf(left, 1_000L)
            t.addPlayed(step)
            left -= step
        }
    }

    @Test
    fun countsDownTheDailyAllowance() {
        val t = tracker()
        assertEquals(30 * 60_000L, t.remainingMs())
        play(t, 10 * 60_000L)
        assertEquals(20 * 60_000L, t.remainingMs())
        assertFalse(t.isTimeUp())
        play(t, 20 * 60_000L)
        assertTrue(t.isTimeUp())
        assertEquals(0L, t.remainingMs())
    }

    @Test
    fun survivesRestartWhenFlushed() {
        val t = tracker()
        play(t, 5 * 60_000L)
        t.flush()
        assertEquals(25 * 60_000L, tracker().remainingMs())
    }

    @Test
    fun resetsOnANewDay() {
        val t = tracker()
        play(t, 30 * 60_000L)
        t.flush()
        assertTrue(tracker().isTimeUp())
        day = "2026-10-21"
        assertFalse(t.isTimeUp())
        assertEquals(30 * 60_000L, tracker().remainingMs())
    }

    @Test
    fun capsLargeSteps() {
        val t = tracker()
        t.addPlayed(60 * 60_000L)
        assertEquals(PlayTimeTracker.MAX_STEP_MS, t.usedTodayMs())
        t.addPlayed(-5_000L)
        assertEquals(PlayTimeTracker.MAX_STEP_MS, t.usedTodayMs())
    }

    @Test
    fun followsLimitChangesAndParentActions() {
        val t = tracker()
        play(t, 20 * 60_000L)
        limit = 15
        assertTrue(t.isTimeUp())
        t.giveExtra(5 * 60_000L)
        assertEquals(5 * 60_000L, t.remainingMs())
        t.resetToday()
        assertEquals(15 * 60_000L, t.remainingMs())
    }
}
