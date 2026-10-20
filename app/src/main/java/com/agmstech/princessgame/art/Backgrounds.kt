package com.agmstech.princessgame.art

/** A scene behind the princess, drawn in a 1000 x 1000 box and scaled to cover the stage. */
class Background(val id: String, val label: String, val elements: List<Element>)

object Backgrounds {
    const val SIZE = 1000f

    private fun c(argb: Long) = solid(argb)

    private fun sky(top: Long, bottom: Long, to: Float = 760f) =
        flat(path { rect(0, 0, 1000, 1000) }, Ink.Linear(0f, 0f, 0f, to, intArrayOf(top.toInt(), bottom.toInt())))

    private fun ground(y: Number, argb: Long) = flat(path { rect(-10, y, 1010, 1010) }, c(argb))

    private fun cloud(cx: Float, cy: Float, s: Float, ink: Ink = c(0xF2FFFFFF)) = flat(path {
        circle(cx - 42 * s, cy, 30 * s)
        circle(cx, cy - 22 * s, 42 * s)
        circle(cx + 42 * s, cy, 30 * s)
        roundRect(cx - 72 * s, cy - 4 * s, cx + 72 * s, cy + 28 * s, 16 * s)
    }, ink)

    private fun sun(cx: Float, cy: Float, r: Float): List<Element> = listOf(
        flat(path { circle(cx, cy, r * 1.9f) }, Ink.Radial(cx, cy, r * 1.9f, intArrayOf(0x88FFF3B0.toInt(), 0x00FFF3B0))),
        fill(path { circle(cx, cy, r) }, c(0xFFFFE066), width = 4f),
    )

    /** Deterministic pseudo-random numbers so scenes look the same every time. */
    private class Rng(var seed: Long) {
        fun next(): Float {
            seed = (seed * 6364136223846793005L + 1442695040888963407L)
            return ((seed ushr 33) % 10000L).toFloat() / 10000f
        }
    }

    private fun sprinkleStars(n: Int, area: Box, rMin: Float, rMax: Float, ink: Ink, seed: Long, points: Int = 5): List<Element> {
        val rng = Rng(seed)
        return (0 until n).map {
            val x = area.left + rng.next() * area.width
            val y = area.top + rng.next() * area.height
            val r = rMin + rng.next() * (rMax - rMin)
            flat(path { star(x, y, r, r * 0.45f, points) }, ink)
        }
    }

    private fun tinyFlowers(n: Int, area: Box, seed: Long): List<Element> {
        val rng = Rng(seed)
        val colors = listOf(0xFFFFFFFF, 0xFFFFE066, 0xFFFF8FB8, 0xFFB39DDB)
        return (0 until n).flatMap { i ->
            flower(area.left + rng.next() * area.width, area.top + rng.next() * area.height, c(colors[i % colors.size]), 5f)
        }
    }

    private fun tower(x: Float, top: Float, w: Float, bottom: Float, wall: Ink, roof: Ink, flag: Ink): List<Element> = listOf(
        fill(path { rect(x, top, x + w, bottom) }, wall, width = 4f),
        fill(path { poly(x - 16, top + 2, x + w / 2, top - w * 1.3f, x + w + 16, top + 2) }, roof, width = 4f),
        line(path { moveTo(x + w / 2, top - w * 1.3f); lineTo(x + w / 2, top - w * 1.3f - 50) }, c(0xFF6D4C41), 4f),
        fill(path { poly(x + w / 2, top - w * 1.3f - 50, x + w / 2 + 40, top - w * 1.3f - 38, x + w / 2, top - w * 1.3f - 26) }, flag, width = 3f),
        fill(path { moveTo(x + w / 2 - 14, top + 90); lineTo(x + w / 2 - 14, top + 60); quadTo(x + w / 2, top + 40, x + w / 2 + 14, top + 60); lineTo(x + w / 2 + 14, top + 90); close() }, c(0xFF7E57C2), width = 3f),
    )

    private fun battlements(left: Float, right: Float, top: Float, ink: Ink): Element {
        val p = PathBuilder()
        var x = left
        while (x < right) {
            p.rect(x, top - 28, x + 30, top + 2)
            x += 52
        }
        return fill(p.build(), ink, width = 4f)
    }

    private fun castle(wall: Ink, roof: Ink, flag: Ink, windowInk: Ink): List<Element> {
        val out = ArrayList<Element>()
        out += fill(path { rect(230, 440, 770, 760) }, wall, width = 4f)
        out += battlements(230f, 770f, 440f, wall)
        out += fill(path { rect(400, 330, 600, 760) }, wall, width = 4f)
        out += battlements(400f, 600f, 330f, wall)
        for (x in listOf(290, 680)) {
            out += fill(path { moveTo(x - 18f, 560); lineTo(x - 18f, 525); quadTo(x.toFloat(), 500, x + 18f, 525); lineTo(x + 18f, 560); close() }, windowInk, width = 3f)
        }
        out += tower(160f, 340f, 110f, 760f, wall, roof, flag)
        out += tower(730f, 340f, 110f, 760f, wall, roof, flag)
        out += tower(455f, 230f, 90f, 340f, wall, roof, flag)
        return out
    }

    private fun butterfly(cx: Float, cy: Float, ink: Ink): List<Element> = listOf(
        fill(path { ellipse(cx - 16, cy - 10, 16, 13) }, ink, width = 2.5f),
        fill(path { ellipse(cx + 16, cy - 10, 16, 13) }, ink, width = 2.5f),
        fill(path { ellipse(cx - 12, cy + 10, 11, 9) }, ink, width = 2.5f),
        fill(path { ellipse(cx + 12, cy + 10, 11, 9) }, ink, width = 2.5f),
        fill(path { ellipse(cx, cy, 4, 18) }, c(0xFF5D4037), width = 1.5f),
    )

    private fun pine(cx: Float, base: Float, h: Float): List<Element> {
        val w = h * 0.55f
        return listOf(
            fill(path { rect(cx - 12, base - 30, cx + 12, base) }, c(0xFF6D4C41)),
            fill(path { poly(cx - w, base - 30, cx, base - h * 0.55f, cx + w, base - 30) }, c(0xFF2E7D32)),
            fill(path { poly(cx - w * 0.8f, base - h * 0.4f, cx, base - h * 0.85f, cx + w * 0.8f, base - h * 0.4f) }, c(0xFF388E3C)),
            fill(path { poly(cx - w * 0.55f, base - h * 0.7f, cx, base - h * 1.1f, cx + w * 0.55f, base - h * 0.7f) }, c(0xFF43A047)),
            flat(path { poly(cx - w * 0.3f, base - h * 0.92f, cx, base - h * 1.1f, cx + w * 0.3f, base - h * 0.92f) }, c(0xFFFFFFFF)),
        )
    }

    private fun fish(cx: Float, cy: Float, ink: Ink, flip: Boolean): List<Element> {
        val d = if (flip) -1 else 1
        return listOf(
            fill(path { poly(cx - d * 40, cy, cx - d * 70, cy - 22, cx - d * 70, cy + 22) }, ink),
            fill(path { ellipse(cx, cy, 46, 28) }, ink),
            flat(path { circle(cx + d * 24, cy - 6, 6) }, c(0xFFFFFFFF)),
            flat(path { circle(cx + d * 25, cy - 6, 3) }, c(0xFF263238)),
        )
    }

    private fun bubbles(seed: Long, area: Box, n: Int): List<Element> {
        val rng = Rng(seed)
        return (0 until n).map {
            val r = 6 + rng.next() * 14
            Shape(path { circle(area.left + rng.next() * area.width, area.top + rng.next() * area.height, r) }, c(0x33FFFFFF), c(0xCCFFFFFF), 2.5f)
        }
    }

    private fun seaweed(x: Float, h: Float, ink: Ink): Element = fill(path {
        moveTo(x - 12, 1000)
        smoothTo(pts(x - 30, 1000 - h * 0.25f, x + 5, 1000 - h * 0.5f, x - 25, 1000 - h * 0.75f, x, 1000 - h))
        smoothTo(pts(x + 8, 1000 - h * 0.75f, x + 25, 1000 - h * 0.5f, x - 5, 1000 - h * 0.25f, x + 12, 1000))
        close()
    }, ink)

    val all: List<Background> = listOf(
        Background("castle", "Castle", buildList<Element> {
            add(sky(0xFF8FD3FF, 0xFFDDF3FF))
            addAll(sun(870f, 130f, 55f))
            add(cloud(170f, 150f, 1.1f)); add(cloud(600f, 90f, 0.8f))
            add(flat(path { ellipse(220, 790, 460, 170) }, c(0xFFA5E08F)))
            add(flat(path { ellipse(820, 800, 420, 160) }, c(0xFF8FD47A)))
            addAll(castle(c(0xFFF8BBD0), c(0xFF9C5BD3), c(0xFFFF5FA2), c(0xFF7E57C2)))
            add(ground(760, 0xFF7BCB6A))
            add(flat(path { moveTo(430, 760); lineTo(570, 760); lineTo(700, 1000); lineTo(300, 1000); close() }, c(0xFFF3E2B3)))
            addAll(tinyFlowers(14, Box(20f, 790f, 260f, 980f), 7))
            addAll(tinyFlowers(14, Box(740f, 790f, 980f, 980f), 11))
        }),
        Background("garden", "Garden", buildList<Element> {
            add(sky(0xFFBDEBFF, 0xFFF0FBFF))
            addAll(sun(140f, 140f, 60f))
            add(cloud(720f, 140f, 1f))
            val fence = PathBuilder()
            for (x in 0..1000 step 64) fence.poly(x, 640, x, 560, x + 20, 540, x + 40, 560, x + 40, 640)
            add(fill(fence.build(), c(0xFFFFFFFF), stroke = c(0xFFB0BEC5), width = 3f))
            add(flat(path { rect(0, 580, 1000, 600) }, c(0xFFFFFFFF)))
            val bushes = PathBuilder()
            for (x in 0..1000 step 110) bushes.circle(x, 700, 80)
            addAll(blob(bushes.build(), c(0xFF5DAE5B), 4f))
            add(ground(720, 0xFF8BD17C))
            for ((x, h, col) in listOf(Triple(80f, 360f, 0xFFFF6B9A), Triple(190f, 280f, 0xFFFFD54F), Triple(820f, 300f, 0xFFB39DDB), Triple(930f, 380f, 0xFFFF8A65))) {
                add(line(path { moveTo(x, 1000); quadTo(x - 20, 1000 - h / 2, x, 1000 - h) }, c(0xFF388E3C), 8f))
                add(fill(path { ellipse(x - 30, 1000 - h * 0.45f, 28, 12) }.rotate(-30f, x - 30, 1000 - h * 0.45f), c(0xFF4CAF50), width = 3f))
                addAll(flower(x, 1000 - h, c(col), 26f))
            }
            addAll(butterfly(260f, 360f, c(0xFFFF8FB8)))
            addAll(butterfly(780f, 420f, c(0xFF81D4FA)))
            addAll(tinyFlowers(18, Box(260f, 760f, 740f, 990f), 5))
        }),
        Background("beach", "Beach", buildList<Element> {
            add(sky(0xFF7FD1FF, 0xFFFFE5C4, 560f))
            addAll(sun(780f, 170f, 65f))
            add(cloud(220f, 130f, 0.9f))
            add(flat(path { rect(0, 520, 1000, 760) }, Ink.Linear(0f, 520f, 0f, 740f, intArrayOf(0xFF2FA5E8.toInt(), 0xFF7FD8FF.toInt()))))
            for (y in listOf(560, 610, 660)) {
                val p = PathBuilder().moveTo(0, y)
                for (x in 0 until 1000 step 80) p.quadTo(x + 40, y - 12, x + 80, y)
                add(line(p.build(), c(0xAAFFFFFF), 4f))
            }
            add(flat(path { moveTo(-10, 720); smoothTo(pts(250, 700, 500, 716, 750, 696, 1010, 712)); lineTo(1010, 1010); lineTo(-10, 1010); close() }, c(0xFFF6DFA6)))
            add(line(path { moveTo(110, 940); cubicTo(130, 760, 120, 600, 190, 420) }, c(0xFFA1887F), 30f))
            for (deg in listOf(-150f, -110f, -60f, -20f, 20f)) {
                add(fill(path { moveTo(190, 420); quadTo(250, 380, 320, 420); quadTo(250, 410, 190, 420); close() }.rotate(deg + 20f, 190f, 420f), c(0xFF43A047), width = 3f))
            }
            add(fill(path { circle(178, 440, 13); circle(204, 444, 13) }, c(0xFF795548), width = 2f))
            add(line(path { moveTo(860, 600); lineTo(870, 900) }, c(0xFF8D6E63), 8f))
            add(fill(path { moveTo(740, 620); quadTo(860, 500, 980, 600); close() }, c(0xFFFF6B6B), width = 3f))
            add(Group(path { moveTo(740, 620); quadTo(860, 500, 980, 600); close() }, listOf(
                flat(path { poly(830, 520, 860, 520, 840, 640, 800, 640) }, c(0xFFFFFFFF)),
                flat(path { poly(900, 520, 930, 520, 960, 640, 920, 640) }, c(0xFFFFFFFF)),
            )))
            add(fill(path { star(800, 900, 26, 11) }, c(0xFFFF8A65), width = 3f))
            add(fill(path { ellipse(160, 940, 20, 14) }, c(0xFFFFCCBC), width = 2.5f))
        }),
        Background("snow", "Snowy hills", buildList<Element> {
            add(sky(0xFFB3D4FF, 0xFFEAF2FF))
            add(fill(path { poly(-50, 700, 220, 300, 500, 700) }, c(0xFFE3EEFA), width = 4f))
            add(fill(path { poly(380, 700, 700, 260, 1050, 700) }, c(0xFFD6E5F7), width = 4f))
            add(flat(path { poly(220, 300, 170, 372, 200, 360, 222, 390, 248, 360, 270, 372) }, c(0xFFFFFFFF)))
            add(flat(path { poly(700, 260, 640, 345, 676, 330, 700, 362, 728, 330, 762, 345) }, c(0xFFFFFFFF)))
            add(ground(690, 0xFFFFFFFF))
            add(flat(path { ellipse(300, 860, 260, 40) }, c(0xFFE3EEFA)))
            addAll(pine(110f, 820f, 300f)); addAll(pine(210f, 760f, 220f))
            add(fill(path { circle(870, 860, 70) }, c(0xFFFFFFFF), width = 4f))
            add(fill(path { circle(870, 760, 50) }, c(0xFFFFFFFF), width = 4f))
            add(fill(path { circle(870, 680, 38) }, c(0xFFFFFFFF), width = 4f))
            add(fill(path { poly(870, 684, 906, 690, 870, 694) }, c(0xFFFF8A3D), width = 2f))
            add(flat(path { circle(858, 672, 5); circle(882, 672, 5) }, c(0xFF263238)))
            add(fill(path { roundRect(830, 708, 910, 724, 8) }, c(0xFFE53935), width = 3f))
            add(fill(path { rect(840, 610, 900, 646) }, c(0xFF37474F), width = 3f))
            add(fill(path { rect(826, 640, 914, 650) }, c(0xFF37474F), width = 3f))
            addAll(sprinkleStars(40, Box(0f, 0f, 1000f, 700f), 4f, 9f, c(0xFFFFFFFF), 3, points = 6))
        }),
        Background("night", "Starry night", buildList<Element> {
            add(sky(0xFF141A46, 0xFF3B2A6B, 900f))
            addAll(sprinkleStars(60, Box(0f, 0f, 1000f, 650f), 3f, 10f, c(0xFFFFF59D), 17))
            add(flat(path { circle(820, 170, 120) }, Ink.Radial(820f, 170f, 120f, intArrayOf(0x55FFF3B0, 0x00FFF3B0))))
            add(fill(path { moveTo(800, 100); cubicTo(720, 120, 720, 240, 820, 250); cubicTo(770, 215, 762, 135, 800, 100); close() }, c(0xFFFFF3B0), width = 3f))
            add(flat(path { ellipse(250, 820, 480, 190) }, c(0xFF2C2257)))
            add(flat(path { ellipse(820, 840, 420, 170) }, c(0xFF33286A)))
            addAll(castle(c(0xFF231A50), c(0xFF1A1340), c(0xFFFF8FB8), c(0xFFFFD54F)))
            add(ground(780, 0xFF231C4D))
            addAll(tinyFlowers(8, Box(30f, 800f, 280f, 980f), 23))
        }),
        Background("rainbow", "Rainbow", buildList<Element> {
            add(sky(0xFFC9F0FF, 0xFFFFF6FB))
            val colors = listOf(0xFFFF6B6B, 0xFFFFA94D, 0xFFFFE066, 0xFF8CE99A, 0xFF74C0FC, 0xFF91A7FF, 0xFFD0A2F7)
            colors.forEachIndexed { i, col ->
                val r = 450f - i * 34f
                add(line(path { moveTo(500 - r, 800); cubicTo(500 - r, 800 - r * 1.33f, 500 + r, 800 - r * 1.33f, 500 + r, 800) }, c(col), 36f))
            }
            add(cloud(90f, 760f, 1.4f, c(0xFFFFFFFF)))
            add(cloud(910f, 760f, 1.4f, c(0xFFFFFFFF)))
            add(cloud(500f, 160f, 0.7f))
            add(flat(path { ellipse(200, 900, 420, 150) }, c(0xFFB9F6CA)))
            add(flat(path { ellipse(820, 910, 400, 140) }, c(0xFFA7E3B5)))
            add(ground(850, 0xFF9EDCA9))
            addAll(sprinkleStars(14, Box(100f, 100f, 900f, 450f), 6f, 12f, c(0xFFFFFFFF), 29, points = 4))
            addAll(tinyFlowers(16, Box(40f, 870f, 960f, 990f), 31))
        }),
        Background("tennis", "Tennis court", buildList<Element> {
            add(sky(0xFF9ED8FF, 0xFFE9F7FF, 420f))
            add(cloud(200f, 120f, 0.9f)); add(cloud(800f, 160f, 0.7f))
            val fence = path { rect(0, 360, 1000, 610) }
            add(flat(fence, c(0xFF2E7D5B)))
            val mesh = ArrayList<Element>()
            for (x in -300..1300 step 40) {
                mesh += line(path { moveTo(x, 360); lineTo(x + 250, 610) }, c(0x553EA77A), 2f)
                mesh += line(path { moveTo(x + 250, 360); lineTo(x, 610) }, c(0x553EA77A), 2f)
            }
            add(Group(fence, mesh))
            add(ground(600, 0xFF5DAE5B))
            add(fill(path { poly(200, 620, 800, 620, 1080, 1000, -80, 1000) }, c(0xFF3E7DD6), stroke = c(0xFFFFFFFF), width = 8f))
            add(line(path { moveTo(300, 620); lineTo(140, 1000) }, c(0xFFFFFFFF), 6f))
            add(line(path { moveTo(700, 620); lineTo(860, 1000) }, c(0xFFFFFFFF), 6f))
            add(line(path { moveTo(500, 620); lineTo(500, 680) }, c(0xFFFFFFFF), 6f))
            add(line(path { moveTo(268, 690); lineTo(732, 690) }, c(0xFFFFFFFF), 6f))
            for (x in listOf(40, 960)) add(line(path { moveTo(x, 690); lineTo(x, 800) }, c(0xFF455A64), 10f))
            val net = path { rect(40, 700, 960, 790) }
            val netLines = ArrayList<Element>()
            for (x in 40..960 step 20) netLines += line(path { moveTo(x, 700); lineTo(x, 790) }, c(0x99FFFFFF), 1.5f)
            for (y in 700..790 step 18) netLines += line(path { moveTo(40, y); lineTo(960, y) }, c(0x99FFFFFF), 1.5f)
            add(flat(net, c(0x55263238)))
            add(Group(net, netLines))
            add(fill(path { rect(36, 692, 964, 706) }, c(0xFFFFFFFF), width = 2f))
            for ((x, y) in listOf(130 to 900, 880 to 940, 820 to 870)) {
                add(fill(path { circle(x, y, 13) }, c(0xFFD7F542), width = 2.5f))
                add(line(path { moveTo(x - 10f, y - 6f); quadTo(x.toFloat(), y + 2f, x + 10f, y - 6f) }, c(0xFFFFFFFF), 2f))
            }
        }),
        Background("volleyball", "Beach volleyball", buildList<Element> {
            add(sky(0xFF87D4FF, 0xFFFFF3D6, 560f))
            addAll(sun(150f, 140f, 55f))
            add(cloud(760f, 120f, 1f))
            add(flat(path { rect(0, 520, 1000, 610) }, c(0xFF29B6F6)))
            add(line(path { moveTo(0, 540); lineTo(1000, 540) }, c(0x88FFFFFF), 4f))
            add(ground(600, 0xFFF4D79B))
            add(flat(path { ellipse(250, 800, 180, 30) }, c(0xFFEBC683)))
            add(flat(path { ellipse(780, 920, 200, 34) }, c(0xFFEBC683)))
            for (x in listOf(90, 910)) add(fill(path { roundRect(x - 9, 360, x + 9, 820, 6) }, c(0xFF8D6E63), width = 3f))
            val net = path { rect(99, 400, 901, 510) }
            val netLines = ArrayList<Element>()
            for (x in 99..901 step 22) netLines += line(path { moveTo(x, 400); lineTo(x, 510) }, c(0xCCFFFFFF), 2f)
            for (y in 400..510 step 22) netLines += line(path { moveTo(99, y); lineTo(901, y) }, c(0xCCFFFFFF), 2f)
            add(Group(net, netLines))
            add(fill(path { rect(96, 392, 904, 408) }, c(0xFFFFFFFF), width = 2f))
            add(fill(path { rect(96, 506, 904, 516) }, c(0xFF1E88E5), width = 2f))
            val ball = path { circle(870, 900, 40) }
            add(fill(ball, c(0xFFFFFFFF)))
            add(Group(ball, listOf(
                line(path { moveTo(834, 860); quadTo(876, 900, 864, 948) }, c(0xFFFFD54F), 14f),
                line(path { moveTo(912, 884); quadTo(870, 896, 842, 934) }, c(0xFF42A5F5), 14f),
            )))
            add(Shape(ball, null, c(0xFF90A4AE), 3f))
        }),
        Background("sea", "Under the sea", buildList<Element> {
            add(sky(0xFF4FC3F7, 0xFF0D47A1, 1000f))
            for (x in listOf(120, 420, 760)) {
                add(flat(path { poly(x, 0, x + 90, 0, x + 260, 900, x + 120, 900) }, c(0x22FFFFFF)))
            }
            add(flat(path { moveTo(-10, 880); smoothTo(pts(250, 860, 500, 875, 750, 855, 1010, 870)); lineTo(1010, 1010); lineTo(-10, 1010); close() }, c(0xFFF2D59B)))
            add(seaweed(70f, 420f, c(0xFF43A047))); add(seaweed(150f, 300f, c(0xFF66BB6A)))
            add(seaweed(870f, 380f, c(0xFF43A047))); add(seaweed(940f, 280f, c(0xFF66BB6A)))
            for ((x, col) in listOf(260 to 0xFFFF8A80, 760 to 0xFFF48FB1)) {
                add(fill(path { smoothClosed(pts(x - 50, 900, x - 60, 820, x - 30, 830, x - 30, 760, x, 800, x + 20, 740, x + 40, 820, x + 64, 790, x + 52, 900)) }, c(col), width = 3f))
            }
            addAll(fish(170f, 300f, c(0xFFFFA726), false))
            addAll(fish(830f, 220f, c(0xFFFFEE58), true))
            addAll(fish(880f, 560f, c(0xFFBA68C8), true))
            addAll(bubbles(41, Box(40f, 60f, 960f, 760f), 22))
            add(fill(path { star(560, 940, 28, 12) }, c(0xFFFF7043), width = 3f))
        }),
        Background("bedroom", "Bedroom", buildList<Element> {
            add(flat(path { rect(0, 0, 1000, 720) }, c(0xFFFCE4EC)))
            for (x in 0..1000 step 80) add(flat(path { rect(x, 0, x + 36, 720) }, c(0xFFF8D1E0)))
            add(fill(path { roundRect(100, 120, 380, 430, 18) }, c(0xFFFFFFFF), width = 5f))
            add(flat(path { roundRect(122, 142, 358, 408, 8) }, Ink.Linear(0f, 142f, 0f, 408f, intArrayOf(0xFF8FD3FF.toInt(), 0xFFE1F5FE.toInt()))))
            add(cloud(240f, 260f, 0.7f, c(0xFFFFFFFF)))
            add(line(path { moveTo(240, 142); lineTo(240, 408) }, c(0xFFFFFFFF), 8f))
            add(line(path { moveTo(122, 275); lineTo(358, 275) }, c(0xFFFFFFFF), 8f))
            add(fill(path { moveTo(70, 100); quadTo(150, 260, 100, 470); lineTo(60, 470); lineTo(60, 100); close() }, c(0xFFF06292), width = 4f))
            add(fill(path { moveTo(410, 100); quadTo(330, 260, 380, 470); lineTo(420, 470); lineTo(420, 100); close() }, c(0xFFF06292), width = 4f))
            add(fill(path { roundRect(50, 86, 430, 104, 9) }, c(0xFFBA68C8), width = 3f))
            add(fill(path { roundRect(700, 150, 880, 310, 10) }, c(0xFFFFE082), width = 5f))
            add(flat(path { roundRect(716, 166, 864, 294, 4) }, c(0xFFFFFFFF)))
            add(fill(path { heart(790, 196, 80) }, c(0xFFFF5FA2), width = 3f))
            val lights = PathBuilder().moveTo(0, 30).quadTo(250, 110, 500, 40).quadTo(750, 110, 1000, 30).build()
            add(line(lights, c(0xFF8D6E63), 3f))
            val bulbs = listOf(0xFFFF6B6B, 0xFFFFE066, 0xFF8CE99A, 0xFF74C0FC, 0xFFD0A2F7)
            for (i in 0 until 12) {
                val t = i / 11f
                val x = t * 1000f
                val half = if (t < 0.5f) t * 2 else (t - 0.5f) * 2
                val y = (if (t < 0.5f) 30f else 40f) + 4f * half * (1 - half) * 70f
                add(fill(path { ellipse(x, y + 16, 9, 13) }, c(bulbs[i % bulbs.size]), width = 2f))
            }
            add(flat(path { rect(0, 720, 1000, 1000) }, c(0xFFD7A574)))
            for (y in listOf(780, 860, 940)) add(line(path { moveTo(0, y); lineTo(1000, y) }, c(0xFFC18F5E), 3f))
            add(line(path { moveTo(0, 720); lineTo(1000, 720) }, c(0xFFB27C4E), 6f))
            add(fill(path { ellipse(500, 900, 400, 74) }, c(0xFFCE93D8), width = 4f))
            add(Shape(path { ellipse(500, 900, 340, 54) }, null, c(0xFFF3E5F5), 5f))
            add(fill(path { roundRect(30, 600, 210, 760, 10) }, c(0xFF81D4FA), width = 4f))
            add(fill(path { roundRect(20, 590, 220, 622, 8) }, c(0xFF4FC3F7), width = 4f))
            add(fill(path { star(120, 690, 28, 12) }, c(0xFFFFE066), width = 3f))
            add(fill(path { roundRect(790, 560, 1010, 800, 24) }, c(0xFFB39DDB), width = 4f))
            add(fill(path { roundRect(780, 640, 1010, 800, 20) }, c(0xFFF48FB1), width = 4f))
            add(fill(path { roundRect(820, 580, 960, 640, 26) }, c(0xFFFFFFFF), width = 4f))
            add(dots(path { roundRect(780, 640, 1010, 800, 20) }, c(0x88FFFFFF), 40f, 8f))
        }),
    )

    fun byId(id: String): Background = all.firstOrNull { it.id == id } ?: all.first()
}
