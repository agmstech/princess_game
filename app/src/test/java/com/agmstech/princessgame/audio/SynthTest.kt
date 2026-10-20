package com.agmstech.princessgame.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SynthTest {

    @Test
    fun parsesNotes() {
        assertEquals(60, Synth.parsePitch("C4"))
        assertEquals(69, Synth.parsePitch("A4"))
        assertEquals(66, Synth.parsePitch("F#4"))
        assertEquals(70, Synth.parsePitch("Bb4"))
        assertEquals(null, Synth.parsePitch("R"))
        assertEquals(listOf(Note(60, 1f), Note(67, 2f), Note(null, 0.5f)), Synth.parse("C4 G4:2 R:1/2"))
        assertEquals(440.0, Synth.frequency(69), 1e-9)
    }

    @Test
    fun melodyAndBassLineUp() {
        for (song in Songs.all) {
            val m = Synth.beats(Synth.parse(song.melody))
            val b = Synth.beats(Synth.parse(song.bass))
            assertEquals("${song.id} melody vs bass", m, b, 0.01f)
        }
    }

    @Test
    fun songsRenderAudibleAndUnclipped() {
        for (song in Songs.all) {
            val pcm = Synth.render(song)
            assertTrue("${song.id} too short", pcm.size > Synth.SAMPLE_RATE * 5)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("${song.id} silent", peak > 10_000)
            assertTrue("${song.id} clipped", peak < 32767)
        }
    }

    @Test
    fun effectsAndWav() {
        for (s in Sfx.values()) {
            val pcm = Synth.effect(s)
            assertTrue(pcm.isNotEmpty())
            val wav = Synth.wav(pcm)
            assertEquals(44 + pcm.size * 2, wav.size)
            assertEquals("RIFF", String(wav, 0, 4))
            assertEquals("WAVE", String(wav, 8, 4))
        }
    }
}
