package com.agmstech.princessgame

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import com.agmstech.princessgame.art.Backgrounds
import com.agmstech.princessgame.audio.MusicPlayer
import com.agmstech.princessgame.audio.Sfx
import com.agmstech.princessgame.audio.SoundEffects
import com.agmstech.princessgame.core.GameState
import com.agmstech.princessgame.core.ParentLock
import com.agmstech.princessgame.core.ParentSettings
import com.agmstech.princessgame.core.PlayTimeTracker
import com.agmstech.princessgame.core.PrefsStore
import com.agmstech.princessgame.ui.CountdownView
import com.agmstech.princessgame.ui.Dialogs
import com.agmstech.princessgame.ui.GameLayout
import com.agmstech.princessgame.ui.LockoutView
import com.agmstech.princessgame.ui.NameRibbonView
import com.agmstech.princessgame.ui.PrincessStageView
import com.agmstech.princessgame.ui.RoundButton
import com.agmstech.princessgame.ui.TimerPieView
import com.agmstech.princessgame.ui.WardrobePanel
import com.agmstech.princessgame.ui.dpi
import com.agmstech.princessgame.wardrobe.Catalog
import com.agmstech.princessgame.wardrobe.Category
import com.agmstech.princessgame.wardrobe.Palettes
import java.time.LocalDate
import kotlin.math.ceil

class MainActivity : Activity(), WardrobePanel.Listener {

    private lateinit var store: PrefsStore
    private lateinit var settings: ParentSettings
    private lateinit var lock: ParentLock
    private lateinit var tracker: PlayTimeTracker
    private lateinit var music: MusicPlayer
    private lateinit var sfx: SoundEffects

    private var state = GameState()

    private lateinit var root: FrameLayout
    private lateinit var stage: PrincessStageView
    private lateinit var panel: WardrobePanel
    private lateinit var ribbon: NameRibbonView
    private lateinit var pie: TimerPieView
    private var countdown: CountdownView? = null
    private var lockout: LockoutView? = null

    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false
    private var lastTick = 0L
    private var lastFlush = 0L
    private var lastCountdownSecond = -1

    /** True while a grown-up has the password or settings screen open; play time is paused. */
    private var parentOpen = false

    /** True once today's time has run out. */
    private var locked = false

    private val tick = object : Runnable {
        override fun run() {
            onTick()
            if (ticking) handler.postDelayed(this, TICK_MS)
        }
    }

    private val closeGame = Runnable {
        if (locked && !parentOpen) {
            saveEverything(sync = true)
            finishAndRemoveTask()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = PrefsStore(this)
        settings = ParentSettings(store)
        lock = ParentLock(store)
        tracker = PlayTimeTracker(store, { settings.dailyLimitMinutes }, { LocalDate.now().toString() })
        music = MusicPlayer(this)
        sfx = SoundEffects(this).also { it.enabled = settings.soundEffects }
        state = GameState.load(store)

        setupWindow()
        buildUi()
        render()
        if (tracker.isTimeUp()) lockOut(justOpened = true)
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        if (locked && !tracker.isTimeUp()) unlock() // a new day has started
        if (locked) {
            scheduleClose(REOPENED_CLOSE_MS)
        } else {
            music.resume()
            startTicking()
        }
    }

    override fun onPause() {
        stopTicking()
        handler.removeCallbacks(closeGame)
        saveEverything(sync = true)
        music.pause()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        music.release()
        sfx.release()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    // ---- Screen ----

    private fun buildUi() {
        root = FrameLayout(this)
        stage = PrincessStageView(this).apply { onPrincessTapped = { sfx.play(Sfx.JUMP) } }
        val stageBox = FrameLayout(this)
        stageBox.addView(stage, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        ribbon = NameRibbonView(this).apply {
            contentDescription = "Change the princess's name"
            setOnClickListener { onEditName() }
        }
        stageBox.addView(ribbon, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
            setMargins(dpi(10), dpi(8), dpi(70), 0)
        })

        val gear = RoundButton(this, "⚙️", 0x99FFFFFF.toInt()).apply {
            contentDescription = "Parent settings"
            setOnClickListener { openParentArea() }
        }
        stageBox.addView(gear, FrameLayout.LayoutParams(dpi(46), dpi(46), Gravity.TOP or Gravity.END).apply { setMargins(0, dpi(10), dpi(10), 0) })

        pie = TimerPieView(this).apply {
            contentDescription = "Play time left today"
            setOnClickListener { showTimeLeft() }
        }
        stageBox.addView(pie, FrameLayout.LayoutParams(dpi(40), dpi(40), Gravity.TOP or Gravity.END).apply { setMargins(0, dpi(64), dpi(13), 0) })

        val dice = RoundButton(this, "🎲").apply {
            contentDescription = "Surprise outfit"
            setOnClickListener { surprise() }
        }
        stageBox.addView(dice, FrameLayout.LayoutParams(dpi(58), dpi(58), Gravity.BOTTOM or Gravity.END).apply { setMargins(0, 0, dpi(10), dpi(10)) })

        panel = WardrobePanel(this, this)
        root.addView(GameLayout(this, stageBox, panel), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        root.setOnApplyWindowInsetsListener { v, insets ->
            val cut = if (Build.VERSION.SDK_INT >= 28) insets.displayCutout else null
            v.setPadding(cut?.safeInsetLeft ?: 0, cut?.safeInsetTop ?: 0, cut?.safeInsetRight ?: 0, cut?.safeInsetBottom ?: 0)
            insets
        }
        setContentView(root)
    }

    private fun setupWindow() {
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.also {
                it.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        // Android 15+ always draws edge to edge; earlier versions need to be asked.
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT in 30..34) window.setDecorFitsSystemWindows(false)
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let {
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                it.hide(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    private fun render() {
        stage.outfit = state.outfit
        stage.background = Backgrounds.byId(state.backgroundId)
        ribbon.name = state.name
        panel.bind(state)
        music.select(state.songId)
        pie.fraction = tracker.remainingMs().toFloat() / tracker.limitMs
    }

    /** Applies a change, shows it and saves it straight away. */
    private fun update(newState: GameState, celebrate: Boolean = true, sound: Sfx? = Sfx.SPARKLE) {
        if (locked) return
        state = newState
        render()
        state.save(store)
        if (celebrate) stage.celebrate()
        sound?.let { sfx.play(it) }
    }

    private fun saveEverything(sync: Boolean) {
        state.save(store, sync)
        tracker.flush(sync = sync, force = true)
    }

    // ---- Wardrobe ----

    override fun onTabSelected(category: Category) = update(state.copy(tab = category), celebrate = false, sound = Sfx.TAP)

    override fun onItemChosen(category: Category, itemId: String) =
        update(state.copy(outfit = state.outfit.choose(category, itemId)))

    override fun onColorChosen(category: Category, color: Int) =
        update(state.copy(outfit = state.outfit.recolor(category, color)))

    override fun onSkinChosen(color: Int) = update(state.copy(outfit = state.outfit.withSkin(color)))

    override fun onBackgroundChosen(id: String) = update(state.copy(backgroundId = id))

    override fun onSongChosen(id: String) = update(state.copy(songId = id), celebrate = false, sound = null)

    override fun onEditName() {
        sfx.play(Sfx.TAP)
        Dialogs.askName(this, state.name) { name ->
            update(state.copy(name = name), sound = Sfx.CHEER)
            hideSystemBars()
        }
    }

    /** Dice button: a random outfit. */
    private fun surprise() {
        var o = state.outfit
        for (cat in listOf(Category.HAIR, Category.CROWN, Category.SOCKS, Category.SHOES, Category.BAG, Category.HAND)) {
            val colors = if (cat == Category.HAIR) Palettes.hair else Palettes.clothes
            o = o.recolor(cat, colors.random()).choose(cat, Catalog.items(cat).random().id)
        }
        val body = listOf(Category.DRESS, Category.DRESS, Category.TOP, Category.SPORTS).random()
        val parts = if (body == Category.TOP) listOf(Category.PANTS, Category.TOP) else listOf(body)
        for (cat in parts) o = o.recolor(cat, Palettes.clothes.random()).choose(cat, Catalog.items(cat).random().id)
        stage.hop()
        update(state.copy(outfit = o), sound = Sfx.CHEER)
    }

    private fun showTimeLeft() {
        val left = tracker.remainingMs()
        val text = if (left >= 60_000L) "⏰ ${ceil(left / 60_000.0).toInt()} minutes of play left today"
        else "⏰ Almost time to stop!"
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    // ---- Play time ----

    private fun startTicking() {
        if (ticking || locked) return
        ticking = true
        lastTick = SystemClock.elapsedRealtime()
        handler.post(tick)
    }

    private fun stopTicking() {
        if (!ticking) return
        accumulate()
        ticking = false
        handler.removeCallbacks(tick)
        tracker.flush(sync = true)
    }

    private fun accumulate() {
        val now = SystemClock.elapsedRealtime()
        if (!parentOpen && !locked) tracker.addPlayed(now - lastTick)
        lastTick = now
    }

    private fun onTick() {
        accumulate()
        if (lastTick - lastFlush > FLUSH_MS) {
            tracker.flush()
            lastFlush = lastTick
        }
        val remaining = tracker.remainingMs()
        pie.fraction = remaining.toFloat() / tracker.limitMs
        when {
            locked || parentOpen -> hideCountdown()
            remaining <= 0L -> lockOut(justOpened = false)
            remaining <= PlayTimeTracker.COUNTDOWN_MS -> showCountdown(ceil(remaining / 1000.0).toInt())
            else -> hideCountdown()
        }
    }

    private fun showCountdown(seconds: Int) {
        val view = countdown ?: CountdownView(this).also {
            countdown = it
            root.addView(it, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
        if (seconds != lastCountdownSecond) {
            lastCountdownSecond = seconds
            view.seconds = seconds
            sfx.play(Sfx.TICK, 1f)
        }
    }

    private fun hideCountdown() {
        countdown?.let { root.removeView(it) }
        countdown = null
        lastCountdownSecond = -1
    }

    /** Time is up: save everything, say goodbye and close the game. */
    private fun lockOut(justOpened: Boolean) {
        if (locked) return
        locked = true
        stopTicking()
        hideCountdown()
        saveEverything(sync = true)
        music.pause()
        if (!justOpened) sfx.play(Sfx.GOODBYE)
        val view = LockoutView(this, state.name) { openParentArea() }
        lockout = view
        root.addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        scheduleClose(if (justOpened) REOPENED_CLOSE_MS else GOODBYE_CLOSE_MS)
    }

    private fun scheduleClose(ms: Long) {
        handler.removeCallbacks(closeGame)
        handler.postDelayed(closeGame, ms)
    }

    private fun unlock() {
        locked = false
        handler.removeCallbacks(closeGame)
        lockout?.let { root.removeView(it) }
        lockout = null
        render()
    }

    // ---- Grown-ups ----

    private fun openParentArea() {
        if (parentOpen) return
        parentOpen = true
        hideCountdown()
        Dialogs.askPassword(
            this, lock,
            onSuccess = {
                Dialogs.parentSettings(
                    this, settings, tracker, lock,
                    onChanged = {
                        sfx.enabled = settings.soundEffects
                        pie.fraction = tracker.remainingMs().toFloat() / tracker.limitMs
                    },
                    onClosed = { leaveParentArea() },
                )
            },
            onCancel = { leaveParentArea() },
        )
    }

    private fun leaveParentArea() {
        parentOpen = false
        lastTick = SystemClock.elapsedRealtime()
        hideSystemBars()
        if (locked) {
            if (!tracker.isTimeUp()) {
                unlock()
                music.resume()
                startTicking()
            } else {
                scheduleClose(GOODBYE_CLOSE_MS)
            }
        }
    }

    companion object {
        private const val TICK_MS = 250L
        private const val FLUSH_MS = 2_000L
        private const val GOODBYE_CLOSE_MS = 5_000L
        private const val REOPENED_CLOSE_MS = 10_000L
    }
}
