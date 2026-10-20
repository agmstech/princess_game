package com.agmstech.princessgame.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.agmstech.princessgame.art.Backgrounds
import com.agmstech.princessgame.art.Box
import com.agmstech.princessgame.art.Placed
import com.agmstech.princessgame.art.RoleColors
import com.agmstech.princessgame.audio.Songs
import com.agmstech.princessgame.core.GameState
import com.agmstech.princessgame.wardrobe.Catalog
import com.agmstech.princessgame.wardrobe.Category
import com.agmstech.princessgame.wardrobe.Item
import com.agmstech.princessgame.wardrobe.PaletteKind
import com.agmstech.princessgame.wardrobe.Palettes
import com.agmstech.princessgame.wardrobe.PrincessArt
import com.agmstech.princessgame.wardrobe.boundsOf

/** The dressing-up controls: category tabs, item tiles and colour swatches. */
class WardrobePanel(context: Context, private val listener: Listener) : LinearLayout(context) {

    interface Listener {
        fun onTabSelected(category: Category)
        fun onItemChosen(category: Category, itemId: String)
        fun onColorChosen(category: Category, color: Int)
        fun onSkinChosen(color: Int)
        fun onEditName()
        fun onBackgroundChosen(id: String)
        fun onSongChosen(id: String)
    }

    private var state = GameState()
    private var shownTab: Category? = null

    private val tabRow = LinearLayout(context).apply { orientation = HORIZONTAL }
    private val tabScroll = HorizontalScrollView(context).apply {
        isHorizontalScrollBarEnabled = false
        addView(tabRow)
    }
    private val tabs = LinkedHashMap<Category, TileView>()

    private val grid = TileGrid(context).apply { setPadding(context.dpi(8), context.dpi(6), context.dpi(8), context.dpi(12)) }
    private val gridScroll = ScrollView(context).apply {
        isVerticalScrollBarEnabled = false
        addView(grid)
    }
    private val tiles = ArrayList<Pair<TileView, () -> Boolean>>()

    private val swatchRow = LinearLayout(context).apply {
        orientation = HORIZONTAL
        setPadding(context.dpi(6), 0, context.dpi(6), 0)
    }
    private val swatchScroll = HorizontalScrollView(context).apply {
        isHorizontalScrollBarEnabled = false
        addView(swatchRow)
    }

    private val thumbBounds = HashMap<String, Box>()

    init {
        orientation = VERTICAL
        setBackgroundColor(Palette.PANEL)
        tabScroll.setBackgroundColor(Palette.PINK_LIGHT)
        tabScroll.setPadding(0, context.dpi(4), 0, context.dpi(4))
        addView(tabScroll, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(gridScroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        swatchScroll.setBackgroundColor(Palette.PINK_LIGHT)
        addView(swatchScroll, LayoutParams(LayoutParams.MATCH_PARENT, context.dpi(64)))
        for (cat in Category.values()) {
            val tab = TileView(context).apply {
                caption = cat.title
                cardColor = Palette.WHITE
                content = tabIcon(cat)
                setOnClickListener { listener.onTabSelected(cat) }
            }
            tabs[cat] = tab
            tabRow.addView(tab, LayoutParams(context.dpi(78), context.dpi(84)))
        }
    }

    /** Shows [newState]; rebuilds the tiles only when the tab changes. */
    fun bind(newState: GameState) {
        val tabChanged = newState.tab != shownTab
        state = newState
        tabs.forEach { (cat, tab) -> tab.selectedTile = cat == state.tab }
        tabs[Category.PRINCESS]?.invalidate()
        if (tabChanged) {
            shownTab = state.tab
            buildTiles()
            buildSwatches()
            gridScroll.scrollTo(0, 0)
            tabs[state.tab]?.let { tab -> tabScroll.post { tabScroll.smoothScrollTo((tab.left - (tabScroll.width - tab.width) / 2).coerceAtLeast(0), 0) } }
        }
        refresh()
    }

    private fun refresh() {
        for ((tile, isSelected) in tiles) {
            tile.selectedTile = isSelected()
            tile.invalidate()
        }
        val current = currentColor()
        for (i in 0 until swatchRow.childCount) {
            val s = swatchRow.getChildAt(i) as SwatchView
            s.chosen = s.color == current
        }
    }

    private fun currentColor(): Int? = when (state.tab.palette) {
        PaletteKind.NONE -> null
        PaletteKind.SKIN -> state.outfit.skin
        else -> state.outfit.choice(state.tab).color
    }

    private fun addTile(caption: String, isSelected: () -> Boolean, content: TileContent, onClick: () -> Unit) {
        val tile = TileView(context).apply {
            this.caption = caption
            this.content = content
            setOnClickListener { onClick() }
        }
        grid.addView(tile)
        tiles += tile to isSelected
    }

    private fun buildTiles() {
        grid.removeAllViews()
        tiles.clear()
        val cat = state.tab
        when (cat) {
            Category.PRINCESS -> {
                addTile("Name", { false }, nameContent()) { listener.onEditName() }
                for (tone in Palettes.skin) {
                    addTile("", { state.outfit.skin == tone }, { c, area ->
                        val o = state.outfit.withSkin(tone)
                        drawFitted(c, area, PrincessArt.portrait(o), PrincessArt.portraitFrame, clip = true)
                    }) { listener.onSkinChosen(tone) }
                }
            }
            Category.PLACE -> for (bg in Backgrounds.all) {
                addTile(bg.label, { state.backgroundId == bg.id }, { c, area ->
                    val save = c.save()
                    val r = context.dp(12)
                    val clip = android.graphics.Path().apply { addRoundRect(area, r, r, android.graphics.Path.Direction.CW) }
                    c.clipPath(clip)
                    val inner = ArtPainter.mapInto(c, Box(0f, 0f, Backgrounds.SIZE, Backgrounds.SIZE), area, cover = true)
                    ArtPainter.drawElements(c, bg.elements, RoleColors(0, 0, 0))
                    c.restoreToCount(inner)
                    c.restoreToCount(save)
                }) { listener.onBackgroundChosen(bg.id) }
            }
            Category.MUSIC -> {
                addTile("No music", { state.songId == Songs.OFF }, symbolContent("🔇")) { listener.onSongChosen(Songs.OFF) }
                for (song in Songs.all) {
                    addTile(song.title, { state.songId == song.id }, symbolContent(song.emoji)) { listener.onSongChosen(song.id) }
                }
            }
            else -> for (item in Catalog.items(cat)) {
                addTile(item.label, { state.outfit.isWorn(cat) && state.outfit.choice(cat).itemId == item.id }, itemContent(cat, item)) {
                    listener.onItemChosen(cat, item.id)
                }
            }
        }
    }

    private fun buildSwatches() {
        swatchRow.removeAllViews()
        val colors = when (state.tab.palette) {
            PaletteKind.CLOTHES -> Palettes.clothes
            PaletteKind.HAIR -> Palettes.hair
            PaletteKind.SKIN -> Palettes.skin
            PaletteKind.NONE -> emptyList()
        }
        swatchScroll.visibility = if (colors.isEmpty()) View.GONE else View.VISIBLE
        val cat = state.tab
        for (color in colors) {
            val sw = SwatchView(context).apply {
                this.color = color
                contentDescription = "Colour"
                setOnClickListener { listener.onColorChosen(cat, color) }
            }
            swatchRow.addView(sw, LayoutParams(context.dpi(56), context.dpi(56)).apply { topMargin = context.dpi(4) })
        }
    }

    private fun itemContent(cat: Category, item: Item): TileContent = { c, area ->
        if (item.isNone) {
            drawNone(c, area)
        } else {
            val placed = PrincessArt.thumbnail(cat, item, state.outfit.choice(cat).color, state.outfit)
            val key = "${cat.name}/${item.id}"
            val box = thumbBounds.getOrPut(key) {
                placed.mapNotNull { boundsOf(it.element) }.reduce { a, b -> a.union(b) }.inset(-6f)
            }
            drawFitted(c, area, placed, box)
        }
    }

    private fun drawFitted(c: Canvas, area: RectF, placed: List<Placed>, box: Box, clip: Boolean = false) {
        val outer = c.save()
        if (clip) c.clipRect(area)
        ArtPainter.mapInto(c, box, area)
        ArtPainter.draw(c, placed)
        c.restoreToCount(outer)
    }

    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    private fun symbolContent(symbol: String): TileContent = { c, area ->
        symbolPaint.textSize = minOf(area.width(), area.height()) * 0.62f
        c.drawText(symbol, area.centerX(), area.centerY() + symbolPaint.textSize * 0.35f, symbolPaint)
    }

    private fun nameContent(): TileContent = { c, area ->
        symbolPaint.textSize = minOf(area.width(), area.height()) * 0.5f
        c.drawText("✏️", area.centerX(), area.centerY() + symbolPaint.textSize * 0.35f, symbolPaint)
    }

    private val nonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFFBDBDBD.toInt() }

    private fun drawNone(c: Canvas, area: RectF) {
        val r = minOf(area.width(), area.height()) * 0.3f
        nonePaint.strokeWidth = r * 0.22f
        c.drawCircle(area.centerX(), area.centerY(), r, nonePaint)
        val d = r * 0.7071f
        c.drawLine(area.centerX() - d, area.centerY() + d, area.centerX() + d, area.centerY() - d, nonePaint)
    }

    /** Fixed, colourful icons for the category tabs. */
    private fun tabIcon(cat: Category): TileContent {
        val (category, itemId) = when (cat) {
            Category.HAIR -> Category.HAIR to "wavy"
            Category.CROWN -> Category.CROWN to "crown"
            Category.DRESS -> Category.DRESS to "gown"
            Category.TOP -> Category.TOP to "tee"
            Category.PANTS -> Category.PANTS to "jeans"
            Category.SPORTS -> Category.HAND to "racket"
            Category.SOCKS -> Category.SOCKS to "stripy"
            Category.SHOES -> Category.SHOES to "sneakers"
            Category.BAG -> Category.BAG to "handbag"
            Category.HAND -> Category.HAND to "balloon"
            Category.PRINCESS -> return { c, area -> drawFitted(c, area, PrincessArt.portrait(state.outfit), PrincessArt.portraitFrame, clip = true) }
            Category.PLACE -> return { c, area ->
                val save = c.save()
                val clip = android.graphics.Path().apply {
                    addCircle(area.centerX(), area.centerY(), minOf(area.width(), area.height()) / 2f, android.graphics.Path.Direction.CW)
                }
                c.clipPath(clip)
                val inner = ArtPainter.mapInto(c, Box(100f, 100f, 900f, 900f), area, cover = true)
                ArtPainter.drawElements(c, Backgrounds.byId("castle").elements, RoleColors(0, 0, 0))
                c.restoreToCount(inner)
                c.restoreToCount(save)
            }
            Category.MUSIC -> return symbolContent("🎵")
        }
        val item = Catalog.item(category, itemId)
        val placed = PrincessArt.thumbnail(category, item, item.defaultColor, GameState().outfit)
        val box = placed.mapNotNull { boundsOf(it.element) }.reduce { a, b -> a.union(b) }.inset(-6f)
        return { c, area -> drawFitted(c, area, placed, box) }
    }
}
