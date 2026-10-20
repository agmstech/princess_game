package com.agmstech.princessgame.art

import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/** Bags (right hand / hip) and toys (left hand). */
object HeldArt {

    private val strap = Ink.Of(Role.MAIN, -0.3f)

    val bags: List<Item> = listOf(
        Item.none(),
        item("handbag", "Handbag", 0xFF9C5BD3) {
            add(Layer.HELD,
                line(path { moveTo(384, 612); cubicTo(384, 546, 428, 546, 428, 612) }, strap, 6f),
                fill(path { moveTo(380, 604); lineTo(432, 604); lineTo(446, 672); quadTo(406, 682, 366, 672); close() }, cloth()),
                fill(path { moveTo(380, 604); lineTo(432, 604); lineTo(429, 630); quadTo(406, 644, 383, 630); close() }, cloth(-0.12f)),
                fill(path { circle(406, 636, 5.5f) }, GOLD, width = 2f),
            )
        },
        item("purse", "Purse", 0xFFFF8FB8) {
            add(Layer.HELD,
                line(path { moveTo(388, 646); lineTo(406, 572); lineTo(424, 646) }, GOLD, 3.5f),
                fill(path { roundRect(376, 640, 436, 690, 16) }, cloth()),
                line(path { moveTo(384, 646); quadTo(406, 634, 428, 646) }, GOLD, 4f),
                fill(path { circle(400, 638, 5) }, GOLD, width = 1.5f),
                fill(path { circle(412, 638, 5) }, GOLD, width = 1.5f),
                sparkle(420, 668, 6),
            )
        },
        item("wallet", "Wallet", 0xFFE53935) {
            add(Layer.HELD,
                fill(path { roundRect(392, 548, 456, 592, 7) }, cloth()),
                line(path { moveTo(394, 564); lineTo(454, 564) }, cloth(-0.3f), 3f),
                fill(path { circle(444, 572, 4.5f) }, GOLD, width = 1.5f),
                fill(path { heart(428, 574, 12) }, WHITE, width = 1.5f),
            )
        },
        item("heartbag", "Heart bag", 0xFFF0508C) {
            add(Layer.HELD,
                line(path { moveTo(388, 606); cubicTo(388, 548, 424, 548, 424, 606) }, strap, 5f),
                fill(path { heart(406, 604, 76) }, cloth()),
                flat(path { ellipse(386, 612, 7, 10) }, solid(0x80FFFFFF)),
                sparkle(424, 640, 6),
            )
        },
        item("tote", "Tote bag", 0xFF26A69A) {
            add(Layer.HELD,
                line(path { moveTo(378, 604); lineTo(400, 566) }, strap, 6f),
                line(path { moveTo(434, 604); lineTo(412, 566) }, strap, 6f),
                fill(path { roundRect(364, 600, 448, 690, 8) }, cloth()),
            )
            add(Layer.HELD, flower(406f, 645f, WHITE, 11f))
        },
        item("crossbody", "Crossbody bag", 0xFFFFA726) {
            add(Layer.HELD,
                line(path { moveTo(246, 350); lineTo(352, 572) }, strap, 6f),
                fill(path { roundRect(330, 566, 390, 612, 16) }, cloth()),
                fill(path { moveTo(330, 582); quadTo(330, 566, 346, 566); lineTo(374, 566); quadTo(390, 566, 390, 582); quadTo(360, 600, 330, 582); close() }, cloth(-0.15f)),
                fill(path { circle(360, 590, 4.5f) }, GOLD, width = 1.5f),
            )
        },
    )

    val toys: List<Item> = listOf(
        Item.none(),
        item("wand", "Magic wand", 0xFFFFC531) {
            add(Layer.HELD,
                line(path { moveTo(194, 574); lineTo(156, 456) }, solid(0xFF8E6BB8), 8f),
                line(path { moveTo(192, 568); lineTo(158, 462) }, solid(0xFFE1BEE7), 3f),
                fill(path { star(152, 440, 30, 14) }, cloth()),
                fill(path { star(152, 441, 12, 6) }, cloth(0.5f), stroke = null),
                sparkle(118, 412, 9, GOLD), sparkle(186, 404, 7, GOLD), sparkle(124, 474, 6, GOLD),
            )
        },
        item("flowers", "Flowers", 0xFFFF8FB8) {
            add(Layer.HELD,
                line(path { moveTo(194, 572); lineTo(174, 486) }, LEAF, 4f),
                line(path { moveTo(194, 572); lineTo(194, 478) }, LEAF, 4f),
                line(path { moveTo(194, 572); lineTo(214, 488) }, LEAF, 4f),
            )
            add(Layer.HELD, flower(172f, 478f, cloth(), 10f))
            add(Layer.HELD, flower(216f, 482f, cloth(0.45f), 10f))
            add(Layer.HELD, flower(194f, 462f, solid(0xFFFFF59D), 10f))
            add(Layer.HELD, fill(path { poly(168, 516, 220, 516, 198, 596, 190, 596) }, solid(0xFFFFF3F8)))
            add(Layer.HELD, line(path { moveTo(180, 540); lineTo(208, 540) }, PINK, 4f))
        },
        item("balloon", "Balloon", 0xFFE53935) {
            add(Layer.HELD,
                line(path { moveTo(194, 560); cubicTo(168, 500, 200, 420, 152, 360) }, solid(0xFF9E9E9E), 2f),
                fill(path { ellipse(146, 300, 46, 56) }, cloth()),
                fill(path { poly(140, 355, 152, 355, 146, 364) }, cloth(), width = 2f),
                flat(path { ellipse(128, 280, 9, 15) }, solid(0x99FFFFFF)),
            )
        },
        item("racket", "Tennis racket", 0xFF42A5F5) {
            val head = path { ellipse(150, 440, 37, 49) }.rotate(-18f, 150f, 440f)
            val inner = path { ellipse(150, 440, 31, 43) }.rotate(-18f, 150f, 440f)
            val strings = ArrayList<Element>()
            for (i in -4..4) {
                strings += line(path { moveTo(150 + i * 8, 380); lineTo(150 + i * 8, 500) }.rotate(-18f, 150f, 440f), solid(0xFFECEFF1), 1.8f)
                strings += line(path { moveTo(100, 440 + i * 10); lineTo(200, 440 + i * 10) }.rotate(-18f, 150f, 440f), solid(0xFFECEFF1), 1.8f)
            }
            add(Layer.HELD,
                line(path { moveTo(196, 584); lineTo(168, 496) }, solid(0xFF5D4037), 11f),
                Group(inner, strings),
                Shape(head, null, cloth(), 8f),
            )
        },
        item("volleyball", "Volleyball", 0xFFFFD54F) {
            val ball = path { circle(170, 600, 34) }
            add(Layer.HELD,
                fill(ball, WHITE),
                Group(ball, listOf(
                    line(path { moveTo(140, 566); quadTo(176, 600, 166, 640) }, cloth(), 12f),
                    line(path { moveTo(206, 586); quadTo(170, 596, 146, 626) }, solid(0xFF42A5F5), 12f),
                    line(path { moveTo(150, 572); quadTo(186, 604, 176, 640) }, solid(0xFF90A4AE), 2f),
                )),
                Shape(ball, null, solid(0xFF90A4AE), 3f),
            )
        },
        item("soccerball", "Soccer ball", 0xFF3A3340) {
            val ball = path { circle(170, 600, 34) }
            add(Layer.HELD,
                fill(ball, WHITE),
                Group(ball, listOf(
                    fill(path { star(170, 600, 13, 10.5f) }, cloth(), stroke = null),
                    fill(path { circle(136, 590, 11) }, cloth(), stroke = null),
                    fill(path { circle(160, 636, 11) }, cloth(), stroke = null),
                    fill(path { circle(204, 610, 11) }, cloth(), stroke = null),
                    fill(path { circle(186, 566, 11) }, cloth(), stroke = null),
                    fill(path { circle(146, 562, 11) }, cloth(), stroke = null),
                )),
                Shape(ball, null, solid(0xFF607D8B), 3f),
            )
        },
        item("basketball", "Basketball", 0xFFFF8A3D) {
            val ball = path { circle(170, 600, 34) }
            val seam = solid(0xFF4E342E)
            add(Layer.HELD,
                fill(ball, cloth()),
                Group(ball, listOf(
                    line(path { moveTo(170, 560); lineTo(170, 640) }, seam, 2.5f),
                    line(path { moveTo(130, 600); lineTo(210, 600) }, seam, 2.5f),
                    line(path { moveTo(146, 570); quadTo(162, 600, 146, 630) }, seam, 2.5f),
                    line(path { moveTo(194, 570); quadTo(178, 600, 194, 630) }, seam, 2.5f),
                )),
            )
        },
        item("teddy", "Teddy bear", 0xFFA1887F) {
            add(Layer.HELD,
                fill(path { ellipse(166, 616, 28, 32) }, cloth()),
                fill(path { ellipse(166, 622, 15, 17) }, cloth(0.4f), width = 2f),
                fill(path { circle(146, 544, 9) }, cloth()),
                fill(path { circle(186, 544, 9) }, cloth()),
                fill(path { circle(166, 566, 25) }, cloth()),
                fill(path { ellipse(166, 576, 10, 8) }, cloth(0.4f), width = 2f),
                flat(path { circle(157, 560, 3.5f) }, solid(0xFF3B2416)),
                flat(path { circle(175, 560, 3.5f) }, solid(0xFF3B2416)),
                flat(path { ellipse(166, 573, 4, 3) }, solid(0xFF3B2416)),
                fill(path { heart(166, 608, 14) }, PINK, width = 1.5f),
            )
        },
    )
}
