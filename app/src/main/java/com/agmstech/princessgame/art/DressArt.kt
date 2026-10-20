package com.agmstech.princessgame.art

import com.agmstech.princessgame.art.Garments.bodice
import com.agmstech.princessgame.art.Garments.folds
import com.agmstech.princessgame.art.Garments.skirt
import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

object DressArt {

    private fun puffSleeves(ink: Ink) = pair(fill(Garments.puffSleeve(), ink)) +
        pair(line(path { moveTo(214, 384); quadTo(232, 394, 252, 386) }, Ink.Of(Role.MAIN, -0.25f), 2.5f))

    private fun shortSleeves(ink: Ink) = pair(fill(Garments.sleeveShort(), ink))

    private fun waistBow(ink: Ink): List<Element> {
        val loop = fill(path { smoothClosed(pts(300, 482, 282, 466, 266, 468, 266, 496, 282, 498)) }, ink, width = 2.5f)
        return listOf(loop, loop.mirrored(), fill(path { ellipse(300, 482, 9, 10) }, ink, width = 2.5f))
    }

    private fun sash(ink: Ink) = fill(path { moveTo(245, 470); quadTo(300, 478, 355, 470); lineTo(356, 492); quadTo(300, 500, 244, 492); close() }, ink)

    val items: List<Item> = listOf(
        item("gown", "Ball gown", 0xFFFF8FB8) {
            val big = skirt(470f, 50f, 868f, 188f, bell = 0.55f, scallops = 7, depth = 10f, curve = 16f)
            val tier = skirt(470f, 50f, 650f, 118f, bell = 0.6f, scallops = 5, depth = 9f, curve = 8f)
            add(Layer.TOP, fill(big, cloth()))
            add(Layer.TOP, folds(470f, 50f, 868f, 188f, 7, cloth(-0.18f)))
            add(Layer.TOP, sparkle(190, 800, 9), sparkle(410, 780, 8), sparkle(300, 830, 10), sparkle(240, 720, 7), sparkle(360, 700, 7))
            add(Layer.TOP, fill(tier, cloth(0.3f)))
            add(Layer.TOP, fill(bodice, cloth()))
            add(Layer.TOP, puffSleeves(cloth(0.3f)))
            add(Layer.TOP, sash(cloth(-0.15f)))
            add(Layer.TOP, waistBow(cloth(0.35f)))
            add(Layer.TOP, fill(path { heart(300, 392, 26) }, cloth(0.45f), width = 2f))
        },
        item("party", "Party dress", 0xFFB39DDB) {
            val s = skirt(470f, 50f, 700f, 128f, bell = 0.75f, scallops = 6, depth = 9f, curve = 10f)
            val ruffle = skirt(470f, 50f, 620f, 112f, bell = 0.8f, scallops = 6, depth = 8f, curve = 8f)
            add(Layer.TOP, fill(s, cloth()))
            add(Layer.TOP, fill(ruffle, cloth(0.35f)))
            add(Layer.TOP, fill(bodice, cloth()))
            add(Layer.TOP, puffSleeves(cloth(0.35f)))
            add(Layer.TOP, sash(cloth(-0.2f)))
            add(Layer.TOP, sparkle(250, 560, 7), sparkle(350, 590, 7), sparkle(300, 660, 8))
        },
        item("twirl", "Twirl dress", 0xFF42A5F5) {
            val s = skirt(476f, 52f, 690f, 122f, bell = 0.2f, scallops = 1, curve = 14f)
            add(Layer.TOP, fill(s, cloth()))
            add(Layer.TOP, dots(s, cloth(0.6f), 34f, 7f))
            add(Layer.TOP, Shape(s, null, Ink.Of(Role.MAIN, -0.4f), 3f))
            add(Layer.TOP, shortSleeves(cloth()))
            add(Layer.TOP, fill(bodice, cloth()))
            add(Layer.TOP, Garments.collar())
            add(Layer.TOP, fill(path { circle(300, 410, 4) }, WHITE, width = 1.5f), fill(path { circle(300, 440, 4) }, WHITE, width = 1.5f))
        },
        item("fairy", "Fairy dress", 0xFF80CBC4) {
            val wingUp = fill(path { ellipse(176, 376, 90, 50) }.rotate(-30f, 176f, 376f), cloth(0.3f, 0.85f))
            val wingLow = fill(path { ellipse(200, 486, 60, 34) }.rotate(26f, 200f, 486f), cloth(0.3f, 0.85f))
            add(Layer.BACK, pair(wingUp) + pair(wingLow))
            add(Layer.BACK, sparkle(130, 350, 9), sparkle(470, 350, 9), sparkle(160, 500, 7), sparkle(440, 500, 7))
            add(Layer.TOP, fill(skirt(474f, 50f, 760f, 140f, bell = 0.4f, scallops = 7, depth = 26f, curve = 8f, pointed = true), cloth(0.35f)))
            add(Layer.TOP, fill(skirt(474f, 50f, 690f, 118f, bell = 0.4f, scallops = 6, depth = 24f, curve = 8f, pointed = true), cloth()))
            add(Layer.TOP, fill(bodice, cloth()))
            add(Layer.TOP, pair(fill(path { smoothClosed(pts(216, 356, 240, 344, 254, 360, 250, 392, 230, 400, 212, 386)) }, cloth(0.35f))))
            add(Layer.TOP, sash(cloth(-0.2f)))
            add(Layer.TOP, sparkle(270, 600, 7), sparkle(330, 640, 7), sparkle(300, 420, 6))
        },
        item("rainbow", "Rainbow dress", 0xFFFFFFFF) {
            val s = skirt(474f, 50f, 770f, 150f, bell = 0.5f, scallops = 6, depth = 8f, curve = 10f)
            val bands = listOf(0xFFFF6B6B, 0xFFFFA94D, 0xFFFFE066, 0xFF8CE99A, 0xFF74C0FC, 0xFFB197FC)
            val step = (800f - 474f) / bands.size
            add(Layer.TOP, Group(s, bands.mapIndexed { i, c -> flat(path { rect(140, 474 + i * step, 460, 474 + (i + 1) * step + 1) }, solid(c)) }))
            add(Layer.TOP, Shape(s, null, solid(0xFF8E6BB8), 3f))
            add(Layer.TOP, fill(bodice, cloth()))
            add(Layer.TOP, puffSleeves(cloth()))
            add(Layer.TOP, sash(solid(0xFFB197FC)))
            add(Layer.TOP, fill(path { star(300, 410, 18, 8) }, solid(0xFFFFE066), width = 2f))
        },
        item("starry", "Starry gown", 0xFF3F51B5) {
            val s = skirt(474f, 50f, 862f, 162f, bell = 0.35f, scallops = 1, curve = 18f)
            val stars = ArrayList<Element>()
            val spots = listOf(220 to 560, 330 to 540, 280 to 620, 380 to 640, 200 to 700, 300 to 720, 400 to 760, 250 to 800, 350 to 830, 170 to 830, 430 to 850)
            spots.forEachIndexed { i, (x, y) -> stars += fill(path { star(x, y, if (i % 3 == 0) 14 else 9) }, GOLD, width = 1.5f) }
            add(Layer.TOP, fill(s, cloth()))
            add(Layer.TOP, Group(s, stars))
            add(Layer.TOP, shortSleeves(cloth(0.15f)))
            add(Layer.TOP, fill(bodice, cloth()))
            add(Layer.TOP, fill(path { star(300, 400, 20, 9) }, GOLD, width = 2f))
            add(Layer.TOP, sash(GOLD))
        },
        item("sundress", "Sundress", 0xFFFFD54F) {
            val s = skirt(476f, 52f, 700f, 118f, bell = 0.3f, scallops = 1, curve = 12f)
            val print = ArrayList<Element>()
            for ((x, y) in listOf(230 to 540, 330 to 520, 280 to 590, 370 to 600, 210 to 650, 310 to 660, 400 to 670, 260 to 690)) {
                print += flower(x.toFloat(), y.toFloat(), WHITE, 6f)
            }
            add(Layer.TOP, fill(s, cloth()))
            add(Layer.TOP, Group(s, print))
            add(Layer.TOP, fill(Garments.strappyBodice, cloth()))
            add(Layer.TOP, flower(300f, 420f, WHITE, 7f))
            add(Layer.TOP, line(path { moveTo(244, 486); quadTo(300, 494, 356, 486) }, cloth(-0.3f), 3f))
        },
    )
}
