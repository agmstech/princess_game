package com.agmstech.princessgame.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView

/** Colours and small view helpers shared by the whole UI. */
object Palette {
    const val PINK = 0xFFE91E8C.toInt()
    const val PINK_LIGHT = 0xFFFFE4F1.toInt()
    const val PINK_BORDER = 0xFFF8BBD9.toInt()
    const val PURPLE = 0xFF9C5BD3.toInt()
    const val PLUM = 0xFF5B2A55.toInt()
    const val PANEL = 0xFFFFF4FA.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()
    const val RED = 0xFFE53935.toInt()
    const val GREEN = 0xFF43A047.toInt()
    const val ORANGE = 0xFFFB8C00.toInt()
}

fun Context.dp(v: Number): Float = v.toFloat() * resources.displayMetrics.density

fun Context.dpi(v: Number): Int = (dp(v) + 0.5f).toInt()

val roundedBold: Typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)

fun rounded(fill: Int, radius: Float, stroke: Int = 0, strokeWidth: Int = 0): GradientDrawable =
    GradientDrawable().apply {
        setColor(fill)
        cornerRadius = radius
        if (strokeWidth > 0) setStroke(strokeWidth, stroke)
    }

/** A big, friendly rounded button. */
fun Context.pillButton(text: String, fill: Int = Palette.PINK, textColor: Int = Palette.WHITE, sizeSp: Float = 18f, onClick: (View) -> Unit): TextView =
    TextView(this).apply {
        this.text = text
        setTextColor(textColor)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        typeface = roundedBold
        gravity = Gravity.CENTER
        minHeight = dpi(52)
        setPadding(dpi(20), dpi(10), dpi(20), dpi(10))
        background = rounded(fill, dp(26))
        isClickable = true
        isFocusable = true
        setOnClickListener(onClick)
    }

fun Context.label(text: String, sizeSp: Float = 16f, color: Int = Palette.PLUM, bold: Boolean = false): TextView =
    TextView(this).apply {
        this.text = text
        setTextColor(color)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        if (bold) typeface = roundedBold
    }
