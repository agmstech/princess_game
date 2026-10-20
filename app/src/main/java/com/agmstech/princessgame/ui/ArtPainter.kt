package com.agmstech.princessgame.ui

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.agmstech.princessgame.art.Box
import com.agmstech.princessgame.art.Cmd
import com.agmstech.princessgame.art.Colors
import com.agmstech.princessgame.art.Element
import com.agmstech.princessgame.art.Group
import com.agmstech.princessgame.art.Ink
import com.agmstech.princessgame.art.Label
import com.agmstech.princessgame.art.Placed
import com.agmstech.princessgame.art.RoleColors
import com.agmstech.princessgame.art.Shape
import com.agmstech.princessgame.art.VPath

/** Draws the platform-independent art model onto an Android [Canvas]. */
object ArtPainter {
    // Art paths are long-lived catalogue objects, so converted paths are cached by identity.
    private val paths = HashMap<VPath, Path>()
    private val shaders = HashMap<Ink, Shader>()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private fun androidPath(p: VPath, evenOdd: Boolean): Path = paths.getOrPut(p) {
        Path().apply {
            for (c in p.cmds) when (c) {
                is Cmd.MoveTo -> moveTo(c.x, c.y)
                is Cmd.LineTo -> lineTo(c.x, c.y)
                is Cmd.QuadTo -> quadTo(c.x1, c.y1, c.x, c.y)
                is Cmd.CubicTo -> cubicTo(c.x1, c.y1, c.x2, c.y2, c.x, c.y)
                Cmd.Close -> close()
            }
            fillType = if (evenOdd) Path.FillType.EVEN_ODD else Path.FillType.WINDING
        }
    }

    private fun shader(ink: Ink): Shader? = when (ink) {
        is Ink.Linear -> shaders.getOrPut(ink) { LinearGradient(ink.x0, ink.y0, ink.x1, ink.y1, ink.colors, ink.stops, Shader.TileMode.CLAMP) }
        is Ink.Radial -> shaders.getOrPut(ink) { RadialGradient(ink.cx, ink.cy, ink.radius, ink.colors, ink.stops, Shader.TileMode.CLAMP) }
        else -> null
    }

    fun draw(canvas: Canvas, items: List<Placed>) {
        for (p in items) drawElement(canvas, p.element, p.colors)
    }

    fun drawElements(canvas: Canvas, elements: List<Element>, colors: RoleColors) {
        for (e in elements) drawElement(canvas, e, colors)
    }

    private fun drawElement(canvas: Canvas, e: Element, colors: RoleColors) {
        when (e) {
            is Shape -> {
                val path = androidPath(e.path, e.evenOdd)
                e.fill?.let { ink ->
                    val sh = shader(ink)
                    fillPaint.shader = sh
                    fillPaint.color = if (sh != null) Palette.WHITE else Colors.flatten(ink, colors) ?: return@let
                    canvas.drawPath(path, fillPaint)
                    fillPaint.shader = null
                }
                Colors.strokeColor(e, colors)?.let { color ->
                    strokePaint.color = color
                    strokePaint.strokeWidth = e.strokeWidth
                    canvas.drawPath(path, strokePaint)
                }
            }
            is Label -> {
                textPaint.textSize = e.size
                e.stroke?.let { Colors.flatten(it, colors) }?.let { color ->
                    textPaint.style = Paint.Style.STROKE
                    textPaint.strokeWidth = e.strokeWidth * 2
                    textPaint.strokeJoin = Paint.Join.ROUND
                    textPaint.color = color
                    canvas.drawText(e.text, e.x, e.y, textPaint)
                }
                textPaint.style = Paint.Style.FILL
                textPaint.color = Colors.flatten(e.fill, colors) ?: Palette.WHITE
                canvas.drawText(e.text, e.x, e.y, textPaint)
            }
            is Group -> {
                val save = canvas.save()
                e.clip?.let { canvas.clipPath(androidPath(it, false)) }
                for (child in e.children) drawElement(canvas, child, colors)
                canvas.restoreToCount(save)
            }
        }
    }

    /**
     * Maps [box] (art units) into [dst] on the canvas. With [cover] the art fills
     * [dst] completely and may be cropped; otherwise it fits inside it.
     * Returns the save count to restore.
     */
    fun mapInto(canvas: Canvas, box: Box, dst: RectF, cover: Boolean = false): Int {
        val sx = dst.width() / box.width
        val sy = dst.height() / box.height
        val s = if (cover) maxOf(sx, sy) else minOf(sx, sy)
        val save = canvas.save()
        canvas.translate(dst.centerX() - box.centerX * s, dst.centerY() - box.centerY * s)
        canvas.scale(s, s)
        return save
    }
}
