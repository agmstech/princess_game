package com.agmstech.princessgame.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.agmstech.princessgame.art.Backgrounds
import com.agmstech.princessgame.art.Box
import com.agmstech.princessgame.art.RoleColors
import kotlin.math.min
import kotlin.math.sin

/**
 * The last-ten-seconds warning: the screen edges flash red and a big red
 * number counts down. Touches pass through so play can continue until the end.
 */
class CountdownView(context: Context) : View(context) {
    var seconds: Int = 10
        set(value) {
            field = value
            contentDescription = "$value seconds left"
            invalidate()
        }

    private val start = SystemClock.uptimeMillis()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = roundedBold
    }
    private val rect = RectF()

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onDraw(canvas: Canvas) {
        val t = (SystemClock.uptimeMillis() - start) / 1000f
        // Two flashes per second.
        val flash = (0.5f + 0.5f * sin(t * 2f * Math.PI.toFloat() * 2f)).coerceIn(0f, 1f)

        paint.style = Paint.Style.FILL
        paint.color = Palette.RED
        paint.alpha = (30 + 70 * flash).toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = context.dp(14)
        paint.alpha = (140 + 115 * flash).toInt()
        rect.set(paint.strokeWidth / 2, paint.strokeWidth / 2, width - paint.strokeWidth / 2, height - paint.strokeWidth / 2)
        canvas.drawRect(rect, paint)

        val r = min(width, height) * 0.16f * (1f + 0.08f * flash)
        val cx = width / 2f
        val cy = height * 0.38f
        paint.style = Paint.Style.FILL
        paint.color = 0x55000000
        canvas.drawCircle(cx, cy + context.dp(4), r, paint)
        paint.color = Palette.RED
        canvas.drawCircle(cx, cy, r, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = context.dp(6)
        paint.color = Palette.WHITE
        canvas.drawCircle(cx, cy, r, paint)

        text.color = Palette.WHITE
        text.textSize = r * 1.15f
        canvas.drawText(seconds.toString(), cx, cy + text.textSize * 0.36f, text)

        text.textSize = context.dp(26)
        text.setShadowLayer(context.dp(4), 0f, context.dp(2), 0xAA000000.toInt())
        canvas.drawText("Time to say bye-bye!", cx, cy + r + context.dp(44), text)
        text.clearShadowLayer()

        postInvalidateOnAnimation()
    }
}

/** "Time's up" screen shown when today's play time has run out. */
class LockoutView(context: Context, name: String, onParent: () -> Unit) : FrameLayout(context) {

    private val message = "Time's up, $name!\nGreat playing today.\nSee you tomorrow!"

    private val scene = object : View(context) {
        private val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Palette.WHITE
            typeface = roundedBold
        }
        private val rect = RectF()
        private val night = Backgrounds.byId("night")

        override fun onDraw(canvas: Canvas) {
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            val save = ArtPainter.mapInto(canvas, Box(0f, 0f, Backgrounds.SIZE, Backgrounds.SIZE), rect, cover = true)
            ArtPainter.drawElements(canvas, night.elements, RoleColors(0, 0, 0))
            canvas.restoreToCount(save)

            val bodySize = min(width, height) * 0.07f
            text.textSize = bodySize
            text.setShadowLayer(context.dp(4), 0f, context.dp(2), 0xCC000000.toInt())
            val w = (width * 0.78f).toInt()
            val layout = StaticLayout.Builder.obtain(message, 0, message.length, text, w)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.15f)
                .build()

            // A soft card keeps the message readable over the night scene.
            val moon = min(width, height) * 0.14f
            val pad = context.dp(24)
            val top = height * 0.45f - (layout.height + moon) / 2f
            card.set((width - w) / 2f - pad, top - pad, (width + w) / 2f + pad, top + moon + context.dp(8) + layout.height + pad)
            canvas.drawRoundRect(card, context.dp(28), context.dp(28), cardPaint)

            text.textSize = moon
            text.textAlign = Paint.Align.CENTER
            canvas.drawText("🌙", width / 2f, top + moon * 0.85f, text)
            text.textAlign = Paint.Align.LEFT
            text.textSize = bodySize

            canvas.save()
            canvas.translate((width - w) / 2f, top + moon + context.dp(8))
            layout.draw(canvas)
            canvas.restore()
        }

        private val card = RectF()
        private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xB3221A52.toInt() }
    }

    init {
        isClickable = true // swallow touches so the game underneath can't be used
        addView(scene, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        val parent = context.pillButton("🔒 Grown-ups", fill = 0x99FFFFFF.toInt(), textColor = Palette.PLUM, sizeSp = 15f) { onParent() }
        addView(parent, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.END).apply {
            setMargins(context.dpi(16), context.dpi(16), context.dpi(20), context.dpi(20))
        })
        contentDescription = message
    }
}
