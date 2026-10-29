package su.gwarden.music

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.ProfileInfo
import su.gwarden.music.model.ProfileTrack
import su.gwarden.music.model.Track
import su.gwarden.music.player.PlayerHub
import su.gwarden.music.ui.ImageLoader

/**
 * Профиль автора в духе TikTok:
 * аватар, имя, статистика (треки / просмотры / лайки), сколько добавили
 * как автора, кнопки «Выложить трек» и «Настроить», сетка своих треков.
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var box: LinearLayout
    private lateinit var refreshHint: TextView
    private var pullStartY = -1f
    private var pulling = false
    private var info: ProfileInfo? = null
    private var avatarView: ImageView? = null
    private var targetUser = 0

    private val pickAvatar =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            toast("Загружаю аватар…")
            ApiClient.uploadAvatar(uri) { ok, err ->
                if (ok != null) {
                    toast("Аватар обновлён")
                    load()
                } else {
                    toast(err ?: "Не получилось загрузить аватар")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        ImageLoader.init(this)
        setContentView(R.layout.activity_profile)
        targetUser = intent.getIntExtra("user_id", 0)
        box = findViewById(R.id.profile_box)
        refreshHint = findViewById(R.id.profile_refresh)
        findViewById<View>(R.id.profile_back).setOnClickListener { finish() }
        setupPullToRefresh()
    }

    /** Потянуть вниз = обновить профиль (как ленту в соцсетях). */
    private fun setupPullToRefresh() {
        val sc = findViewById<android.widget.ScrollView>(R.id.profile_scroll)
        sc.setOnTouchListener { _, ev ->
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    pullStartY = if (sc.scrollY <= 0) ev.rawY else -1f
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    if (pullStartY >= 0f) {
                        val dy = ev.rawY - pullStartY
                        if (dy > 0f && sc.scrollY <= 0) {
                            val shift = kotlin.math.min(dy * 0.45f, dp(84).toFloat())
                            box.translationY = shift
                            pulling = true
                            refreshHint.visibility = View.VISIBLE
                            refreshHint.text =
                                if (shift > dp(52)) "Отпусти, чтобы обновить"
                                else "Потяни вниз"
                        }
                    }
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    if (pulling) {
                        val refresh = box.translationY > dp(52)
                        box.animate().translationY(0f).setDuration(150).start()
                        pulling = false
                        pullStartY = -1f
                        if (refresh) {
                            refreshHint.visibility = View.VISIBLE
                            refreshHint.text = "Обновляю…"
                            load()
                        } else {
                            refreshHint.visibility = View.GONE
                        }
                    } else {
                        pullStartY = -1f
                    }
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        ApiClient.profileInfo(targetUser) { p ->
            if (p == null) {
                refreshHint.visibility = View.GONE
                toast("Не удалось загрузить профиль")
                return@profileInfo
            }
            info = p
            render(p)
        }
    }

    // ------------------------------------------------------------------ отрисовка

    private fun render(p: ProfileInfo) {
        refreshHint.visibility = View.GONE
        box.translationY = 0f
        box.removeAllViews()

        val av = ImageView(this)
        val sz = dp(96)
        av.layoutParams = LinearLayout.LayoutParams(sz, sz).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = dp(8)
        }
        av.setBackgroundResource(R.drawable.bg_avatar_dark)
        av.clipToOutline = true
        avatarView = av
        showAvatar(p.avatar)
        if (p.self) {
            av.setOnClickListener { pickImage() }
        }
        box.addView(av)

        val name = TextView(this).apply {
            text = p.name.ifBlank { "слушатель" }
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 20f
            setTypeface(typeface, 1)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        box.addView(name)

        val who = TextView(this).apply {
            text = if (p.self) "твой профиль автора" else "автор Wonder"
            setTextColor(0xFF8A8A8A.toInt())
            textSize = 12.5f
            gravity = Gravity.CENTER
            setPadding(0, dp(3), 0, 0)
        }
        box.addView(who)

        if (p.bio.isNotBlank()) {
            val bio = TextView(this).apply {
                text = p.bio
                setTextColor(0xFFCFCFCF.toInt())
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(10), dp(12), 0)
            }
            box.addView(bio)
        }

        box.addView(statsRow(p))

        val subs = TextView(this).apply {
            text = p.stats.subs.toString() + " добавили " +
                    (if (p.self) "тебя как автора" else "как автора")
            setTextColor(0xFF8A8A8A.toInt())
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, 0)
        }
        box.addView(subs)

        box.addView(buttonsRow(p))

        if (p.self && p.canUploadIn > 0) {
            box.addView(hint("Следующий трек через " + humanLeft(p.canUploadIn)))
        }

        box.addView(section("ТРЕКИ"))
        if (p.self && p.tracks.isNotEmpty()) {
            box.addView(TextView(this).apply {
                text = "Долгое нажатие на трек — удалить"
                setTextColor(0xFF6E6E6E.toInt())
                textSize = 11f
                setPadding(dp(2), 0, 0, dp(6))
            })
        }
        if (p.tracks.isEmpty()) {
            box.addView(
                hint(
                    if (p.self) "Пока пусто. Выложи первый — он попадёт в шортсы."
                    else "Треков пока нет"
                )
            )
        } else {
            buildGrid(p.tracks)
        }
    }

    private fun statsRow(p: ProfileInfo): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(18), 0, dp(4))
        }
        row.addView(stat(fmt(p.stats.tracks), "треков"))
        row.addView(stat(fmt(p.stats.plays), "прослушиваний"))
        row.addView(stat(fmt(p.stats.likes), "лайков"))
        return row
    }

    private fun stat(value: String, label: String): View =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            addView(TextView(this@ProfileActivity).apply {
                text = value
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 18f
                setTypeface(typeface, 1)
                gravity = Gravity.CENTER
            })
            addView(TextView(this@ProfileActivity).apply {
                text = label
                setTextColor(0xFF8A8A8A.toInt())
                textSize = 11f
                gravity = Gravity.CENTER
                setPadding(0, dp(2), 0, 0)
            })
        }

    private fun buttonsRow(p: ProfileInfo): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(4))
        }
        if (p.self) {
            val ready = p.canUploadIn <= 0
            val up = TextView(this).apply {
                text = if (ready) "Выложить трек" else "Через " + humanLeft(p.canUploadIn)
                setTextColor(0xFF0A0A0A.toInt())
                textSize = 14f
                setTypeface(typeface, 1)
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.bg_btn)
                alpha = if (ready) 1f else 0.45f
                if (ready) {
                    setOnClickListener {
                        startActivity(Intent(this@ProfileActivity, UploadActivity::class.java))
                    }
                }
            }
            row.addView(up, LinearLayout.LayoutParams(0, dp(46), 1f))

            val edit = TextView(this).apply {
                text = "Настроить"
                setTextColor(0xFFEBEBEB.toInt())
                textSize = 13f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.bg_pill_outline)
                setPadding(dp(18), 0, dp(18), 0)
                setOnClickListener { showEdit() }
            }
            row.addView(edit, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(46)
            ).apply { marginStart = dp(10) })
        } else {
            val follow = TextView(this).apply {
                text = if (p.subscribed) "Вы подписаны" else "Добавить как автора"
                setTextColor(if (p.subscribed) 0xFFEBEBEB.toInt() else 0xFF0A0A0A.toInt())
                textSize = 14f
                setTypeface(typeface, 1)
                gravity = Gravity.CENTER
                setBackgroundResource(
                    if (p.subscribed) R.drawable.bg_pill_outline else R.drawable.bg_btn
                )
                setOnClickListener {
                    ApiClient.authorSub(p.uid, !p.subscribed) { load() }
                }
            }
            row.addView(follow, LinearLayout.LayoutParams(0, dp(46), 1f))
        }
        return row
    }

    private fun buildGrid(tracks: List<ProfileTrack>) {
        val gap = dp(6)
        val screen = resources.displayMetrics.widthPixels
        val cell = (screen - dp(32) - gap * 2) / 3
        var i = 0
        while (i < tracks.size) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(4), 0, 0)
            }
            for (j in 0 until 3) {
                val idx = i + j
                if (idx < tracks.size) {
                    val v = cellView(tracks[idx], cell, tracks)
                    row.addView(v, LinearLayout.LayoutParams(cell, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        if (j < 2) marginEnd = gap
                    })
                } else {
                    row.addView(View(this), LinearLayout.LayoutParams(cell, dp(1)))
                }
            }
            box.addView(row)
            i += 3
        }
    }

    private fun cellView(t: ProfileTrack, w: Int, all: List<ProfileTrack>): View {
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val cover = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(w, w)
            setBackgroundResource(R.drawable.bg_cover)
            clipToOutline = true
        }
        val img = ImageView(this)
        cover.addView(img, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ))
        ImageLoader.load(img, ApiClient.img(t.artwork, 400), 0, R.drawable.ic_music_note, null)

        val plays = TextView(this).apply {
            text = fmt(t.plays)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 10f
            setTypeface(typeface, 1)
            setPadding(dp(6), dp(3), dp(6), dp(3))
            setBackgroundResource(R.drawable.bg_card_scrim)
        }
        cover.addView(plays, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.START
        ).apply {
            leftMargin = dp(5)
            bottomMargin = dp(5)
        })

        val title = TextView(this).apply {
            text = t.title
            setTextColor(0xFFEDEDED.toInt())
            textSize = 11.5f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(dp(1), dp(6), dp(1), 0)
        }
        val meta = TextView(this).apply {
            text = t.genre
            setTextColor(0xFF8A8A8A.toInt())
            textSize = 10.5f
            maxLines = 1
            setPadding(dp(1), dp(1), dp(1), 0)
        }
        col.addView(cover)
        col.addView(title)
        col.addView(meta)
        col.setOnClickListener { playOwn(t) }
        col.setOnLongClickListener {
            if (info?.self == true) {
                confirmDelete(t)
                true
            } else {
                false
            }
        }
        return col
    }

    // ------------------------------------------------------------------ действия

    /**
     * Свой трек открывается КАК ШОРТС: полноэкранный плеер с эквалайзером,
     * дропом (его выбрал автор), лайками и комментариями - а не тихое
     * воспроизведение в фоне.
     */
    private fun playOwn(t: ProfileTrack) {
        runCatching {
            startActivity(Intent(this, ShortsActivity::class.java).apply {
                putExtra("short_source", t.source.ifEmpty { "own" })
                putExtra("short_id", t.id)
            })
        }
    }

    private fun pickImage() {
        runCatching { pickAvatar.launch(arrayOf("image/*")) }
    }

    private fun showEdit() {
        val p = info ?: return
        val d = android.app.Dialog(this, R.style.Theme_Wonder_Sheet)
        val root = FrameLayout(this)
        root.setBackgroundColor(0xC8000000.toInt())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(16))
            setBackgroundResource(R.drawable.bg_dialog)
        }
        card.addView(TextView(this).apply {
            text = "Профиль"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 17f
            setTypeface(typeface, 1)
        })

        card.addView(smallLabel("ИМЯ"))
        val name = EditText(this).apply {
            setText(p.name.removePrefix("@"))
            hint = "Как тебя зовут"
            setSingleLine(true)
            setTextColor(0xFFEDEDED.toInt())
            setHintTextColor(0xFF8A8A8A.toInt())
            setBackgroundResource(R.drawable.bg_search)
            setPadding(dp(14), 0, dp(14), 0)
        }
        card.addView(name, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
        ))

        card.addView(smallLabel("О СЕБЕ"))
        val bio = EditText(this).apply {
            setText(p.bio)
            hint = "Пара слов о себе"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 3
            gravity = Gravity.TOP
            setTextColor(0xFFEDEDED.toInt())
            setHintTextColor(0xFF8A8A8A.toInt())
            setBackgroundResource(R.drawable.bg_search)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        card.addView(bio, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(96)
        ))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(16), 0, 0)
        }
        val avBtn = TextView(this).apply {
            text = "Аватар"
            setTextColor(0xFFEBEBEB.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_pill_outline)
            setPadding(dp(16), 0, dp(16), 0)
            setOnClickListener {
                d.dismiss()
                pickImage()
            }
        }
        row.addView(avBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)
        ))
        row.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        val save = TextView(this).apply {
            text = "Сохранить"
            setTextColor(0xFF0A0A0A.toInt())
            textSize = 13f
            setTypeface(typeface, 1)
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_btn)
            setPadding(dp(20), 0, dp(20), 0)
            setOnClickListener {
                d.dismiss()
                ApiClient.profileUpdate(name.text.toString(), bio.text.toString()) { body, err ->
                    if (body != null) {
                        load()
                    } else {
                        toast(err ?: "Не сохранилось")
                    }
                }
            }
        }
        row.addView(save, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)
        ))
        card.addView(row)

        root.addView(card, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ).apply {
            leftMargin = dp(20)
            rightMargin = dp(20)
        })
        root.setOnClickListener { d.dismiss() }
        card.setOnClickListener { }
        d.setContentView(root)
        d.show()
        d.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /** Подтверждение удаления своего трека. */
    private fun confirmDelete(t: ProfileTrack) {
        val d = android.app.Dialog(this, R.style.Theme_Wonder_Sheet)
        val root = FrameLayout(this)
        root.setBackgroundColor(0xC8000000.toInt())

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(16))
            setBackgroundResource(R.drawable.bg_dialog)
        }
        card.addView(TextView(this).apply {
            text = "Удалить трек?"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 17f
            setTypeface(typeface, 1)
        })
        card.addView(TextView(this).apply {
            text = "«" + t.title + "» исчезнет из профиля и шортсов."
            setTextColor(0xFFB5B5B5.toInt())
            textSize = 13f
            setPadding(0, dp(10), 0, 0)
        })

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0, dp(18), 0, 0)
        }
        val cancel = TextView(this).apply {
            text = "Отмена"
            setTextColor(0xFFEBEBEB.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_pill_outline)
            setPadding(dp(16), 0, dp(16), 0)
            setOnClickListener { d.dismiss() }
        }
        row.addView(cancel, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)
        ))
        val del = TextView(this).apply {
            text = "Удалить"
            setTextColor(0xFF0A0A0A.toInt())
            textSize = 13f
            setTypeface(typeface, 1)
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_listen)
            setPadding(dp(20), 0, dp(20), 0)
            setOnClickListener {
                d.dismiss()
                ApiClient.ownDelete(t.id) { ok, err ->
                    if (ok) {
                        // чистим и локальные копии: иначе трек играет из кэша
                        // телефона, хотя в аккаунте его уже нет
                        runCatching {
                            val tr = Track(
                                id = t.id, source = t.source.ifEmpty { "own" },
                                title = t.title, artist = t.artist,
                                duration = t.duration, artwork = t.artwork,
                                playable = true
                            )
                            PlayerHub.controller?.forget(tr)
                        }
                        // лента на главной кэширует «Недавно слушали» -
                        // просим её перезагрузиться при возврате
                        getSharedPreferences("music_player", 0).edit()
                            .putBoolean("home_dirty", true).apply()
                        toast("Трек удалён")
                        load()
                    } else {
                        toast(err ?: "Не удалилось")
                    }
                }
            }
        }
        row.addView(del, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)
        ).apply { marginStart = dp(10) })
        card.addView(row)

        root.addView(card, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ).apply {
            leftMargin = dp(24)
            rightMargin = dp(24)
        })
        root.setOnClickListener { d.dismiss() }
        card.setOnClickListener { }
        d.setContentView(root)
        d.show()
        d.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun smallLabel(s: String): TextView = TextView(this).apply {
        text = s
        setTextColor(0xFF6E6E6E.toInt())
        textSize = 10.5f
        letterSpacing = 0.08f
        setPadding(0, dp(14), 0, dp(6))
    }

    // ------------------------------------------------------------------ мелкое

    private fun showAvatar(url: String) {
        val v = avatarView ?: return
        if (url.isNotBlank()) {
            v.setPadding(0, 0, 0, 0)
            v.scaleType = ImageView.ScaleType.CENTER_CROP
            ImageLoader.load(v, ApiClient.img(url, 300), 0, R.drawable.ic_artist, null)
        } else {
            v.setImageResource(R.drawable.ic_artist)
            v.setPadding(dp(26), dp(26), dp(26), dp(26))
            v.scaleType = ImageView.ScaleType.CENTER_INSIDE
            v.imageTintList =
                android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
        }
    }

    private fun section(text: String): View = TextView(this).apply {
        this.text = text
        setTextColor(0xFF6E6E6E.toInt())
        textSize = 11f
        letterSpacing = 0.08f
        setPadding(dp(2), dp(22), 0, dp(6))
    }

    private fun hint(text: String): View = TextView(this).apply {
        this.text = text
        setTextColor(0xFF8A8A8A.toInt())
        textSize = 12.5f
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(12), dp(8), dp(4))
    }

    private fun humanLeft(sec: Int): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        return when {
            h > 0 -> "$h ч $m мин"
            m > 0 -> "$m мин"
            else -> "$sec сек"
        }
    }

    private fun fmt(n: Int): String = when {
        n >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", n / 1_000_000f)
        n >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", n / 1_000f)
        else -> n.toString()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
