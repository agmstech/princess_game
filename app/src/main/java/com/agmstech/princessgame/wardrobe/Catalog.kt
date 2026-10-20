package com.agmstech.princessgame.wardrobe

import com.agmstech.princessgame.art.Body
import com.agmstech.princessgame.art.Box
import com.agmstech.princessgame.art.DressArt
import com.agmstech.princessgame.art.FeetArt
import com.agmstech.princessgame.art.HairArt
import com.agmstech.princessgame.art.HeadwearArt
import com.agmstech.princessgame.art.HeldArt
import com.agmstech.princessgame.art.Placed
import com.agmstech.princessgame.art.RoleColors
import com.agmstech.princessgame.art.SeparatesArt
import com.agmstech.princessgame.art.SportsArt

/** Everything the princess can wear, by category. */
object Catalog {

    private val byCategory: Map<Category, List<Item>> = mapOf(
        Category.HAIR to HairArt.items,
        Category.CROWN to HeadwearArt.items,
        Category.DRESS to DressArt.items,
        Category.TOP to SeparatesArt.tops,
        Category.PANTS to SeparatesArt.bottoms,
        Category.SPORTS to SportsArt.items,
        Category.SOCKS to FeetArt.socks,
        Category.SHOES to FeetArt.shoes,
        Category.BAG to HeldArt.bags,
        Category.HAND to HeldArt.toys,
    )

    /** Item worn by a brand-new princess in each category. */
    private val starters: Map<Category, String> = mapOf(
        Category.HAIR to "long",
        Category.CROWN to "tiara",
        Category.DRESS to "gown",
        Category.TOP to "tee",
        Category.PANTS to "jeans",
        Category.SPORTS to "tennis",
        Category.SOCKS to NONE_ID,
        Category.SHOES to "flats",
        Category.BAG to NONE_ID,
        Category.HAND to "wand",
    )

    val wearables: List<Category> = Category.values().filter { it.isWearable }

    fun items(category: Category): List<Item> = byCategory[category].orEmpty()

    fun find(category: Category, id: String): Item? = items(category).firstOrNull { it.id == id }

    fun item(category: Category, id: String): Item = find(category, id) ?: items(category).first()

    /** First item that is not "none" — used when a colour is picked while nothing is worn. */
    fun firstReal(category: Category): Item = items(category).first { !it.isNone }

    fun starter(category: Category): Choice {
        val item = item(category, starters[category] ?: NONE_ID)
        val color = if (item.isNone) firstReal(category).defaultColor else item.defaultColor
        return Choice(item.id, color)
    }
}

data class Choice(val itemId: String, val color: Int)

/** What the princess is wearing. Immutable; every change returns a new outfit. */
data class Outfit(
    val skin: Int,
    val choices: Map<Category, Choice>,
    val bodyMode: BodyMode,
) {
    fun choice(category: Category): Choice = choices[category] ?: Catalog.starter(category)

    fun item(category: Category): Item = Catalog.item(category, choice(category).itemId)

    val hairColor: Int get() = choice(Category.HAIR).color

    /** True when this category's item can currently be seen on the princess. */
    fun isWorn(category: Category): Boolean {
        val mode = category.bodyMode()
        if (mode != null && mode != bodyMode) return false
        return !item(category).isNone
    }

    fun choose(category: Category, itemId: String): Outfit {
        val c = choice(category).copy(itemId = Catalog.item(category, itemId).id)
        return copy(choices = choices + (category to c), bodyMode = category.bodyMode() ?: bodyMode)
    }

    /** Recolours the category; if nothing is worn there yet, puts on its first item so the change is visible. */
    fun recolor(category: Category, color: Int): Outfit {
        var c = choice(category).copy(color = color)
        if (Catalog.item(category, c.itemId).isNone) c = c.copy(itemId = Catalog.firstReal(category).id)
        return copy(choices = choices + (category to c), bodyMode = category.bodyMode() ?: bodyMode)
    }

    fun withSkin(color: Int) = copy(skin = color)

    /** Categories drawn on the princess right now, in drawing priority order. */
    fun wornCategories(): List<Category> {
        val body = when (bodyMode) {
            BodyMode.DRESS -> listOf(Category.DRESS)
            BodyMode.SEPARATES -> listOf(Category.PANTS, Category.TOP)
            BodyMode.SPORTS -> listOf(Category.SPORTS)
        }
        return listOf(Category.HAIR) + body + listOf(Category.SOCKS, Category.SHOES, Category.BAG, Category.HAND, Category.CROWN)
    }

    companion object {
        fun starter(): Outfit = Outfit(
            skin = Palettes.skin[1],
            choices = Catalog.wearables.associateWith { Catalog.starter(it) },
            bodyMode = BodyMode.DRESS,
        )
    }
}

/** Puts the princess together layer by layer. */
object PrincessArt {

    fun compose(outfit: Outfit, blink: Boolean = false): List<Placed> {
        val base = Body.parts(blink)
        val baseColors = RoleColors(outfit.skin, outfit.hairColor, outfit.skin)
        val worn = outfit.wornCategories().map { cat ->
            outfit.item(cat) to RoleColors(outfit.skin, outfit.hairColor, outfit.choice(cat).color)
        }
        val out = ArrayList<Placed>()
        for (layer in Layer.values()) {
            base[layer]?.forEach { out += Placed(it, baseColors) }
            for ((item, colors) in worn) item.parts[layer]?.forEach { out += Placed(it, colors) }
        }
        return out
    }

    /** Elements to show on a wardrobe tile for [item] painted in [color]. */
    fun thumbnail(category: Category, item: Item, color: Int, outfit: Outfit): List<Placed> {
        val colors = when (category) {
            Category.HAIR -> RoleColors(outfit.skin, color, color)
            else -> RoleColors(outfit.skin, outfit.hairColor, color)
        }
        val base = if (category == Category.HAIR) Body.parts(false) else emptyMap()
        val out = ArrayList<Placed>()
        for (layer in Layer.values()) {
            if (layer == Layer.HEAD || layer == Layer.FACE) base[layer]?.forEach { out += Placed(it, colors) }
            item.parts[layer]?.forEach { out += Placed(it, colors) }
        }
        return out
    }

    /** A head-and-shoulders portrait used for the skin tone tiles. */
    fun portrait(outfit: Outfit): List<Placed> {
        val base = Body.parts(false)
        val colors = RoleColors(outfit.skin, outfit.hairColor, outfit.skin)
        val hairItem = outfit.item(Category.HAIR)
        val elements = hairItem.parts[Layer.HAIR_BACK].orEmpty() +
            base[Layer.HEAD].orEmpty() + base[Layer.FACE].orEmpty() +
            hairItem.parts[Layer.HAIR_FRONT].orEmpty()
        return elements.map { Placed(it, colors) }
    }

    /** The part of the art box a [portrait] should be framed to. */
    val portraitFrame = Box(170f, 70f, 430f, 340f)
}
