package com.agmstech.princessgame.art

import com.agmstech.princessgame.art.Garments.hips
import com.agmstech.princessgame.art.Garments.legTube
import com.agmstech.princessgame.art.Garments.shirt
import com.agmstech.princessgame.art.Garments.skirt
import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/** Tops, and pants & skirts, worn together. */
object SeparatesArt {

    private fun legs(t0: Float, t1: Float, extra: Float, ink: Ink): List<Element> =
        pair(fill(legTube(t0, t1, extra), ink))

    private val neckRib = line(path { moveTo(272, 338); quadTo(300, 358, 328, 338) }, Ink.Of(Role.MAIN, -0.3f), 3f)

    val tops: List<Item> = listOf(
        item("tee", "T-shirt", 0xFFFF8FB8) {
            add(Layer.TOP, pair(fill(Garments.sleeveShort(), cloth())))
            add(Layer.TOP, fill(shirt, cloth()), neckRib)
            add(Layer.TOP, fill(path { heart(300, 418, 64) }, WHITE, width = 2.5f))
        },
        item("blouse", "Puffy blouse", 0xFFFFF59D) {
            add(Layer.TOP, fill(shirt, cloth()))
            add(Layer.TOP, pair(fill(Garments.puffSleeve(), cloth(0.2f))))
            add(Layer.TOP, Garments.collar())
            for (y in listOf(400, 440, 480, 520)) add(Layer.TOP, fill(path { circle(300, y, 4.5f) }, PINK, width = 1.5f))
            add(Layer.TOP, line(path {
                moveTo(238, 590)
                for (i in 0 until 8) quadTo(238 + i * 15.5f + 7.75f, 604, 238 + (i + 1) * 15.5f, 592)
            }, cloth(-0.3f), 2.5f))
        },
        item("sweater", "Sweater", 0xFFE53935) {
            add(Layer.TOP, pair(fill(Garments.sleeveLong(), cloth())))
            add(Layer.TOP, pair(fill(Garments.cuff(), cloth(-0.15f))))
            add(Layer.TOP, fill(shirt, cloth()), neckRib)
            add(Layer.TOP, fill(path { moveTo(238, 572); quadTo(300, 580, 362, 572); lineTo(364, 598); quadTo(300, 606, 236, 598); close() }, cloth(-0.15f)))
            for (x in listOf(262, 300, 338)) add(Layer.TOP, fill(path { heart(x, 438, 26) }, WHITE, width = 2f))
            add(Layer.TOP, line(path { moveTo(246, 476); lineTo(354, 476) }, WHITE, 3f), line(path { moveTo(246, 408); lineTo(354, 408) }, WHITE, 3f))
        },
        item("hoodie", "Hoodie", 0xFF81D4FA) {
            add(Layer.TOP, fill(path { smoothClosed(pts(240, 352, 258, 318, 300, 302, 342, 318, 360, 352, 300, 366)) }, cloth(-0.12f)))
            add(Layer.TOP, pair(fill(Garments.sleeveLong(), cloth())))
            add(Layer.TOP, pair(fill(Garments.cuff(), cloth(-0.12f))))
            add(Layer.TOP, fill(shirt, cloth()))
            add(Layer.TOP, fill(path { moveTo(258, 500); lineTo(342, 500); lineTo(354, 562); lineTo(246, 562); close() }, cloth(-0.08f)))
            add(Layer.TOP, line(path { moveTo(288, 350); lineTo(284, 404) }, WHITE, 3.5f), line(path { moveTo(312, 350); lineTo(316, 404) }, WHITE, 3.5f))
            add(Layer.TOP, fill(path { circle(284, 406, 4) }, WHITE, width = 1.5f), fill(path { circle(316, 406, 4) }, WHITE, width = 1.5f))
        },
        item("startee", "Star tee", 0xFFFFD54F) {
            add(Layer.TOP, pair(fill(Garments.sleeveShort(), cloth())))
            add(Layer.TOP, fill(shirt, cloth()), neckRib)
            add(Layer.TOP, fill(path { star(300, 450, 50, 22) }, cloth(0.65f)))
            add(Layer.TOP, sparkle(250, 400, 7), sparkle(352, 510, 7))
        },
        item("stripes", "Stripy top", 0xFF42A5F5) {
            val sleeveL = Garments.sleeveLong()
            val sleeveR = sleeveL.mirrorX()
            for (s in listOf(sleeveL, sleeveR)) {
                add(Layer.TOP, fill(s, cloth()))
                add(Layer.TOP, stripes(s, WHITE, 372f, 560f, 30f, 12f))
                add(Layer.TOP, Shape(s, null, Ink.Of(Role.MAIN, -0.45f), 3f))
            }
            add(Layer.TOP, fill(shirt, cloth()))
            add(Layer.TOP, stripes(shirt, WHITE, 372f, 610f, 30f, 12f))
            add(Layer.TOP, Shape(shirt, null, Ink.Of(Role.MAIN, -0.45f), 3f))
        },
        item("ruffle", "Ruffle top", 0xFFF0508C) {
            add(Layer.TOP, fill(Garments.tank, cloth()))
            val ruffle = PathBuilder()
            for (i in 0 until 6) ruffle.circle(258 + i * 17, 356 + (if (i in 1..4) 8 else 0), 13)
            add(Layer.TOP, blob(ruffle.build(), cloth(0.35f), 2.5f))
            add(Layer.TOP, line(path {
                moveTo(238, 590)
                for (i in 0 until 8) quadTo(238 + i * 15.5f + 7.75f, 606, 238 + (i + 1) * 15.5f, 592)
            }, cloth(-0.3f), 2.5f))
        },
    )

    val bottoms: List<Item> = listOf(
        item("jeans", "Long pants", 0xFF4A78C2) {
            add(Layer.BOTTOM, legs(0.05f, 0.975f, 15f, cloth()))
            add(Layer.BOTTOM, fill(hips, cloth()))
            add(Layer.BOTTOM, pair(line(path { moveTo(250, 562); quadTo(266, 584, 286, 560) }, GOLD, 2.5f)))
            add(Layer.BOTTOM, line(path { moveTo(300, 552); lineTo(300, 612) }, cloth(-0.3f), 2.5f))
            add(Layer.BOTTOM, pair(fill(legTube(0.93f, 0.975f, 17f), cloth(-0.1f))))
        },
        item("shorts", "Shorts", 0xFFFFA726) {
            add(Layer.BOTTOM, legs(0.05f, 0.4f, 20f, cloth()))
            add(Layer.BOTTOM, fill(hips, cloth()))
            add(Layer.BOTTOM, line(path { moveTo(300, 552); lineTo(300, 612) }, cloth(-0.3f), 2.5f))
            add(Layer.BOTTOM, pair(fill(legTube(0.35f, 0.4f, 22f), cloth(-0.12f))))
        },
        item("leggings", "Leggings", 0xFF9C5BD3) {
            add(Layer.BOTTOM, legs(0.05f, 0.97f, 6f, cloth()))
            add(Layer.BOTTOM, fill(hips, cloth()))
            add(Layer.BOTTOM, pair(fill(path { star(262, 680, 9, 4) }, WHITE, width = 1.5f)))
            add(Layer.BOTTOM, pair(fill(path { star(270, 800, 8, 3.5f) }, WHITE, width = 1.5f)))
        },
        item("skirt", "Skirt", 0xFFE53935) {
            val s = skirt(538f, 58f, 705f, 104f, bell = 0.3f, scallops = 1, curve = 10f)
            add(Layer.BOTTOM, fill(s, cloth()))
            add(Layer.BOTTOM, Garments.folds(538f, 58f, 705f, 104f, 6, cloth(-0.25f), inset = 20f))
            add(Layer.BOTTOM, fill(path { moveTo(243, 536); lineTo(357, 536); lineTo(358, 556); lineTo(242, 556); close() }, cloth(-0.12f)))
        },
        item("tutu", "Tutu", 0xFFFF8FB8) {
            add(Layer.BOTTOM, fill(skirt(540f, 60f, 650f, 132f, bell = 0.9f, scallops = 9, depth = 8f, curve = 6f), cloth(0.15f, 0.92f)))
            add(Layer.BOTTOM, fill(skirt(540f, 60f, 630f, 120f, bell = 0.9f, scallops = 8, depth = 8f, curve = 6f), cloth(0.35f, 0.92f)))
            add(Layer.BOTTOM, fill(skirt(540f, 60f, 606f, 104f, bell = 0.9f, scallops = 7, depth = 7f, curve = 4f), cloth(0.55f, 0.95f)))
            add(Layer.BOTTOM, fill(path { moveTo(242, 534); lineTo(358, 534); lineTo(359, 554); lineTo(241, 554); close() }, cloth()))
            add(Layer.BOTTOM, sparkle(250, 600, 6), sparkle(340, 615, 6), sparkle(300, 590, 5))
        },
        item("longskirt", "Long skirt", 0xFF26A69A) {
            val s = skirt(538f, 58f, 840f, 132f, bell = 0.25f, scallops = 1, curve = 12f)
            add(Layer.BOTTOM, fill(s, cloth()))
            add(Layer.BOTTOM, Garments.folds(538f, 58f, 840f, 132f, 6, cloth(-0.25f), inset = 20f))
            add(Layer.BOTTOM, fill(skirt(790f, 124f, 858f, 140f, bell = 0f, scallops = 8, depth = 6f, curve = 12f), cloth(0.3f)))
            add(Layer.BOTTOM, fill(path { moveTo(243, 536); lineTo(357, 536); lineTo(358, 556); lineTo(242, 556); close() }, cloth(-0.12f)))
            for ((x, y) in listOf(250 to 640, 330 to 700, 280 to 760, 360 to 620)) add(Layer.BOTTOM, flower(x.toFloat(), y.toFloat(), WHITE, 6f))
        },
    )
}
