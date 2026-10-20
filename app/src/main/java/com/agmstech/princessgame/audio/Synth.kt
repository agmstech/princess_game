package com.agmstech.princessgame.audio

import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/** A note: [midi] pitch (null = rest) lasting [beats]. */
data class Note(val midi: Int?, val beats: Float)

enum class Sfx { TAP, SPARKLE, TICK, GOODBYE, JUMP, CHEER }

/** Tiny additive synthesizer: all music and sounds are generated, so the app ships no audio files. */
object Synth {
    const val SAMPLE_RATE = 22050

    private val NOTE_INDEX = mapOf('C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11)

    fun parse(notation: String): List<Note> = notation.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { token ->
        val parts = token.split(":")
        val beats = if (parts.size > 1) parseBeats(parts[1]) else 1f
        Note(parsePitch(parts[0]), beats)
    }

    private fun parseBeats(s: String): Float {
        val frac = s.split("/")
        return if (frac.size == 2) frac[0].toFloat() / frac[1].toFloat() else s.toFloat()
    }

    /** "C4" = 60 (middle C). "R" is a rest. */
    fun parsePitch(s: String): Int? {
        if (s.equals("R", ignoreCase = true)) return null
        val base = NOTE_INDEX[s[0].uppercaseChar()] ?: throw IllegalArgumentException("Bad note $s")
        var i = 1
        var semis = base
        while (i < s.length && (s[i] == '#' || s[i] == 'b')) {
            semis += if (s[i] == '#') 1 else -1
            i++
        }
        val octave = s.substring(i).toInt()
        return 12 * (octave + 1) + semis
    }

    fun frequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    fun beats(notes: List<Note>): Float = notes.sumOf { it.beats.toDouble() }.toFloat()

    /**
     * Renders one loop of [song]. Ringing notes that run past the end wrap
     * around to the start, so the buffer loops seamlessly.
     */
    fun render(song: Song, sampleRate: Int = SAMPLE_RATE): ShortArray {
        val melody = parse(song.melody)
        val bass = parse(song.bass)
        val spb = sampleRate * 60.0 / song.bpm
        val total = (maxOf(beats(melody), beats(bass)) * spb).toInt()
        val mix = FloatArray(total)
        var t = 0.0
        for (n in melody) {
            n.midi?.let { addVoice(mix, (t * spb).toInt(), n.beats * spb / sampleRate, frequency(it), song.instrument, 0.32f, sampleRate) }
            t += n.beats
        }
        t = 0.0
        for (n in bass) {
            n.midi?.let { addBass(mix, (t * spb).toInt(), n.beats * spb / sampleRate, frequency(it), 0.2f, sampleRate) }
            t += n.beats
        }
        return toPcm(mix, 0.7f)
    }

    private fun addVoice(buf: FloatArray, start: Int, seconds: Double, f: Double, inst: Instrument, amp: Float, sr: Int) {
        val ring = when (inst) {
            Instrument.MUSIC_BOX -> 1.4
            Instrument.MARIMBA -> 0.9
            Instrument.FLUTE -> seconds + 0.09
        }
        val n = (ring * sr).toInt()
        var phase = 0.0
        for (i in 0 until n) {
            val time = i.toDouble() / sr
            val v: Double = when (inst) {
                Instrument.MUSIC_BOX -> {
                    phase += 2 * PI * f / sr
                    val env = attack(time, 0.004) * exp(-time / 0.55)
                    env * (sin(phase) + 0.3 * sin(2 * phase) * exp(-time / 0.18) + 0.12 * sin(3 * phase) * exp(-time / 0.1))
                }
                Instrument.MARIMBA -> {
                    phase += 2 * PI * f / sr
                    val env = attack(time, 0.003) * exp(-time / 0.3)
                    env * (sin(phase) + 0.25 * sin(4 * phase) * exp(-time / 0.04))
                }
                Instrument.FLUTE -> {
                    val vib = 1.0 + 0.004 * sin(2 * PI * 5.0 * time) * minOf(1.0, time / 0.3)
                    phase += 2 * PI * f * vib / sr
                    val release = if (time > seconds) maxOf(0.0, 1.0 - (time - seconds) / 0.09) else 1.0
                    val env = attack(time, 0.04) * (0.85 + 0.15 * exp(-time / 0.2)) * release
                    env * (sin(phase) + 0.12 * sin(2 * phase) + 0.04 * sin(3 * phase)) * 0.8
                }
            }
            val idx = (start + i) % buf.size
            buf[idx] += (v * amp).toFloat()
        }
    }

    private fun addBass(buf: FloatArray, start: Int, seconds: Double, f: Double, amp: Float, sr: Int) {
        val n = ((seconds + 0.08) * sr).toInt()
        var phase = 0.0
        for (i in 0 until n) {
            val time = i.toDouble() / sr
            phase += 2 * PI * f / sr
            val release = if (time > seconds) maxOf(0.0, 1.0 - (time - seconds) / 0.08) else 1.0
            val env = attack(time, 0.01) * (0.35 + 0.65 * exp(-time / 0.35)) * release
            val v = env * (sin(phase) + 0.2 * sin(2 * phase))
            val idx = (start + i) % buf.size
            buf[idx] += (v * amp).toFloat()
        }
    }

    private fun attack(t: Double, len: Double) = if (t < len) t / len else 1.0

    /** Normalises to [peak] and converts to 16-bit PCM. */
    fun toPcm(mix: FloatArray, peak: Float): ShortArray {
        var max = 1e-6f
        for (v in mix) if (abs(v) > max) max = abs(v)
        val k = peak / max
        return ShortArray(mix.size) { (mix[it] * k * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort() }
    }

    /** Short sound effects. */
    fun effect(kind: Sfx, sampleRate: Int = SAMPLE_RATE): ShortArray {
        fun bells(vararg notes: Pair<Int, Double>, length: Double): ShortArray {
            val buf = FloatArray((length * sampleRate).toInt())
            for ((midi, at) in notes) bell(buf, (at * sampleRate).toInt(), frequency(midi), sampleRate)
            return toPcm(buf, 0.6f)
        }
        return when (kind) {
            Sfx.TAP -> sweep(520.0, 880.0, 0.09, sampleRate, 0.5f)
            Sfx.SPARKLE -> bells(84 to 0.0, 88 to 0.06, 91 to 0.12, 96 to 0.18, length = 0.7)
            Sfx.TICK -> bells(81 to 0.0, length = 0.35)
            Sfx.GOODBYE -> bells(79 to 0.0, 76 to 0.22, 72 to 0.44, length = 1.3)
            Sfx.JUMP -> sweep(300.0, 760.0, 0.25, sampleRate, 0.55f)
            Sfx.CHEER -> bells(72 to 0.0, 76 to 0.08, 79 to 0.16, 84 to 0.24, 88 to 0.32, length = 1.0)
        }
    }

    private fun bell(buf: FloatArray, start: Int, f: Double, sr: Int) {
        var phase = 0.0
        for (i in 0 until buf.size - start) {
            val t = i.toDouble() / sr
            phase += 2 * PI * f / sr
            val env = attack(t, 0.002) * exp(-t / 0.25)
            val v = env * (sin(phase) + 0.4 * sin(2.76 * phase) * exp(-t / 0.08) + 0.2 * sin(5.4 * phase) * exp(-t / 0.05))
            buf[start + i] += v.toFloat()
        }
    }

    private fun sweep(f0: Double, f1: Double, seconds: Double, sr: Int, peak: Float): ShortArray {
        val n = (seconds * sr).toInt()
        val buf = FloatArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val u = i.toDouble() / n
            phase += 2 * PI * (f0 + (f1 - f0) * u) / sr
            val env = attack(u * seconds, 0.005) * (1.0 - u).pow(1.5)
            buf[i] = (env * sin(phase)).toFloat()
        }
        return toPcm(buf, peak)
    }

    /** Wraps PCM samples in a WAV file (16-bit mono), e.g. for SoundPool. */
    fun wav(samples: ShortArray, sampleRate: Int = SAMPLE_RATE): ByteArray {
        val out = ByteArrayOutputStream(44 + samples.size * 2)
        fun int(v: Int) { out.write(v and 0xFF); out.write((v shr 8) and 0xFF); out.write((v shr 16) and 0xFF); out.write((v shr 24) and 0xFF) }
        fun short(v: Int) { out.write(v and 0xFF); out.write((v shr 8) and 0xFF) }
        val dataLen = samples.size * 2
        out.write("RIFF".toByteArray()); int(36 + dataLen); out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray()); int(16); short(1); short(1); int(sampleRate); int(sampleRate * 2); short(2); short(16)
        out.write("data".toByteArray()); int(dataLen)
        for (s in samples) short(s.toInt())
        return out.toByteArray()
    }
}
