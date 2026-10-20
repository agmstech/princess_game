package com.agmstech.princessgame.audio

enum class Instrument { MUSIC_BOX, MARIMBA, FLUTE }

/**
 * A looping tune. Notes are written as e.g. "C5", "F#4", "Bb3" or "R" (rest),
 * optionally followed by ":beats" ("G4:2", "A4:1/2"); the default is one beat.
 */
class Song(
    val id: String,
    val title: String,
    val emoji: String,
    val bpm: Int,
    val instrument: Instrument,
    val melody: String,
    val bass: String,
)

/** Traditional public-domain children's tunes, synthesized on the device. */
object Songs {
    const val OFF = "off"

    val all: List<Song> = listOf(
        Song(
            "twinkle", "Twinkle Star", "⭐", 100, Instrument.MUSIC_BOX,
            "C5 C5 G5 G5 A5 A5 G5:2 F5 F5 E5 E5 D5 D5 C5:2 G5 G5 F5 F5 E5 E5 D5:2 " +
                "G5 G5 F5 F5 E5 E5 D5:2 C5 C5 G5 G5 A5 A5 G5:2 F5 F5 E5 E5 D5 D5 C5:2",
            "C3:2 C3:2 F3:2 C3:2 F3:2 C3:2 G3:2 C3:2 C3:2 F3:2 C3:2 G3:2 " +
                "C3:2 F3:2 C3:2 G3:2 C3:2 C3:2 F3:2 C3:2 F3:2 C3:2 G3:2 C3:2",
        ),
        Song(
            "lamb", "Little Lamb", "🐑", 112, Instrument.MARIMBA,
            "E5 D5 C5 D5 E5 E5 E5:2 D5 D5 D5:2 E5 G5 G5:2 E5 D5 C5 D5 E5 E5 E5 E5 D5 D5 E5 D5 C5:4",
            "C3:2 G3:2 C3:2 G3:2 G2:2 D3:2 C3:2 G3:2 C3:2 G3:2 C3:2 G3:2 G2:2 D3:2 C3:4",
        ),
        Song(
            "jacques", "Brother John", "🔔", 108, Instrument.FLUTE,
            "C5 D5 E5 C5 C5 D5 E5 C5 E5 F5 G5:2 E5 F5 G5:2 " +
                "G5:1/2 A5:1/2 G5:1/2 F5:1/2 E5 C5 G5:1/2 A5:1/2 G5:1/2 F5:1/2 E5 C5 C5 G4 C5:2 C5 G4 C5:2",
            "C3:2 G2:2 C3:2 G2:2 C3:2 G2:2 C3:2 G2:2 C3:2 G2:2 C3:2 G2:2 C3:2 G2:2 C3:2 G2:2",
        ),
        Song(
            "joy", "Joy Song", "🎉", 112, Instrument.MUSIC_BOX,
            "E5 E5 F5 G5 G5 F5 E5 D5 C5 C5 D5 E5 E5:3/2 D5:1/2 D5:2 " +
                "E5 E5 F5 G5 G5 F5 E5 D5 C5 C5 D5 E5 D5:3/2 C5:1/2 C5:2",
            "C3:4 G2:4 C3:4 G2:4 C3:4 G2:4 C3:4 G2:2 C3:2",
        ),
        Song(
            "bridge", "London Bridge", "🌉", 116, Instrument.MARIMBA,
            "G5:3/2 A5:1/2 G5 F5 E5 F5 G5:2 D5 E5 F5:2 E5 F5 G5:2 G5:3/2 A5:1/2 G5 F5 E5 F5 G5:2 D5:2 G5:2 E5 C5:3",
            "C3:2 G3:2 C3:2 G3:2 G2:2 D3:2 C3:2 G3:2 C3:2 G3:2 C3:2 G3:2 G2:2 D3:2 C3:4",
        ),
        Song(
            "boat", "Row Your Boat", "🚣", 80, Instrument.FLUTE,
            "C5 C5 C5:2/3 D5:1/3 E5 E5:2/3 D5:1/3 E5:2/3 F5:1/3 G5:2 " +
                "C6:1/3 C6:1/3 C6:1/3 G5:1/3 G5:1/3 G5:1/3 E5:1/3 E5:1/3 E5:1/3 C5:1/3 C5:1/3 C5:1/3 " +
                "G5:2/3 F5:1/3 E5:2/3 D5:1/3 C5:2",
            "C3:2 C3:2 C3:2 C3:2 C3:2 C3:2 G2:2 C3:2",
        ),
        Song(
            "lullaby", "Lullaby", "🌙", 84, Instrument.MUSIC_BOX,
            "E5:1/2 E5:1/2 G5:2 E5:1/2 E5:1/2 G5:2 E5:1/2 G5:1/2 C6 B5:3/2 A5:1/2 A5 G5 D5:1/2 E5:1/2 " +
                "F5 D5 D5:1/2 E5:1/2 F5:2 D5:1/2 F5:1/2 B5:1/2 A5:1/2 G5 B5 C6:2",
            "R C3:3 C3:3 F3:3 C3:3 G2:3 G2:3 G2:3 C3:2",
        ),
    )

    fun byId(id: String): Song? = all.firstOrNull { it.id == id }
}
