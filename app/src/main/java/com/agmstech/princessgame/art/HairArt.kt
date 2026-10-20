package com.agmstech.princessgame.art

import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.Layer

/** Hair styles. Every style has a back part (behind the body) and a fringe in front of the face. */
object HairArt {

    private val tieInk = PINK

    /** Hair pulled back close to the head (for ponytails, buns and braids). */
    private val cap = path {
        smoothClosed(pts(300, 100, 370, 118, 405, 178, 408, 240, 395, 268, 300, 284, 205, 268, 192, 240, 195, 178, 230, 118))
    }

    /** Builds a fringe: an arc over the top of the head closed by the given lower [edge] (left to right). */
    private fun bangs(edge: List<Pt>): VPath = path {
        moveTo(edge.first())
        smoothTo(pts(193, 200, 214, 132, 300, 97, 386, 132, 407, 200) + edge.last())
        smoothTo(edge.reversed().drop(1))
        close()
    }

    private val shine = flat(path {
        smoothClosed(pts(236, 130, 268, 111, 302, 105, 300, 113, 270, 120, 242, 138))
    }, hair(0.3f))

    private val straightFringe = listOf(
        fill(bangs(pts(203, 252, 213, 212, 228, 182, 248, 191, 263, 172, 282, 191, 300, 174, 318, 191, 337, 172, 352, 191, 372, 182, 387, 212, 397, 252)), hair()),
        shine,
    )

    private val sweptFringe = listOf(
        fill(bangs(pts(203, 252, 211, 212, 228, 194, 262, 188, 300, 178, 336, 162, 364, 152, 385, 172, 395, 212, 397, 252)), hair()),
        shine,
    )

    private val partedFringe = listOf(
        fill(bangs(pts(203, 252, 214, 206, 238, 165, 270, 136, 300, 124, 330, 136, 362, 165, 386, 206, 397, 252)), hair()),
        shine,
    )

    private val curlyFringe: List<Element> = run {
        val p = PathBuilder()
        p.add(bangs(pts(203, 240, 210, 200, 228, 178, 260, 166, 300, 161, 340, 166, 372, 178, 390, 200, 397, 240)))
        for ((x, y) in listOf(224 to 192, 250 to 174, 280 to 166, 310 to 166, 340 to 172, 366 to 186, 386 to 208, 207 to 224, 393 to 224)) {
            p.circle(x, y, 18)
        }
        blob(p.build(), hair())
    }

    private fun tie(x: Number, y: Number, r: Number = 12) = fill(path { circle(x, y, r) }, tieInk)

    val items: List<Item> = listOf(
        item("long", "Long", 0xFF7B4B2A) {
            add(Layer.HAIR_BACK, fill(path {
                smoothClosed(pts(300, 95, 372, 108, 412, 160, 420, 240, 418, 330, 424, 440, 430, 556, 368, 572, 300, 576, 232, 572, 170, 556, 176, 440, 182, 330, 180, 240, 188, 160, 228, 108))
            }, hair()))
            add(Layer.HAIR_FRONT, straightFringe)
        },
        item("wavy", "Wavy", 0xFFE8B04F) {
            add(Layer.HAIR_BACK, fill(path {
                smoothClosed(pts(
                    300, 95, 372, 108, 414, 165, 424, 240, 416, 300, 434, 360, 420, 420, 442, 480, 428, 540, 446, 600,
                    400, 612, 370, 596, 340, 614, 300, 600, 260, 614, 230, 596, 200, 612, 154, 600, 172, 540, 158, 480,
                    180, 420, 166, 360, 184, 300, 176, 240, 186, 165, 228, 108,
                ))
            }, hair()))
            add(Layer.HAIR_FRONT, sweptFringe)
        },
        item("ponytail", "Ponytail", 0xFF4E342E) {
            add(Layer.HAIR_BACK, fill(path {
                smoothClosed(pts(384, 128, 420, 142, 448, 200, 458, 280, 450, 360, 464, 430, 432, 470, 420, 420, 414, 350, 404, 280, 388, 210, 366, 160))
            }, hair()))
            add(Layer.HAIR_BACK, fill(cap, hair()))
            add(Layer.HAIR_FRONT, straightFringe)
            add(Layer.HAIR_FRONT, tie(396, 138, 13))
        },
        item("pigtails", "Pigtails", 0xFFD2691E) {
            val tail = fill(path {
                smoothClosed(pts(212, 178, 186, 192, 160, 242, 146, 302, 150, 372, 164, 424, 180, 382, 188, 322, 198, 262, 216, 214))
            }, hair())
            add(Layer.HAIR_BACK, pair(tail))
            add(Layer.HAIR_BACK, fill(cap, hair()))
            add(Layer.HAIR_FRONT, straightFringe)
            add(Layer.HAIR_FRONT, tie(203, 196), tie(397, 196))
        },
        item("bun", "Bun", 0xFF2B2024) {
            add(Layer.HAIR_BACK, fill(path { circle(300, 88, 44) }, hair()))
            add(Layer.HAIR_BACK, line(path { moveTo(278, 76); quadTo(300, 58, 320, 80); quadTo(312, 100, 294, 94) }, hair(-0.3f), 3f))
            add(Layer.HAIR_BACK, fill(cap, hair()))
            add(Layer.HAIR_FRONT, partedFringe)
        },
        item("spacebuns", "Space buns", 0xFFF48FB1) {
            add(Layer.HAIR_BACK, fill(path { circle(232, 112, 38) }, hair()), fill(path { circle(368, 112, 38) }, hair()))
            add(Layer.HAIR_BACK, fill(cap, hair()))
            add(Layer.HAIR_FRONT, straightFringe)
        },
        item("bob", "Bob", 0xFFA0522D) {
            add(Layer.HAIR_BACK, fill(path {
                smoothClosed(pts(300, 95, 372, 108, 414, 160, 426, 230, 424, 290, 410, 330, 382, 338, 362, 318, 300, 330, 238, 318, 218, 338, 190, 330, 176, 290, 174, 230, 186, 160, 228, 108))
            }, hair()))
            add(Layer.HAIR_FRONT, straightFringe)
        },
        item("braid", "Braid", 0xFFF5DFA0) {
            add(Layer.HAIR_BACK, fill(cap, hair()))
            add(Layer.HAIR_FRONT, sweptFringe)
            val spine = listOf(392 to 266, 386 to 304, 378 to 342, 372 to 380, 369 to 418, 368 to 456)
            spine.forEachIndexed { i, (x, y) ->
                val dx = if (i % 2 == 0) -3 else 3
                add(Layer.HAIR_FRONT, fill(path { ellipse(x + dx, y, 22, 25) }, hair()))
                add(Layer.HAIR_FRONT, line(path { moveTo(x + dx - 12, y - 6); quadTo(x + dx, y + 6, x + dx + 12, y - 6) }, hair(-0.3f), 2.5f))
            }
            add(Layer.HAIR_FRONT, fill(path { smoothClosed(pts(358, 486, 378, 486, 388, 510, 368, 524, 350, 510)) }, hair()))
            add(Layer.HAIR_FRONT, tie(368, 482, 11))
        },
        item("curly", "Curly", 0xFF2B2024) {
            val p = PathBuilder()
            p.ellipse(300, 215, 120, 128)
            for (i in 0 until 14) {
                val a = Math.toRadians(i * 360.0 / 14)
                p.circle(300 + 118 * Math.cos(a), 215 + 124 * Math.sin(a), 46)
            }
            add(Layer.HAIR_BACK, blob(p.build(), hair()))
            add(Layer.HAIR_FRONT, curlyFringe)
        },
    )
}
