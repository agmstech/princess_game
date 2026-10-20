package com.agmstech.princessgame.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import java.util.concurrent.ConcurrentHashMap

/**
 * Plays the chosen song on a loop from a background thread. Songs are
 * synthesized the first time they are needed and then kept in memory.
 */
class MusicPlayer(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stopThread()
                AudioManager.AUDIOFOCUS_GAIN -> if (wanted) startThread()
            }
        }
        .build()

    private val rendered = ConcurrentHashMap<String, ShortArray>()
    private var songId: String = Songs.OFF
    private var wanted = false
    private var worker: Worker? = null

    /** Selects the song to play ([Songs.OFF] for silence). Takes effect immediately if playing. */
    fun select(id: String) {
        if (id == songId) return
        songId = id
        if (wanted) {
            stopThread()
            startPlayback()
        }
    }

    fun resume() {
        wanted = true
        startPlayback()
    }

    fun pause() {
        wanted = false
        stopThread()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    fun release() = pause()

    private fun startPlayback() {
        if (Songs.byId(songId) == null) return
        if (audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) startThread()
    }

    @Synchronized
    private fun startThread() {
        val song = Songs.byId(songId) ?: return
        if (worker?.song == song) return
        worker?.finish()
        worker = Worker(song).also { it.start() }
    }

    @Synchronized
    private fun stopThread() {
        worker?.finish()
        worker = null
    }

    private inner class Worker(val song: Song) : Thread("princess-music") {
        @Volatile
        private var running = true

        fun finish() {
            running = false
            interrupt()
        }

        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val pcm = rendered.getOrPut(song.id) { Synth.render(song) }
            if (!running) return
            val rate = Synth.SAMPLE_RATE
            val minBuf = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val track = try {
                AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(rate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(maxOf(minBuf, 8192) * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                return
            }
            try {
                track.setVolume(0.55f)
                track.play()
                var pos = 0
                val chunk = 2048
                while (running) {
                    val n = minOf(chunk, pcm.size - pos)
                    val written = track.write(pcm, pos, n)
                    if (written < 0) break
                    pos += written
                    if (pos >= pcm.size) pos = 0
                }
                track.pause()
                track.flush()
            } catch (e: IllegalStateException) {
                // The track was torn down underneath us; nothing more to play.
            } finally {
                track.release()
            }
        }
    }
}
