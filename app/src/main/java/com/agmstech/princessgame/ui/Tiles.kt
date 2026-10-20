package com.agmstech.princessgame.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import kotlin.math.max
import kotlin.math.min

/** Draws a tile's picture into the given rectangle. */
typealias TileContent = (canvas: Canvas, area: RectF) -> Unit

/** A big rounded square button showing a picture and a short caption. */
class TileView(context: Context) : View(context) {
    var content: TileContent? = null
        set(value) { field = value; invalidate() }
    var caption: String = ""
        set(value) { field = value; contentDescription = value; invalidate() }
    var selectedTile: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var cardColor: Int = Palette.WHITE

    private val card = Paint(Paint.ANTI_ALIAS_FLAG)
    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.PLUM
        textAlign = Paint.Align.CENTER
        typeface = roundedBold
    }
    private val rect = RectF()
    private val art = RectF()

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        val pad = context.dp(4)
        val r = context.dp(18)
        rect.set(pad, pad, width - pad, height - pad)
        card.color = if (selectedTile) Palette.PINK_LIGHT else cardColor
        canvas.drawRoundRect(rect, r, r, card)
        border.color = if (selectedTile) Palette.PINK else Palette.PINK_BORDER
        border.strokeWidth = context.dp(if (selectedTile) 4 else 2)
        canvas.drawRoundRect(rect, r, r, border)

        val captionH = if (caption.isEmpty()) 0f else context.dp(18)
        art.set(rect.left + context.dp(8), rect.top + context.dp(8), rect.right - context.dp(8), rect.bottom - context.dp(6) - captionH)
        content?.invoke(canvas, art)

        if (caption.isNotEmpty()) {
            text.textSize = context.dp(11.5f)
            val shown = TextUtils.ellipsize(caption, text, rect.width() - context.dp(8), TextUtils.TruncateAt.END)
            canvas.drawText(shown, 0, shown.length, rect.centerX(), rect.bottom - context.dp(8), text)
        }
        if (selectedTile) {
            // A little star badge on the chosen tile.
            val cx = rect.right - context.dp(12)
            val cy = rect.top + context.dp(12)
            card.color = Palette.PINK
            canvas.drawCircle(cx, cy, context.dp(10), card)
            text.color = Palette.WHITE
            text.textSize = context.dp(12)
            canvas.drawText("★", cx, cy + context.dp(4.5f), text)
            text.color = Palette.PLUM
        }
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        animate().scaleX(if (pressed) 0.93f else 1f).scaleY(if (pressed) 0.93f else 1f)
            .setDuration(if (pressed) 80 else 220).setInterpolator(OvershootInterpolator()).start()
    }
}

/** A round colour choice. */
class SwatchView(context: Context) : View(context) {
    var color: Int = Palette.WHITE
        set(value) { field = value; invalidate() }
    var chosen: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = min(width, height) / 2f - context.dp(3)
        paint.style = Paint.Style.FILL
        if (chosen) {
            paint.color = Palette.PLUM
            canvas.drawCircle(cx, cy, r, paint)
            paint.color = Palette.WHITE
            canvas.drawCircle(cx, cy, r - context.dp(3), paint)
            paint.color = color
            canvas.drawCircle(cx, cy, r - context.dp(6), paint)
        } else {
            paint.color = color
            canvas.drawCircle(cx, cy, r - context.dp(4), paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = context.dp(1.5f)
            paint.color = 0x33000000
            canvas.drawCircle(cx, cy, r - context.dp(4), paint)
        }
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        animate().scaleX(if (pressed) 0.85f else 1f).scaleY(if (pressed) 0.85f else 1f).setDuration(120).start()
    }
}

/** Lays out equally sized square children in as many columns as fit. */
class TileGrid(context: Context) : ViewGroup(context) {
    var minTile = context.dpi(92)
    var gap = context.dpi(6)
    private var columns = 1
    private var tile = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val avail = w - paddingLeft - paddingRight
        columns = max(2, (avail + gap) / (minTile + gap))
        tile = (avail - gap * (columns - 1)) / columns
        val spec = MeasureSpec.makeMeasureSpec(tile, MeasureSpec.EXACTLY)
        var count = 0
        for (i in 0 until childCount) {
            val c = getChildAt(i)
            if (c.visibility == GONE) continue
            c.measure(spec, spec)
            count++
        }
        val rows = (count + columns - 1) / columns
        val h = paddingTop + paddingBottom + rows * tile + max(0, rows - 1) * gap
        setMeasuredDimension(w, resolveSize(h, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var i = 0
        for (k in 0 until childCount) {
            val c = getChildAt(k)
            if (c.visibility == GONE) continue
            val x = paddingLeft + (i % columns) * (tile + gap)
            val y = paddingTop + (i / columns) * (tile + gap)
            c.layout(x, y, x + tile, y + tile)
            i++
        }
    }
}

/** Places children left to right, wrapping onto new rows (for chips of different widths). */
class FlowLayout(context: Context) : ViewGroup(context) {
    var gap = context.dpi(8)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxW = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var x = 0
        var y = 0
        var rowH = 0
        for (i in 0 until childCount) {
            val c = getChildAt(i)
            if (c.visibility == GONE) continue
            c.measure(MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST), MeasureSpec.UNSPECIFIED)
            if (x > 0 && x + c.measuredWidth > maxW) {
                x = 0
                y += rowH + gap
                rowH = 0
            }
            x += c.measuredWidth + gap
            rowH = max(rowH, c.measuredHeight)
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(y + rowH + paddingTop + paddingBottom, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val maxW = r - l - paddingLeft - paddingRight
        var x = 0
        var y = 0
        var rowH = 0
        for (i in 0 until childCount) {
            val c = getChildAt(i)
            if (c.visibility == GONE) continue
            if (x > 0 && x + c.measuredWidth > maxW) {
                x = 0
                y += rowH + gap
                rowH = 0
            }
            c.layout(paddingLeft + x, paddingTop + y, paddingLeft + x + c.measuredWidth, paddingTop + y + c.measuredHeight)
            x += c.measuredWidth + gap
            rowH = max(rowH, c.measuredHeight)
        }
    }
}
