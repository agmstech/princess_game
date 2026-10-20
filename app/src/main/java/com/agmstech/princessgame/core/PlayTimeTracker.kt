package com.agmstech.princessgame.core

import kotlin.math.max

/**
 * Counts how long the game has been played today and how much of the daily
 * allowance is left. The count starts again from zero on a new calendar day.
 *
 * Time is added by the caller while the game is actually on screen; [today]
 * returns the current local date (e.g. "2026-10-20").
 */
class PlayTimeTracker(
    private val store: KeyValueStore,
    private val limitMinutes: () -> Int,
    private val today: () -> String,
) {
    private var day: String = store.get(K_DAY) ?: today()
    private var usedMs: Long = store.get(K_USED)?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    private var dirty = false

    private fun rollover() {
        val now = today()
        if (now != day) {
            day = now
            usedMs = 0L
            dirty = true
        }
    }

    val limitMs: Long get() = limitMinutes() * 60_000L

    fun usedTodayMs(): Long {
        rollover()
        return usedMs
    }

    fun remainingMs(): Long = max(0L, limitMs - usedTodayMs())

    fun isTimeUp(): Boolean = remainingMs() <= 0L

    /** Adds played time. Steps are capped so a frozen or suspended clock can't eat the allowance at once. */
    fun addPlayed(ms: Long) {
        rollover()
        usedMs += ms.coerceIn(0L, MAX_STEP_MS)
        dirty = true
    }

    /** Parent action: start today's allowance again. */
    fun resetToday() {
        rollover()
        usedMs = 0L
        flush(sync = true, force = true)
    }

    /** Parent action: allow [ms] more play today. */
    fun giveExtra(ms: Long) {
        rollover()
        usedMs = if (usedMs > limitMs) limitMs - ms else usedMs - ms
        usedMs = max(0L, usedMs)
        flush(sync = true, force = true)
    }

    fun flush(sync: Boolean = false, force: Boolean = false) {
        if (!dirty && !force) return
        store.put(mapOf(K_DAY to day, K_USED to usedMs.toString()), sync)
        dirty = false
    }

    companion object {
        const val COUNTDOWN_MS = 10_000L
        const val MAX_STEP_MS = 5_000L
        private const val K_DAY = "time.day"
        private const val K_USED = "time.usedMs"
    }
}
