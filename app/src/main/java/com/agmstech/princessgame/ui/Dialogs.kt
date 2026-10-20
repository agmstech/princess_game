package com.agmstech.princessgame.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.agmstech.princessgame.core.GameState
import com.agmstech.princessgame.core.ParentLock
import com.agmstech.princessgame.core.ParentSettings
import com.agmstech.princessgame.core.PlayTimeTracker
import kotlin.math.ceil

/** The name picker for children and the password-protected parent screens. */
object Dialogs {

    private val nameIdeas = listOf("Lily", "Rosie", "Luna", "Mia", "Ava", "Zara", "Bella", "Sofia", "Ella", "Ruby", "Aria", "Maya")

    private fun Context.column(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dpi(24), dpi(20), dpi(24), dpi(8))
    }

    private fun Context.field(hint: String, password: Boolean): EditText = EditText(this).apply {
        this.hint = hint
        setTextSize(TypedValue.COMPLEX_UNIT_SP, if (password) 22f else 26f)
        setTextColor(Palette.PLUM)
        isSingleLine = true
        inputType = if (password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        imeOptions = EditorInfo.IME_ACTION_DONE
        background = rounded(Palette.WHITE, dp(14), Palette.PINK_BORDER, dpi(2))
        setPadding(dpi(16), dpi(12), dpi(16), dpi(12))
    }

    private fun LinearLayout.addSpaced(v: View, top: Int = 12) {
        addView(v, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = context.dpi(top)
        })
    }

    private fun builder(context: Context) = AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)

    /** Lets the child type a name or pick one of the suggestions. */
    fun askName(context: Context, current: String, onDone: (String) -> Unit) {
        val box = context.column()
        box.addView(context.label("What is your princess called?", 22f, bold = true))
        val input = context.field("Princess name", password = false).apply {
            setText(current)
            filters = arrayOf(InputFilter.LengthFilter(GameState.MAX_NAME_LENGTH))
            setSelection(text.length)
        }
        box.addSpaced(input, 16)
        val chips = FlowLayout(context)
        for (idea in nameIdeas) {
            chips.addView(context.pillButton(idea, fill = Palette.PINK_LIGHT, textColor = Palette.PLUM, sizeSp = 17f) {
                input.setText(idea)
                input.setSelection(input.text.length)
            })
        }
        box.addSpaced(chips, 16)

        val dialog = builder(context)
            .setView(ScrollView(context).apply { addView(box) })
            .setPositiveButton("OK ✔") { _, _ -> onDone(GameState.cleanName(input.text.toString())) }
            .setNegativeButton("Cancel", null)
            .create()
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                true
            } else false
        }
        dialog.show()
    }

    /**
     * Asks for the parent password, or asks the parent to create one the first time.
     * [onSuccess] runs only after a correct (or newly created) password.
     */
    fun askPassword(context: Context, lock: ParentLock, onSuccess: () -> Unit, onCancel: () -> Unit = {}) {
        if (!lock.hasPassword()) {
            createPassword(context, lock, "Create a parent password", onSuccess, onCancel)
            return
        }
        val box = context.column()
        box.addView(context.label("🔒 Grown-ups only", 22f, bold = true))
        box.addSpaced(context.label("Enter the parent password to change play time and settings.", 15f))
        val input = context.field("Password", password = true)
        box.addSpaced(input, 16)
        val error = context.label("", 15f, Palette.RED).apply { visibility = View.GONE }
        box.addSpaced(error, 8)
        box.addSpaced(context.label("Forgot it? Clear this app's storage in Android Settings → Apps. That also resets the game.", 12f, 0xFF8A6A86.toInt()), 16)

        var ok = false
        val dialog = builder(context)
            .setView(box)
            .setPositiveButton("Unlock", null)
            .setNegativeButton("Cancel", null)
            .setOnDismissListener { if (!ok) onCancel() }
            .create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                if (lock.verify(input.text.toString())) {
                    ok = true
                    dialog.dismiss()
                    onSuccess()
                } else {
                    error.text = "That password is not right. Please try again."
                    error.visibility = View.VISIBLE
                    input.setText("")
                }
            }
        }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                true
            } else false
        }
        dialog.show()
    }

    private fun createPassword(context: Context, lock: ParentLock, title: String, onSuccess: () -> Unit, onCancel: () -> Unit) {
        val box = context.column()
        box.addView(context.label("🔒 $title", 22f, bold = true))
        box.addSpaced(context.label("Only grown-ups who know this password can change the daily play time. Use at least ${ParentLock.MIN_LENGTH} characters.", 15f))
        val first = context.field("New password", password = true)
        val second = context.field("Type it again", password = true)
        box.addSpaced(first, 16)
        box.addSpaced(second, 10)
        val error = context.label("", 15f, Palette.RED).apply { visibility = View.GONE }
        box.addSpaced(error, 8)

        var ok = false
        val dialog = builder(context)
            .setView(box)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .setOnDismissListener { if (!ok) onCancel() }
            .create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val a = first.text.toString()
                val b = second.text.toString()
                val problem = when {
                    !ParentLock.isAcceptable(a) -> "Please use at least ${ParentLock.MIN_LENGTH} characters."
                    a != b -> "The two passwords are different."
                    else -> null
                }
                if (problem != null) {
                    error.text = problem
                    error.visibility = View.VISIBLE
                } else {
                    lock.setPassword(a)
                    ok = true
                    dialog.dismiss()
                    onSuccess()
                }
            }
        }
        dialog.show()
    }

    private val limitSteps = listOf(1, 2, 3, 5, 10, 15, 20, 25, 30, 40, 45, 50, 60, 75, 90, 105, 120, 150, 180, 210, 240)

    private fun minutes(n: Int) = if (n == 1) "1 minute" else "$n minutes"

    private fun timeLeft(ms: Long): String = when {
        ms <= 0L -> "none"
        ms < 60_000L -> "${ceil(ms / 1000.0).toInt()} seconds"
        else -> minutes(ceil(ms / 60_000.0).toInt())
    }

    /** The parent settings screen (only shown after the password check). */
    fun parentSettings(
        context: Context,
        settings: ParentSettings,
        tracker: PlayTimeTracker,
        lock: ParentLock,
        onChanged: () -> Unit,
        onClosed: () -> Unit,
    ) {
        val box = context.column()
        box.addView(context.label("Parent settings", 24f, bold = true))

        box.addSpaced(context.label("Daily play time", 18f, Palette.PURPLE, bold = true), 18)
        val value = context.label("", 26f, Palette.PLUM, bold = true).apply { gravity = Gravity.CENTER }
        val status = context.label("", 15f)

        fun update() {
            value.text = minutes(settings.dailyLimitMinutes)
            status.text = "Played today: ${timeLeft(tracker.usedTodayMs())}\nTime left today: ${timeLeft(tracker.remainingMs())}"
        }

        fun setLimit(m: Int) {
            settings.dailyLimitMinutes = m
            update()
            onChanged()
        }

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(context.pillButton("−", sizeSp = 26f) {
                val cur = settings.dailyLimitMinutes
                setLimit(limitSteps.lastOrNull { it < cur } ?: limitSteps.first())
            }, LinearLayout.LayoutParams(context.dpi(64), context.dpi(56)))
            addView(value, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(context.pillButton("+", sizeSp = 26f) {
                val cur = settings.dailyLimitMinutes
                setLimit(limitSteps.firstOrNull { it > cur } ?: limitSteps.last())
            }, LinearLayout.LayoutParams(context.dpi(64), context.dpi(56)))
        }
        box.addSpaced(row, 8)

        val presets = FlowLayout(context)
        for (m in listOf(10, 15, 20, 30, 45, 60, 90, 120)) {
            presets.addView(context.pillButton("$m min", fill = Palette.PINK_LIGHT, textColor = Palette.PLUM, sizeSp = 15f) { setLimit(m) })
        }
        box.addSpaced(presets, 10)
        box.addSpaced(status, 14)

        val actions = FlowLayout(context)
        actions.addView(context.pillButton("+5 minutes today", fill = Palette.PURPLE, sizeSp = 15f) {
            tracker.giveExtra(5 * 60_000L)
            update()
            onChanged()
        })
        actions.addView(context.pillButton("Restart today's time", fill = Palette.PURPLE, sizeSp = 15f) {
            tracker.resetToday()
            update()
            onChanged()
        })
        box.addSpaced(actions, 12)

        val sfx = Switch(context).apply {
            text = "Sound effects"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setTextColor(Palette.PLUM)
            isChecked = settings.soundEffects
            setOnCheckedChangeListener { _, on ->
                settings.soundEffects = on
                onChanged()
            }
        }
        box.addSpaced(sfx, 18)

        val change = context.pillButton("Change password", fill = Palette.PINK_LIGHT, textColor = Palette.PLUM, sizeSp = 15f) {
            createPassword(context, lock, "Choose a new password", onSuccess = {}, onCancel = {})
        }
        box.addSpaced(change, 14)

        box.addSpaced(context.label(
            "Play time is only counted while the game is open. Ten seconds before the time runs out, a red " +
                "countdown flashes; then the game saves everything and closes until tomorrow.",
            13f, 0xFF8A6A86.toInt(),
        ), 18)

        update()
        builder(context)
            .setView(ScrollView(context).apply { addView(box) })
            .setPositiveButton("Done", null)
            .setOnDismissListener { onClosed() }
            .create()
            .show()
    }
}
