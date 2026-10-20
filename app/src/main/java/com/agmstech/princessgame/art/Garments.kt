package com.agmstech.princessgame.art

/** Reusable clothing outlines shared by dresses, tops, bottoms and sports kits. */
internal object Garments {

    /** Dress bodice: neckline to waist. */
    val bodice = path {
        moveTo(272, 336); quadTo(300, 356, 328, 336)
        lineTo(362, 344); quadTo(382, 350, 382, 372)
        lineTo(366, 410); quadTo(355, 445, 356, 488)
        lineTo(244, 488); quadTo(245, 445, 234, 410)
        lineTo(218, 372); quadTo(218, 350, 238, 344)
        close()
    }

    /** Pinafore-style bodice with straps, worn over the light vest. */
    val strappyBodice = path {
        moveTo(240, 344); lineTo(258, 344); lineTo(262, 378)
        quadTo(300, 392, 338, 378); lineTo(342, 344); lineTo(360, 344)
        lineTo(362, 410); quadTo(355, 445, 356, 488)
        lineTo(244, 488); quadTo(245, 445, 238, 410)
        close()
    }

    /** Shirt body from neckline to just below the waistband of the bottoms. */
    val shirt = path {
        moveTo(272, 336); quadTo(300, 356, 328, 336)
        lineTo(362, 344); quadTo(382, 350, 382, 372)
        lineTo(366, 410); quadTo(356, 450, 358, 500)
        lineTo(364, 598); quadTo(300, 606, 236, 598)
        lineTo(242, 500); quadTo(244, 450, 234, 410)
        lineTo(218, 372); quadTo(218, 350, 238, 344)
        close()
    }

    /** Sleeveless shirt body (tank tops and jerseys). */
    val tank = path {
        moveTo(270, 336); quadTo(300, 362, 330, 336)
        lineTo(350, 340); quadTo(352, 384, 368, 404)
        quadTo(356, 450, 358, 500)
        lineTo(364, 598); quadTo(300, 606, 236, 598)
        lineTo(242, 500); quadTo(244, 450, 232, 404)
        quadTo(248, 384, 250, 340)
        close()
    }

    /** Waist-to-hip block that tops the legs of trousers and shorts. */
    val hips = path {
        moveTo(244, 538); lineTo(356, 538)
        quadTo(368, 580, 366, 616); lineTo(234, 616)
        quadTo(232, 580, 244, 538)
        close()
    }

    fun sleeveShort(extra: Float = 12f, to: Float = 0.32f) =
        Body.armLeft.segment(0f, to, extra, roundStart = true, roundEnd = false)

    fun sleeveLong(extra: Float = 8f) = Body.armLeft.segment(0f, 0.93f, extra, roundStart = true, roundEnd = false)

    fun cuff(extra: Float = 13f) = Body.armLeft.segment(0.86f, 0.95f, extra, roundStart = false, roundEnd = false)

    fun puffSleeve() = path { ellipse(233, 372, 27, 25) }

    /** Left leg tube between fractions [t0] and [t1] (0 = hip, 1 = ankle). */
    fun legTube(t0: Float, t1: Float, extra: Float, roundEnd: Boolean = false) =
        Body.legLeft.segment(t0, t1, extra, roundStart = false, roundEnd = roundEnd)

    /**
     * A skirt hanging from [top]. [bell] pushes the sides outward near the top
     * (0 = straight A-line, 0.7 = very full), [scallops] shapes the hem and
     * [curve] makes the middle of the hem hang lower than its sides.
     */
    fun skirt(
        top: Float, waistHalf: Float, hemY: Float, hemHalf: Float,
        bell: Float = 0.3f, scallops: Int = 1, depth: Float = 0f, curve: Float = 10f, pointed: Boolean = false,
    ): VPath = path {
        val h = hemY - top
        fun hemAt(x: Float): Float {
            val u = (x - 300f) / hemHalf
            return hemY + curve * (1f - u * u)
        }
        moveTo(300 - waistHalf, top)
        cubicTo(300 - waistHalf - (hemHalf - waistHalf) * bell, top + h * 0.35f, 300 - hemHalf, hemY - h * 0.35f, 300 - hemHalf, hemY)
        val w = 2 * hemHalf / scallops
        for (i in 0 until scallops) {
            val x0 = 300 - hemHalf + i * w
            val xm = x0 + w / 2
            val x1 = x0 + w
            if (pointed) {
                lineTo(xm, hemAt(xm) + depth)
                lineTo(x1, hemAt(x1))
            } else {
                quadTo(xm, hemAt(xm) + depth * 2, x1, hemAt(x1))
            }
        }
        cubicTo(300 + hemHalf, hemY - h * 0.35f, 300 + waistHalf + (hemHalf - waistHalf) * bell, top + h * 0.35f, 300 + waistHalf, top)
        close()
    }

    /** Soft fold lines from the waist to the hem of a skirt. */
    fun folds(top: Float, waistHalf: Float, hemY: Float, hemHalf: Float, count: Int, ink: Ink, inset: Float = 30f): List<Element> =
        (1 until count).map { k ->
            val xt = 300 - waistHalf + k * 2 * waistHalf / count
            val xb = 300 - hemHalf + k * 2 * hemHalf / count
            line(path { moveTo(xt, top + inset); quadTo((xt + xb) / 2 + (xb - xt) * 0.15f, (top + hemY) / 2, xb, hemY - 4) }, ink, 2.5f)
        }

    /** A round peter-pan collar. */
    fun collar(ink: Ink = WHITE): List<Element> = pair(fill(path {
        moveTo(300, 348); cubicTo(290, 372, 262, 374, 254, 352); quadTo(266, 342, 274, 338); quadTo(286, 350, 300, 348); close()
    }, ink, width = 2.5f))
}
