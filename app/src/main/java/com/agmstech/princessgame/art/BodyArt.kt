package com.agmstech.princessgame.art

import com.agmstech.princessgame.wardrobe.Layer

/**
 * The princess herself, in a 600 x 1000 art box with her centre line at x = 300.
 *
 * The face is deliberately simple: eyes, eyebrows, a nose and a brown smile line.
 * There is no makeup of any kind (no lipstick, blush or eye shadow).
 */
object Body {
    const val WIDTH = 600f
    const val HEIGHT = 1000f

    val armLeft = Limb(pts(242, 362, 214, 462, 198, 546), listOf(36f, 31f, 27f))
    val armRight = armLeft.mirrorX()
    val legLeft = Limb(pts(271, 555, 267, 735, 263, 893), listOf(54f, 42f, 31f))
    val legRight = legLeft.mirrorX()

    val handLeft = pt(194, 566)
    val handRight = pt(406, 566)
    const val HAND_RADIUS = 20f

    val footLeft = pt(254, 912)
    val footRight = pt(346, 912)
    const val FOOT_RX = 28f
    const val FOOT_RY = 14f

    val headPath = path {
        smoothClosed(pts(300, 115, 365, 130, 397, 188, 393, 252, 362, 300, 300, 318, 238, 300, 207, 252, 203, 188, 235, 130))
    }

    /** The light vest every outfit is layered over, so the princess is never uncovered. */
    val torsoPath = path {
        moveTo(275, 338)
        quadTo(300, 352, 325, 338)
        lineTo(360, 346)
        quadTo(378, 352, 378, 372)
        lineTo(362, 410)
        quadTo(352, 450, 352, 480)
        quadTo(352, 520, 360, 560)
        lineTo(362, 592)
        lineTo(238, 592)
        lineTo(240, 560)
        quadTo(248, 520, 248, 480)
        quadTo(248, 450, 238, 410)
        lineTo(222, 372)
        quadTo(222, 352, 240, 346)
        close()
    }

    private val eyeInk = solid(0xFF3B2416)

    private val eyesOpen by lazy { build(blink = false) }
    private val eyesClosed by lazy { build(blink = true) }

    /** The body's layers; cached so renderers can reuse converted paths. */
    fun parts(blink: Boolean): Map<Layer, List<Element>> = if (blink) eyesClosed else eyesOpen

    private fun build(blink: Boolean): Map<Layer, List<Element>> = mapOf(
        Layer.LEGS to listOf(
            fill(legLeft.segment(), skin()),
            fill(legRight.segment(), skin()),
            fill(path { ellipse(footLeft.x, footLeft.y, FOOT_RX, FOOT_RY) }, skin()),
            fill(path { ellipse(footRight.x, footRight.y, FOOT_RX, FOOT_RY) }, skin()),
        ),
        Layer.TORSO to listOf(
            fill(path { rect(281, 288, 319, 352) }, skin()),
            fill(torsoPath, solid(0xFFFFF4F8)),
        ),
        Layer.ARMS to listOf(
            fill(armLeft.segment(), skin()),
            fill(armRight.segment(), skin()),
        ),
        Layer.HANDS to listOf(
            fill(path { circle(handLeft.x, handLeft.y, HAND_RADIUS) }, skin()),
            fill(path { circle(handRight.x, handRight.y, HAND_RADIUS) }, skin()),
        ),
        Layer.HEAD to listOf(fill(headPath, skin())),
        Layer.FACE to face(blink),
    )

    private fun face(blink: Boolean): List<Element> {
        val out = ArrayList<Element>()
        // Eyebrows
        out += pair(line(path { moveTo(245, 204); quadTo(262, 195, 279, 203) }, hair(-0.35f), 4.5f))
        if (blink) {
            out += pair(line(path { moveTo(248, 234); quadTo(262, 246, 276, 234) }, eyeInk, 4.5f))
        } else {
            for (cx in listOf(262f, 338f)) {
                out += flat(path { ellipse(cx, 234, 14, 18) }, eyeInk)
                out += flat(path { circle(cx - 5, 226, 5.5f) }, WHITE)
                out += flat(path { circle(cx + 5, 241, 2.6f) }, WHITE)
            }
        }
        // Nose and a friendly smile (a plain brown line, never lipstick).
        out += line(path { moveTo(295, 259); quadTo(300, 264, 305, 259) }, skin(-0.4f), 3f)
        out += line(path { moveTo(283, 279); quadTo(300, 295, 317, 279) }, solid(0xFF6E3B2E), 4.5f)
        return out
    }
}
