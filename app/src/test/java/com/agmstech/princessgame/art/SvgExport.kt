package com.agmstech.princessgame.art

/**
 * Renders the game's vector art to SVG so drawings can be previewed and
 * checked without an Android device. Mirrors what PrincessCanvas does on Android.
 */
class SvgExport {
    private val defs = StringBuilder()
    private var nextId = 0

    private fun hex(c: Int) = "#%06X".format(c and 0xFFFFFF)
    private fun opacity(c: Int) = "%.3f".format(Colors.alpha(c) / 255f)

    fun d(p: VPath): String = buildString {
        for (c in p.cmds) {
            when (c) {
                is Cmd.MoveTo -> append("M${c.x} ${c.y} ")
                is Cmd.LineTo -> append("L${c.x} ${c.y} ")
                is Cmd.QuadTo -> append("Q${c.x1} ${c.y1} ${c.x} ${c.y} ")
                is Cmd.CubicTo -> append("C${c.x1} ${c.y1} ${c.x2} ${c.y2} ${c.x} ${c.y} ")
                Cmd.Close -> append("Z ")
            }
        }
    }

    private fun gradient(ink: Ink): String {
        val id = "g${nextId++}"
        when (ink) {
            is Ink.Linear -> {
                defs.append("<linearGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" x1=\"${ink.x0}\" y1=\"${ink.y0}\" x2=\"${ink.x1}\" y2=\"${ink.y1}\">")
                stops(ink.colors, ink.stops)
                defs.append("</linearGradient>")
            }
            is Ink.Radial -> {
                defs.append("<radialGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" cx=\"${ink.cx}\" cy=\"${ink.cy}\" r=\"${ink.radius}\">")
                stops(ink.colors, ink.stops)
                defs.append("</radialGradient>")
            }
            else -> error("not a gradient")
        }
        return "url(#$id)"
    }

    private fun stops(colors: IntArray, stops: FloatArray?) {
        colors.forEachIndexed { i, c ->
            val off = stops?.get(i) ?: (i.toFloat() / (colors.size - 1))
            defs.append("<stop offset=\"$off\" stop-color=\"${hex(c)}\" stop-opacity=\"${opacity(c)}\"/>")
        }
    }

    private fun paint(ink: Ink?, colors: RoleColors): String = when (ink) {
        null, Ink.Auto -> "fill=\"none\""
        is Ink.Linear, is Ink.Radial -> "fill=\"${gradient(ink)}\""
        else -> Colors.flatten(ink, colors)!!.let { "fill=\"${hex(it)}\" fill-opacity=\"${opacity(it)}\"" }
    }

    fun element(e: Element, colors: RoleColors): String = when (e) {
        is Shape -> {
            val stroke = Colors.strokeColor(e, colors)
            val strokeAttr = if (stroke == null) "" else
                " stroke=\"${hex(stroke)}\" stroke-opacity=\"${opacity(stroke)}\" stroke-width=\"${e.strokeWidth}\" stroke-linejoin=\"round\" stroke-linecap=\"round\""
            val rule = if (e.evenOdd) " fill-rule=\"evenodd\"" else ""
            "<path d=\"${d(e.path)}\" ${paint(e.fill, colors)}$strokeAttr$rule/>"
        }
        is Label -> {
            val fill = Colors.flatten(e.fill, colors)!!
            val stroke = e.stroke?.let { Colors.flatten(it, colors) }
            val strokeAttr = if (stroke == null) "" else " stroke=\"${hex(stroke)}\" stroke-width=\"${e.strokeWidth}\" paint-order=\"stroke\""
            "<text x=\"${e.x}\" y=\"${e.y}\" font-size=\"${e.size}\" font-family=\"sans-serif\" font-weight=\"bold\" text-anchor=\"middle\" fill=\"${hex(fill)}\"$strokeAttr>${e.text}</text>"
        }
        is Group -> {
            val inner = e.children.joinToString("") { element(it, colors) }
            if (e.clip == null) "<g>$inner</g>" else {
                val id = "c${nextId++}"
                defs.append("<clipPath id=\"$id\"><path d=\"${d(e.clip)}\"/></clipPath>")
                "<g clip-path=\"url(#$id)\">$inner</g>"
            }
        }
    }

    fun group(items: List<Placed>, transform: String = ""): String =
        "<g transform=\"$transform\">" + items.joinToString("") { element(it.element, it.colors) } + "</g>"

    fun document(width: Int, height: Int, body: String, background: String = "#FFFFFF"): String =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">" +
            "<defs>$defs</defs><rect width=\"100%\" height=\"100%\" fill=\"$background\"/>$body</svg>"

    /** Transform that fits [box] inside a [w] x [h] area, centred. */
    fun fit(box: Box, w: Float, h: Float, x: Float = 0f, y: Float = 0f): String {
        val s = minOf(w / box.width, h / box.height)
        val tx = x + (w - box.width * s) / 2 - box.left * s
        val ty = y + (h - box.height * s) / 2 - box.top * s
        return "translate($tx $ty) scale($s)"
    }
}
