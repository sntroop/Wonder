package su.gwarden.music.player

import android.content.Context
import android.media.AudioAttributes
import android.util.Log
import androidx.media3.common.AudioAttributes as ExoAttr
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import su.gwarden.music.Diag
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.Track
import java.io.File

/**
 * Плеер с ПРОГРЕССИВНЫМ воспроизведением.
 *
 * Схема:
 *  1) Трек качается в фоне ПАРАЛЛЕЛЬНЫМИ сегментами через Range
 *     (короткие запросы - доезжают через DPI владельца).
 *  2) MediaPlayer подключается к ЛОКАЛЬНОМУ HTTP-серверу (127.0.0.1),
 *     который отдаёт уже скачанные байты и ждёт остальные.
 *  3) Итог: звук идёт с первого чанка (~1%), остальное докачивается на ходу.
 *
 * Если файл уже в кэше - играем локальный файл напрямую (мгновенно).
 */
class PlayerController(private val context: Context) {

    /** Дольше этого (15 минут) - играем потоком и не качаем целиком. */
    private val LONG_TRACK_SEC = 15 * 60

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val downloader = ParallelDownloader()
    private var downloadJob: Job? = null

    private var exo: ExoPlayer? = null
    private var localServer: LocalStreamServer? = null
    private var queue: List<Track> = emptyList()
    private var index: Int = -1
    private var activeDownload: ParallelDownloader.Download? = null

    // тихая обработка сбоев MediaPlayer (-38 и подобные): перезапуск -> пропуск
    private var errTrackId: String? = null
    private var errCount = 0
    private var skipStreak = 0

    // 0 - играем с локального сервера, 1 - напрямую с сервера, 2 - замена
    private var errStage = 0

    /** Файл кэша текущего трека - если он битый, удаляем и качаем заново. */
    private var cacheFile: File? = null
    private var playingFromCache = false
    /** Откуда играем сейчас - только для подписи ошибок (file/stream/remote). */
    private var currentMode = "file"

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
    private var focusRequest: android.media.AudioFocusRequest? = null
    private var lastErrAudio = false
    private var watchdog: Job? = null

    /**
     * Номер попытки воспроизведения. Растёт на каждом новом старте трека.
     * Отложенные действия (перезапуск, прямой стрим, замена) проверяют его,
     * иначе «старый» колбэк мог стартовать второй плеер поверх нового трека.
     */
    private var playGen = 0
    private val replacing = HashSet<String>()

    var onState: ((Boolean) -> Unit)? = null
    var onTrackChanged: ((Track?) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onProgress: ((Int, String) -> Unit)? = null

    /** Как достать прямой URL VK. */
    var localVkUrl: ((Track) -> String?)? = null

    /**
     * Поиск замены треку, который VK/сервер не отдают: та же песня на
     * SoundCloud. Реализует MainActivity через подбор замены.
     */
    var findReplacement: ((Track, (Track?) -> Unit) -> Unit)? = null

    /** Очередь закончилась (для бесконечного радио вроде «Моей волны»). */
    var onQueueEnd: (() -> Unit)? = null

    val isPlaying: Boolean get() = runCatching { exo?.isPlaying == true }.getOrDefault(false)

    /** VK точно не сыграет: VK недоступен И рабочего токена нет. */
    private fun vkVeto(): Boolean =
        su.gwarden.music.vk.VkRelay.vkDead && !su.gwarden.music.vk.VkApi.hasToken()

    /** Скачанный «навсегда» файл трека (null - не скачан). Проверка дешёвая. */
    private fun offlineFile(t: Track): File? =
        runCatching { su.gwarden.music.offline.Offline.fileFor(t) }.getOrNull()

    fun current(): Track? = queue.getOrNull(index)

    /** Сколько треков ещё впереди (для прелоада радио). */
    val remaining: Int get() = (queue.size - index - 1).coerceAtLeast(0)

    /** Дописать треки в конец очереди (бесконечное радио). */
    fun append(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val ids = queue.mapTo(HashSet()) { it.id }
        val add = tracks.filter { it.id !in ids }
        if (add.isEmpty()) return
        queue = queue + add
    }

    fun play(track: Track, queue: List<Track>) {
        // Без рабочего VK на телефоне VK-треки не заиграют:
        // ходит в VK через этот же телефон. Не начинаем - иначе бесконечные
        // 401 на поток и бит-карту и «тормоза».
        // блокируем VK только если он реально мёртв (нет токена), а не
        // из-за одной случайной ошибки: иначе «В моей музыке играет только SC»
        val vkDead = vkVeto()
        // скачанный трек играет и без VK - файл уже на телефоне
        if (track.source == "vk" && vkDead && offlineFile(track) == null) {
            onProgress?.invoke(-1, "VK недоступен: войдите в VK")
            return
        }
        val clean = if (vkDead) {
            queue.filter { it.source != "vk" || offlineFile(it) != null }
        } else queue
        this.queue = if (clean.isEmpty()) listOf(track) else clean
        this.index = this.queue.indexOfFirst { it.id == track.id }
        if (this.index < 0) {
            this.index = 0
            this.queue = listOf(track)
        }
        start(track)
    }

    fun resume() {
        val p = exo ?: return
        if (p.mediaItemCount == 0) return
        runCatching { p.playWhenReady = true }
        onState?.invoke(true)
    }

    fun pause() {
        runCatching { exo?.playWhenReady = false }
        onState?.invoke(false)
    }

    fun play() = resume()

    /**
     * Закрыть текущий трек из нижнего бара: остановить звук, забыть очередь
     * и убрать плашку с уведомлением. Следующий запуск начнётся с чистого
     * листа (как будто ничего не играло).
     */
    fun stop() {
        playGen++
        flushFeedback()
        downloadJob?.cancel()
        activeDownload = null
        runCatching { exo?.stop() }
        releasePlayer()
        queue = emptyList()
        index = -1
        onProgress?.invoke(-1, "")
        onState?.invoke(false)
        onTrackChanged?.invoke(null)
        runCatching { PlaybackService.hide(context) }
    }

    /**
     * Следующий трек. force=true - только для автоперехода, когда трек
     * доиграл: там ждать нечего, start() сам скачает и заиграет.
     * force=false (кнопка «Дальше»): если следующий ещё не скачан - НЕ
     * переключаемся, докачиваем и ждём (кнопка в интерфейсе серая).
     */
    fun next(force: Boolean = false): Track? {
        var i = index + 1
        if (vkVeto()) {
            // при мёртвом VK пропускаем только НЕскачанные VK-треки
            while (i < queue.size && queue[i].source == "vk" && offlineFile(queue[i]) == null) i++
        }
        val nxt = queue.getOrNull(i)
        if (nxt == null) {
            onQueueEnd?.invoke()
            return null
        }
        if (!force && !nextReady) {
            nextPressedWhileLoading++
            if (nextPressedWhileLoading < 3) {
                Log.i(TAG, "next not ready yet: ${nxt.id}")
                onProgress?.invoke(-1, "Дальше: качаю ${nxt.title}")
                prefetch(nxt)
                notifyReady()
                return null
            }
            // нажали трижды - не держим силой, переключаемся (там сработает
            // восстановление: скачивание и повтор)
            Log.w(TAG, "next pressed while loading x$nextPressedWhileLoading - switch anyway")
        }
        nextPressedWhileLoading = 0
        index = i
        start(nxt)
        return nxt
    }

    /** Следующий трек очереди без переключения (для подписи «качаю ...»). */
    fun peekNext(): Track? = queue.getOrNull(index + 1)

    fun prev(): Track? {
        if (index <= 0) return null
        index--
        start(current()!!)
        return current()
    }

    /** Аудиосессия текущего MediaPlayer - к ней цепляется визуализатор. */
    val audioSessionId: Int get() = runCatching { exo?.audioSessionId ?: 0 }.getOrDefault(0)

    /**
     * Готов ли следующий трек (скачан целиком). Пока не готов - кнопка
     * «Дальше» серая, и мы НЕ переключаемся: лучше подождать загрузку,
     * чем молча проскочить трек.
     */
    var onReadyChanged: ((Boolean) -> Unit)? = null
    private var lastReady: Boolean? = null
    private var retryCount = 0
    private var retryTrackId: String? = null

    /**
     * Забыть трек: удаляем локальную копию и «скачано навсегда».
     * Нужно после удаления своего трека - иначе он играет из кэша телефона.
     */
    fun forget(t: Track) {
        val dir = File(context.cacheDir, "tracks")
        runCatching {
            File(dir, "${t.source}_${t.id}.mp3").delete()
            File(dir, "${t.source}_${t.id}.mp3.ok").delete()
            File(dir, "${t.source}_${t.id}.mp3.pfc").delete()
        }
        runCatching { su.gwarden.music.offline.Offline.remove(t) }
        runCatching { su.gwarden.music.model.LocalLibrary.remove(t) }
        if (current()?.id == t.id) {
            runCatching { stop() }
        }
    }

    fun isCached(t: Track): Boolean {
        val dir = File(context.cacheDir, "tracks")
        val f = File(dir, "${t.source}_${t.id}.mp3")
        return File(f.path + ".ok").exists() && f.exists() && f.length() > 1024
    }

    val nextReady: Boolean
        get() {
            val n = queue.getOrNull(index + 1) ?: return false
            if (isCached(n)) return true
            // скачать не получилось - не запираем человека на этом треке
            return synchronized(prefetchFailed) { n.id in prefetchFailed }
        }

    private fun notifyReady() {
        val r = nextReady
        if (r != lastReady) {
            lastReady = r
            onReadyChanged?.invoke(r)
        }
    }

    // ---- анонимная статистика для «Моей волны» ----
    private var fbTrack: Track? = null
    private var fbRewinds = 0
    private var fbSent = false
    /** Откуда включили трек: wave / similar / search / my / playlist. */
    var playSource: String = "wave"
    /** Сколько трека реально прослушано (битовые корзины по 1% длины). */
    private val fbBuckets = BooleanArray(100)
    private var fbTicker: Job? = null
    private var fbForwardSeeks = 0

    private fun startFbTicker() {
        fbTicker?.cancel()
        fbTicker = scope.launch {
            while (true) {
                delay(1500)
                if (!isPlaying) continue
                val d = duration
                if (d <= 0) continue
                val b = ((position.toLong() * fbBuckets.size) / d).toInt()
                    .coerceIn(0, fbBuckets.size - 1)
                fbBuckets[b] = true
            }
        }
    }

    private fun fbCoverage(): Float {
        var n = 0
        for (b in fbBuckets) if (b) n++
        return n.toFloat() / fbBuckets.size
    }

    /** Сколько трека уже скачано, 0..100 (для белой полоски буфера). */
    var bufferedPct: Int = 0
        private set

    fun seekTo(ms: Int) {
        val cur = position
        runCatching { exo?.seekTo(ms.toLong().coerceAtLeast(0L)) }
        // перемотал НАЗАД послушать ещё - сильный признак «нравится»
        if (ms < cur - 5_000) {
            fbRewinds++
            if (fbRewinds == 1) {
                fbTrack?.let {
                    ApiClient.feedback("replay", it.source, it.id, it.artist, cur, ms,
                        duration, fbCoverage(), playSource, hour())
                }
            }
        } else if (ms > cur + 20_000) {
            // перескочил вперёд - значит эта часть не нужна (для полноты картины)
            fbForwardSeeks++
        }
    }

    /** Трек добавили в «Мою музыку» - тоже сигнал вкуса. */
    fun feedbackAdd(t: Track) {
        ApiClient.feedback("add", t.source, t.id, t.artist, 0, 0, t.duration * 1000,
            0f, playSource, hour())
        Diag.add("fb add ${t.id}")
    }

    /** Явное «не нравится» - самый сильный сигнал против. */
    fun feedbackDislike(t: Track) {
        ApiClient.feedback("dislike", t.source, t.id, t.artist, 0, 0, t.duration * 1000,
            0f, playSource, hour())
        Diag.add("fb dislike ${t.id}")
    }

    private fun hour(): Int = runCatching {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }.getOrDefault(-1)

    /**
     * Закрываем статистику по текущему треку: дослушал / слушал / скипнул.
     * Вызывается при смене трека и при выходе.
     */
    private fun flushFeedback() {
        val t = fbTrack ?: return
        if (fbSent) return
        fbSent = true
        val live = t.id == current()?.id
        val played = if (live) position.coerceAtLeast(0) else 0
        val dur = if (live) duration else 0
        val durMs = if (dur > 0) dur else t.duration * 1000
        val ratio = if (durMs > 0) played.toFloat() / durMs else 0f
        // покрытие важнее «финальной позиции»: перемотки назад и паузы
        // учитываются как реально прослушанное
        val cov = fbCoverage()
        val ev = when {
            cov >= 0.8f -> "complete"
            cov >= 0.45f -> "listen"
            cov >= 0.2f -> if (played < 20_000) "skip" else "skip_late"
            played < 12_000 -> "skip"
            else -> "skip_late"
        }
        ApiClient.feedback(ev, t.source, t.id, t.artist, played, played, durMs,
            cov, playSource, hour())
        Diag.add("fb $ev cov=${(cov * 100).toInt()}% ${played / 1000}с ${t.id}")
    }

    /** Текущая позиция, мс. */
    val position: Int get() = runCatching { exo?.currentPosition?.toInt() ?: 0 }.getOrDefault(0)

    /** Длительность трека, мс. */
    val duration: Int
        get() = runCatching {
            val d = exo?.duration ?: 0L
            if (d == C.TIME_UNSET || d <= 0) 0 else d.toInt()
        }.getOrDefault(0)

    @Volatile
    private var prefetching = false

    /** Тихо качаем трек в кэш, чтобы он потом стартовал мгновенно. */
    private val prefetchIds = HashSet<String>()
    private val prefetchJobs = HashMap<String, Job>()
    /** Треки, которые предзагрузка не смогла скачать (кнопка «Дальше» — активна). */
    private val prefetchFailed = HashSet<String>()
    /** Сколько раз жали «Дальше», пока следующий не скачан (аварийный выход). */
    private var nextPressedWhileLoading = 0

    /**
     * Тихо качаем трек в кэш ЦЕЛИКОМ (сегментами, как обычное воспроизведение).
     * Раньше качали одним соединением - было медленно и обрывалось.
     */
    fun prefetch(track: Track) {
        if (track.id == current()?.id) return
        // длинные миксы/подкасты не префетчим целиком (сотни мегабайт)
        if (track.duration > LONG_TRACK_SEC) return
        val dir = File(context.cacheDir, "tracks").apply { mkdirs() }
        val dest = File(dir, "${track.source}_${track.id}.mp3")
        if (dest.exists() && File(dest.path + ".ok").exists()) return
        synchronized(prefetchIds) {
            if (!prefetchIds.add(track.id)) return
        }
        val url = ApiClient.streamUrl(track.source, track.id)
        val job = scope.launch {
            val ok = withContext(Dispatchers.IO) {
                val part = File(dest.path + ".pfc")
                runCatching { if (part.exists()) part.delete() }
                val good = fetchWholeBlocking(url, part) { }
                // трек включили вручную - результат этой загрузки уже не нужен
                if (good && part.length() > 1024 && isActive) {
                    runCatching { if (dest.exists()) dest.delete() }
                    part.renameTo(dest)
                    runCatching { File(dest.path + ".ok").writeText("ok") }
                    true
                } else {
                    runCatching { part.delete() }
                    false
                }
            }
            if (ok) {
                Log.i(TAG, "prefetched ${track.id}")
                Diag.add("prefetch ok ${track.id}")
                synchronized(prefetchFailed) { prefetchFailed.remove(track.id) }
            } else {
                synchronized(prefetchFailed) { prefetchFailed.add(track.id) }
            }
            notifyReady()
            synchronized(prefetchIds) { prefetchIds.remove(track.id) }
            synchronized(prefetchJobs) { prefetchJobs.remove(track.id) }
        }
        synchronized(prefetchJobs) { prefetchJobs[track.id] = job }
    }

    private fun prefetchNext() {
        val n = if (su.gwarden.music.eco.Eco.enabled()) 1 else 2
        for (k in 1..n) {
            val t = queue.getOrNull(index + k) ?: break
            prefetch(t)
        }
    }

    private fun start(track: Track) {
        // свой трек автора: считаем просмотр в его профиле
        if (track.source == "own") {
            runCatching { ApiClient.ownPlay(track.id) }
        }
        // Скачан «навсегда»? Тогда играем файл с телефона и VK нам не нужен.
        val offline = offlineFile(track)
        // VK недоступен (гость/протух токен) - трек не заиграет: молча
        // останавливаемся, а не крутим загрузку по кругу.
        if (track.source == "vk" && vkVeto() && offline == null) {
            Log.w(TAG, "skip vk (VK недоступен): ${track.id}")
            Diag.add("skip vk ${track.id}")
            ApiClient.log("skip_vk", "t=${track.id}")
            onProgress?.invoke(-1, "VK недоступен: войдите в VK")
            onState?.invoke(false)
            return
        }
        // MediaPlayer можно создавать только в потоке с Looper: иначе его
        // колбэки не приходят и он падает с -38 без причины
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            scope.launch { start(track) }
            return
        }
        // Замена «slowed»-версии может оказаться speed-up'ом (и наоборот):
        // подгоняем темп, чтобы трек звучал с той же скоростью, что в VK.
        runCatching { exo?.setPlaybackSpeed(track.speed.coerceIn(0.7f, 1.45f)) }
        // Офлайн: если трек скачан «навсегда» - играем файл с телефона.
        // Ни сети, ни VK, ни сервера: работает в метро и при мёртвом VK.
        // САМО ЗАПУСКАНИЕ делаем НИЖЕ, вместе с обновлением интерфейса:
        // раньше офлайн-ветка выходила раньше onTrackChanged, и в нижнем баре
        // оставался предыдущий трек (звук новый, а плашка старая).
        // закрываем статистику по предыдущему треку ДО остановки плеера
        flushFeedback()
        val gen = ++playGen
        errStage = 0
        // если этот трек сейчас тихо качается предзагрузкой - отменяем её,
        // иначе две загрузки пишут один файл и мешают друг другу
        synchronized(prefetchJobs) { prefetchJobs.remove(track.id) }?.cancel()
        synchronized(prefetchIds) { prefetchIds.remove(track.id) }
        synchronized(prefetchFailed) { prefetchFailed.remove(track.id) }
        // новый трек - начинаем счётчик восстановлений заново
        if (retryTrackId != track.id) {
            retryTrackId = track.id
            retryCount = 0
        }
        // Отменяем предыдущее - иначе при частых тапах накапливаются
        // параллельные загрузки и рвётся путь.
        downloadJob?.cancel()
        stopLocalServer()
        // останавливаем текущее воспроизведение, но плеер не освобождаем:
        // ExoPlayer сам аккуратно освобождает аудиотрек
        runCatching { exo?.stop() }
        runCatching { exo?.clearMediaItems() }
        // старую загрузку ЗАКРЫВАЕМ: иначе её воркеры продолжают писать в тот же
        // файл (при повторе того же трека) и корёжат данные
        runCatching { activeDownload?.close() }
        activeDownload = null
        bufferedPct = 0
        if (errTrackId != track.id) errStage = 0
        // офлайн-файл не кэш трека: если он не заиграет, retrySame не должен
        // удалять файл ПРЕДЫДУЩЕГО трека
        playingFromCache = false
        cacheFile = null

        // история «Недавно слушали» (последние 30 треков на аккаунт)
        runCatching { ApiClient.recent(track) }
        fbTrack = track
        fbRewinds = 0
        fbSent = false
        fbForwardSeeks = 0
        fbBuckets.fill(false)
        startFbTicker()
        Diag.add("start ${track.id} ${track.artist} - ${track.title}")
        onTrackChanged?.invoke(track)
        // цвет приложения берём из обложки ЛЮБОГО запущенного трека,
        // не только когда открыт экран «Моя волна»
        runCatching {
            su.gwarden.music.ui.ArtworkTint.applyUrl(ApiClient.img(track.artwork, 300))
        }
        notifyReady()

        if (offline != null) {
            // файл уже на телефоне: интерфейс обновлён выше, просто играем
            Diag.add("offline play ${track.id}")
            bufferedPct = 100
            playLocalFile(offline)
            onProgress?.invoke(100, "Офлайн")
            onState?.invoke(true)
            prefetchNext()
            return
        }

        onState?.invoke(false)
        onProgress?.invoke(-1, "Подготовка: ${track.title}")

        val cacheDir = File(context.cacheDir, "tracks").apply { mkdirs() }
        val dest = File(cacheDir, "${track.source}_${track.id}.mp3")
        val okMarker = File(dest.path + ".ok")
        cacheFile = dest

        // Очень длинный трек (микс/подкаст/альбом одним файлом) НЕ качаем
        // целиком: 72-минутный файл - это сотни мегабайт. Играем потоком
        // (прямой поток с Range, без кэша на диск).
        if (track.duration > LONG_TRACK_SEC && !isCached(track)) {
            Log.i(TAG, "long track ${track.id} (${track.duration}s) -> direct stream")
            Diag.add("long stream ${track.id}")
            bufferedPct = 100
            onProgress?.invoke(-1, "Длинный трек — играю потоком")
            playRemoteUrl(ApiClient.streamUrl(track.source, track.id, direct = true))
            prefetchNext()
            notifyReady()
            return
        }

        val url = ApiClient.streamUrl(track.source, track.id)
        val t0 = System.currentTimeMillis()

        downloadJob = scope.launch {
            try {
                // .ok пишем только когда файл докачан ЦЕЛИКОМ, иначе
                // прерванная загрузка играла бы обрезанный трек
                val cached = withContext(Dispatchers.IO) {
                    okMarker.exists() && dest.exists() && dest.length() > 1024
                }
                playingFromCache = cached
                if (cached) {
                    Log.i(TAG, "cache hit, playing local file")
                    bufferedPct = 100
                    onProgress?.invoke(100, "Кэш")
                    playLocalFile(dest)
                    prefetchNext()
                    notifyReady()
                    return@launch
                }

                // Трек может быть недоступен по региону -
                // сразу спрашиваем прямую ссылку у VK С ТЕЛЕФОНА, не тратя
                // время на заведомо неудачную загрузку
                if (track.playable == false && track.source == "vk") {
                    val direct = withContext(Dispatchers.IO) {
                        runCatching { localVkUrl?.invoke(track) }.getOrNull()
                    }
                    if (!direct.isNullOrEmpty()) {
                        Log.i(TAG, "playable=false -> local VK url")
                        bufferedPct = 100
                        onProgress?.invoke(100, "VK")
                        playRemoteUrl(direct)
                        prefetchNext()
                        return@launch
                    }
                    // телефон тоже не смог - сразу замена, без doomed-загрузки
                    requestReplacement(track)
                    return@launch
                }

                // СКАЧИВАЕМ ЦЕЛИКОМ и играем с диска (одним соединением,
                // с докачкой). На этом телефоне MediaPlayer падает с -38 на
                // http-потоке, а готовый файл играет.
                runCatching { if (dest.exists()) dest.delete() }
                val done = withContext(Dispatchers.IO) {
                    fetchWholeBlocking(url, dest) { pct ->
                        bufferedPct = pct
                        scope.launch { onProgress?.invoke(pct, "Загрузка: ${track.title}") }
                    }
                }
                if (gen != playGen) return@launch
                // ВАЖНО: неполный файл не играем никогда - иначе MediaPlayer
                // получает мусор и падает с -38
                if (!done) throw IllegalStateException("не докачалось")
                if (dest.length() < 1024) throw IllegalStateException("пустой файл")
                bufferedPct = 100
                onProgress?.invoke(100, "Готово")
                // замер: сколько заняла загрузка и сколько весит трек
                val ms = System.currentTimeMillis() - t0
                ApiClient.log("dl_time", "${ms}мс ${dest.length() / 1024}КБ ${track.id}")
                Diag.add("dl ${ms}мс ${dest.length() / 1024}КБ")
                runCatching { okMarker.writeText("ok") }
                Log.i(TAG, "играем целый файл (${dest.length()} Б)")
                playLocalFile(dest)
                armWatchdog(gen, track)
                prefetchNext()
                notifyReady()
            } catch (e: Exception) {
                // отмена предыдущей загрузки при новом тапе - это не ошибка
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.i(TAG, "download cancelled (new track)")
                    return@launch
                }
                Log.e(TAG, "start failed: ${e.message}")
                ApiClient.log("dl_fail", "${e.message} t=${track.id}")
                Diag.add("dl_fail ${e.message} t=${track.id}")
                // трек закрыт по региону - пробуем
                // спросить прямую ссылку у VK С ТЕЛЕФОНА
                val direct = withContext(Dispatchers.IO) {
                    runCatching { localVkUrl?.invoke(track) }.getOrNull()
                }
                if (!direct.isNullOrEmpty()) {
                    Log.i(TAG, "fallback to local VK url")
                    bufferedPct = 100
                    onProgress?.invoke(100, "VK")
                    playRemoteUrl(direct)
                    return@launch
                }
                // своего источника нет - ищем ту же песню на SoundCloud
                Log.w(TAG, "no source (${e.message}), looking for replacement")
                requestReplacement(track)
                return@launch
            }
        }
    }

    /**
     * Сбой MediaPlayer. Раньше мы показывали «Ошибка плеера: -38/0» тостом -
     * владельца это бесило, а лечится почти всегда простым перезапуском.
     *
     * Поэтому: 1-2 раза молча перезапускаем тот же трек, если не помогло -
     * молча переходим к следующему. Сообщение показываем только если подряд
     * не заиграло 6 треков (тогда проблема не в треке, а в сети/сервере).
     */
    private fun onPlaybackError(reason: String) {
        val t = current()
        Log.e(TAG, "playback error: $reason (track=${t?.id}, stage=$errStage)")
        ApiClient.log("play_err", "$reason t=${t?.id} stage=$errStage")
        Diag.add("err stage=$errStage $reason t=${t?.id}")
        lastErrAudio = reason.contains("-38")
        onState?.invoke(false)
        if (t == null) {
            onError?.invoke(reason)
            return
        }
        // битый файл в кэше - самая частая причина -38: prefetch мог записать
        // обрезанный mp3 и пометить его как готовый. Удаляем - скачаем заново.
        if (playingFromCache) {
            cacheFile?.let { f ->
                runCatching { if (f.exists()) f.delete() }
                runCatching { File(f.path + ".ok").delete() }
            }
        }
        playingFromCache = false

        // Плеер освобождаем ВСЕГДА (на Android живёт ограниченное число
        // AudioTrack, «-38» = нельзя создать новый). А вот загрузку НЕ трогаем:
        // на стадии 1 она ещё нужна - докачиваем файл и играем с диска.
        runCatching { exo?.stop() }
        runCatching { exo?.clearMediaItems() }

        errStage++
        if (errStage != 1) {
            stopLocalServer()
            runCatching { activeDownload?.close() }
            activeDownload = null
        }
        when (errStage) {
            // 1) файл не сыграл (редко) - пробуем прямой поток с сервера
            1 -> {
                val gen = playGen
                val url = ApiClient.streamUrl(t.source, t.id)
                scope.launch {
                    delay(400)
                    if (gen == playGen && current()?.id == t.id) playRemoteUrl(url)
                }
            }
            // 2) ищем ту же песню на SoundCloud
            2 -> requestReplacement(t)
            // 3) совсем не вышло - НЕ пропускаем, а пробуем ещё раз
            else -> {
                errStage = 0
                retrySame(t)
            }
        }
    }

    /**
     * Источник недоступен (VK закрыл трек по региону и телефон его тоже не
     * отдал). Ищем ту же песню на SoundCloud и играем её вместо оригинала;
     * не нашли - молча идём дальше по очереди.
     */
    private fun requestReplacement(track: Track) {
        val finder = findReplacement
        ApiClient.log("replace", "ask t=${track.id} ${track.artist} - ${track.title}")
        onProgress?.invoke(-1, "Ищу другой источник: ${track.title}")
        if (finder == null || !replacing.add(track.id)) {
            Log.w(TAG, "no replacement path for ${track.id}")
            retrySame(track)
            return
        }
        finder(track) { sub ->
            replacing.remove(track.id)
            if (current()?.id != track.id) {
                Log.i(TAG, "replacement arrived too late for ${track.id}")
                return@finder
            }
            if (sub == null || (sub.id == track.id && sub.source == track.source)) {
                Log.w(TAG, "replacement not found: ${track.id}")
                retrySame(track)
                return@finder
            }
            // оставляем название/артиста/обложку ОРИГИНАЛА - меняем только
            // источник, чтобы в интерфейсе ничего не прыгало
            val rep = Track(
                id = sub.id,
                source = sub.source,
                title = track.title.ifEmpty { sub.title },
                artist = track.artist.ifEmpty { sub.artist },
                duration = if (track.duration > 0) track.duration else sub.duration,
                artwork = track.artwork ?: sub.artwork,
                permalink = sub.permalink,
                playable = true,
            )
            Log.i(TAG, "replacement: ${rep.source}/${rep.id} for ${track.id}")
            ApiClient.log("replace", "found ${rep.source}/${rep.id} for ${track.id}")
            Diag.add("replace -> ${rep.source}/${rep.id}")
            queue = queue.map {
                if (it.id == track.id && it.source == track.source) rep else it
            }
            start(rep)
        }
    }

    /**
     * НИКОГДА не пропускаем трек сами. Не заиграло - значит либо файл побился,
     * либо у телефона «залип» аудиотрек: освобождаем плеер ЦЕЛИКОМ и пробуем
     * снова ЭТОТ ЖЕ трек с нарастающей паузой. Дальше человек сам решит.
     */
    private fun retrySame(track: Track) {
        retryCount++
        if (retryCount > 5) {
            // совсем не выходит - честно говорим и останавливаемся (без скипа)
            retryCount = 0
            retryTrackId = null
            onState?.invoke(false)
            onProgress?.invoke(-1, "Не удалось воспроизвести")
            onError?.invoke("Не заиграло: ${track.artist} — ${track.title}. Нажми «Дальше»")
            return
        }
        val waitMs = (600L * retryCount).coerceAtMost(6000L)
        ApiClient.log("retry", "#$retryCount ${waitMs}мс t=${track.id}")
        Diag.add("retry #$retryCount t=${track.id}")
        onProgress?.invoke(-1, "Не заиграло · пробую снова ($retryCount)")
        // со второй попытки файл считаем подозрительным - скачаем заново
        if (retryCount >= 2) {
            cacheFile?.let { f ->
                runCatching { if (f.exists()) f.delete() }
                runCatching { File(f.path + ".ok").delete() }
            }
        }
        playingFromCache = false
        // -38 = телефон не выдал аудиотрек: старый MediaPlayer уже бесполезен,
        // освобождаем его целиком (следующий старт создаст новый)
        releasePlayer()
        errStage = 0
        onState?.invoke(false)
        val gen = playGen
        scope.launch {
            delay(waitMs)
            if (gen == playGen && current()?.id == track.id) start(track)
        }
    }

    /**
     * Аудио-фокус. Без него некоторые прошивки (Xiaomi/Samsung) просто не дают
     * создать новый аудиотрек - MediaPlayer падает с -38.
     */
    private fun requestFocus() {
        val am = audioManager ?: return
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                val req = android.media.AudioFocusRequest
                    .Builder(android.media.AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attrs)
                    .setOnAudioFocusChangeListener { }
                    .build()
                focusRequest = req
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null, android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.AUDIOFOCUS_GAIN,
                )
            }
        }
    }

    private fun abandonFocus() {
        val am = audioManager ?: return
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
            focusRequest = null
        }
    }

    /**
     * Сторож старта: если за 15с трек так и не заиграл (плеер «повис»),
     * не ждём вечно и не роняем приложение - идём по цепочке восстановления.
     */
    private fun armWatchdog(gen: Int, track: Track) {
        watchdog?.cancel()
        watchdog = scope.launch {
            delay(15_000)
            if (gen == playGen && current()?.id == track.id && !isPlaying) {
                ApiClient.log("watchdog", "нет звука 15с t=${track.id}")
                onPlaybackError("watchdog: не заиграло за 15с")
            }
        }
    }

    /**
     * ОДИН ExoPlayer на всё приложение.
     *
     * Раньше здесь был системный android.media.MediaPlayer: на Samsung
     * (Android 16) он падал с -38 (ENOSYS) - сначала на http-потоке, потом
     * вообще на любом файле, причём «волной»: один сбой - и не играет ничего.
     * ExoPlayer декодирует сам и переживает такие состояния.
     */
    private fun ensurePlayer(): ExoPlayer {
        val e = exo
        if (e != null) return e
        val np = ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            // без wake-режима при погашенном экране воспроизведение встаёт
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
        runCatching {
            np.setAudioAttributes(
                ExoAttr.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true, // аудио-фокус ExoPlayer берёт на себя
            )
            np.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            watchdog?.cancel()
                            errTrackId = null
                            errCount = 0
                            skipStreak = 0
                            errStage = 0
                            retryCount = 0
                            nextPressedWhileLoading = 0
                            notifyReady()
                            Log.i(TAG, "prepared $currentMode")
                            onState?.invoke(np.isPlaying)
                        }
                        Player.STATE_ENDED -> {
                            Log.i(TAG, "completed $currentMode")
                            onNextAuto()
                        }
                        else -> Unit
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    onState?.invoke(isPlaying)
                }

                override fun onPlayerError(error: PlaybackException) {
                    onPlaybackError("$currentMode ${error.errorCodeName}")
                }
            })
        }
        exo = np
        // новый плеер - вернуть темп текущего трека (замены slowed/speed-up)
        runCatching {
            np.setPlaybackSpeed((current()?.speed ?: 1f).coerceIn(0.7f, 1.45f))
        }
        return np
    }

    /** Полностью освободить плеер (после сбоя / при выходе). */
    private fun releasePlayer() {
        val p = exo
        exo = null
        if (p == null) return
        runCatching { p.stop() }
        runCatching { p.release() }
    }

    private val dlClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Скачивание ЦЕЛОКОМ одним соединением, с докачкой (Range) при обрыве.
     *
     * Почему не параллельные сегменты: при обрыве/частичном ответе сегменты
     * могли лечь не туда - файл считался готовым, а внутри мусор, и MediaPlayer
     * падал с -38. Здесь всё просто: пишем строго по порядку и сверяем с
     * Content-Length.
     */
    private fun fetchWholeBlocking(
        url: String,
        dest: File,
        onProgress: (Int) -> Unit,
    ): Boolean {
        var written = 0L
        var total = -1L
        runCatching { if (dest.exists()) dest.delete() }
        var attempt = 0
        while (attempt < 4) {
            attempt++
            try {
                val b = okhttp3.Request.Builder().url(url)
                if (written > 0) b.header("Range", "bytes=$written-")
                dlClient.newCall(b.build()).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        if (resp.code == 404 || resp.code == 410) return false
                        throw IllegalStateException("HTTP ${resp.code}")
                    }
                    val body = resp.body ?: return false
                    if (total <= 0) {
                        total = if (resp.code == 206) {
                            resp.header("Content-Range")
                                ?.substringAfter("/")?.trim()?.toLongOrNull()
                                ?: body.contentLength()
                        } else {
                            body.contentLength()
                        }
                    }
                    val raf = java.io.RandomAccessFile(dest, "rw")
                    try {
                        raf.seek(written)
                        body.byteStream().use { input ->
                            val buf = ByteArray(64 * 1024)
                            while (true) {
                                val n = input.read(buf)
                                if (n <= 0) break
                                raf.write(buf, 0, n)
                                written += n
                                if (total > 0) onProgress(((written * 100) / total).toInt())
                            }
                        }
                    } finally {
                        runCatching { raf.close() }
                    }
                }
                if (total <= 0 || written >= total) return written > 1024
            } catch (e: Exception) {
                Log.w(TAG, "download attempt $attempt failed: ${e.message} (${written}Б)")
                try {
                    Thread.sleep(350L * attempt)
                } catch (_: InterruptedException) {
                    return false
                }
            }
        }
        return written > 1024 && (total <= 0 || written >= total)
    }

    private fun playLocalFile(file: File) {
        currentMode = "file"
        runCatching {
            val p = ensurePlayer()
            p.setMediaItem(MediaItem.fromUri(android.net.Uri.fromFile(file)))
            p.prepare()
            p.playWhenReady = true
        }.onFailure {
            onPlaybackError("playLocalFile: ${it.message}")
        }
    }

    private fun playLocalUrl(url: String) {
        currentMode = "stream"
        runCatching {
            val p = ensurePlayer()
            p.setMediaItem(MediaItem.fromUri(url))
            p.prepare()
            p.playWhenReady = true
        }.onFailure {
            onPlaybackError("playLocalUrl: ${it.message}")
        }
    }

    /** Прямое воспроизведение внешнего URL (запасной путь). */
    private fun playRemoteUrl(url: String) {
        currentMode = "remote"
        runCatching {
            val p = ensurePlayer()
            p.setMediaItem(MediaItem.fromUri(url))
            p.prepare()
            p.playWhenReady = true
        }.onFailure {
            onPlaybackError("playRemoteUrl: ${it.message}")
        }
    }

    private fun onNextAuto() {
        // трек доиграл - переходим сразу (start() сам скачает, если надо)
        if (next(force = true) == null) onState?.invoke(false)
    }

    private fun release() {
        // ВАЖНО: каждый шаг отдельно. Раньше всё было в одном runCatching:
        // если reset() бросал IllegalStateException (после ошибки плеера),
        // release() уже не выполнялся - старый MediaPlayer продолжал играть,
        // а сверху стартовал новый. Отсюда «два трека одновременно».
        releasePlayer()
    }

    private fun stopLocalServer() {
        localServer?.stop()
        localServer = null
    }

    fun destroy() {
        flushFeedback()
        fbTicker?.cancel()
        playGen++
        abandonFocus()
        watchdog?.cancel()
        downloadJob?.cancel()
        stopLocalServer()
        scope.cancel()
        release()
    }

    companion object {
        private const val TAG = "MusicPlayer"
    }
}
