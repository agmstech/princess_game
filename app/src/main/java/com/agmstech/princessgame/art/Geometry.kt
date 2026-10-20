package com.agmstech.princessgame.art

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Platform-independent vector geometry. All game art is described with these
 * types so the same drawings can be rendered by Android's Canvas in the app
 * and exported to SVG in unit tests for previewing.
 */
data class Pt(val x: Float, val y: Float) {
    operator fun plus(o: Pt) = Pt(x + o.x, y + o.y)
    operator fun minus(o: Pt) = Pt(x - o.x, y - o.y)
    operator fun times(k: Float) = Pt(x * k, y * k)
}

fun pt(x: Number, y: Number) = Pt(x.toFloat(), y.toFloat())

data class Box(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
    val centerX get() = (left + right) / 2f
    val centerY get() = (top + bottom) / 2f

    fun union(o: Box) = Box(min(left, o.left), min(top, o.top), max(right, o.right), max(bottom, o.bottom))

    fun inset(d: Float) = Box(left + d, top + d, right - d, bottom - d)
}

sealed class Cmd {
    data class MoveTo(val x: Float, val y: Float) : Cmd()
    data class LineTo(val x: Float, val y: Float) : Cmd()
    data class QuadTo(val x1: Float, val y1: Float, val x: Float, val y: Float) : Cmd()
    data class CubicTo(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val x: Float, val y: Float,
    ) : Cmd()

    object Close : Cmd()
}

/** An immutable path made only of absolute commands. */
class VPath(val cmds: List<Cmd>) {

    fun map(f: (Pt) -> Pt): VPath = VPath(cmds.map { c ->
        when (c) {
            is Cmd.MoveTo -> f(Pt(c.x, c.y)).let { Cmd.MoveTo(it.x, it.y) }
            is Cmd.LineTo -> f(Pt(c.x, c.y)).let { Cmd.LineTo(it.x, it.y) }
            is Cmd.QuadTo -> {
                val a = f(Pt(c.x1, c.y1)); val b = f(Pt(c.x, c.y))
                Cmd.QuadTo(a.x, a.y, b.x, b.y)
            }
            is Cmd.CubicTo -> {
                val a = f(Pt(c.x1, c.y1)); val b = f(Pt(c.x2, c.y2)); val e = f(Pt(c.x, c.y))
                Cmd.CubicTo(a.x, a.y, b.x, b.y, e.x, e.y)
            }
            Cmd.Close -> Cmd.Close
        }
    })

    /** Mirror image across the vertical line x = [axis]. */
    fun mirrorX(axis: Float = 300f) = map { Pt(2 * axis - it.x, it.y) }

    /** Rotate by [degrees] around ([cx], [cy]). */
    fun rotate(degrees: Float, cx: Float, cy: Float): VPath {
        val r = degrees * PI.toFloat() / 180f
        val c = cos(r); val s = sin(r)
        return map { p ->
            val dx = p.x - cx; val dy = p.y - cy
            Pt(cx + dx * c - dy * s, cy + dx * s + dy * c)
        }
    }

    operator fun plus(o: VPath) = VPath(cmds + o.cmds)

    /** Bounds of all points including control points (a cheap, safe over-estimate). */
    fun bounds(): Box {
        var l = Float.MAX_VALUE; var t = Float.MAX_VALUE
        var r = -Float.MAX_VALUE; var b = -Float.MAX_VALUE
        fun add(x: Float, y: Float) {
            if (x < l) l = x; if (x > r) r = x
            if (y < t) t = y; if (y > b) b = y
        }
        for (c in cmds) when (c) {
            is Cmd.MoveTo -> add(c.x, c.y)
            is Cmd.LineTo -> add(c.x, c.y)
            is Cmd.QuadTo -> { add(c.x1, c.y1); add(c.x, c.y) }
            is Cmd.CubicTo -> { add(c.x1, c.y1); add(c.x2, c.y2); add(c.x, c.y) }
            Cmd.Close -> Unit
        }
        return if (l > r) Box(0f, 0f, 0f, 0f) else Box(l, t, r, b)
    }
}

/** Small DSL for building [VPath]s. Accepts any number type to keep the art code readable. */
class PathBuilder {
    private val cmds = ArrayList<Cmd>()
    private var cur = Pt(0f, 0f)
    private var start = Pt(0f, 0f)

    fun moveTo(x: Number, y: Number) = apply {
        cur = pt(x, y); start = cur
        cmds += Cmd.MoveTo(cur.x, cur.y)
    }

    fun moveTo(p: Pt) = moveTo(p.x, p.y)

    fun lineTo(x: Number, y: Number) = apply {
        cur = pt(x, y)
        cmds += Cmd.LineTo(cur.x, cur.y)
    }

    fun lineTo(p: Pt) = lineTo(p.x, p.y)

    fun quadTo(x1: Number, y1: Number, x: Number, y: Number) = apply {
        cur = pt(x, y)
        cmds += Cmd.QuadTo(x1.toFloat(), y1.toFloat(), cur.x, cur.y)
    }

    fun cubicTo(x1: Number, y1: Number, x2: Number, y2: Number, x: Number, y: Number) = apply {
        cur = pt(x, y)
        cmds += Cmd.CubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), cur.x, cur.y)
    }

    fun close() = apply {
        cmds += Cmd.Close
        cur = start
    }

    /** Polyline through the given flat list of coordinates: x0, y0, x1, y1, ... */
    fun poly(vararg xy: Number, closed: Boolean = true) = apply {
        require(xy.size >= 4 && xy.size % 2 == 0)
        moveTo(xy[0], xy[1])
        var i = 2
        while (i < xy.size) { lineTo(xy[i], xy[i + 1]); i += 2 }
        if (closed) close()
    }

    /** Adds a closed ellipse sub-path (clockwise). */
    fun ellipse(cx: Number, cy: Number, rx: Number, ry: Number) = apply {
        val x = cx.toFloat(); val y = cy.toFloat(); val a = rx.toFloat(); val b = ry.toFloat()
        val k = 0.5522848f
        moveTo(x + a, y)
        cubicTo(x + a, y + b * k, x + a * k, y + b, x, y + b)
        cubicTo(x - a * k, y + b, x - a, y + b * k, x - a, y)
        cubicTo(x - a, y - b * k, x - a * k, y - b, x, y - b)
        cubicTo(x + a * k, y - b, x + a, y - b * k, x + a, y)
        close()
    }

    fun circle(cx: Number, cy: Number, r: Number) = ellipse(cx, cy, r, r)

    fun rect(l: Number, t: Number, r: Number, b: Number) = poly(l, t, r, t, r, b, l, b)

    fun roundRect(l: Number, t: Number, r: Number, b: Number, radius: Number) = apply {
        val L = l.toFloat(); val T = t.toFloat(); val R = r.toFloat(); val B = b.toFloat()
        val rr = min(radius.toFloat(), min((R - L) / 2f, (B - T) / 2f))
        moveTo(L + rr, T)
        lineTo(R - rr, T); quadTo(R, T, R, T + rr)
        lineTo(R, B - rr); quadTo(R, B, R - rr, B)
        lineTo(L + rr, B); quadTo(L, B, L, B - rr)
        lineTo(L, T + rr); quadTo(L, T, L + rr, T)
        close()
    }

    /** A five (or [points]) pointed star centred at ([cx], [cy]). */
    fun star(cx: Number, cy: Number, outer: Number, inner: Number = outer.toFloat() * 0.45f, points: Int = 5, rotationDeg: Float = 0f) = apply {
        val x = cx.toFloat(); val y = cy.toFloat()
        val o = outer.toFloat(); val i = inner.toFloat()
        for (k in 0 until points * 2) {
            val r = if (k % 2 == 0) o else i
            val a = (-90f + rotationDeg + k * 180f / points) * PI.toFloat() / 180f
            val px = x + r * cos(a); val py = y + r * sin(a)
            if (k == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    /** A heart centred horizontally at [cx], top dip at [top], of total width [w]. */
    fun heart(cx: Number, top: Number, w: Number) = apply {
        val x = cx.toFloat(); val t = top.toFloat(); val s = w.toFloat() / 2f
        moveTo(x, t + s * 0.35f)
        cubicTo(x - s * 0.15f, t - s * 0.25f, x - s * 1.05f, t - s * 0.2f, x - s, t + s * 0.45f)
        cubicTo(x - s * 0.95f, t + s * 0.95f, x - s * 0.35f, t + s * 1.25f, x, t + s * 1.7f)
        cubicTo(x + s * 0.35f, t + s * 1.25f, x + s * 0.95f, t + s * 0.95f, x + s, t + s * 0.45f)
        cubicTo(x + s * 1.05f, t - s * 0.2f, x + s * 0.15f, t - s * 0.25f, x, t + s * 0.35f)
        close()
    }

    /**
     * Continues the current sub-path smoothly through [pts] using a Catmull-Rom
     * spline converted to cubic Béziers.
     */
    fun smoothTo(pts: List<Pt>, tension: Float = 1f) = apply {
        if (pts.isEmpty()) return@apply
        val all = listOf(cur) + pts
        for (i in 0 until all.size - 1) {
            val p0 = if (i == 0) all[0] else all[i - 1]
            val p1 = all[i]
            val p2 = all[i + 1]
            val p3 = if (i + 2 < all.size) all[i + 2] else all[i + 1]
            val c1 = p1 + (p2 - p0) * (tension / 6f)
            val c2 = p2 - (p3 - p1) * (tension / 6f)
            cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
        }
    }

    /** A smooth closed blob through the given points. */
    fun smoothClosed(pts: List<Pt>, tension: Float = 1f) = apply {
        require(pts.size >= 3)
        val n = pts.size
        moveTo(pts[0])
        for (i in 0 until n) {
            val p0 = pts[(i - 1 + n) % n]
            val p1 = pts[i]
            val p2 = pts[(i + 1) % n]
            val p3 = pts[(i + 2) % n]
            val c1 = p1 + (p2 - p0) * (tension / 6f)
            val c2 = p2 - (p3 - p1) * (tension / 6f)
            cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
        }
        close()
    }

    fun add(p: VPath) = apply { cmds += p.cmds }

    fun build() = VPath(ArrayList(cmds))
}

fun path(block: PathBuilder.() -> Unit): VPath = PathBuilder().apply(block).build()

/** Convenience for writing point lists as flat coordinate pairs. */
fun pts(vararg xy: Number): List<Pt> {
    require(xy.size % 2 == 0)
    return (xy.indices step 2).map { pt(xy[it], xy[it + 1]) }
}

/**
 * A tapered tube along a polyline (arms, legs, sleeves, socks...).
 * [centre] are spine points and [widths] the full width at each of them.
 */
class Limb(val centre: List<Pt>, val widths: List<Float>) {
    init {
        require(centre.size >= 2 && centre.size == widths.size)
    }

    private val lengths: FloatArray = FloatArray(centre.size).also { acc ->
        for (i in 1 until centre.size) {
            acc[i] = acc[i - 1] + hypot(centre[i].x - centre[i - 1].x, centre[i].y - centre[i - 1].y)
        }
    }

    val length get() = lengths.last()

    /** Point and width at fraction [t] (0..1) of the total length. */
    fun at(t: Float): Pair<Pt, Float> {
        val d = t.coerceIn(0f, 1f) * length
        var i = 1
        while (i < centre.size - 1 && lengths[i] < d) i++
        val seg = lengths[i] - lengths[i - 1]
        val f = if (seg <= 0f) 0f else (d - lengths[i - 1]) / seg
        val p = centre[i - 1] + (centre[i] - centre[i - 1]) * f
        val w = widths[i - 1] + (widths[i] - widths[i - 1]) * f
        return p to w
    }

    private fun direction(t: Float): Pt {
        val a = at((t - 0.01f).coerceAtLeast(0f)).first
        val b = at((t + 0.01f).coerceAtMost(1f)).first
        val len = hypot(b.x - a.x, b.y - a.y).coerceAtLeast(1e-4f)
        return Pt((b.x - a.x) / len, (b.y - a.y) / len)
    }

    /**
     * Outline of the section between [t0] and [t1], widened by [extra].
     * Round ends make it look like a limb; flat ends suit cuffs and hems.
     */
    fun segment(t0: Float = 0f, t1: Float = 1f, extra: Float = 0f, roundStart: Boolean = true, roundEnd: Boolean = true): VPath {
        val steps = 10
        val left = ArrayList<Pt>()
        val right = ArrayList<Pt>()
        for (k in 0..steps) {
            val t = t0 + (t1 - t0) * k / steps
            val (p, w) = at(t)
            val d = direction(t)
            val n = Pt(-d.y, d.x)
            val h = (w + extra) / 2f
            left += p + n * h
            right += p - n * h
        }
        val (pEnd, wEnd) = at(t1)
        val (pStart, wStart) = at(t0)
        val dEnd = direction(t1)
        val dStart = direction(t0)
        return path {
            moveTo(right.first())
            smoothTo(right.drop(1))
            if (roundEnd) {
                val h = (wEnd + extra) / 2f
                val tip = pEnd + dEnd * h
                val a = right.last() + dEnd * (h * 0.75f)
                val b = left.last() + dEnd * (h * 0.75f)
                cubicTo(a.x, a.y, tip.x - (left.last().x - right.last().x) * 0.28f, tip.y - (left.last().y - right.last().y) * 0.28f, tip.x, tip.y)
                cubicTo(tip.x + (left.last().x - right.last().x) * 0.28f, tip.y + (left.last().y - right.last().y) * 0.28f, b.x, b.y, left.last().x, left.last().y)
            } else {
                lineTo(left.last())
            }
            smoothTo(left.reversed().drop(1))
            if (roundStart) {
                val h = (wStart + extra) / 2f
                val tip = pStart - dStart * h
                val a = left.first() - dStart * (h * 0.75f)
                val b = right.first() - dStart * (h * 0.75f)
                cubicTo(a.x, a.y, tip.x + (left.first().x - right.first().x) * 0.28f, tip.y + (left.first().y - right.first().y) * 0.28f, tip.x, tip.y)
                cubicTo(tip.x - (left.first().x - right.first().x) * 0.28f, tip.y - (left.first().y - right.first().y) * 0.28f, b.x, b.y, right.first().x, right.first().y)
            }
            close()
        }
    }

    fun mirrorX(axis: Float = 300f) = Limb(centre.map { Pt(2 * axis - it.x, it.y) }, widths)
}
