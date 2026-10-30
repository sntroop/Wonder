package su.gwarden.music.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import su.gwarden.music.R
import su.gwarden.music.audio.Fx
import su.gwarden.music.player.PlayerController
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Шторка «Звук» из полноэкранного плеера.
 *
 * 10-полосный эквалайзер с пресетами, усиление басов, реверб, дистошн,
 * темп, тон и кроссфейд. Настройки применяются на лету — перезапускать
 * трек не нужно.
 */
object FxSheet {

    private fun dp(ctx: Context, v: Int): Int =
        (v * ctx.resources.displayMetrics.density).roundToInt()

    private fun bold(t: TextView): TextView {
        t.setTypeface(t.typeface, Typeface.BOLD)
        return t
    }

    fun show(activity: Activity, player: PlayerController? = null) {
        Fx.load(activity)
        val pl = player ?: findPlayer(activity)

        val dialog = Dialog(activity, R.style.Theme_Wonder_Sheet)
        Skin.prepare(dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dm = activity.resources.displayMetrics
        val sheetH = (dm.heightPixels * 0.92f).toInt()

        val root = FrameLayout(activity)
        root.setBackgroundColor(0xB3000000.toInt())
        root.setOnClickListener { dialog.dismiss() }

        val sheet = LinearLayout(activity)
        sheet.orientation = LinearLayout.VERTICAL
        sheet.setBackgroundResource(R.drawable.bg_sheet)
        sheet.setOnClickListener { }
        root.addView(sheet, FrameLayout.LayoutParams(-1, sheetH).apply {
            gravity = Gravity.BOTTOM
        })

        // ── шапка ─────────────────────────────────────────────────────
        val head = LinearLayout(activity)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        head.setPadding(dp(activity, 20), dp(activity, 18), dp(activity, 12), dp(activity, 8))

        val title = TextView(activity)
        title.text = "Звук"
        title.setTextColor(Color.WHITE)
        title.setTextSize(22f)
        bold(title)
        head.addView(title, LinearLayout.LayoutParams(0, -2, 1f))

        val close = TextView(activity)
        close.text = "Готово"
        close.setTextColor(Skin.accent)
        close.setTextSize(14f)
        bold(close)
        close.setPadding(dp(activity, 14), dp(activity, 8), dp(activity, 14), dp(activity, 8))
        close.setOnClickListener { dialog.dismiss() }
        head.addView(close)
        sheet.addView(head)

        val scroll = ScrollView(activity)
        scroll.isFillViewport = true
        val box = LinearLayout(activity)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(activity, 18), 0, dp(activity, 18), dp(activity, 26))
        scroll.addView(box, ViewGroup.LayoutParams(-1, -2))
        sheet.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // ── выключатель ───────────────────────────────────────────────
        val powerRow = LinearLayout(activity)
        powerRow.orientation = LinearLayout.HORIZONTAL
        powerRow.gravity = Gravity.CENTER_VERTICAL
        powerRow.setPadding(0, dp(activity, 6), 0, dp(activity, 6))

        val powerText = LinearLayout(activity)
        powerText.orientation = LinearLayout.VERTICAL
        powerText.addView(label(activity, "Обработка звука", 15f, Color.WHITE, true))
        val powerHint = label(activity, "", 11f, 0xFF8A8A8A.toInt())
        powerText.addView(powerHint)
        powerRow.addView(powerText, LinearLayout.LayoutParams(0, -2, 1f))

        fun syncPowerHint() {
            powerHint.text = if (Fx.settings.enabled) "включено — эквалайзер и эффекты работают" else "выключено — звук как на источнике"
        }
        syncPowerHint()

        val sw = android.widget.Switch(activity)
        sw.isChecked = Fx.settings.enabled
        sw.thumbTintList = ColorStateList.valueOf(Skin.accent)
        sw.trackTintList = ColorStateList.valueOf(Skin.withAlpha(Skin.accent, 0x66))
        sw.setOnCheckedChangeListener { _, checked ->
            Fx.update(activity) { it.enabled = checked }
            syncPowerHint()
            pl?.refreshFx()
        }
        powerRow.addView(sw)
        // вся строка — большая цель для пальца, а не только переключатель
        powerRow.setOnClickListener {
            sw.isChecked = !sw.isChecked
        }
        box.addView(powerRow)

        // ── пресеты ───────────────────────────────────────────────────
        box.addView(section(activity, "ПРЕСЕТЫ"))
        val chipsScroll = HorizontalScrollView(activity)
        chipsScroll.isHorizontalScrollBarEnabled = false
        val chips = LinearLayout(activity)
        chips.orientation = LinearLayout.HORIZONTAL
        chips.setPadding(0, dp(activity, 2), 0, dp(activity, 2))
        chipsScroll.addView(chips)
        box.addView(chipsScroll)

        // ── эквалайзер ────────────────────────────────────────────────
        box.addView(section(activity, "ЭКВАЛАЙЗЕР · 10 ПОЛОС"))

        val eq = EqView(activity)
        eq.accent = Skin.accent
        eq.bands = Fx.settings.bands.copyOf()
        val eqLp = LinearLayout.LayoutParams(-1, -2)
        eqLp.topMargin = dp(activity, 4)
        box.addView(eq, eqLp)

        val sliders = LinearLayout(activity)
        sliders.orientation = LinearLayout.VERTICAL
        box.addView(sliders)

        var rebuildSliders: () -> Unit = {}

        eq.onBandChanged = { i, db ->
            if (i >= 0) {
                Fx.update(activity) { it.bands[i] = db }
                pl?.refreshFx()
            }
        }

        fun refreshChips() {
            chips.removeAllViews()
            val all = Fx.presets + Fx.customPresets(activity)
            for (p in all) {
                chips.addView(presetChip(activity, p.name) {
                    Fx.applyPreset(activity, p)
                    eq.bands = Fx.settings.bands.copyOf()
                    pl?.refreshFx()
                    rebuildSliders()
                })
            }
        }

        fun slider(
            titleText: String,
            value: () -> Float,
            min: Float,
            max: Float,
            step: Float,
            fmt: (Float) -> String,
            onChange: (Float) -> Unit,
        ) {
            val row = LinearLayout(activity)
            row.orientation = LinearLayout.VERTICAL
            row.setPadding(0, dp(activity, 12), 0, 0)

            val headRow = LinearLayout(activity)
            headRow.orientation = LinearLayout.HORIZONTAL
            headRow.gravity = Gravity.CENTER_VERTICAL
            val titleTx = label(activity, titleText, 13.5f, 0xFFDADADA.toInt(), true)
            titleTx.maxLines = 1
            titleTx.ellipsize = android.text.TextUtils.TruncateAt.END
            headRow.addView(titleTx, LinearLayout.LayoutParams(0, -2, 1f))
            val valTx = label(activity, fmt(value()), 13f, Skin.accent, true)
            valTx.maxLines = 1
            val vlp = LinearLayout.LayoutParams(-2, -2)
            vlp.marginStart = dp(activity, 12)
            headRow.addView(valTx, vlp)
            row.addView(headRow)

            val sb = SeekBar(activity)
            sb.max = ((max - min) / step).roundToInt().coerceAtLeast(1)
            sb.progress = (((value() - min) / step).roundToInt()).coerceIn(0, sb.max)
            sb.progressTintList = ColorStateList.valueOf(Skin.accent)
            sb.thumbTintList = ColorStateList.valueOf(Skin.accent)
            sb.setPadding(0, dp(activity, 4), 0, 0)
            sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    val v = (min + progress * step).coerceIn(min, max)
                    valTx.text = fmt(v)
                    onChange(v)
                }

                override fun onStartTrackingTouch(bar: SeekBar?) = Unit

                override fun onStopTrackingTouch(bar: SeekBar?) = Unit
            })
            row.addView(sb, LinearLayout.LayoutParams(-1, -2))
            sliders.addView(row)
        }

        rebuildSliders = {
            sliders.removeAllViews()
            slider("Предусиление", { Fx.settings.preamp }, -12f, 12f, 0.5f, { fmtDb(it) }) {
                Fx.update(activity) { st -> st.preamp = it }
                pl?.refreshFx()
            }
            slider("Басы", { Fx.settings.bass }, 0f, 15f, 0.5f,
                { "+" + String.format(Locale.US, "%.1f", it) + " dB" }) {
                Fx.update(activity) { st -> st.bass = it }
                pl?.refreshFx()
            }
            slider("Реверб", { Fx.settings.reverbMix }, 0f, 1f, 0.02f,
                { "${(it * 100).roundToInt()}%" }) {
                Fx.update(activity) { st -> st.reverbMix = it }
                pl?.refreshFx()
            }
            slider("Размер зала", { Fx.settings.reverbSize }, 0f, 1f, 0.02f,
                { "${(it * 100).roundToInt()}%" }) {
                Fx.update(activity) { st -> st.reverbSize = it }
                pl?.refreshFx()
            }
            slider("Искажение", { Fx.settings.distDrive }, 0f, 1f, 0.02f,
                { "${(it * 100).roundToInt()}%" }) {
                Fx.update(activity) { st -> st.distDrive = it }
                pl?.refreshFx()
            }
            slider("Тон искажения", { Fx.settings.distTone }, 0f, 1f, 0.02f,
                {
                    if (it < 0.5f) "тёмный ${(it * 200).roundToInt()}%"
                    else "яркий ${((it - 0.5f) * 200).roundToInt()}%"
                }) {
                Fx.update(activity) { st -> st.distTone = it }
                pl?.refreshFx()
            }
            slider("Темп", { Fx.settings.tempo }, 0.5f, 2f, 0.01f,
                { "${(it * 100).roundToInt()}%" }) {
                Fx.update(activity) { st -> st.tempo = it }
                pl?.refreshFx()
            }
            slider("Высота тона", { Fx.settings.semitones }, -12f, 12f, 1f,
                { (if (it > 0) "+" else "") + it.roundToInt() + " пт" }) {
                Fx.update(activity) { st -> st.semitones = it }
                pl?.refreshFx()
            }
            slider("Кроссфейд", { Fx.settings.crossfade.toFloat() }, 0f, 12f, 1f,
                { if (it < 0.5f) "выкл" else "${it.roundToInt()} с" }) {
                Fx.update(activity) { st -> st.crossfade = it.roundToInt() }
                pl?.refreshFx()
            }
        }
        rebuildSliders()

        // ── пользовательские пресеты ──────────────────────────────────
        box.addView(section(activity, "ПРЕСЕТЫ ПОЛЬЗОВАТЕЛЯ"))
        val saveRow = LinearLayout(activity)
        saveRow.orientation = LinearLayout.HORIZONTAL
        for (slot in 1..3) {
            val exists = Fx.customPresets(activity).any { it.id == "custom_$slot" }
            val b = smallButton(activity, if (exists) "Слот $slot ↻" else "В слот $slot")
            b.setOnClickListener {
                Fx.saveCustom(activity, slot, "Мой $slot")
                refreshChips()
                Toast.makeText(activity, "Сохранено в слот $slot", Toast.LENGTH_SHORT).show()
            }
            val lp = LinearLayout.LayoutParams(0, dp(activity, 42), 1f)
            lp.marginEnd = dp(activity, 6)
            saveRow.addView(b, lp)
        }
        box.addView(saveRow)

        val reset = smallButton(activity, "Сбросить всё")
        reset.setTextColor(0xFFFF6B6B.toInt())
        val rlp = LinearLayout.LayoutParams(-1, dp(activity, 44))
        rlp.topMargin = dp(activity, 14)
        reset.setOnClickListener {
            Fx.resetAll(activity)
            eq.bands = FloatArray(Fx.BANDS.size)
            pl?.refreshFx()
            rebuildSliders()
            refreshChips()
            sw.isChecked = false
            Toast.makeText(activity, "Настройки звука сброшены", Toast.LENGTH_SHORT).show()
        }
        box.addView(reset, rlp)

        refreshChips()

        val hint = label(
            activity,
            "Настройки хранятся на телефоне и применяются сразу ко всем трекам.",
            11f, 0xFF6E6E6E.toInt(),
        )
        hint.setPadding(0, dp(activity, 16), 0, 0)
        box.addView(hint)

        dialog.setContentView(root)
        dialog.show()
        runCatching {
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
            )
            Skin.installDialogBackground(dialog)
            Skin.applyAccent(dialog.window?.decorView)
        }
    }

    private fun findPlayer(activity: Activity): PlayerController? = runCatching {
        val f = activity.javaClass.getDeclaredField("player")
        f.isAccessible = true
        f.get(activity) as? PlayerController
    }.getOrNull()

    private fun label(ctx: Context, text: String, size: Float, color: Int, isBold: Boolean = false): TextView {
        val t = TextView(ctx)
        t.text = text
        t.setTextColor(color)
        t.setTextSize(size)
        if (isBold) t.setTypeface(t.typeface, Typeface.BOLD)
        return t
    }

    private fun section(ctx: Context, text: String): TextView {
        val t = label(ctx, text, 10.5f, 0xFF6E6E6E.toInt(), true)
        t.letterSpacing = 0.09f
        t.setPadding(0, dp(ctx, 22), 0, dp(ctx, 10))
        return t
    }

    private fun presetChip(ctx: Context, name: String, click: () -> Unit): TextView {
        val t = TextView(ctx)
        t.text = name
        t.setTextSize(12.5f)
        t.setTypeface(t.typeface, Typeface.BOLD)
        t.setTextColor(Color.WHITE)
        t.setPadding(dp(ctx, 14), dp(ctx, 9), dp(ctx, 14), dp(ctx, 9))
        t.maxLines = 1
        t.ellipsize = android.text.TextUtils.TruncateAt.END
        t.setBackgroundResource(R.drawable.bg_chip)
        t.backgroundTintList = ColorStateList.valueOf(Skin.accent)
        t.setOnClickListener { click() }
        val lp = LinearLayout.LayoutParams(-2, -2)
        lp.marginEnd = dp(ctx, 8)
        t.layoutParams = lp
        return t
    }

    private fun smallButton(ctx: Context, text: String): TextView {
        val t = TextView(ctx)
        t.text = text
        t.gravity = Gravity.CENTER
        t.setTextSize(10.5f)
        t.maxLines = 1
        t.ellipsize = android.text.TextUtils.TruncateAt.END
        t.setPadding(dp(ctx, 4), 0, dp(ctx, 4), 0)
        t.setTypeface(t.typeface, Typeface.BOLD)
        t.setTextColor(Color.WHITE)
        t.setBackgroundResource(R.drawable.bg_ripple_14)
        t.isClickable = true
        t.isFocusable = true
        return t
    }

    private fun fmtDb(v: Float): String =
        (if (v > 0.05f) "+" else "") + String.format(Locale.US, "%.1f", v) + " dB"

    @Suppress("unused")
    private fun unusedFontRef(ctx: Context) = ResourcesCompat.getFont(ctx, R.font.unbounded_family)
}
