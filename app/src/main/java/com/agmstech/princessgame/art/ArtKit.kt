package com.agmstech.princessgame.art

import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/* Short helpers that keep the hand-written art readable. */

internal fun cloth(shade: Float = 0f, alpha: Float = 1f) = Ink.Of(Role.MAIN, shade, alpha)
internal fun hair(shade: Float = 0f) = Ink.Of(Role.HAIR, shade)
internal fun skin(shade: Float = 0f) = Ink.Of(Role.SKIN, shade)
internal fun solid(argb: Long) = Ink.Solid(argb.toInt())

internal val WHITE = solid(0xFFFFFFFF)
internal val GOLD = solid(0xFFFFC531)
internal val PINK = solid(0xFFFF5FA2)
internal val LEAF = solid(0xFF66BB6A)

internal fun fill(p: VPath, ink: Ink, stroke: Ink? = Ink.Auto, width: Float = 3f) = Shape(p, ink, stroke, width)

/** A fill with no outline (highlights, prints, small details). */
internal fun flat(p: VPath, ink: Ink) = Shape(p, ink, null)

/**
 * A filled shape made of overlapping pieces (curls, ruffles, bushes) with an
 * outline only around the outside: the stroke is drawn first and the fill
 * covers its inner half.
 */
internal fun blob(p: VPath, ink: Ink, width: Float = 3f): List<Shape> =
    listOf(Shape(p, null, Ink.Outline(ink), width * 2), Shape(p, ink, null))

/** An unfilled line. */
internal fun line(p: VPath, ink: Ink, width: Float = 3f) = Shape(p, null, ink, width)

internal fun Shape.mirrored(axis: Float = 300f) = Shape(path.mirrorX(axis), fill, stroke, strokeWidth, evenOdd)

/** The shape plus its mirror image across the body's centre line. */
internal fun pair(s: Shape): List<Shape> = listOf(s, s.mirrored())

internal fun Element.mirroredElement(axis: Float = 300f): Element = when (this) {
    is Shape -> mirrored(axis)
    is Label -> Label(text, 2 * axis - x, y, size, fill, stroke, strokeWidth)
    is Group -> Group(clip?.mirrorX(axis), children.map { it.mirroredElement(axis) })
}

/** Horizontal stripes, clipped to [clip]. */
internal fun stripes(clip: VPath, ink: Ink, top: Float, bottom: Float, gap: Float, thickness: Float): Group {
    val b = clip.bounds()
    val shapes = ArrayList<Element>()
    var y = top
    while (y < bottom) {
        shapes += flat(path { rect(b.left - 5, y, b.right + 5, y + thickness) }, ink)
        y += gap
    }
    return Group(clip, shapes)
}

/** Polka dots, clipped to [clip]. */
internal fun dots(clip: VPath, ink: Ink, spacing: Float, radius: Float): Group {
    val b = clip.bounds()
    val shapes = ArrayList<Element>()
    var row = 0
    var y = b.top + spacing / 2
    while (y < b.bottom + spacing) {
        var x = b.left + if (row % 2 == 0) spacing / 2 else spacing
        while (x < b.right + spacing) {
            shapes += flat(path { circle(x, y, radius) }, ink)
            x += spacing
        }
        y += spacing * 0.85f
        row++
    }
    return Group(clip, shapes)
}

/** A five-petal flower with a yellow centre. */
internal fun flower(cx: Float, cy: Float, petal: Ink, r: Float = 10f): List<Element> {
    val petals = PathBuilder()
    for (k in 0 until 5) {
        val a = Math.toRadians(-90.0 + k * 72.0)
        petals.circle(cx + r * Math.cos(a), cy + r * Math.sin(a), r)
    }
    return listOf(
        fill(petals.build(), petal, width = 2.5f),
        fill(path { circle(cx, cy, r * 0.7f) }, solid(0xFFFFD54F), width = 2f),
    )
}

/** A tiny four-pointed sparkle. */
internal fun sparkle(cx: Number, cy: Number, r: Number, ink: Ink = WHITE): Shape =
    flat(path { star(cx, cy, r, r.toFloat() * 0.3f, points = 4) }, ink)

internal class ItemBuilder {
    private val parts = LinkedHashMap<Layer, MutableList<Element>>()

    fun add(layer: Layer, vararg e: Element) {
        parts.getOrPut(layer) { ArrayList() }.addAll(e)
    }

    fun add(layer: Layer, e: List<Element>) {
        parts.getOrPut(layer) { ArrayList() }.addAll(e)
    }

    fun build(): Map<Layer, List<Element>> = parts
}

internal fun item(id: String, label: String, defaultColor: Long, block: ItemBuilder.() -> Unit): Item =
    Item(id, label, defaultColor.toInt(), ItemBuilder().apply(block).build())
