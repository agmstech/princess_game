package com.agmstech.princessgame.art

/** Colour slots an element can be painted with, filled in when the outfit is drawn. */
enum class Role { SKIN, HAIR, MAIN }

/** How an element is painted. */
sealed class Ink {
    /** A fixed ARGB colour. */
    class Solid(val argb: Int) : Ink()

    /**
     * A colour taken from the outfit ([Role]), optionally lightened (shade > 0)
     * or darkened (shade < 0), with an optional [alpha] (0..1).
     */
    class Of(val role: Role, val shade: Float = 0f, val alpha: Float = 1f) : Ink()

    class Linear(
        val x0: Float, val y0: Float, val x1: Float, val y1: Float,
        val colors: IntArray, val stops: FloatArray? = null,
    ) : Ink()

    class Radial(
        val cx: Float, val cy: Float, val radius: Float,
        val colors: IntArray, val stops: FloatArray? = null,
    ) : Ink()

    /** Only valid as a stroke: a darker version of the element's own fill. */
    object Auto : Ink()

    /** Only valid as a stroke: the outline colour that [of] would get. */
    class Outline(val of: Ink) : Ink()
}

/** The concrete colours for the [Role]s of whatever is being drawn. */
data class RoleColors(val skin: Int, val hair: Int, val main: Int) {
    fun of(role: Role) = when (role) {
        Role.SKIN -> skin
        Role.HAIR -> hair
        Role.MAIN -> main
    }
}

sealed interface Element

class Shape(
    val path: VPath,
    val fill: Ink?,
    val stroke: Ink? = Ink.Auto,
    val strokeWidth: Float = 3f,
    val evenOdd: Boolean = false,
) : Element

/** Bold centred text, e.g. a number on a sports jersey. [y] is the baseline. */
class Label(
    val text: String,
    val x: Float,
    val y: Float,
    val size: Float,
    val fill: Ink,
    val stroke: Ink? = null,
    val strokeWidth: Float = 0f,
) : Element

/** Children drawn clipped to [clip] (used for stripes, dots and other patterns). */
class Group(val clip: VPath?, val children: List<Element>) : Element

/** An element together with the colours its [Ink.Of] references resolve to. */
class Placed(val element: Element, val colors: RoleColors)

object Colors {
    fun argb(a: Int, r: Int, g: Int, b: Int) = (a shl 24) or (r shl 16) or (g shl 8) or b
    fun alpha(c: Int) = (c ushr 24) and 0xFF
    fun red(c: Int) = (c shr 16) and 0xFF
    fun green(c: Int) = (c shr 8) and 0xFF
    fun blue(c: Int) = c and 0xFF

    /** Parses "#RRGGBB" or "#AARRGGBB". */
    fun parse(s: String): Int {
        val hex = s.removePrefix("#")
        val v = hex.toLong(16).toInt()
        return when (hex.length) {
            6 -> v or (0xFF shl 24)
            8 -> v
            else -> throw IllegalArgumentException("Bad colour: $s")
        }
    }

    fun toHex(c: Int): String = "#%08X".format(c)

    fun mix(a: Int, b: Int, t: Float): Int {
        val u = t.coerceIn(0f, 1f)
        fun ch(x: Int, y: Int) = (x + (y - x) * u + 0.5f).toInt().coerceIn(0, 255)
        return argb(
            ch(alpha(a), alpha(b)), ch(red(a), red(b)), ch(green(a), green(b)), ch(blue(a), blue(b)),
        )
    }

    /** Lighten toward white (amount > 0) or darken toward deep plum (amount < 0). */
    fun shade(c: Int, amount: Float): Int = when {
        amount > 0f -> mix(c, (alpha(c) shl 24) or 0xFFFFFF, amount)
        amount < 0f -> mix(c, (alpha(c) shl 24) or 0x2A1630, -amount)
        else -> c
    }

    fun withAlpha(c: Int, a: Float): Int =
        ((alpha(c) * a.coerceIn(0f, 1f) + 0.5f).toInt() shl 24) or (c and 0xFFFFFF)

    /** Perceived brightness 0..1. */
    fun luminance(c: Int): Float =
        (0.299f * red(c) + 0.587f * green(c) + 0.114f * blue(c)) / 255f

    /** The cartoon outline colour for a fill: darker, or lighter for very dark fills. */
    fun outlineFor(fill: Int): Int {
        val opaque = fill or (0xFF shl 24)
        val line = if (luminance(opaque) < 0.18f) shade(opaque, 0.35f) else shade(opaque, -0.45f)
        return withAlpha(line, alpha(fill) / 255f)
    }

    fun resolve(ink: Ink.Of, colors: RoleColors): Int =
        withAlpha(shade(colors.of(ink.role), ink.shade), ink.alpha)

    /** Representative flat colour of an ink, used for automatic outlines. */
    fun flatten(ink: Ink?, colors: RoleColors): Int? = when (ink) {
        null, Ink.Auto -> null
        is Ink.Solid -> ink.argb
        is Ink.Of -> resolve(ink, colors)
        is Ink.Linear -> ink.colors.first()
        is Ink.Radial -> ink.colors.first()
        is Ink.Outline -> flatten(ink.of, colors)?.let { outlineFor(it) }
    }

    /** Resolves the stroke colour of [shape] (null = no stroke). */
    fun strokeColor(shape: Shape, colors: RoleColors): Int? = when (val s = shape.stroke) {
        null -> null
        Ink.Auto -> flatten(shape.fill, colors)?.let { outlineFor(it) }
        else -> flatten(s, colors)
    }
}
