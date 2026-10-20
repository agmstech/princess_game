package com.agmstech.princessgame.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlin.math.min

/**
 * Puts the stage and the wardrobe side by side in landscape, or the stage on
 * top of the wardrobe in portrait, so the game works on phones and tablets.
 */
class GameLayout(context: Context, private val stage: View, private val panel: View) : ViewGroup(context) {

    init {
        addView(stage)
        addView(panel)
    }

    private fun isLandscape(w: Int, h: Int) = w > h * 1.1f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val exactly = MeasureSpec.EXACTLY
        if (isLandscape(w, h)) {
            val stageW = min((w * 0.55f).toInt(), (h * 1.1f).toInt())
            stage.measure(MeasureSpec.makeMeasureSpec(stageW, exactly), MeasureSpec.makeMeasureSpec(h, exactly))
            panel.measure(MeasureSpec.makeMeasureSpec(w - stageW, exactly), MeasureSpec.makeMeasureSpec(h, exactly))
        } else {
            val stageH = min((w * 1.05f).toInt(), (h * 0.53f).toInt())
            stage.measure(MeasureSpec.makeMeasureSpec(w, exactly), MeasureSpec.makeMeasureSpec(stageH, exactly))
            panel.measure(MeasureSpec.makeMeasureSpec(w, exactly), MeasureSpec.makeMeasureSpec(h - stageH, exactly))
        }
        setMeasuredDimension(w, h)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val w = r - l
        val h = b - t
        if (isLandscape(w, h)) {
            stage.layout(0, 0, stage.measuredWidth, h)
            panel.layout(stage.measuredWidth, 0, w, h)
        } else {
            stage.layout(0, 0, w, stage.measuredHeight)
            panel.layout(0, stage.measuredHeight, w, h)
        }
    }
}
