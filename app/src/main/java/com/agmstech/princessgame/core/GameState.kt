package com.agmstech.princessgame.core

import com.agmstech.princessgame.art.Backgrounds
import com.agmstech.princessgame.art.Colors
import com.agmstech.princessgame.audio.Songs
import com.agmstech.princessgame.wardrobe.BodyMode
import com.agmstech.princessgame.wardrobe.Catalog
import com.agmstech.princessgame.wardrobe.Category
import com.agmstech.princessgame.wardrobe.Choice
import com.agmstech.princessgame.wardrobe.Outfit

/** Everything the child has made, saved automatically and restored on the next launch. */
data class GameState(
    val name: String = DEFAULT_NAME,
    val outfit: Outfit = Outfit.starter(),
    val backgroundId: String = Backgrounds.all.first().id,
    val songId: String = Songs.all.first().id,
    val tab: Category = Category.DRESS,
) {
    fun toMap(): Map<String, String> {
        val m = LinkedHashMap<String, String>()
        m[K_NAME] = name
        m[K_SKIN] = Colors.toHex(outfit.skin)
        m[K_MODE] = outfit.bodyMode.name
        m[K_BACKGROUND] = backgroundId
        m[K_SONG] = songId
        m[K_TAB] = tab.name
        for (cat in Catalog.wearables) {
            val c = outfit.choice(cat)
            m[K_ITEM + cat.name] = c.itemId
            m[K_COLOR + cat.name] = Colors.toHex(c.color)
        }
        return m
    }

    fun save(store: KeyValueStore, sync: Boolean = false) = store.put(toMap(), sync)

    companion object {
        const val DEFAULT_NAME = "Princess"
        const val MAX_NAME_LENGTH = 16

        private const val K_NAME = "state.name"
        private const val K_SKIN = "state.skin"
        private const val K_MODE = "state.mode"
        private const val K_BACKGROUND = "state.background"
        private const val K_SONG = "state.song"
        private const val K_TAB = "state.tab"
        private const val K_ITEM = "state.item."
        private const val K_COLOR = "state.color."

        fun cleanName(raw: String): String =
            raw.replace(Regex("\\s+"), " ").trim().take(MAX_NAME_LENGTH).ifEmpty { DEFAULT_NAME }

        private fun color(s: String?): Int? = try {
            s?.let { Colors.parse(it) }
        } catch (e: IllegalArgumentException) {
            null
        }

        /** Loads saved state, falling back to defaults for anything missing or unknown. */
        fun load(store: KeyValueStore): GameState {
            val d = GameState()
            if (store.get(K_NAME) == null) return d
            val choices = Catalog.wearables.associateWith { cat ->
                val starter = Catalog.starter(cat)
                val id = store.get(K_ITEM + cat.name)?.takeIf { Catalog.find(cat, it) != null } ?: starter.itemId
                Choice(id, color(store.get(K_COLOR + cat.name)) ?: starter.color)
            }
            val mode = BodyMode.values().firstOrNull { it.name == store.get(K_MODE) } ?: d.outfit.bodyMode
            return GameState(
                name = cleanName(store.get(K_NAME).orEmpty()),
                outfit = Outfit(color(store.get(K_SKIN)) ?: d.outfit.skin, choices, mode),
                backgroundId = store.get(K_BACKGROUND)?.takeIf { id -> Backgrounds.all.any { it.id == id } } ?: d.backgroundId,
                songId = store.get(K_SONG)?.takeIf { id -> Songs.all.any { it.id == id } || id == Songs.OFF } ?: d.songId,
                tab = Category.values().firstOrNull { it.name == store.get(K_TAB) } ?: d.tab,
            )
        }
    }
}
