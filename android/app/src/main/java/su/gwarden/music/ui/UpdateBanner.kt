package su.gwarden.music.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import su.gwarden.music.R
import su.gwarden.music.update.UpdateChecker
import su.gwarden.music.update.UpdateInfo
import java.io.File
import kotlin.math.roundToInt

/**
 * Баннер обновления сверху главной: «Обновите Wonder до версии X».
 *
 * Свайп вверх — «не сейчас» (запоминаем версию, больше не мозолим глаза).
 * Тап — скачивание со спиннером и процентами, затем системный установщик.
 * После установки приложение открывается само (UpdateReceiver).
 */
object UpdateBanner {

    private var shownVersion = 0
    private var pending: File? = null
    private var busy = false
    private var lastCheckAt = 0L

    private fun dp(ctx: Activity, v: Int): Int =
        (v * ctx.resources.displayMetrics.density).roundToInt()

    fun attach(activity: Activity) {
        if (shownVersion != 0) return
        lastCheckAt = android.os.SystemClock.uptimeMillis()
        val appRoot = activity.findViewById<LinearLayout>(R.id.app_root) ?: return
        UpdateChecker.check(activity, force = true) { info ->
            if (info == null) return@check
            activity.runOnUiThread { show(activity, appRoot, info) }
        }
    }

    /**
     * Проверка при возврате в приложение (onResume), не чаще раза в минуту.
     * Раньше обновление искали только при холодном старте — если приложение
     * просто свернули и открыли, баннер не появлялся.
     */
    fun onResume(activity: Activity) {
        if (shownVersion != 0) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastCheckAt < 60_000) return
        lastCheckAt = now
        val appRoot = activity.findViewById<LinearLayout>(R.id.app_root) ?: return
        UpdateChecker.check(activity, force = true) { info ->
            if (info != null) activity.runOnUiThread { show(activity, appRoot, info) }
        }
    }

    /** Тихая проверка (pull-to-refresh), без тостов. */
    fun checkSilent(activity: Activity) {
        if (shownVersion != 0) return
        val appRoot = activity.findViewById<LinearLayout>(R.id.app_root) ?: return
        UpdateChecker.check(activity, force = true) { info ->
            if (info != null) activity.runOnUiThread { show(activity, appRoot, info) }
        }
    }

    /** Ручная проверка из настроек. */
    fun checkNow(activity: Activity) {
        val appRoot = activity.findViewById<LinearLayout>(R.id.app_root) ?: return
        shownVersion = 0
        Toast.makeText(activity, "Проверяю обновления…", Toast.LENGTH_SHORT).show()
        UpdateChecker.check(activity, force = true) { info ->
            activity.runOnUiThread {
                if (info == null) {
                    Toast.makeText(activity, "Обновлений нет", Toast.LENGTH_SHORT).show()
                } else {
                    show(activity, appRoot, info)
                }
            }
        }
    }

    private fun show(activity: Activity, appRoot: LinearLayout, info: UpdateInfo) {
        if (shownVersion == info.versionCode) return
        shownVersion = info.versionCode
        busy = false
        pending = null

        val accent = Skin.accent
        val card = LinearLayout(activity)
        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setPadding(dp(activity, 14), dp(activity, 11), dp(activity, 10), dp(activity, 11))
        card.background = GradientDrawable().apply {
            cornerRadius = dp(activity, 18).toFloat()
            setColor(0xF2141416.toInt())
            setStroke(dp(activity, 1), Skin.withAlpha(accent, 0x66))
        }

        val icon = ImageView(activity)
        icon.setImageResource(R.drawable.ic_download)
        icon.setColorFilter(accent)
        icon.layoutParams = LinearLayout.LayoutParams(dp(activity, 24), dp(activity, 24)).apply {
            marginEnd = dp(activity, 12)
        }
        card.addView(icon)

        val col = LinearLayout(activity)
        col.orientation = LinearLayout.VERTICAL
        val title = TextView(activity)
        title.text = "Обновите Wonder до версии ${info.versionName}"
        title.setTextColor(Color.WHITE)
        title.setTextSize(13.5f)
        title.setTypeface(title.typeface, Typeface.BOLD)
        title.maxLines = 1
        title.ellipsize = android.text.TextUtils.TruncateAt.END
        col.addView(title)

        val sub = TextView(activity)
        sub.text = buildString {
            append(if (info.notes.isBlank()) "Нажмите, чтобы скачать" else info.notes)
            append(" · нажмите, чтобы скачать")
        }
        sub.setTextColor(0xFF8A8A8A.toInt())
        sub.setTextSize(11f)
        sub.maxLines = 2
        sub.ellipsize = android.text.TextUtils.TruncateAt.END
        sub.setPadding(0, dp(activity, 2), 0, 0)
        col.addView(sub)

        card.addView(col, LinearLayout.LayoutParams(0, -2, 1f))

        val close = ImageView(activity)
        close.setImageResource(R.drawable.ic_close)
        close.setColorFilter(0xFF8A8A8A.toInt())
        close.setPadding(dp(activity, 8), dp(activity, 8), dp(activity, 8), dp(activity, 8))
        close.layoutParams = LinearLayout.LayoutParams(dp(activity, 32), dp(activity, 32))
        close.setOnClickListener {
            skip(activity, appRoot, card, info)
        }
        card.addView(close)

        val lp = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(dp(activity, 12), dp(activity, 6), dp(activity, 12), 0)
        }
        appRoot.addView(card, 0, lp)

        // появление сверху
        card.translationY = -dp(activity, 60).toFloat()
        card.alpha = 0f
        card.animate().translationY(0f).alpha(1f).setDuration(260L).start()

        var downY = 0f
        var dragging = false

        fun setSub(text: String) {
            sub.text = text
        }

        fun startDownload() {
            if (busy) return
            val ready = pending
            if (ready != null && ready.exists()) {
                install(activity, ready, sub)
                return
            }
            busy = true
            icon.visibility = View.GONE
            val spin = ProgressBar(activity, null, android.R.attr.progressBarStyleSmall)
            spin.isIndeterminate = false
            spin.max = 100
            spin.progressTintList = android.content.res.ColorStateList.valueOf(accent)
            spin.layoutParams = LinearLayout.LayoutParams(dp(activity, 26), dp(activity, 26)).apply {
                marginEnd = dp(activity, 10)
            }
            card.addView(spin, 0)
            setSub("Скачивание… 0 %")
            UpdateChecker.download(
                activity,
                info,
                onProgress = { pct ->
                    spin.progress = pct
                    setSub("Скачивание… $pct %")
                },
                onDone = { file ->
                    busy = false
                    icon.visibility = View.VISIBLE
                    card.removeView(spin)
                    if (file == null) {
                        setSub("Не удалось скачать · нажмите, чтобы повторить")
                    } else {
                        pending = file
                        install(activity, file, sub)
                    }
                },
            )
        }

        card.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = e.rawY
                    dragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = e.rawY - downY
                    if (dy < 0f) {
                        dragging = true
                        v.translationY = dy
                        v.alpha = (1f + dy / dp(activity, 90)).coerceIn(0.25f, 1f)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dy = v.translationY
                    if (dragging && dy < -dp(activity, 28)) {
                        skip(activity, appRoot, card, info)
                    } else {
                        v.animate().translationY(0f).alpha(1f).setDuration(150L).start()
                        if (!dragging) startDownload()
                    }
                    dragging = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().translationY(0f).alpha(1f).setDuration(150L).start()
                    true
                }
                else -> false
            }
        }
    }

    private fun install(activity: Activity, file: File, sub: TextView) {
        if (!UpdateChecker.canInstall(activity)) {
            sub.text = "Разрешите установку из этого источника и нажмите ещё раз"
            Toast.makeText(activity, "Разрешите установку APK для Wonder", Toast.LENGTH_LONG).show()
            UpdateChecker.openInstallSettings(activity)
            return
        }
        sub.text = "Открываю установщик…"
        runCatching { UpdateChecker.install(activity, file) }
            .onFailure {
                sub.text = "Не удалось открыть установщик"
                Toast.makeText(activity, "Нет установщика APK", Toast.LENGTH_LONG).show()
            }
    }

    private fun skip(activity: Activity, appRoot: LinearLayout, card: View, info: UpdateInfo) {
        UpdateChecker.prefs(activity).edit().putInt("skipped", info.versionCode).apply()
        card.animate().translationY(-dp(activity, 80).toFloat()).alpha(0f)
            .setDuration(200L)
            .withEndAction {
                runCatching { appRoot.removeView(card) }
                shownVersion = 0
            }
            .start()
    }
}
