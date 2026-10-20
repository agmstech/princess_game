package com.agmstech.princessgame.art

import com.agmstech.princessgame.art.Garments.legTube
import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/** Socks and shoes. Art is drawn for the left foot and mirrored for the right. */
object FeetArt {

    private val foot = Body.footLeft

    private fun sockFoot() = path { ellipse(foot.x, foot.y, Body.FOOT_RX + 2, Body.FOOT_RY + 2) }

    /** A sock from leg fraction [from] down over the foot. */
    private fun sock(from: Float): VPath = legTube(from, 0.985f, 5f) + sockFoot()

    private fun both(vararg e: Element): List<Element> = e.toList() + e.map { it.mirroredElement() }

    val socks: List<Item> = listOf(
        Item.none(),
        item("ankle", "Ankle socks", 0xFFFFFFFF) {
            add(Layer.SOCKS, both(fill(sock(0.86f), cloth()), fill(legTube(0.86f, 0.9f, 8f), cloth(-0.12f))))
        },
        item("knee", "Knee socks", 0xFFF0508C) {
            add(Layer.SOCKS, both(fill(sock(0.47f), cloth()), fill(legTube(0.47f, 0.52f, 9f), cloth(-0.15f))))
        },
        item("stripy", "Stripy socks", 0xFF9C5BD3) {
            val s = sock(0.47f)
            add(Layer.SOCKS, both(fill(s, cloth()), stripes(s, WHITE, 735f, 900f, 26f, 11f), Shape(s, null, Ink.Of(Role.MAIN, -0.45f), 3f)))
        },
        item("dotty", "Spotty socks", 0xFF42A5F5) {
            val s = sock(0.65f)
            add(Layer.SOCKS, both(fill(s, cloth()), dots(s, WHITE, 18f, 4f), Shape(s, null, Ink.Of(Role.MAIN, -0.45f), 3f)))
        },
        item("frilly", "Frilly socks", 0xFFFFF0F6) {
            val frill = PathBuilder()
            for (i in 0 until 4) frill.circle(250 + i * 9, 852, 7)
            add(Layer.SOCKS, both(fill(sock(0.87f), cloth()), *blob(frill.build(), cloth(-0.18f), 2f).toTypedArray()))
        },
        item("tights", "Tights", 0xFFB39DDB) {
            add(Layer.SOCKS, both(fill(Body.legLeft.segment(0f, 0.985f, 3f), cloth()), fill(sockFoot(), cloth())))
            add(Layer.SOCKS, fill(Garments.hips, cloth(), stroke = null))
        },
    )

    private fun shoeBody() = path { ellipse(foot.x, foot.y + 3, 32, 17) }

    val shoes: List<Item> = listOf(
        Item.none("Bare feet"),
        item("flats", "Ballet flats", 0xFFF0508C) {
            add(Layer.SHOES, both(
                fill(shoeBody(), cloth()),
                line(path { moveTo(234, 906); quadTo(256, 896, 278, 906) }, cloth(-0.35f), 2.5f),
                fill(path { ellipse(236, 909, 6, 5) }, cloth(0.45f), width = 2f),
                fill(path { ellipse(246, 909, 6, 5) }, cloth(0.45f), width = 2f),
            ))
        },
        item("sneakers", "Sneakers", 0xFF42A5F5) {
            add(Layer.SHOES, both(
                fill(path { smoothClosed(pts(222, 922, 226, 900, 248, 887, 272, 890, 286, 906, 288, 922)) }, cloth()),
                fill(path { roundRect(218, 918, 290, 934, 8) }, WHITE),
                line(path { moveTo(248, 896); lineTo(266, 894) }, WHITE, 3f),
                line(path { moveTo(246, 905); lineTo(268, 903) }, WHITE, 3f),
                fill(path { star(275, 910, 6, 2.5f) }, WHITE, width = 1f),
            ))
        },
        item("boots", "Boots", 0xFF8D6E63) {
            add(Layer.SHOES, both(
                fill(legTube(0.72f, 1f, 13f, roundEnd = true), cloth()),
                fill(shoeBody(), cloth()),
                fill(legTube(0.72f, 0.76f, 19f), cloth(0.25f)),
                line(path { moveTo(224, 924); quadTo(254, 934, 284, 924) }, cloth(-0.35f), 3f),
            ))
        },
        item("sandals", "Sandals", 0xFFFFC531) {
            add(Layer.SHOES, both(
                fill(path { ellipse(foot.x, foot.y + 11, 32, 8) }, cloth(-0.1f)),
                fill(path { roundRect(232, 902, 276, 911, 4) }, cloth()),
                fill(legTube(0.955f, 0.99f, 7f), cloth()),
            ))
            add(Layer.SHOES, both(*flower(254f, 906f, WHITE, 4f).toTypedArray()))
        },
        item("maryjanes", "Mary Janes", 0xFFE53935) {
            add(Layer.SHOES, both(
                fill(shoeBody(), cloth()),
                fill(path { roundRect(238, 897, 272, 905, 4) }, cloth(-0.15f)),
                fill(path { circle(266, 901, 3.5f) }, GOLD, width = 1.5f),
                flat(path { ellipse(240, 918, 7, 3.5f) }, WHITE),
            ))
        },
        item("slippers", "Sparkle slippers", 0xFF81D4FA) {
            add(Layer.SHOES, both(
                fill(shoeBody(), cloth(0.35f, 0.9f)),
                sparkle(240, 912, 6), sparkle(262, 920, 5), sparkle(270, 905, 4),
                fill(path { heart(254, 900, 12) }, PINK, width = 1.5f),
            ))
        },
        item("rainboots", "Rain boots", 0xFFFFD54F) {
            add(Layer.SHOES, both(
                fill(legTube(0.66f, 1f, 14f, roundEnd = true), cloth()),
                fill(shoeBody(), cloth()),
                flat(path { roundRect(252, 790, 258, 870, 3) }, solid(0x99FFFFFF)),
                fill(path { roundRect(220, 922, 288, 934, 6) }, cloth(-0.25f)),
            ))
        },
    )
}
