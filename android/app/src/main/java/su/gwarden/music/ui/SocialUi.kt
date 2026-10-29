package su.gwarden.music.ui

import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import su.gwarden.music.R
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.Track
import su.gwarden.music.offline.Offline
import su.gwarden.music.player.PlayerController

/**
 * Соцслой в плеере: лайк, комментарии по таймкодам (как в SoundCloud),
 * синхронный текст песни и офлайн-скачивание.
 *
 * Всё строится кодом, чтобы не плодить layout-файлы: шторки в стиле
 * приложения (bg_sheet, монохром), шрифт Unbounded.
 */
object SocialUi {

    private fun dp(activity: Activity, v: Int): Int =
        (v * activity.resources.displayMetrics.density).toInt()

    private fun font(activity: Activity, bold: Boolean = false): Typeface? {
        val regular = runCatching {
            ResourcesCompat.getFont(activity, R.font.unbounded_family)
        }.getOrNull()
        return if (bold) Typeface.create(regular, Typeface.BOLD) else regular
    }

    private fun color(activity: Activity, id: Int): Int =
        ContextCompat.getColor(activity, id)

    fun time(ms: Long): String {
        val s = (ms / 1000).coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }

    /** Привязать кнопки плеера: текст песни и офлайн-скачивание (иконки). */
    fun bind(
        activity: Activity,
        dialog: Dialog,
        track: Track,
        player: PlayerController?,
        toast: (String) -> Unit,
    ) {
        val lyricsBtn = dialog.findViewById<View>(R.id.now_lyrics)
        val download = dialog.findViewById<View>(R.id.now_download)
        val downloadIc = dialog.findViewById<android.widget.ImageView>(R.id.now_download_ic)
        val downloadTx = dialog.findViewById<TextView>(R.id.now_download_tx)

        fun refreshDownload() {
            val done = Offline.isDownloaded(track)
            downloadIc?.setImageResource(
                if (done) R.drawable.ic_downloaded else R.drawable.ic_download
            )
            downloadIc?.imageTintList = android.content.res.ColorStateList.valueOf(
                color(activity, if (done) R.color.text_primary else R.color.text_secondary)
            )
            downloadTx?.text = if (done) "Скачано" else "Скачать"
            downloadIc?.contentDescription = if (done) "Скачано" else "Скачать"
        }
        refreshDownload()

        lyricsBtn?.setOnClickListener { toggleLyrics(activity, dialog, player, toast) }

        download?.setOnClickListener {
            if (Offline.isDownloaded(track)) {
                Offline.remove(track)
                refreshDownload()
                toast("Удалено с телефона")
                return@setOnClickListener
            }
            download.isEnabled = false
            toast("Качаю…")
            Offline.download(activity, track) { ok, msg ->
                download.isEnabled = true
                refreshDownload()
                toast(if (ok) "Скачано — играет без интернета" else "Не скачалось: $msg")
            }
        }

        // поделиться: ссылка открывается в браузере у друга (обложка + кнопка)
        val share = dialog.findViewById<android.widget.ImageButton>(R.id.now_share)
        share?.setOnClickListener {
            val url = ApiClient.baseUrl + "/s/" + track.source + "/" + track.id
            val text = "${track.artist} — ${track.title}\nСлушаю в Wonder: $url"
            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, text)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "${track.artist} — ${track.title}")
            }
            runCatching {
                activity.startActivity(
                    android.content.Intent.createChooser(send, "Поделиться")
                )
            }
        }
    }

    // ------------------------------------------------------------ шторки

    private fun sheet(
        activity: Activity,
        title: String,
        heightPct: Float,
        fill: (LinearLayout, Dialog) -> Unit,
    ): Dialog {
        val d = Dialog(activity, R.style.Theme_Wonder_Sheet)
        d.setCanceledOnTouchOutside(true)
        val w: Window? = d.window
        w?.setBackgroundDrawable(ColorDrawable(0))
        w?.setGravity(Gravity.BOTTOM)
        w?.setLayout(-1, -1)
        w?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        w?.attributes?.let { lp ->
            lp.dimAmount = 0.62f
            w.attributes = lp
        }

        val col = LinearLayout(activity)
        col.orientation = LinearLayout.VERTICAL
        col.setBackgroundResource(R.drawable.bg_sheet)
        col.setPadding(dp(activity, 20), dp(activity, 6), dp(activity, 20), dp(activity, 16))
        col.isClickable = true

        val grip = View(activity)
        grip.setBackgroundColor(0x26FFFFFF.toInt())
        val glp = LinearLayout.LayoutParams(dp(activity, 38), dp(activity, 4))
        glp.topMargin = dp(activity, 10)
        glp.bottomMargin = dp(activity, 12)
        glp.gravity = Gravity.CENTER_HORIZONTAL
        grip.layoutParams = glp
        col.addView(grip)

        val header = LinearLayout(activity)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL

        val tv = TextView(activity)
        tv.text = title
        tv.setTextColor(color(activity, R.color.text_primary))
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        tv.typeface = font(activity, true)
        tv.layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        header.addView(tv)

        val close = TextView(activity)
        close.text = "Закрыть"
        close.setTextColor(color(activity, R.color.text_tertiary))
        close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        close.typeface = font(activity)
        close.setPadding(dp(activity, 10), dp(activity, 8), 0, dp(activity, 8))
        close.setOnClickListener { d.dismiss() }
        header.addView(close)
        col.addView(header)

        fill(col, d)

        val root = FrameLayout(activity)
        root.setBackgroundColor(0)
        root.setOnClickListener { d.dismiss() }
        // ВАЖНО: col добавляется во FrameLayout, значит и LayoutParams
        // должны быть FrameLayout.LayoutParams. Иначе Activity падает:
        // LinearLayout$LayoutParams cannot be cast to FrameLayout$LayoutParams
        root.addView(col, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        col.post {
            val h = (activity.resources.displayMetrics.heightPixels * heightPct).toInt()
            col.layoutParams = FrameLayout.LayoutParams(-1, h, Gravity.BOTTOM)
        }
        d.setContentView(root)
        return d
    }

    // -------------------------------------------------------- комментарии
    // Как в SoundCloud: комментарий ставится НА ТАЙМКОД трека. В шортсах он
    // всплывает внизу («0:21 @sntroop: мясо»), когда проигрывание доходит до
    // этого места. Здесь - только одно поле: написал и отправил.
    fun showCommentInput(
        activity: Activity,
        track: Track,
        player: PlayerController?,
        toast: (String) -> Unit,
    ) {
        val pos = (player?.position ?: 0).coerceAtLeast(0)
        val d = sheet(activity, "Комментарий на " + time(pos.toLong()), 0.34f) { col, dlg ->
            val input = EditText(activity)
            input.hint = "Что здесь происходит?"
            input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            input.typeface = font(activity)
            input.setTextColor(color(activity, R.color.text_primary))
            input.setHintTextColor(color(activity, R.color.text_tertiary))
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            input.setBackgroundResource(R.drawable.bg_field)
            input.setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12))
            input.layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(activity, 14)
            }
            col.addView(input)

            val hint = TextView(activity)
            hint.text = "Появится у всех, кто слушает этот трек на этой секунде"
            hint.setTextColor(color(activity, R.color.text_tertiary))
            hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
            hint.typeface = font(activity)
            hint.setPadding(0, dp(activity, 8), 0, 0)
            col.addView(hint)

            val send = TextView(activity)
            send.text = "Отправить"
            send.gravity = Gravity.CENTER
            send.setTextColor(0xFF0A0A0A.toInt())
            send.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            send.typeface = font(activity, true)
            send.setBackgroundResource(R.drawable.bg_btn)
            send.setPadding(0, dp(activity, 12), 0, dp(activity, 12))
            send.layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(activity, 14)
            }
            col.addView(send)

            send.setOnClickListener {
                val text = input.text?.toString()?.trim().orEmpty()
                if (text.isEmpty()) {
                    toast("Пусто")
                    return@setOnClickListener
                }
                send.isEnabled = false
                ApiClient.addComment(track, pos, text) { ok ->
                    activity.runOnUiThread {
                        send.isEnabled = true
                        if (ok) {
                            dlg.dismiss()
                            toast("Комментарий на " + time(pos.toLong()))
                        } else {
                            toast("Не отправилось")
                        }
                    }
                }
            }
            input.requestFocus()
            dlg.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }
        d.show()
    }

    // -------------------------------------------------------------- текст
    // Текст живёт ВНУТРИ экрана «Сейчас играет»: обложка плавно уменьшается и
    // уезжает вверх, а свободное место занимает текст с подсветкой текущей
    // строки. Никаких отдельных попапов - и при смене трека текст меняется сам.

    private class LyricsState {
        var on = false
        var trackId = ""
        var handler: Handler? = null
        var times: List<Long> = emptyList()
        var texts: List<String> = emptyList()
        var views: MutableList<TextView> = mutableListOf()
        var scroll: ScrollView? = null
        var coverW = 0
        var coverH = 0
    }

    private val lyricsStates = HashMap<Dialog, LyricsState>()

    fun toggleLyrics(
        activity: Activity,
        dialog: Dialog,
        player: PlayerController?,
        toast: (String) -> Unit,
    ) {
        val box = dialog.findViewById<ScrollView>(R.id.now_lyrics_box) ?: return
        val list = dialog.findViewById<LinearLayout>(R.id.now_lyrics_list) ?: return
        val card = dialog.findViewById<View>(R.id.now_card) ?: return
        val cover = dialog.findViewById<View>(R.id.now_cover_box) ?: return
        val lyricsIc = dialog.findViewById<ImageView>(R.id.now_lyrics_ic)
        val lyricsTx = dialog.findViewById<TextView>(R.id.now_lyrics_tx)
        val state = lyricsStates.getOrPut(dialog) { LyricsState() }

        if (state.on) {
            // свернуть текст: вернуть обложку
            state.on = false
            state.handler?.removeCallbacksAndMessages(null)
            state.handler = null
            box.visibility = View.GONE
            lyricsTx?.text = "Текст"
            lyricsIc?.imageTintList = android.content.res.ColorStateList.valueOf(
                color(activity, R.color.text_secondary)
            )
            lyricsTx?.setTextColor(color(activity, R.color.text_tertiary))
            val lpCard = card.layoutParams
            lpCard.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            card.layoutParams = lpCard
            animateCover(cover, state.coverW, state.coverH)
            return
        }

        state.on = true
        state.coverW = cover.layoutParams.width
        state.coverH = cover.layoutParams.height
        val lpCard = card.layoutParams
        lpCard.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
        card.layoutParams = lpCard
        // кнопка остаётся на месте, но подсвечена: нажатие возвращает обложку
        lyricsIc?.imageTintList = android.content.res.ColorStateList.valueOf(
            color(activity, R.color.text_primary)
        )
        lyricsTx?.text = "Свернуть"
        lyricsTx?.setTextColor(color(activity, R.color.text_secondary))
        box.visibility = View.VISIBLE
        box.alpha = 0f
        box.animate().alpha(1f).setDuration(220).start()
        val small = dp(activity, 96)
        animateCover(cover, small, small)
        state.scroll = box
        loadLyricsFor(activity, dialog, state, player, toast)
    }

    private fun animateCover(cover: View, targetW: Int, targetH: Int) {
        val lp = cover.layoutParams
        val fromW = lp.width
        val fromH = lp.height
        val anim = android.animation.ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 260
        anim.addUpdateListener { a ->
            val f = a.animatedValue as Float
            lp.width = (fromW + (targetW - fromW) * f).toInt()
            lp.height = (fromH + (targetH - fromH) * f).toInt()
            cover.layoutParams = lp
        }
        anim.start()
    }

    private fun loadLyricsFor(
        activity: Activity,
        dialog: Dialog,
        state: LyricsState,
        player: PlayerController?,
        toast: (String) -> Unit,
    ) {
        val list = dialog.findViewById<LinearLayout>(R.id.now_lyrics_list) ?: return
        val scroll = state.scroll ?: return
        val track = player?.current() ?: return
        state.trackId = track.id
        state.handler?.removeCallbacksAndMessages(null)
        state.handler = null
        list.removeAllViews()
        state.views = mutableListOf()
        state.times = emptyList()

        val loading = TextView(activity)
        loading.text = "Ищу текст…"
        loading.setTextColor(color(activity, R.color.text_tertiary))
        loading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        loading.typeface = font(activity)
        list.addView(loading)

        val askedFor = track.id
        ApiClient.lyrics(track.artist, track.title, track.duration, track.source, track.id) { data ->
            activity.runOnUiThread {
                // трек уже сменился, пока искали - ответ не наш
                if (state.trackId != askedFor || !state.on) return@runOnUiThread
                list.removeAllViews()
                val pairs: List<Pair<Long, String>> = when {
                    data == null -> emptyList()
                    data.synced && data.lines.isNotEmpty() -> data.lines.map { it.t to it.text }
                    data.plain.isNotBlank() ->
                        data.plain.split("\n").filter { it.isNotBlank() }.map { -1L to it }
                    else -> emptyList()
                }
                if (pairs.isEmpty()) {
                    val none = TextView(activity)
                    none.text = "Текста для этого трека нет"
                    none.setTextColor(color(activity, R.color.text_tertiary))
                    none.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    none.typeface = font(activity)
                    list.addView(none)
                    return@runOnUiThread
                }
                state.times = pairs.map { it.first }
                state.texts = pairs.map { it.second }
                for ((t, text) in pairs) {
                    val line = TextView(activity)
                    line.text = text
                    line.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP, if (t >= 0) 16f else 13.5f
                    )
                    line.typeface = font(activity, t >= 0)
                    line.setTextColor(color(activity, R.color.text_tertiary))
                    line.setPadding(0, dp(activity, 6), 0, dp(activity, 6))
                    if (t >= 0) {
                        line.setOnClickListener { player?.seekTo(t.toInt()) }
                    }
                    list.addView(line)
                    state.views.add(line)
                }
                if (data?.synced != true) return@runOnUiThread

                // Длительность строки считаем по ЧИСЛУ СИМВОЛОВ, а не по
                // расстоянию до следующей строки: раньше короткая фраза
                // растягивалась на весь проигрыш (6-8 секунд) и подсветка
                // отставала от голоса. 14 символов/сек ~ обычный темп,
                // но не быстрее, чем наступит следующая строка.
                val ends = LongArray(pairs.size) { i ->
                    val startT = pairs[i].first
                    val next = if (i + 1 < pairs.size) pairs[i + 1].first
                    else startT + 9000
                    val est = (pairs[i].second.length * 1000f / 14f).toLong()
                        .coerceAtLeast(900L)
                    minOf(next, startT + est)
                }

                val h = Handler(Looper.getMainLooper())
                state.handler = h
                var last = -1
                val tick = object : Runnable {
                    override fun run() {
                        if (!state.on) return
                        // трек сменился - подгружаем его текст (никакого «старого»)
                        val cur = player?.current()
                        if (cur != null && cur.id != state.trackId) {
                            loadLyricsFor(activity, dialog, state, player, toast)
                            return
                        }
                        val pos = (player?.position ?: 0).toLong()
                        var idx = -1
                        for (i in state.times.indices) {
                            if (state.times[i] in 0..pos) idx = i
                            else if (state.times[i] > pos) break
                        }
                        if (idx != last) {
                            if (last >= 0 && last < state.views.size) {
                                paintLine(
                                    activity, state.views[last],
                                    state.texts[last], 0f, false
                                )
                            }
                            if (idx >= 0 && idx < state.views.size) {
                                val v = state.views[idx]
                                val target = (v.top - scroll.height / 2 + v.height / 2)
                                    .coerceAtLeast(0)
                                scroll.smoothScrollTo(0, target)
                            }
                            last = idx
                        }
                        if (idx >= 0 && idx < state.views.size) {
                            val startT = state.times[idx]
                            val endT = ends[idx]
                            val frac = if (endT > startT) {
                                ((pos - startT).toFloat() / (endT - startT))
                                    .coerceIn(0f, 1f)
                            } else 1f
                            paintLine(
                                activity, state.views[idx],
                                state.texts[idx], frac, true
                            )
                        }
                        h.postDelayed(this, 80)
                    }
                }
                h.post(tick)
            }
        }
    }

    /**
     * Караоке: буквы «дописываются» по мере проигрывания (100 мс шаг).
     * Никаких пословных таймингов в LRC нет, поэтому прогресс считаем по
     * длине строки между её началом и началом следующей - на глаз плавно.
     */
    private fun paintLine(
        activity: Activity, v: TextView, text: String, frac: Float, active: Boolean,
    ) {
        if (!active) {
            v.text = text
            v.setTextColor(color(activity, R.color.text_tertiary))
            v.setTypeface(font(activity, false))
            return
        }
        v.setTypeface(font(activity, true))
        val n = (text.length * frac).toInt().coerceIn(0, text.length)
        val sp = android.text.SpannableString(text)
        sp.setSpan(
            android.text.style.ForegroundColorSpan(color(activity, R.color.text_primary)),
            0, n, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        sp.setSpan(
            android.text.style.ForegroundColorSpan(color(activity, R.color.text_tertiary)),
            n, text.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        v.text = sp
    }
}
