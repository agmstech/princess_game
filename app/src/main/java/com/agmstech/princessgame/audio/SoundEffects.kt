package com.agmstech.princessgame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File

/** Short synthesized sounds for taps, sparkles and the countdown. */
class SoundEffects(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val ids = HashMap<Sfx, Int>()

    var enabled = true

    init {
        val dir = File(context.cacheDir, "sfx").apply { mkdirs() }
        // Generating the clips takes a few milliseconds; keep it off the UI thread.
        Thread {
            val loaded = HashMap<Sfx, Int>()
            for (s in Sfx.values()) {
                val f = File(dir, "${s.name.lowercase()}.wav")
                try {
                    f.writeBytes(Synth.wav(Synth.effect(s)))
                    loaded[s] = pool.load(f.path, 1)
                } catch (e: Exception) {
                    // A missing effect just stays silent.
                }
            }
            synchronized(ids) { ids.putAll(loaded) }
        }.start()
    }

    fun play(s: Sfx, volume: Float = 0.8f) {
        if (!enabled) return
        val id = synchronized(ids) { ids[s] } ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    fun release() = pool.release()
}
