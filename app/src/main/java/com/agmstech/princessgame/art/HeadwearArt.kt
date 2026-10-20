package com.agmstech.princessgame.art

import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/** Crowns, tiaras and other things worn on the head. */
object HeadwearArt {

    private fun bow(cx: Float, cy: Float, s: Float, ink: Ink): List<Element> {
        fun p(x: Number, y: Number) = pt(cx + (x.toFloat() - 300f) * s, cy + (y.toFloat() - 100f) * s)
        val loop = fill(path { smoothClosed(listOf(p(300, 100), p(270, 70), p(238, 62), p(228, 95), p(240, 128), p(272, 120))) }, ink)
        val fold = line(path { moveTo(p(258, 82)); val a = p(248, 96); val b = p(256, 112); quadTo(a.x, a.y, b.x, b.y) }, Ink.Of(Role.MAIN, -0.3f), 2.5f * s)
        val knot = fill(path { ellipse(cx, cy - 2 * s, 16 * s, 18 * s) }, Ink.Of(Role.MAIN, -0.1f))
        return listOf(loop, loop.mirrored(cx), fold, fold.mirrored(cx), knot)
    }

    val items: List<Item> = listOf(
        Item.none(),
        item("tiara", "Tiara", 0xFFCFD8DC) {
            add(Layer.HEADWEAR,
                fill(path { poly(282, 120, 300, 70, 318, 120) }, cloth()),
                fill(path { poly(250, 132, 262, 98, 276, 124) }, cloth()),
                fill(path { poly(324, 124, 338, 98, 350, 132) }, cloth()),
                fill(path { moveTo(234, 136); quadTo(300, 92, 366, 136); lineTo(362, 147); quadTo(300, 105, 238, 147); close() }, cloth()),
                fill(path { circle(300, 68, 5) }, cloth(0.3f)),
                fill(path { circle(300, 101, 9) }, PINK),
                fill(path { circle(262, 116, 5.5f) }, solid(0xFF7C4DFF)),
                fill(path { circle(338, 116, 5.5f) }, solid(0xFF7C4DFF)),
                sparkle(306, 96, 4),
            )
        },
        item("crown", "Crown", 0xFFFFC531) {
            add(Layer.HEADWEAR,
                fill(path {
                    moveTo(240, 124); lineTo(234, 54); lineTo(266, 88); lineTo(284, 40); lineTo(300, 76)
                    lineTo(316, 40); lineTo(334, 88); lineTo(366, 54); lineTo(360, 124); quadTo(300, 134, 240, 124); close()
                }, cloth()),
                fill(path { moveTo(238, 100); quadTo(300, 110, 362, 100); lineTo(361, 124); quadTo(300, 134, 239, 124); close() }, cloth(-0.12f)),
                fill(path { circle(234, 52, 7) }, cloth(0.25f)),
                fill(path { circle(284, 38, 7) }, cloth(0.25f)),
                fill(path { circle(316, 38, 7) }, cloth(0.25f)),
                fill(path { circle(366, 52, 7) }, cloth(0.25f)),
                fill(path { circle(268, 114, 7) }, solid(0xFFE53935)),
                fill(path { heart(300, 106, 20) }, solid(0xFF42A5F5)),
                fill(path { circle(332, 114, 7) }, solid(0xFF43A047)),
                sparkle(296, 60, 5), sparkle(250, 76, 4),
            )
        },
        item("flowers", "Flowers", 0xFFFF8FB8) {
            val spots = listOf(208f to 166f, 228f to 129f, 261f to 107f, 300f to 99f, 339f to 107f, 372f to 129f, 392f to 166f)
            for (i in 0 until spots.size - 1) {
                val (x0, y0) = spots[i]; val (x1, y1) = spots[i + 1]
                add(Layer.HEADWEAR, fill(path { ellipse((x0 + x1) / 2, (y0 + y1) / 2 - 4, 10, 6) }, LEAF, width = 2f))
            }
            spots.forEachIndexed { i, (x, y) -> add(Layer.HEADWEAR, flower(x, y, if (i % 2 == 0) cloth() else cloth(0.45f))) }
        },
        item("bow", "Bow", 0xFFFF5FA2) {
            add(Layer.HEADWEAR, bow(300f, 100f, 1f, cloth()))
        },
        item("headband", "Headband", 0xFF9C5BD3) {
            add(Layer.HEADWEAR, fill(path {
                moveTo(203, 188)
                smoothTo(pts(224, 128, 300, 96, 376, 128, 397, 188))
                lineTo(385, 192)
                smoothTo(pts(364, 140, 300, 111, 236, 140, 215, 192))
                close()
            }, cloth()))
            add(Layer.HEADWEAR, bow(352f, 122f, 0.55f, cloth(0.25f)))
        },
        item("stars", "Star clips", 0xFFFFD54F) {
            add(Layer.HEADWEAR,
                fill(path { star(226, 150, 20, 9, rotationDeg = -15f) }, cloth()),
                fill(path { star(374, 150, 20, 9, rotationDeg = 15f) }, cloth()),
                fill(path { star(250, 128, 10, 4.5f) }, cloth(0.3f)),
                fill(path { star(350, 128, 10, 4.5f) }, cloth(0.3f)),
            )
        },
        item("visor", "Tennis visor", 0xFFFFFFFF) {
            add(Layer.HEADWEAR,
                fill(path {
                    moveTo(204, 172); smoothTo(pts(240, 140, 300, 130, 360, 140, 396, 172))
                    lineTo(396, 188); smoothTo(pts(360, 158, 300, 148, 240, 158, 204, 188)); close()
                }, cloth()),
                fill(path { moveTo(222, 168); cubicTo(240, 224, 360, 224, 378, 168); quadTo(300, 150, 222, 168); close() }, cloth(-0.08f)),
                line(path { moveTo(240, 176); cubicTo(256, 206, 344, 206, 360, 176) }, solid(0xFF4CAF50), 4f),
            )
        },
        item("sweatband", "Sweatband", 0xFFF0508C) {
            add(Layer.HEADWEAR,
                fill(path {
                    moveTo(202, 166); smoothTo(pts(240, 142, 300, 134, 360, 142, 398, 166))
                    lineTo(399, 192); smoothTo(pts(360, 166, 300, 158, 240, 166, 201, 192)); close()
                }, cloth()),
                flat(path {
                    moveTo(203, 176); smoothTo(pts(240, 151, 300, 143, 360, 151, 397, 176))
                    lineTo(397, 182); smoothTo(pts(360, 157, 300, 149, 240, 157, 203, 182)); close()
                }, WHITE),
            )
        },
        item("sunhat", "Sun hat", 0xFFF5D78E) {
            add(Layer.HEADWEAR,
                fill(path { ellipse(300, 146, 170, 34) }, cloth(0.1f)),
                fill(path { moveTo(222, 142); cubicTo(222, 58, 378, 58, 378, 142); quadTo(300, 152, 222, 142); close() }, cloth()),
                fill(path { moveTo(223, 122); quadTo(300, 132, 377, 122); lineTo(378, 142); quadTo(300, 152, 222, 142); close() }, solid(0xFFFF8FB8)),
            )
            add(Layer.HEADWEAR, flower(360f, 132f, solid(0xFFFFFFFF), 8f))
        },
    )
}
