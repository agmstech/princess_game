package com.agmstech.princessgame.art

import com.agmstech.princessgame.wardrobe.BodyMode
import com.agmstech.princessgame.wardrobe.Catalog
import com.agmstech.princessgame.wardrobe.Category
import com.agmstech.princessgame.wardrobe.Outfit
import com.agmstech.princessgame.wardrobe.PrincessArt
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Writes SVG contact sheets of all the art. Only runs when the system property
 * `preview.dir` is set, e.g. `./gradlew test -Dpreview.dir=/tmp/previews`
 * (forwarded by the test task), so normal test runs stay fast.
 */
class ArtPreviewTest {

    private val dir: File? = System.getProperty("preview.dir")?.let { File(it) }

    private fun write(name: String, svg: String) {
        dir!!.mkdirs()
        File(dir, "$name.svg").writeText(svg)
    }

    private fun stage(outfit: Outfit, bg: Background, w: Int = 700, h: Int = 700): String {
        val svg = SvgExport()
        val bgScale = maxOf(w / Backgrounds.SIZE, h / Backgrounds.SIZE)
        val bgBody = svg.group(bg.elements.map { Placed(it, RoleColors(0, 0, 0)) },
            "translate(${(w - Backgrounds.SIZE * bgScale) / 2} ${(h - Backgrounds.SIZE * bgScale) / 2}) scale($bgScale)")
        val s = h * 0.94f / Body.HEIGHT
        val princess = svg.group(PrincessArt.compose(outfit), "translate(${(w - Body.WIDTH * s) / 2} ${h * 0.03f}) scale($s)")
        return svg.document(w, h, bgBody + princess)
    }

    @Test
    fun writePreviews() {
        assumeTrue(dir != null)

        val starter = Outfit.starter()
        write("princess_starter", stage(starter, Backgrounds.all.first()))

        // Contact sheets: every item of every wearable category, on the princess.
        for (cat in Catalog.wearables) {
            val items = Catalog.items(cat)
            val cell = 300
            val cols = 5
            val rows = (items.size + cols - 1) / cols
            val svg = SvgExport()
            val body = StringBuilder()
            // Socks and shoes would hide under the starter gown, so show them with shorts.
            val base = if (cat == Category.SOCKS || cat == Category.SHOES) starter.choose(Category.PANTS, "shorts") else starter
            items.forEachIndexed { i, item ->
                val outfit = base.choose(cat, item.id).recolor(cat, item.defaultColor).let {
                    if (item.isNone) it.choose(cat, item.id) else it
                }
                val x = (i % cols) * cell
                val y = (i / cols) * cell * 5 / 3
                body.append(svg.group(PrincessArt.compose(outfit), svg.fit(Box(0f, 0f, Body.WIDTH, Body.HEIGHT), cell.toFloat(), cell * 5 / 3f, x.toFloat(), y.toFloat())))
                body.append("<text x=\"${x + cell / 2}\" y=\"${y + 24}\" font-size=\"20\" text-anchor=\"middle\" font-family=\"sans-serif\">${item.label}</text>")
            }
            write("sheet_${cat.name.lowercase()}", svg.document(cols * cell, rows * cell * 5 / 3, body.toString()))

            // Tile thumbnails, as shown in the wardrobe.
            val tsvg = SvgExport()
            val tbody = StringBuilder()
            items.forEachIndexed { i, item ->
                val x = (i % 8) * 130f
                val y = (i / 8) * 130f
                tbody.append("<rect x=\"${x + 5}\" y=\"${y + 5}\" width=\"120\" height=\"120\" rx=\"18\" fill=\"#FFF0F6\" stroke=\"#F8BBD9\"/>")
                val placed = PrincessArt.thumbnail(cat, item, starter.choice(cat).color, starter)
                val bounds = placed.mapNotNull { com.agmstech.princessgame.wardrobe.boundsOf(it.element) }.reduceOrNull { a, b -> a.union(b) }
                if (bounds != null) tbody.append(tsvg.group(placed, tsvg.fit(bounds.inset(-6f), 104f, 104f, x + 13, y + 13)))
            }
            write("tiles_${cat.name.lowercase()}", tsvg.document(8 * 130 + 10, ((items.size + 7) / 8) * 130 + 10, tbody.toString()))
        }

        // Backgrounds with a princess in front.
        Backgrounds.all.forEachIndexed { i, bg ->
            val looks = listOf(
                starter,
                starter.choose(Category.SPORTS, "tennis").choose(Category.HAIR, "ponytail").choose(Category.CROWN, "visor").choose(Category.HAND, "racket").choose(Category.SHOES, "sneakers").choose(Category.SOCKS, "ankle"),
                starter.choose(Category.TOP, "hoodie").choose(Category.PANTS, "jeans").choose(Category.HAIR, "curly").choose(Category.BAG, "crossbody").choose(Category.SHOES, "boots"),
                starter.choose(Category.SPORTS, "volleyball").choose(Category.HAIR, "pigtails").choose(Category.HAND, "volleyball").choose(Category.SHOES, "sneakers").choose(Category.CROWN, "sweatband"),
                starter.choose(Category.DRESS, "fairy").choose(Category.HAIR, "wavy").choose(Category.CROWN, "flowers").choose(Category.BAG, "purse").choose(Category.SOCKS, "frilly").withSkin(0xFF7A4E33.toInt()),
            )
            write("stage_${bg.id}", stage(looks[i % looks.size], bg))
        }
        check(starter.bodyMode == BodyMode.DRESS)
    }
}
