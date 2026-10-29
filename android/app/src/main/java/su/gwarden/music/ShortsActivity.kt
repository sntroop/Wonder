package su.gwarden.music

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.Short
import su.gwarden.music.model.Track
import su.gwarden.music.player.PlayerHub
import su.gwarden.music.ui.EqBackground
import su.gwarden.music.ui.ImageLoader
import su.gwarden.music.ui.ShortAdapter
import su.gwarden.music.ui.SocialUi

/**
 * «Шортсы»: вертикальная лента дропов из «Моей волны».
 *
 * Листаешь вверх - следующий трек, но играет не с начала, а с дропа (его
 * посчитал сервер по громкости/басу/атакам). Позиция внутри куска видна
 * полоской внизу, а когда кусок заканчивается - лента сама едет дальше.
 * Фон и эквалайзер вокруг обложки - как в «Моей волне».
 */
class ShortsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var adapter: ShortAdapter
    private lateinit var loading: View
    private lateinit var loadingText: TextView
    private lateinit var eq: EqBackground

    private val shorts = ArrayList<Short>()
    private val handler = Handler(Looper.getMainLooper())

    private var current = 0
    private var seeked = false
    private var playingId: String? = null
    private var waitStartedAt = 0L
    private var loadingMore = false
    private var tries = 0
    private var finishedLoading = false
    private var keepPlaying = false
    private var moreRefreshTried = false
    private var resumed = false
    private var pendingSwitch = false
    private var moreRetryDelay = 2000L
    private var notPlayingTicks = 0
    private var seedSource = ""
    private var seedId = ""
    private var seeded = false

    private val player get() = PlayerHub.controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        ImageLoader.init(this)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK

        setContentView(R.layout.activity_shorts)
        list = findViewById(R.id.shorts_list)
        loading = findViewById(R.id.shorts_loading)
        loadingText = findViewById(R.id.shorts_loading_text)
        eq = findViewById(R.id.shorts_eq)

        eq.positionProvider = { player?.position ?: 0 }
        // фон сам переспрашивает, где сейчас обложка: лучи не отрываются
        // после смены раскладки/системных панелей
        eq.orbSync = { syncOrbNearest() }

        findViewById<View>(R.id.shorts_close).setOnClickListener { finish() }
        findViewById<View>(R.id.shorts_top).setOnApplyWindowInsetsListener { v, insets ->
            val top = if (android.os.Build.VERSION.SDK_INT >= 30) {
                insets.getInsets(android.view.WindowInsets.Type.statusBars()).top
            } else {
                @Suppress("DEPRECATION")
                insets.systemWindowInsetTop
            }
            v.setPadding(v.paddingLeft, top + dp(8), v.paddingRight, v.paddingBottom)
            insets
        }

        adapter = ShortAdapter(
            onFull = { sh -> playFull(sh) },
            onAdd = { sh -> addTrack(sh) },
            onArtist = { sh -> openAuthor(sh) },
            onToggle = { sh -> togglePlay(sh) },
        )
        adapter.socialOf = { sh -> social[skey(sh.track)] }
        adapter.onLike = { sh -> toggleLike(sh) }
        adapter.onComments = { sh -> openComments(sh) }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.setBackgroundColor(Color.TRANSPARENT)
        PagerSnapHelper().attachToRecyclerView(list)
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                // ГРАБЛЯ: обновляли центр только по остановке ленты - при листании
                // эквалайзер оставался на прошлой обложке и «отрывался» от страницы.
                syncOrbNearest()
            }

            override fun onScrollStateChanged(rv: RecyclerView, state: Int) {
                if (state != RecyclerView.SCROLL_STATE_IDLE) return
                val vh = rv.findChildViewUnder(rv.width / 2f, rv.height / 2f) ?: return
                val pos = rv.getChildAdapterPosition(vh)
                if (pos >= 0 && pos != current) {
                    current = pos
                    playCurrent()
                    prefetchNext()
                    loadSocial(pos)
                    if (pos >= shorts.size - 3) loadMore()
                }
                syncEqOrb()
            }
        })
        list.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> syncEqOrb() }

        seedSource = intent.getStringExtra("short_source") ?: ""
        seedId = intent.getStringExtra("short_id") ?: ""
        restoreOrLoad()
    }

    /**
     * Открыли шортсы заново, а трек всё ещё играет - возвращаемся ИМЕННО к нему,
     * а не начинаем ленту с другого дропа. Список прошлой сессии помним в
     * companion object: activity закрывается, а музыка остаётся.
     */
    private fun restoreOrLoad() {
        // открыли КОНКРЕТНЫЙ трек (из профиля) - показываем его как шортс,
        // а остальную ленту догружаем следом
        if (seedSource.isNotEmpty() && seedId.isNotEmpty() && !seeded) {
            seeded = true
            loadSeededShort()
            return
        }
        val p = player
        val cur = p?.current()
        val prev = session
        val idx = if (cur != null) {
            prev.indexOfFirst { it.track.source == cur.source && it.track.id == cur.id }
        } else -1
        if (prev.isNotEmpty() && idx >= 0 && cur != null) {
            shorts.addAll(prev)
            current = idx
            playingId = cur.id
            waitStartedAt = System.currentTimeMillis()
            pendingSwitch = false
            val sh = shorts[idx]
            val pos = runCatching { p?.position ?: 0 }.getOrDefault(0)
            // если мы всё ещё ВНУТРИ дропа - продолжаем с места, а не с начала
            seeked = pos in (sh.start * 1000f).toInt()..(sh.end * 1000f).toInt()
            adapter.submit(shorts)
            val playingNow = p?.isPlaying == true
            if (!playingNow) notPlayingTicks = 6
            adapter.setPlayback(cur.id, !playingNow)
            loading.visibility = View.GONE
            finishedLoading = true
            list.scrollToPosition(idx)
            list.post { syncEqOrb() }
            bindEq(sh.track)
            loadSocial(idx)
            loadMore()
            return
        }
        load(initial = true)
    }

    /** Первый шортс - тот трек, на который нажали в профиле. */
    private fun loadSeededShort() {
        ApiClient.shortOf(seedSource, seedId) { items ->
            runOnUiThread {
                if (items.isNullOrEmpty()) {
                    load(initial = true)
                    return@runOnUiThread
                }
                shorts.addAll(items)
                adapter.submit(shorts)
                session = shorts.toList()
                loading.visibility = View.GONE
                finishedLoading = true
                current = 0
                playShort(0)
                prefetchNext()
                loadMore()
            }
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    // ------------------------------------------------- лайки и комментарии
    private val social = HashMap<String, ShortAdapter.Social>()
    private val commentsOf = HashMap<String, List<ApiClient.SocialComment>>()

    private fun skey(t: Track): String = t.source + ":" + t.id

    private fun loadSocial(from: Int) {
        for (i in from..minOf(from + 3, shorts.size - 1)) {
            val t = shorts[i].track
            val k = skey(t)
            if (social.containsKey(k)) continue
            social[k] = ShortAdapter.Social()   // чтобы не дублировать запрос
            ApiClient.trackSocial(t.source, t.id) { info ->
                if (info == null) {
                    social.remove(k)
                    return@trackSocial
                }
                social[k] = ShortAdapter.Social(info.liked, info.likes, info.comments.size)
                commentsOf[k] = info.comments
                val idx = shorts.indexOfFirst { skey(it.track) == k }
                if (idx >= 0) runOnUiThread { adapter.notifyItemChanged(idx) }
            }
        }
    }

    private fun toggleLike(sh: Short) {
        val t = sh.track
        val k = skey(t)
        val cur = social[k] ?: ShortAdapter.Social()
        ApiClient.toggleLike(t) { likes, liked ->
            social[k] = ShortAdapter.Social(liked, likes, cur.comments)
            val idx = shorts.indexOfFirst { skey(it.track) == k }
            if (idx >= 0) runOnUiThread { adapter.notifyItemChanged(idx) }
        }
    }

    private fun openComments(sh: Short) {
        val k = skey(sh.track)
        SocialUi.showCommentInput(this, sh.track, player) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        // после закрытия обновим счётчик
        handler.postDelayed({
            val t = sh.track
            ApiClient.trackSocial(t.source, t.id) { info ->
                if (info != null) {
                    social[k] = ShortAdapter.Social(info.liked, info.likes, info.comments.size)
                    commentsOf[k] = info.comments
                    val idx = shorts.indexOfFirst { skey(it.track) == k }
                    if (idx >= 0) runOnUiThread { adapter.notifyItemChanged(idx) }
                }
            }
        }, 1200L)
    }

    // ------------------------------------------------------------------ данные

    private fun load(initial: Boolean, refresh: Boolean = false) {
        if (loadingMore) return
        loadingMore = true
        // учитываем всё, что уже в ленте: иначе приходят те же дропы,
        // мы их отбрасываем - и шортсы «заканчиваются»
        val exclude = shorts.map { "${it.track.source}:${it.track.id}" }
        ApiClient.shorts(
            limit = if (initial) 8 else 6,
            refresh = refresh,
            exclude = exclude,
        ) { r ->
            runOnUiThread {
                loadingMore = false
                r.onSuccess { items ->
                    if (initial && shorts.isEmpty()) {
                        if (items.isEmpty()) {
                            retry()
                            return@onSuccess
                        }
                        shorts.addAll(items)
                        adapter.submit(shorts)
                        session = shorts.toList()
                        loading.visibility = View.GONE
                        finishedLoading = true
                        list.scrollToPosition(0)
                        list.post { syncEqOrb() }
                        playCurrent()
                        prefetchNext()
                        if (shorts.size < 3) loadMore()
                        return@onSuccess
                    }
                    val known = shorts.mapTo(HashSet()) { "${it.track.source}:${it.track.id}" }
                    val add = items.filter { "${it.track.source}:${it.track.id}" !in known }
                    if (add.isNotEmpty()) {
                        shorts.addAll(add)
                        adapter.append(add)
                        session = shorts.toList()
                        moreRefreshTried = false
                        moreRetryDelay = 2000L
                    } else if (!moreRefreshTried) {
                        // буфер отдал только то, что уже видели - просим свежее
                        moreRefreshTried = true
                        load(initial = false, refresh = true)
                    } else {
                        scheduleMore()
                    }
                }
                r.onFailure {
                    if (shorts.isEmpty()) retry() else scheduleMore()
                }
            }
        }
    }

    /** Лента бесконечная: если сервер пока не дал нового - тихо пробуем позже. */
    private fun scheduleMore() {
        if (isFinishing || isDestroyed) return
        val delay = moreRetryDelay
        moreRetryDelay = (moreRetryDelay * 2).coerceAtMost(20000L)
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                moreRefreshTried = false
                loadMore()
            }
        }, delay)
    }

    private fun loadMore() {
        if (!finishedLoading) return
        load(initial = false)
    }

    private fun retry() {
        if (isFinishing || isDestroyed) return
        tries++
        if (tries > 14) {
            loadingText.text = "Не получилось собрать ленту.\nПроверь связь и зайди ещё раз."
            return
        }
        loadingText.text = when {
            tries < 4 -> "Ищу дропы в новых треках…"
            tries < 9 -> "Ещё чуть-чуть — слушаю треки…"
            else -> "Почти готово…"
        }
        handler.postDelayed({ load(initial = shorts.isEmpty()) }, 1200L)
    }

    // ------------------------------------------------------------------ плеер

    private fun playCurrent() {
        val sh = shorts.getOrNull(current) ?: return
        if (playingId == sh.track.id) return
        playShort(current)
    }

    /** Запускает конкретный шортс (и переводит на него ленту). */
    private fun playShort(idx: Int) {
        val sh = shorts.getOrNull(idx) ?: return
        val p = player ?: return
        current = idx
        notPlayingTicks = 0
        adapter.setPlayback(sh.track.id, false)
        playingId = sh.track.id
        pendingSwitch = true
        seeked = false
        waitStartedAt = System.currentTimeMillis()
        p.play(sh.track, shorts.map { it.track })
        bindEq(sh.track)
        if (resumed) {
            runCatching { list.smoothScrollToPosition(idx) }
            list.post { syncEqOrb() }
        }
    }

    private fun prefetchNext() {
        val p = player ?: return
        val nxt = shorts.getOrNull(current + 1) ?: return
        runCatching { p.prefetch(nxt.track) }
    }

    /** Тап по обложке: пауза/продолжить. Для другого шортса - просто включаем его. */
    private fun togglePlay(sh: Short) {
        val p = player ?: return
        val cur = p.current()
        if (cur == null || cur.id != sh.track.id) {
            val idx = shorts.indexOfFirst {
                it.track.source == sh.track.source && it.track.id == sh.track.id
            }
            if (idx >= 0) playShort(idx)
            return
        }
        if (p.isPlaying) {
            p.pause()
            notPlayingTicks = 6
            adapter.setPlayback(cur.id, true)
        } else {
            p.play()
            notPlayingTicks = 0
            adapter.setPlayback(cur.id, false)
        }
    }

    private fun playFull(sh: Short) {
        val p = player
        if (p != null) {
            keepPlaying = true
            p.play(sh.track, listOf(sh.track))
        }
        // «слушать целиком» - это уже не лента: следующий вход в шортсы
        // должен начать новую подборку, а не возвращать сюда
        session = emptyList()
        finish()
    }

    private fun addTrack(sh: Short) {
        val t = sh.track
        // сначала в «Мою музыку» на сервере: работает и для гостя без VK/SC,
        // и именно это двигает волну/рекомендации
        ApiClient.libraryAdd(t) { ok ->
            runOnUiThread {
                if (ok) {
                    adapter.markAdded(t.id)
                    Toast.makeText(this, "Добавлено в «Мою музыку»", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Не получилось добавить", Toast.LENGTH_SHORT).show()
                }
            }
            if (!ok) return@libraryAdd
            // и в сам сервис, если он подключён (не критично)
            if (t.source == "vk") {
                ApiClient.vkAdd(t.id, t.accessKey) { _ -> }
            } else {
                ApiClient.scLike(t.id) { _ -> }
            }
        }
    }

    /**
     * Открыть карточку артиста: она живёт в MainActivity (singleTask),
     * поэтому просто шлём intent с именем - дальше отработает onNewIntent.
     */
    /** Свой трек - открываем профиль автора, иначе обычную карточку артиста. */
    private fun openAuthor(sh: Short) {
        val t = sh.track
        if (t.source == "own" && t.owner > 0) {
            runCatching {
                startActivity(
                    Intent(this, ProfileActivity::class.java)
                        .putExtra("user_id", t.owner)
                )
            }
            return
        }
        openArtistProfile(t.artist)
    }

    private fun openArtistProfile(raw: String?) {
        val name = (raw ?: "").trim()
        if (name.isEmpty()) return
        try {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    putExtra("artist_name", name)
                }
            )
        } catch (e: Throwable) {
            Toast.makeText(this, "Не получилось открыть артиста", Toast.LENGTH_SHORT).show()
        }
    }

    // ------------------------------------------------------------------ эквалайзер

    private fun bindEq(t: Track) {
        val url = ApiClient.img(t.artwork, 300)
        ImageLoader.bitmap(url, 120) { bmp -> eq.setArtwork(bmp) }
        ApiClient.beat(t.source, t.id) { r ->
            r.onSuccess { bm ->
                runOnUiThread {
                    if (shorts.getOrNull(current)?.track?.id == t.id) {
                        eq.setBeatData(bm.energy, bm.rate, bm.beats, bm.bands)
                    }
                }
            }
        }
    }

    /** Центр/радиус стеклянного кружка текущей страницы - вокруг него бьют лучи. */
    private fun syncEqOrb(retry: Boolean = true) {
        val vh = list.findViewHolderForAdapterPosition(current) as? ShortAdapter.VH
        if (vh == null) {
            // страница ещё не приклеилась после scrollToPosition - пробуем чуть позже
            if (retry) list.postDelayed({ syncEqOrb(false) }, 140L)
            return
        }
        val orb = vh.orb
        if (orb.width == 0 || eq.width == 0) {
            if (retry) list.postDelayed({ syncEqOrb(false) }, 140L)
            return
        }
        applyOrb(vh.art)
    }

    /**
     * Лучи всегда вокруг обложки той страницы, которая ближе к центру экрана, -
     * даже в момент, когда лента ещё едет под пальцем.
     */
    private fun syncOrbNearest() {
        if (list.childCount == 0 || eq.width == 0) return
        val mid = list.height / 2f
        var best: View? = null
        var bestD = Float.MAX_VALUE
        for (i in 0 until list.childCount) {
            val c = list.getChildAt(i) ?: continue
            val d = kotlin.math.abs(c.top + c.height / 2f - mid)
            if (d < bestD) {
                bestD = d
                best = c
            }
        }
        val vh = (best?.let { list.getChildViewHolder(it) } as? ShortAdapter.VH) ?: return
        applyOrb(vh.art)
    }

    /** Лучи считаем от самой обложки (не от стеклянного кружка) - так они
     *  всегда ровно по её краю, даже если лента едет. */
    private fun applyOrb(art: View) {
        if (art.width == 0 || eq.width == 0) return
        val a = IntArray(2)
        val b = IntArray(2)
        art.getLocationOnScreen(a)
        eq.getLocationOnScreen(b)
        val cx = (a[0] - b[0]) + art.width / 2f
        val cy = (a[1] - b[1]) + art.height / 2f
        eq.setOrb(cx, cy, minOf(art.width, art.height) / 2f + dp(6))
    }

    /** Комментарий, оставленный на этой секунде: «0:21 @sntroop: мясо».

     *  Появляется «из тумана» и так же уходит: альфа 0 -> 1 -> 0, а не
     *  резкий показ/скрытие.
     */
    private fun showCommentAt(posMs: Int) {
        val sh = shorts.getOrNull(current) ?: return
        val vh = list.findViewHolderForAdapterPosition(current) as? ShortAdapter.VH ?: return
        val all = commentsOf[skey(sh.track)].orEmpty()
        val near = if (all.isEmpty()) emptyList() else all
            .filter { it.pos <= posMs + 400L && it.pos >= posMs - 4000L }
            .sortedByDescending { it.pos }
            .take(2)
            .reversed()
        val text = near.joinToString("    ") {
            SocialUi.time(it.pos) + " " + it.name + ": " + it.text
        }
        if (text == vh.comment.tag as? String) return
        vh.comment.tag = text
        if (text.isEmpty()) {
            if (vh.comment.visibility == View.VISIBLE) {
                vh.comment.animate().alpha(0f).setDuration(320).withEndAction {
                    if ((vh.comment.tag as? String).isNullOrEmpty()) {
                        vh.comment.visibility = View.GONE
                    }
                }.start()
            }
            return
        }
        vh.comment.text = text
        vh.comment.visibility = View.VISIBLE
        vh.comment.alpha = 0f
        vh.comment.animate().alpha(1f).setDuration(320).start()
    }

    private fun setProgress(frac: Float) {
        val vh = list.findViewHolderForAdapterPosition(current) as? ShortAdapter.VH ?: return
        vh.prog.played = frac
    }

    private fun next() {
        if (current >= shorts.size - 1) {
            loadMore()
            // новый дроп ещё не приехал - повторяем текущий, а не замираем
            val p = player
            val sh = shorts.getOrNull(current)
            if (p != null && sh != null) {
                p.seekTo((sh.start * 1000f).toInt())
                seeked = true
            }
            return
        }
        playShort(current + 1)
        prefetchNext()
        if (current >= shorts.size - 3) loadMore()
    }

    /**
     * Раз в 120 мс: перематываем на дроп (когда плеер готов) и следим,
     * не доиграл ли он до конца куска - тогда едем к следующему.
     */
    private val tick = object : Runnable {
        override fun run() {
            val p = player
            val curId = p?.current()?.id
            if (p != null && curId != null) {
                if (curId == playingId) {
                    pendingSwitch = false
                } else if (!pendingSwitch) {
                    // трек переключили СНАРУЖИ (кнопка «Дальше» в уведомлении
                    // или на экране блокировки) - догоняем и прыгаем на его дроп
                    val idx = shorts.indexOfFirst { it.track.id == curId }
                    if (idx >= 0) {
                        current = idx
                        playingId = curId
                        seeked = false
                        waitStartedAt = System.currentTimeMillis()
                        bindEq(shorts[idx].track)
                        if (resumed) {
                            runCatching { list.scrollToPosition(idx) }
                            list.post { syncEqOrb() }
                        }
                    } else if (!resumed) {
                        // включили обычную музыку - не мешаем и засыпаем
                        return
                    }
                }
            }
            val sh = shorts.getOrNull(current)
            if (p != null && sh != null) {
                val playingThis = p.current()?.id == sh.track.id
                // пауза по тапу (или из уведомления): показываем кружок «плей»,
                // но не дёргаем его во время загрузки/буферизации
                if (playingThis && seeked && !p.isPlaying) {
                    notPlayingTicks++
                    if (notPlayingTicks == 5) adapter.setPlayback(sh.track.id, true)
                } else {
                    notPlayingTicks = 0
                    if (playingThis && p.isPlaying) adapter.setPlayback(sh.track.id, false)
                }
                if (playingThis && !seeked && p.duration > 0) {
                    p.seekTo((sh.start * 1000f).toInt())
                    seeked = true
                }
                if (playingThis && seeked) {
                    val from = (sh.start * 1000f).toInt()
                    var to = (sh.end * 1000f).toInt()
                    if (p.duration > 0) to = to.coerceAtMost(p.duration - 250)
                    val pos = p.position
                    val frac = if (to > from) {
                        ((pos - from).toFloat() / (to - from)).coerceIn(0f, 1f)
                    } else 0f
                    if (resumed) {
                        setProgress(frac)
                        showCommentAt(pos)
                    }
                    if (to > from && pos >= to) next()
                }
                // трек не завёлся (нет сети/битый) - не держим человека на нём
                if (!playingThis && resumed && waitStartedAt > 0 &&
                    System.currentTimeMillis() - waitStartedAt > 14_000
                ) {
                    waitStartedAt = System.currentTimeMillis()
                    next()
                }
            }
            // и в фоне тоже: шортсы должны сами ехать дальше
            handler.postDelayed(this, 120L)
        }
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        handler.removeCallbacks(tick)
        handler.post(tick)
        eq.setActive(true)
        list.post {
            // пока были в фоне лента могла уехать дальше - встаём на текущий
            runCatching { list.scrollToPosition(current) }
            list.postDelayed({ syncEqOrb() }, 60L)
        }
        val sh = shorts.getOrNull(current)
        if (sh != null) bindEq(sh.track)
    }

    override fun onPause() {
        resumed = false
        // ВАЖНО: тикер НЕ останавливаем и трек НЕ паузим - шортсы должны
        // продолжать играть и листаться, даже если человек свернул приложение.
        eq.setActive(false)   // фоновую отрисовку гасим (экономия батареи)
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        eq.release()
        super.onDestroy()
    }

    private companion object {
        /** Лента прошлого открытия: при повторном входе возвращаемся к тому
         *  же треку, который сейчас играет, а не начинаем с другого дропа. */
        var session: List<Short> = emptyList()
    }
}
