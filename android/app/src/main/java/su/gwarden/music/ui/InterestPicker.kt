package su.gwarden.music.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import su.gwarden.music.R
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.InterestGroup
import su.gwarden.music.model.InterestItem
import java.util.function.Consumer

/**
 * Онбординг «Что ты слушаешь?»: каталог интересов карточками с обложками.
 *
 * Жанров много (70+), поэтому они сгруппированы, есть поиск по названию и
 * список на RecyclerView - карточки вне экрана не грузят картинки и память.
 * Выбранные id уходят на сервер (/api/guest/prefs) как семена волны.
 */
object InterestPicker {

    private const val MAX_PICK = 8

    fun show(activity: Activity, toast: Consumer<String>, onFinished: Runnable) {
        val ctx = activity
        val en = ApiClient.lang == "en"
        fun s(ru: String, eng: String) = if (en) eng else ru
        val picked = LinkedHashSet<String>()
        var groups: List<InterestGroup> = emptyList()

        val root = FrameLayout(ctx)
        root.setBackgroundResource(R.drawable.bg_app)
        root.isClickable = true

        val col = LinearLayout(ctx)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(ctx, 20), dp(ctx, 28), dp(ctx, 20), dp(ctx, 12))
        root.addView(col, FrameLayout.LayoutParams(-1, -1))

        col.addView(
            tv(ctx, s("Что ты слушаешь?", "What do you listen to?"), 25f, C_PRIMARY, true)
        )

        val sub = tv(
            ctx,
            s(
                "Выбери жанры — соберём волну под тебя. Дальше она подстроится сама.",
                "Pick genres — we build your wave. It adapts on its own later."
            ),
            12f, C_SECONDARY, false
        )
        sub.setLineSpacing(dp(ctx, 2).toFloat(), 1f)
        sub.setPadding(0, dp(ctx, 8), 0, 0)
        col.addView(sub)

        // ---- поиск + счётчик
        val searchRow = LinearLayout(ctx)
        searchRow.orientation = LinearLayout.HORIZONTAL
        searchRow.gravity = Gravity.CENTER_VERTICAL
        searchRow.setBackgroundResource(R.drawable.bg_search)
        searchRow.setPadding(dp(ctx, 12), 0, dp(ctx, 12), 0)
        val searchIc = ImageView(ctx)
        searchIc.setImageResource(R.drawable.ic_search)
        searchIc.imageTintList = ColorStateList.valueOf(C_TERTIARY)
        searchRow.addView(searchIc, LinearLayout.LayoutParams(dp(ctx, 16), dp(ctx, 16)))
        val search = EditText(ctx)
        search.hint = s("Найти жанр", "Find a genre")
        search.setHintTextColor(C_TERTIARY)
        search.setTextColor(C_PRIMARY)
        search.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f)
        search.typeface = font(ctx, false)
        search.setSingleLine(true)
        search.background = null
        search.setPadding(dp(ctx, 8), 0, dp(ctx, 6), 0)
        search.inputType = InputType.TYPE_CLASS_TEXT
        searchRow.addView(search, LinearLayout.LayoutParams(0, -1, 1f))
        val counter = tv(ctx, "0/$MAX_PICK", 11f, C_TERTIARY, false)
        searchRow.addView(counter, LinearLayout.LayoutParams(-2, -2))
        val lpSearch = LinearLayout.LayoutParams(-1, dp(ctx, 44))
        lpSearch.topMargin = dp(ctx, 14)
        col.addView(searchRow, lpSearch)

        // ---- каталог
        val recycler = RecyclerView(ctx)
        recycler.layoutManager = LinearLayoutManager(ctx)
        recycler.setHasFixedSize(false)
        recycler.itemAnimator = null
        recycler.overScrollMode = View.OVER_SCROLL_NEVER
        val adapter = GenresAdapter(ctx, picked, toast) {
            counter.text = "${picked.size}/$MAX_PICK"
            counter.setTextColor(if (picked.isEmpty()) C_TERTIARY else C_PRIMARY)
        }
        recycler.adapter = adapter
        val lpList = LinearLayout.LayoutParams(-1, 0, 1f)
        lpList.topMargin = dp(ctx, 6)
        col.addView(recycler, lpList)

        // ---- любимые артисты (одна строка, всегда видна)
        val artists = EditText(ctx)
        artists.hint = s(
            "Любимые артисты — через запятую",
            "Favorite artists — comma separated"
        )
        artists.setHintTextColor(C_TERTIARY)
        artists.setTextColor(C_PRIMARY)
        artists.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        artists.typeface = font(ctx, false)
        artists.setSingleLine(true)
        artists.inputType = InputType.TYPE_CLASS_TEXT
        artists.setBackgroundResource(R.drawable.bg_field)
        artists.setPadding(dp(ctx, 13), 0, dp(ctx, 13), 0)
        val lpArt = LinearLayout.LayoutParams(-1, dp(ctx, 44))
        lpArt.topMargin = dp(ctx, 10)
        col.addView(artists, lpArt)

        // ---- кнопки
        val cta = tv(ctx, s("Начать слушать", "Start listening"), 15f, 0xFF0A0A0A.toInt(), true)
        cta.gravity = Gravity.CENTER
        cta.setBackgroundResource(R.drawable.bg_listen)
        cta.isClickable = true
        val lpCta = LinearLayout.LayoutParams(-1, dp(ctx, 52))
        lpCta.topMargin = dp(ctx, 10)
        col.addView(cta, lpCta)

        val skip = tv(ctx, s("Пропустить", "Skip"), 13f, C_SECONDARY, false)
        skip.gravity = Gravity.CENTER
        skip.setPadding(0, dp(ctx, 10), 0, dp(ctx, 2))
        skip.isClickable = true
        col.addView(skip)

        // «Назад» = пропустить, а не выйти из приложения
        val backCb = if (activity is androidx.activity.ComponentActivity) {
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    skip.performClick()
                }
            }.also { activity.onBackPressedDispatcher.addCallback(it) }
        } else null

        fun close() {
            backCb?.remove()
            val parent = root.parent
            if (parent is ViewGroup) parent.removeView(root)
        }

        fun render() {
            adapter.rows = buildRows(groups, adapter.filter, s("Ничего не нашлось", "Nothing found"))
        }

        var filterRun: Runnable? = null
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.filter = s?.toString().orEmpty()
                filterRun?.let { search.removeCallbacks(it) }
                val run = Runnable { render() }
                filterRun = run
                search.postDelayed(run, 160)
            }
        })

        cta.setOnClickListener {
            val gens = picked.toList()
            val arts = artists.text.toString().split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .take(20)
            if (gens.isEmpty() && arts.isEmpty()) {
                toast.accept(s("Выбери хотя бы один жанр", "Pick at least one genre"))
                return@setOnClickListener
            }
            cta.isEnabled = false
            cta.text = s("Сохраняю…", "Saving…")
            ApiClient.saveGuestPrefs(gens, arts) { ok, err ->
                if (!ok) {
                    cta.isEnabled = true
                    cta.text = s("Начать слушать", "Start listening")
                    toast.accept(
                        s("Не сохранилось: ", "Not saved: ")
                            + (err ?: s("нет связи", "no connection"))
                    )
                    return@saveGuestPrefs
                }
                close()
                onFinished.run()
            }
        }

        skip.setOnClickListener {
            close()
            onFinished.run()
        }

        // старый каталог - сразу из кэша, свежий - из сети
        val cached = ApiClient.interestsCached()
        if (cached != null) {
            groups = cached
            render()
        } else {
            adapter.rows = listOf(Row.Placeholder(s("Загружаю жанры…", "Loading genres…")))
        }
        ApiClient.interests { list ->
            if (activity.isFinishing) return@interests
            if (list != null) {
                groups = list
                render()
            } else if (cached == null) {
                groups = fallback()
                render()
            }
        }

        val host = activity.findViewById<ViewGroup>(android.R.id.content)
        host?.addView(root, ViewGroup.LayoutParams(-1, -1))
    }

    // ------------------------------------------------------------- список

    private sealed class Row {
        class Head(val title: String) : Row()
        class Line(val items: List<InterestItem>) : Row()
        class Placeholder(val text: String) : Row()
    }

    private class HeadVH(view: TextView) : RecyclerView.ViewHolder(view)
    private class LineVH(view: LinearLayout) : RecyclerView.ViewHolder(view)
    private class TextVH(view: TextView) : RecyclerView.ViewHolder(view)

    private fun buildRows(groups: List<InterestGroup>, filter: String, empty: String): List<Row> {
        val q = filter.trim().lowercase()
        val out = ArrayList<Row>()
        for (g in groups) {
            val items = g.items.filter { q.isEmpty() || it.title.lowercase().contains(q) }
            if (items.isEmpty()) continue
            out.add(Row.Head(g.title))
            var i = 0
            while (i < items.size) {
                val pair = ArrayList<InterestItem>(2)
                pair.add(items[i])
                if (i + 1 < items.size) pair.add(items[i + 1])
                out.add(Row.Line(pair))
                i += 2
            }
        }
        if (out.isEmpty()) out.add(Row.Placeholder(empty))
        return out
    }

    private class GenresAdapter(
        private val ctx: Context,
        private val picked: LinkedHashSet<String>,
        private val toast: Consumer<String>,
        private val onChange: () -> Unit,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var filter: String = ""
        var rows: List<Row> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int = rows.size

        override fun getItemViewType(position: Int): Int = when (rows[position]) {
            is Row.Head -> 0
            is Row.Line -> 1
            is Row.Placeholder -> 2
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                0 -> HeadVH(section(ctx, ""))
                2 -> TextVH(tv(ctx, "", 13f, C_TERTIARY, false).apply {
                    setPadding(dp(ctx, 2), dp(ctx, 16), 0, dp(ctx, 6))
                })
                else -> {
                    val row = LinearLayout(ctx)
                    row.orientation = LinearLayout.HORIZONTAL
                    LineVH(row)
                }
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val r = rows[position]) {
                is Row.Head -> (holder.itemView as TextView).text = r.title
                is Row.Placeholder -> (holder.itemView as TextView).text = r.text
                is Row.Line -> {
                    val row = holder.itemView as LinearLayout
                    row.removeAllViews()
                    for ((i, item) in r.items.withIndex()) {
                        val lp = LinearLayout.LayoutParams(0, dp(ctx, 92), 1f)
                        if (i == 0 && r.items.size > 1) lp.marginEnd = dp(ctx, 8)
                        row.addView(makeCard(item), lp)
                    }
                    if (r.items.size == 1) {
                        row.addView(View(ctx), LinearLayout.LayoutParams(0, 1, 1f))
                    }
                }
            }
        }

        private fun makeCard(item: InterestItem): View {
            val card = FrameLayout(ctx)
            card.isClickable = true
            card.clipToOutline = true

            val img = ImageView(ctx)
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            card.addView(img, FrameLayout.LayoutParams(-1, -1))
            // сервер отдаёт относительный путь /api/genre_art/<id>.png
            val raw = item.img
            val cover = when {
                raw.isNullOrEmpty() -> null
                raw.startsWith("http") -> ApiClient.img(raw, 400)
                else -> ApiClient.baseUrl + raw
            }
            if (!cover.isNullOrEmpty()) {
                ImageLoader.load(img, cover, 0, 0)
            } else {
                // обложки ещё нет - спокойный серый градиент и нота
                img.background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(0xFF232323.toInt(), 0xFF0D0D0D.toInt())
                )
                img.scaleType = ImageView.ScaleType.CENTER
                img.setImageResource(R.drawable.ic_music_note)
                img.imageTintList = ColorStateList.valueOf(C_PRIMARY)
                img.setImageAlpha(46)
            }

            val scrim = View(ctx)
            scrim.setBackgroundResource(R.drawable.bg_card_scrim)
            card.addView(scrim, FrameLayout.LayoutParams(-1, dp(ctx, 56), Gravity.BOTTOM))

            val label = tv(ctx, item.title, 12f, C_PRIMARY, true)
            label.maxLines = 1
            label.ellipsize = TextUtils.TruncateAt.END
            label.setPadding(dp(ctx, 10), 0, dp(ctx, 10), dp(ctx, 9))
            card.addView(label, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))

            val check = ImageView(ctx)
            check.setBackgroundResource(R.drawable.bg_circle_white)
            check.setImageResource(R.drawable.ic_check)
            check.imageTintList = ColorStateList.valueOf(0xFF0A0A0A.toInt())
            check.setPadding(dp(ctx, 3), dp(ctx, 3), dp(ctx, 3), dp(ctx, 3))
            val lpCheck = FrameLayout.LayoutParams(
                dp(ctx, 20), dp(ctx, 20), Gravity.TOP or Gravity.END
            )
            lpCheck.topMargin = dp(ctx, 7)
            lpCheck.marginEnd = dp(ctx, 7)
            card.addView(check, lpCheck)

            applyCard(card, check, picked.contains(item.id))

            card.setOnClickListener {
                val on = if (picked.contains(item.id)) {
                    picked.remove(item.id)
                    false
                } else if (picked.size >= MAX_PICK) {
                    toast.accept(
                        if (ApiClient.lang == "en") "You can pick up to $MAX_PICK genres"
                        else "Можно выбрать до $MAX_PICK жанров"
                    )
                    picked.contains(item.id)
                } else {
                    picked.add(item.id)
                    true
                }
                applyCard(card, check, on)
                onChange()
            }
            return card
        }
    }

    // ------------------------------------------------------------- мелочи

    private val C_PRIMARY = 0xFFFFFFFF.toInt()
    private val C_SECONDARY = 0xFFA0A0A0.toInt()
    private val C_TERTIARY = 0xFF6B6B6B.toInt()

    private fun dp(ctx: Context, v: Int): Int =
        (v * ctx.resources.displayMetrics.density).toInt()

    private fun font(ctx: Context, bold: Boolean): Typeface? =
        Skin.typeface(ctx, bold)

    private fun tv(
        ctx: Context, text: String, sp: Float, color: Int, bold: Boolean,
    ): TextView {
        val v = TextView(ctx)
        v.text = text
        v.setTextColor(color)
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        v.typeface = font(ctx, bold)
        return v
    }

    private fun section(ctx: Context, text: String): TextView {
        val v = tv(ctx, text, 11f, C_TERTIARY, true)
        v.letterSpacing = 0.06f
        v.setPadding(dp(ctx, 2), dp(ctx, 18), 0, dp(ctx, 4))
        return v
    }

    private fun applyCard(card: FrameLayout, check: ImageView, selected: Boolean) {
        val d = card.resources.displayMetrics.density
        if (card.background == null) {
            val bg = GradientDrawable()
            bg.cornerRadius = 16 * d
            bg.setColor(0xFF141414.toInt())
            card.background = bg
        }
        // ВАЖНО: рамку рисуем foreground'ом ПОВЕРХ обложки - фон с обводкой
        // полностью закрывался картинкой, и выбранная карточка не выделялась.
        card.foreground = if (selected) {
            GradientDrawable().apply {
                cornerRadius = 16 * d
                setColor(0x00000000)
                setStroke((2 * d).toInt(), 0xFFFFFFFF.toInt())
            }
        } else null
        check.visibility = if (selected) View.VISIBLE else View.GONE
    }

    /** Если сервер недоступен и кэша нет - хотя бы базовый набор. */
    private fun fallback(): List<InterestGroup> {
        val en = ApiClient.lang == "en"
        fun g(id: String, ru: String, eng: String) = InterestItem(id, if (en) eng else ru)
        return listOf(
            InterestGroup("electronic", if (en) "Electronic" else "Электроника", listOf(
                g("phonk", "Фонк", "Phonk"),
                g("hardtekk", "Хардтекк", "Hardtekk"),
                g("techno", "Техно", "Techno"),
                g("house", "Хаус", "House"),
                g("dnb", "Драм-н-бейс", "Drum & Bass"),
                g("electronic", "Электроника", "Electronic"),
            )),
            InterestGroup("rap", if (en) "Rap & Hip-Hop" else "Рэп и хип-хоп", listOf(
                g("rap", "Рэп", "Rap"),
                g("hiphop", "Хип-хоп", "Hip-Hop"),
                g("trap", "Трэп", "Trap"),
                g("drill", "Дрилл", "Drill"),
                g("emorap", "Эмо-рэп", "Emo Rap"),
            )),
            InterestGroup("pop", if (en) "Pop & Soul" else "Поп и соул", listOf(
                g("pop", "Поп", "Pop"),
                g("rnb", "R&B", "R&B"),
                g("hyperpop", "Хиперпоп", "Hyperpop"),
            )),
            InterestGroup("rock", if (en) "Rock" else "Рок", listOf(
                g("rock", "Рок", "Rock"),
                g("indie", "Инди", "Indie"),
                g("metal", "Метал", "Metal"),
                g("punk", "Панк", "Punk"),
            )),
            InterestGroup("chill", if (en) "Chill" else "Спокойное", listOf(
                g("lofi", "Лоу-фай", "Lo-Fi"),
                g("jazz", "Джаз", "Jazz"),
                g("classical", "Классика", "Classical"),
                g("ambient", "Эмбиент", "Ambient"),
            )),
        )
    }
}
