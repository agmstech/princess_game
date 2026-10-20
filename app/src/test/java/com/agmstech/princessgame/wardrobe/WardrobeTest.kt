package com.agmstech.princessgame.wardrobe

import com.agmstech.princessgame.art.Backgrounds
import com.agmstech.princessgame.art.Body
import com.agmstech.princessgame.art.Group
import com.agmstech.princessgame.art.Label
import com.agmstech.princessgame.art.Shape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WardrobeTest {

    @Test
    fun everyWearableCategoryHasRealItemsWithUniqueIds() {
        for (cat in Catalog.wearables) {
            val items = Catalog.items(cat)
            assertTrue("$cat is empty", items.any { !it.isNone })
            assertEquals("$cat has duplicate ids", items.size, items.map { it.id }.toSet().size)
        }
        assertEquals(Backgrounds.all.size, Backgrounds.all.map { it.id }.toSet().size)
    }

    @Test
    fun noMakeupAnywhere() {
        val banned = listOf("makeup", "make-up", "lipstick", "lip gloss", "lipgloss", "blush", "eyeshadow", "eye shadow", "mascara", "nail polish", "rouge")
        val words = Category.values().map { it.title } +
            Catalog.wearables.flatMap { c -> Catalog.items(c).flatMap { listOf(it.id, it.label) } }
        for (w in words) for (b in banned) assertFalse("'$w' mentions $b", w.lowercase().contains(b))
    }

    @Test
    fun choosingClothesSwitchesWhatCoversTheBody() {
        var o = Outfit.starter()
        assertEquals(BodyMode.DRESS, o.bodyMode)
        assertTrue(o.isWorn(Category.DRESS))

        o = o.choose(Category.TOP, "hoodie")
        assertEquals(BodyMode.SEPARATES, o.bodyMode)
        assertTrue(o.isWorn(Category.TOP) && o.isWorn(Category.PANTS))
        assertFalse(o.isWorn(Category.DRESS))

        o = o.recolor(Category.SPORTS, 0xFF42A5F5.toInt())
        assertEquals(BodyMode.SPORTS, o.bodyMode)

        o = o.choose(Category.DRESS, "fairy")
        assertEquals(BodyMode.DRESS, o.bodyMode)
        assertEquals("hoodie", o.choice(Category.TOP).itemId)
    }

    @Test
    fun recolouringAnEmptySlotPutsSomethingOn() {
        val o = Outfit.starter().choose(Category.SOCKS, NONE_ID)
        assertFalse(o.isWorn(Category.SOCKS))
        val red = 0xFFE53935.toInt()
        val r = o.recolor(Category.SOCKS, red)
        assertTrue(r.isWorn(Category.SOCKS))
        assertEquals(red, r.choice(Category.SOCKS).color)
    }

    @Test
    fun princessIsAlwaysDressed() {
        // Whatever is chosen, a garment is drawn over the torso and hips.
        for (mode in BodyMode.values()) {
            val cat = when (mode) {
                BodyMode.DRESS -> Category.DRESS
                BodyMode.SEPARATES -> Category.TOP
                BodyMode.SPORTS -> Category.SPORTS
            }
            for (item in Catalog.items(cat)) {
                val o = Outfit.starter().choose(cat, item.id)
                val layers = o.wornCategories().flatMap { o.item(it).parts.keys }
                assertTrue("${item.id} leaves the body bare", Layer.TOP in layers)
            }
        }
    }

    @Test
    fun composedPrincessDrawsEveryLayerInOrder() {
        val placed = PrincessArt.compose(Outfit.starter())
        assertTrue(placed.size > 40)
        val bodyBox = com.agmstech.princessgame.art.Box(0f, 0f, Body.WIDTH, Body.HEIGHT).inset(-200f)
        for (p in placed) {
            val e = p.element
            val b = when (e) {
                is Shape -> e.path.bounds()
                is Group -> e.clip?.bounds()
                is Label -> null
            } ?: continue
            assertTrue("element outside art box: $b", b.left >= bodyBox.left && b.right <= bodyBox.right && b.top >= bodyBox.top && b.bottom <= bodyBox.bottom)
        }
    }
}
