package com.agmstech.princessgame.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.agmstech.princessgame.art.Background
import com.agmstech.princessgame.art.Backgrounds
import com.agmstech.princessgame.art.Body
import com.agmstech.princessgame.art.Box
import com.agmstech.princessgame.art.Placed
import com.agmstech.princessgame.art.RoleColors
import com.agmstech.princessgame.wardrobe.Outfit
import com.agmstech.princessgame.wardrobe.PrincessArt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** The scene: background, the princess, and her little animations. */
class PrincessStageView(context: Context) : View(context) {

    var onPrincessTapped: (() -> Unit)? = null

    var outfit: Outfit = Outfit.starter()
        set(value) {
            if (field != value) {
                field = value
                composedOpen = null
                composedBlink = null
                invalidate()
            }
        }

    var background: Background = Backgrounds.all.first()
        set(value) { field = value; invalidate() }

    private var composedOpen: List<Placed>? = null
    private var composedBlink: List<Placed>? = null
    private var blinking = false
    private var jump = 0f
    private val noColors = RoleColors(0, 0, 0)

    private class Spark(var x: Float, var y: Float, val vx: Float, val vy: Float, val size: Float, val color: Int, val spin: Float)
    private val sparks = ArrayList<Spark>()
    private var sparkAge = 0f
    private var sparkAnimator: ValueAnimator? = null

    private val sceneRect = RectF()
    private val princessRect = RectF()
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33402040 }
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sparkPath = Path()

    private val blinkLoop = object : Runnable {
        override fun run() {
            blinking = true
            invalidate()
            postDelayed({ blinking = false; invalidate() }, 150)
            postDelayed(this, 2500L + Random.nextLong(3000L))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(blinkLoop, 2000)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(blinkLoop)
        sparkAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        sceneRect.set(0f, 0f, w.toFloat(), h.toFloat())
        val ph = h * 0.9f
        val pw = ph * Body.WIDTH / Body.HEIGHT
        val left = (w - pw) / 2f
        val top = h * 0.07f
        princessRect.set(left, top, left + pw, top + ph)
    }

    override fun onDraw(canvas: Canvas) {
        var save = ArtPainter.mapInto(canvas, Box(0f, 0f, Backgrounds.SIZE, Backgrounds.SIZE), sceneRect, cover = true)
        ArtPainter.drawElements(canvas, background.elements, noColors)
        canvas.restoreToCount(save)

        // Soft shadow on the ground, shrinking while she jumps.
        val feetY = princessRect.top + princessRect.height() * 0.935f
        val sw = princessRect.width() * 0.32f * (1f - jump * 0.5f)
        canvas.drawOval(princessRect.centerX() - sw, feetY - sw * 0.16f, princessRect.centerX() + sw, feetY + sw * 0.16f, shadowPaint)

        save = canvas.save()
        canvas.translate(0f, -jump * princessRect.height() * 0.08f)
        val inner = ArtPainter.mapInto(canvas, Box(0f, 0f, Body.WIDTH, Body.HEIGHT), princessRect)
        val art = if (blinking) composedBlink ?: PrincessArt.compose(outfit, blink = true).also { composedBlink = it }
        else composedOpen ?: PrincessArt.compose(outfit).also { composedOpen = it }
        ArtPainter.draw(canvas, art)
        canvas.restoreToCount(inner)
        canvas.restoreToCount(save)

        drawSparks(canvas)
    }

    private fun drawSparks(canvas: Canvas) {
        if (sparks.isEmpty()) return
        val alpha = ((1f - sparkAge) * 255).toInt().coerceIn(0, 255)
        for (s in sparks) {
            sparkPaint.color = s.color
            sparkPaint.alpha = alpha
            val x = s.x + s.vx * sparkAge
            val y = s.y + s.vy * sparkAge + 0.5f * princessRect.height() * 0.4f * sparkAge * sparkAge
            starPath(x, y, s.size * (1f - sparkAge * 0.4f), s.spin * sparkAge)
            canvas.drawPath(sparkPath, sparkPaint)
        }
    }

    private fun starPath(cx: Float, cy: Float, r: Float, rot: Float) {
        sparkPath.reset()
        for (k in 0 until 8) {
            val rr = if (k % 2 == 0) r else r * 0.38f
            val a = (rot + k * PI / 4 - PI / 2).toFloat()
            val x = cx + rr * cos(a)
            val y = cy + rr * sin(a)
            if (k == 0) sparkPath.moveTo(x, y) else sparkPath.lineTo(x, y)
        }
        sparkPath.close()
    }

    /** A burst of sparkles around the princess. */
    fun celebrate() {
        if (width == 0) return
        sparks.clear()
        val colors = intArrayOf(0xFFFFD54F.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF8FB8.toInt(), 0xFFB388FF.toInt(), 0xFF81D4FA.toInt())
        val cx = princessRect.centerX()
        val cy = princessRect.top + princessRect.height() * 0.45f
        val spread = min(width, height) * 0.45f
        repeat(22) {
            val a = Random.nextFloat() * 2 * PI.toFloat()
            val speed = spread * (0.4f + Random.nextFloat() * 0.6f)
            sparks += Spark(
                cx + cos(a) * princessRect.width() * 0.25f, cy + sin(a) * princessRect.height() * 0.3f,
                cos(a) * speed, sin(a) * speed - spread * 0.3f,
                context.dp(7 + Random.nextInt(9)), colors[Random.nextInt(colors.size)], (Random.nextFloat() - 0.5f) * 6f,
            )
        }
        sparkAnimator?.cancel()
        sparkAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900
            interpolator = DecelerateInterpolator()
            addUpdateListener { sparkAge = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    /** A happy little hop. */
    fun hop() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 480
            addUpdateListener {
                jump = sin((it.animatedValue as Float) * PI).toFloat()
                invalidate()
            }
            start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            return princessRect.contains(event.x, event.y)
        }
        if (event.action == MotionEvent.ACTION_UP && princessRect.contains(event.x, event.y)) {
            performClick()
            hop()
            celebrate()
            onPrincessTapped?.invoke()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

/** The princess's name on a ribbon; tap it to change the name. */
class NameRibbonView(context: Context) : View(context) {
    var name: String = ""
        set(value) { field = value; requestLayout(); invalidate() }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = roundedBold
        color = Palette.WHITE
        textSize = context.dp(20)
    }
    private val rect = RectF()

    init {
        isClickable = true
        isFocusable = true
    }

    private fun shown(): String = "👑 $name  ✏️"

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxW = MeasureSpec.getSize(widthMeasureSpec)
        val want = (text.measureText(shown()) + context.dp(36)).toInt()
        setMeasuredDimension(if (maxW > 0) min(want, maxW) else want, context.dpi(50))
    }

    override fun onDraw(canvas: Canvas) {
        rect.set(context.dp(2), context.dp(4), width - context.dp(2), height - context.dp(4))
        fill.color = 0x40000000
        canvas.drawRoundRect(rect.left, rect.top + context.dp(3), rect.right, rect.bottom + context.dp(3), rect.height() / 2, rect.height() / 2, fill)
        fill.color = Palette.PINK
        canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, fill)
        fill.color = 0x33FFFFFF
        canvas.drawRoundRect(rect.left + context.dp(6), rect.top + context.dp(4), rect.right - context.dp(6), rect.centerY(), rect.height() / 4, rect.height() / 4, fill)
        val s = TextUtils.ellipsize(shown(), text, rect.width() - context.dp(24), TextUtils.TruncateAt.MIDDLE)
        canvas.drawText(s, 0, s.length, rect.left + context.dp(14), rect.centerY() + text.textSize * 0.35f, text)
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        animate().scaleX(if (pressed) 0.95f else 1f).scaleY(if (pressed) 0.95f else 1f).setDuration(100).start()
    }
}

/** A round button showing a symbol, for the stage corners. */
class RoundButton(context: Context, private val symbol: String, private val fillColor: Int = 0xCCFFFFFF.toInt()) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        val r = min(width, height) / 2f - context.dp(2)
        paint.color = 0x33000000
        canvas.drawCircle(width / 2f, height / 2f + context.dp(2), r, paint)
        paint.color = fillColor
        canvas.drawCircle(width / 2f, height / 2f, r, paint)
        text.textSize = r * 1.05f
        canvas.drawText(symbol, width / 2f, height / 2f + text.textSize * 0.36f, text)
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        animate().scaleX(if (pressed) 0.9f else 1f).scaleY(if (pressed) 0.9f else 1f).setDuration(100).start()
    }
}

/** A pie that shrinks as today's play time runs out: green, then orange, then red. */
class TimerPieView(context: Context) : View(context) {
    var fraction: Float = 1f
        set(value) {
            val v = value.coerceIn(0f, 1f)
            if (kotlin.math.abs(v - field) > 0.002f) {
                field = v
                invalidate()
            }
        }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val oval = RectF()

    override fun onDraw(canvas: Canvas) {
        val r = min(width, height) / 2f - context.dp(2)
        val cx = width / 2f
        val cy = height / 2f
        paint.style = Paint.Style.FILL
        paint.color = 0xCCFFFFFF.toInt()
        canvas.drawCircle(cx, cy, r, paint)
        paint.color = when {
            fraction > 0.5f -> Palette.GREEN
            fraction > 0.2f -> Palette.ORANGE
            else -> Palette.RED
        }
        oval.set(cx - r + context.dp(4), cy - r + context.dp(4), cx + r - context.dp(4), cy + r - context.dp(4))
        canvas.drawArc(oval, -90f, 360f * fraction, true, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = context.dp(2)
        paint.color = Palette.PLUM
        canvas.drawCircle(cx, cy, r - context.dp(1), paint)
    }
}
