package com.agmstech.princessgame.core

import com.agmstech.princessgame.audio.Songs
import com.agmstech.princessgame.wardrobe.BodyMode
import com.agmstech.princessgame.wardrobe.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateTest {

    @Test
    fun roundTripsEverything() {
        val store = MemoryStore()
        val outfit = GameState().outfit
            .choose(Category.SPORTS, "volleyball")
            .recolor(Category.SPORTS, 0xFF42A5F5.toInt())
            .choose(Category.HAIR, "braid")
            .recolor(Category.HAIR, 0xFFF48FB1.toInt())
            .choose(Category.BAG, "wallet")
            .withSkin(0xFFA8714A.toInt())
        val state = GameState("Luna", outfit, "tennis", "lullaby", Category.SHOES)
        state.save(store)

        val loaded = GameState.load(store)
        assertEquals(state, loaded)
        assertEquals(BodyMode.SPORTS, loaded.outfit.bodyMode)
    }

    @Test
    fun emptyStoreGivesDefaults() {
        assertEquals(GameState(), GameState.load(MemoryStore()))
    }

    @Test
    fun unknownValuesFallBackToDefaults() {
        val store = MemoryStore()
        GameState().save(store)
        store.put(mapOf(
            "state.item.DRESS" to "spaceship",
            "state.color.DRESS" to "not-a-colour",
            "state.background" to "moon",
            "state.song" to "nope",
            "state.tab" to "LIPSTICK",
            "state.mode" to "???",
        ))
        assertEquals(GameState(), GameState.load(store))
    }

    @Test
    fun musicCanBeTurnedOff() {
        val store = MemoryStore()
        GameState(songId = Songs.OFF).save(store)
        assertEquals(Songs.OFF, GameState.load(store).songId)
    }

    @Test
    fun namesAreTidied() {
        assertEquals("Princess", GameState.cleanName("   "))
        assertEquals("Rosie Bell", GameState.cleanName("  Rosie   Bell "))
        assertEquals(GameState.MAX_NAME_LENGTH, GameState.cleanName("A".repeat(40)).length)
    }
}
