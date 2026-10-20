package com.agmstech.princessgame.wardrobe

import com.agmstech.princessgame.art.Box
import com.agmstech.princessgame.art.Element
import com.agmstech.princessgame.art.Group
import com.agmstech.princessgame.art.Label
import com.agmstech.princessgame.art.Shape

/** Drawing order of the princess, back to front. */
enum class Layer {
    BACK, HAIR_BACK, LEGS, TORSO, SOCKS, SHOES, BOTTOM, ARMS, TOP, HELD, HANDS, HEAD, FACE, HAIR_FRONT, HEADWEAR
}

/** One choosable thing: a hair style, a dress, a pair of shoes... */
class Item(
    val id: String,
    val label: String,
    val defaultColor: Int,
    val parts: Map<Layer, List<Element>>,
) {
    val isNone get() = parts.isEmpty()

    companion object {
        fun none(label: String = "None") = Item(NONE_ID, label, 0, emptyMap())
    }
}

const val NONE_ID = "none"

fun boundsOf(e: Element): Box? = when (e) {
    is Shape -> e.path.bounds()
    is Label -> Box(e.x - e.size * 0.35f * e.text.length, e.y - e.size * 0.75f, e.x + e.size * 0.35f * e.text.length, e.y)
    is Group -> e.clip?.bounds() ?: e.children.mapNotNull { boundsOf(it) }.reduceOrNull { a, b -> a.union(b) }
}

enum class PaletteKind { CLOTHES, HAIR, SKIN, NONE }

/** The tabs of the wardrobe. */
enum class Category(val title: String, val palette: PaletteKind) {
    PRINCESS("Me", PaletteKind.NONE),
    HAIR("Hair", PaletteKind.HAIR),
    CROWN("Crowns", PaletteKind.CLOTHES),
    DRESS("Dresses", PaletteKind.CLOTHES),
    TOP("Tops", PaletteKind.CLOTHES),
    PANTS("Pants", PaletteKind.CLOTHES),
    SPORTS("Sports", PaletteKind.CLOTHES),
    SOCKS("Socks", PaletteKind.CLOTHES),
    SHOES("Shoes", PaletteKind.CLOTHES),
    BAG("Bags", PaletteKind.CLOTHES),
    HAND("Toys", PaletteKind.CLOTHES),
    PLACE("Places", PaletteKind.NONE),
    MUSIC("Music", PaletteKind.NONE);

    /** Categories whose items are worn by the princess (and saved in the outfit). */
    val isWearable get() = this != PRINCESS && this != PLACE && this != MUSIC
}

/** Which garments cover the princess's body: a dress, a top with pants/skirt, or a sports kit. */
enum class BodyMode { DRESS, SEPARATES, SPORTS }

fun Category.bodyMode(): BodyMode? = when (this) {
    Category.DRESS -> BodyMode.DRESS
    Category.TOP, Category.PANTS -> BodyMode.SEPARATES
    Category.SPORTS -> BodyMode.SPORTS
    else -> null
}

object Palettes {
    val clothes: List<Int> = listOf(
        0xFFFF8FB8, 0xFFF0508C, 0xFFE53935, 0xFFFF8A65, 0xFFFFA726, 0xFFFFD54F,
        0xFFFFF59D, 0xFFAED581, 0xFF4CAF50, 0xFF80CBC4, 0xFF26A69A, 0xFF81D4FA,
        0xFF42A5F5, 0xFF3F51B5, 0xFFB39DDB, 0xFF9C5BD3, 0xFFF3E5F5, 0xFFFFFFFF,
        0xFFFFC531, 0xFFCFD8DC, 0xFF9E9E9E, 0xFF8D6E63, 0xFF3A3340,
    ).map { it.toInt() }

    val hair: List<Int> = listOf(
        0xFF2B2024, 0xFF4E342E, 0xFF7B4B2A, 0xFFA0522D, 0xFFD2691E, 0xFFE8B04F,
        0xFFF5DFA0, 0xFFF8F1E0, 0xFFF48FB1, 0xFFB388FF, 0xFF64B5F6, 0xFF80CBC4,
    ).map { it.toInt() }

    val skin: List<Int> = listOf(
        0xFFFFE3D1, 0xFFF9D2B4, 0xFFEDBB94, 0xFFD39B6E, 0xFFA8714A, 0xFF7A4E33,
    ).map { it.toInt() }
}
