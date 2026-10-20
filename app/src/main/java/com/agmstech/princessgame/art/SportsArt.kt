package com.agmstech.princessgame.art

import com.agmstech.princessgame.art.Garments.hips
import com.agmstech.princessgame.art.Garments.legTube
import com.agmstech.princessgame.art.Garments.shirt
import com.agmstech.princessgame.art.Garments.skirt
import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/** Full sports kits: each one replaces the dress or top-and-bottoms. */
object SportsArt {

    private fun number(text: String, ink: Ink = WHITE) =
        Label(text, 300f, 500f, 78f, ink, Ink.Of(Role.MAIN, -0.45f), 3f)

    private fun shorts(ink: Ink, to: Float = 0.36f): List<Element> =
        pair(fill(legTube(0.05f, to, 20f), ink)) + fill(hips, ink)

    val items: List<Item> = listOf(
        item("tennis", "Tennis", 0xFFFF8FB8) {
            val s = skirt(538f, 58f, 684f, 100f, bell = 0.25f, scallops = 8, depth = 2f, curve = 6f)
            add(Layer.BOTTOM, fill(s, WHITE))
            add(Layer.BOTTOM, Garments.folds(538f, 58f, 684f, 100f, 8, solid(0xFFB0BEC5), inset = 16f))
            add(Layer.BOTTOM, Group(s, listOf(flat(path { rect(180, 664, 420, 674) }, cloth()))))
            add(Layer.TOP, pair(fill(Garments.sleeveShort(), cloth())))
            add(Layer.TOP, pair(fill(Body.armLeft.segment(0.27f, 0.32f, 13f, roundStart = false, roundEnd = false), WHITE, width = 2f)))
            add(Layer.TOP, fill(shirt, cloth()))
            add(Layer.TOP, pair(fill(path { moveTo(300, 346); lineTo(270, 336); lineTo(264, 362); lineTo(292, 376); close() }, WHITE, width = 2.5f)))
            add(Layer.TOP, line(path { moveTo(300, 360); lineTo(300, 420) }, cloth(-0.35f), 2.5f))
            add(Layer.TOP, fill(path { circle(300, 384, 3.5f) }, WHITE, width = 1.5f), fill(path { circle(300, 404, 3.5f) }, WHITE, width = 1.5f))
        },
        item("volleyball", "Volleyball", 0xFF42A5F5) {
            add(Layer.BOTTOM, shorts(cloth(-0.4f), 0.3f))
            add(Layer.BOTTOM, pair(fill(legTube(0.47f, 0.6f, 12f, roundEnd = false), solid(0xFFF5F5F5))))
            add(Layer.TOP, pair(fill(Garments.sleeveShort(10f, 0.22f), cloth())))
            add(Layer.TOP, fill(shirt, cloth()))
            add(Layer.TOP, pair(flat(path { moveTo(240, 420); lineTo(252, 420); lineTo(250, 596); lineTo(238, 596); close() }, WHITE)))
            add(Layer.TOP, number("7"))
        },
        item("soccer", "Soccer", 0xFFE53935) {
            add(Layer.BOTTOM, shorts(WHITE))
            add(Layer.TOP, pair(fill(Garments.sleeveShort(), cloth())))
            add(Layer.TOP, fill(shirt, cloth()))
            add(Layer.TOP, Group(shirt, (0 until 4).map { i -> flat(path { rect(250 + i * 30, 330, 262 + i * 30, 610) }, WHITE) }))
            add(Layer.TOP, Shape(shirt, null, Ink.Of(Role.MAIN, -0.45f), 3f))
            add(Layer.TOP, fill(path { moveTo(272, 337); lineTo(300, 372); lineTo(328, 337); lineTo(318, 335); lineTo(300, 356); lineTo(282, 335); close() }, WHITE, width = 2f))
            add(Layer.TOP, fill(path { star(336, 400, 12, 5) }, GOLD, width = 1.5f))
        },
        item("basketball", "Basketball", 0xFFFFA726) {
            add(Layer.BOTTOM, pair(fill(legTube(0.05f, 0.47f, 22f), cloth())) + fill(hips, cloth()))
            add(Layer.BOTTOM, pair(flat(path { moveTo(236, 560); lineTo(244, 560); lineTo(236, 712); lineTo(228, 712); close() }, WHITE)))
            add(Layer.TOP, fill(Garments.tank, cloth()))
            add(Layer.TOP, line(path { moveTo(270, 338); quadTo(300, 364, 330, 338) }, WHITE, 4f))
            add(Layer.TOP, number("23"))
        },
        item("karate", "Karate", 0xFFFFD54F) {
            add(Layer.BOTTOM, pair(fill(legTube(0.05f, 0.95f, 22f), WHITE)) + fill(hips, WHITE))
            add(Layer.TOP, pair(fill(Body.armLeft.segment(0f, 0.72f, 18f, roundStart = true, roundEnd = false), WHITE)))
            add(Layer.TOP, fill(shirt, WHITE))
            add(Layer.TOP, line(path { moveTo(270, 338); lineTo(330, 470) }, solid(0xFFB0BEC5), 3f))
            add(Layer.TOP, line(path { moveTo(330, 338); lineTo(286, 440) }, solid(0xFFB0BEC5), 3f))
            add(Layer.TOP, fill(path { moveTo(240, 512); quadTo(300, 520, 360, 512); lineTo(361, 536); quadTo(300, 544, 239, 536); close() }, cloth()))
            add(Layer.TOP, fill(path { poly(296, 524, 280, 590, 292, 592, 304, 530) }, cloth()), fill(path { poly(304, 524, 322, 588, 334, 584, 310, 524) }, cloth()))
            add(Layer.TOP, fill(path { roundRect(290, 514, 312, 538, 5) }, cloth(-0.1f)))
        },
        item("ballet", "Ballet", 0xFFFF8FB8) {
            add(Layer.TOP, pair(fill(Garments.sleeveShort(8f, 0.18f), cloth())))
            add(Layer.TOP, fill(path {
                moveTo(272, 336); quadTo(300, 356, 328, 336); lineTo(362, 344); quadTo(382, 350, 382, 372)
                lineTo(366, 410); quadTo(356, 450, 358, 520); lineTo(242, 520); quadTo(244, 450, 234, 410)
                lineTo(218, 372); quadTo(218, 350, 238, 344); close()
            }, cloth()))
            add(Layer.TOP, fill(skirt(512f, 62f, 640f, 140f, bell = 0.95f, scallops = 9, depth = 8f, curve = 6f), cloth(0.25f, 0.92f)))
            add(Layer.TOP, fill(skirt(512f, 62f, 616f, 122f, bell = 0.95f, scallops = 8, depth = 8f, curve = 5f), cloth(0.5f, 0.94f)))
            add(Layer.TOP, fill(path { moveTo(240, 504); quadTo(300, 512, 360, 504); lineTo(360, 524); quadTo(300, 532, 240, 524); close() }, cloth(-0.1f)))
            add(Layer.TOP, sparkle(300, 420, 8), sparkle(268, 380, 5), sparkle(334, 452, 5))
        },
    )
}
