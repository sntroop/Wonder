package su.gwarden.music.player

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

/**
 * Растущий файл: скачивается сегментами в фоне, «доступно» постепенно.
 * Нужен для прогрессивного воспроизведения: играем с первого чанка.
 */
interface GrowingFile {
    /** Полный размер, если известен; -1 если нет. */
    fun totalSize(): Long

    /** Сколько байт доступно подряд с начала файла (можно играть). */
    fun availableBytes(): Long

    /** Ждёт, пока availableBytes >= minBytes. Возвращает доступный размер или -1 по таймауту. */
    fun awaitBytes(minBytes: Long, timeoutMs: Long): Long

    /** Читает кусок из уже скачанной части. */
    fun readAt(pos: Long, len: Int): ByteArray
}

/**
 * Скачивание файла ПАРАЛЛЕЛЬНЫМИ сегментами через Range-запросы.
 *
 * Почему так: у владельца путь/DPI рвёт длинные ответы (~250 КБ), поэтому
 * большие запросы не доезжают. Короткие сегменты доезжают, а параллельность
 * даёт скорость.
 */
class ParallelDownloader {

    companion object {
        private const val TAG = "ParallelDL"

        /** Стартовый размер сегмента. */
        const val SEGMENT_START = 192L * 1024

        /** Минимальный размер сегмента. */
        const val SEGMENT_MIN = 16L * 1024

        /**
         * Сколько сегментов качаем одновременно.
         * Путь владельца душит длинные ответы (~250 КБ), поэтому качаем
         * МНОГО мелких сегментов параллельно - так быстрее, чем один большой.
         */
        const val PARALLEL = 12

        private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    class Download(
        val file: File,
        val raf: RandomAccessFile,
    ) : GrowingFile {

        class Segment(val from: Long, val to: Long) {
            @Volatile var filled: Long = 0
            val done: Boolean get() = filled >= (to - from)
        }

        val lock = Object()
        val segments = mutableListOf<Segment>()

        @Volatile
        private var total: Long = -1L

        @Volatile
        var failed: Boolean = false

        /** Файл закрыт (плеер переключился) - воркеры должны выйти. */
        @Volatile
        var closed: Boolean = false

        fun setTotalSize(v: Long) { total = v }

        override fun totalSize(): Long = total

        override fun availableBytes(): Long = synchronized(lock) {
            var sum = 0L
            var seenIncomplete = false
            for (s in segments) {
                if (s.done) {
                    if (!seenIncomplete) sum += (s.to - s.from)
                } else {
                    if (!seenIncomplete) {
                        sum += s.filled
                        seenIncomplete = true
                    }
                }
            }
            sum
        }

        override fun awaitBytes(minBytes: Long, timeoutMs: Long): Long {
            val deadline = System.currentTimeMillis() + timeoutMs
            synchronized(lock) {
                while (true) {
                    val avail = availableBytes()
                    if (avail >= minBytes || failed) return avail
                    val left = deadline - System.currentTimeMillis()
                    if (left <= 0) return -1
                    try {
                        lock.wait(minOf(left, 500))
                    } catch (e: InterruptedException) {
                        return -1
                    }
                }
            }
        }

        /**
         * Читает с ЛЮБОГО места, а не только из непрерывного начала.
         *
         * Это важно для шортсов: плеер прыгает в середину трека (дроп), и
         * если нужный сегмент уже скачался, его можно отдавать сразу —
         * даже когда предыдущие сегменты ещё в пути.
         */
        override fun readAt(pos: Long, len: Int): ByteArray {
            synchronized(lock) {
                val s = segments.firstOrNull { pos >= it.from && pos < it.to }
                    ?: return ByteArray(0)
                val canRead = minOf(
                    len.toLong(),
                    s.filled - (pos - s.from),
                    s.to - pos,
                ).toInt()
                if (canRead <= 0) return ByteArray(0)
                val buf = ByteArray(canRead)
                raf.seek(pos)
                raf.readFully(buf)
                return buf
            }
        }

        /** Есть ли уже байт по смещению [pos] (для ожидания в локальном сервере). */
        fun readableAt(pos: Long): Boolean = synchronized(lock) {
            val s = segments.firstOrNull { pos >= it.from && pos < it.to } ?: return false
            s.filled > pos - s.from
        }

        /** Ждём конкретное смещение — не требуя непрерывного начала файла. */
        fun awaitAt(pos: Long, timeoutMs: Long): Boolean {
            val deadline = System.currentTimeMillis() + timeoutMs
            synchronized(lock) {
                while (true) {
                    if (readableAt(pos)) return true
                    if (failed || closed) return false
                    val left = deadline - System.currentTimeMillis()
                    if (left <= 0) return false
                    try {
                        lock.wait(minOf(left, 300))
                    } catch (e: InterruptedException) {
                        return false
                    }
                }
            }
        }

        fun notifyData() {
            synchronized(lock) { lock.notifyAll() }
        }

        fun close() {
            closed = true
            runCatching { raf.close() }
        }
    }

    /**
     * Скачивает url в dest. Возвращает объект, по которому можно читать
     * по мере поступления данных.
     */
    fun download(
        url: String,
        dest: File,
        onProgress: (available: Long, total: Long) -> Unit = { _, _ -> },
    ): Download {
        dest.parentFile?.mkdirs()
        val raf = RandomAccessFile(dest, "rw")
        raf.setLength(0)

        val dl = Download(dest, raf)

        val total = headSize(url)
        dl.setTotalSize(total ?: -1L)

        val workers = ArrayList<Thread>()
        var offset = 0L

        if ((total ?: -1L) > 0) {
            val t = total!!
            while (offset < t) {
                val to = minOf(offset + SEGMENT_START, t)
                val seg = Download.Segment(offset, to)
                synchronized(dl.lock) { dl.segments.add(seg) }
                workers.add(worker(url, dl, seg))
                offset = to
            }
        } else {
            val seg = Download.Segment(0, Long.MAX_VALUE)
            synchronized(dl.lock) { dl.segments.add(seg) }
            workers.add(workerOpenEnded(url, dl, seg))
        }

        workers.forEach { it.start() }

        Thread {
            while (!dl.failed && !dl.closed) {
                val avail = dl.availableBytes()
                onProgress(avail, dl.totalSize())
                if (dl.totalSize() > 0 && avail >= dl.totalSize()) break
                Thread.sleep(300)
            }
        }.apply { isDaemon = true }.start()

        return dl
    }

    /** Тихая докачка трека в кэш ОДНИМ соединением (для prefetch следующего). */
    fun prefetch(url: String, dest: File, onDone: (Boolean) -> Unit) {
        Thread {
            val tmp = File(dest.absolutePath + ".part")
            var ok = false
            try {
                val resp = client.newCall(Request.Builder().url(url).build()).execute()
                resp.use { r ->
                    if (r.isSuccessful) {
                        val body = r.body
                        if (body != null) {
                            val expected = body.contentLength()
                            var written = 0L
                            body.byteStream().use { input ->
                                tmp.outputStream().use { out ->
                                    val buf = ByteArray(64 * 1024)
                                    while (true) {
                                        val n = input.read(buf)
                                        if (n <= 0) break
                                        out.write(buf, 0, n)
                                        written += n
                                    }
                                }
                            }
                            // ГРАБЛЯ: при обрыве copyTo просто заканчивается -
                            // файл обрезан, но выглядит готовым, и MediaPlayer
                            // потом падает с -38. Сверяем с Content-Length.
                            val complete = expected <= 0 || written == expected
                            ok = complete && tmp.renameTo(dest)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    if (!ok && tmp.exists()) tmp.delete()
                } catch (_: Exception) {
                }
                onDone(ok)
            }
        }.apply { isDaemon = true; name = "prefetch"; start() }
    }

    private fun worker(url: String, dl: Download, seg: Download.Segment): Thread =
        Thread {
            var attempt = 0
            // filled - authoritative прогресс сегмента: при обрыве
            // продолжаем с места обрыва, а не качаем заново.
            while (seg.filled < (seg.to - seg.from) && attempt < 60 && !dl.closed) {
                attempt++
                val start = seg.from + seg.filled
                try {
                    val got = fetchRange(url, start, seg.to - 1, dl, seg, start)
                    if (got <= 0L) {
                        Thread.sleep(250)
                        continue
                    }
                    attempt = 0
                } catch (e: Exception) {
                    Log.w(TAG, "seg ${seg.from} @${seg.filled} failed: ${e.message}")
                    Thread.sleep(300)
                }
            }
            if (seg.filled < (seg.to - seg.from)) {
                Log.w(TAG, "segment ${seg.from}-${seg.to} incomplete (${seg.filled})")
            }
        }.apply { isDaemon = true; name = "dl-${seg.from}" }

    private fun workerOpenEnded(url: String, dl: Download, seg: Download.Segment): Thread =
        Thread {
            try {
                var pos = 0L
                while (!dl.closed) {
                    val got = fetchRange(url, pos, -1, dl, seg, pos)
                    if (got <= 0L) break
                    pos += got
                    dl.setTotalSize(pos)
                }
            } catch (e: Exception) {
                Log.w(TAG, "open-ended failed: ${e.message}")
            }
        }.apply { isDaemon = true; name = "dl-open" }

    /** Докачивает [from, to] (to=-1 => до конца) в позицию pos файла. */
    private fun fetchRange(
        url: String,
        from: Long,
        to: Long,
        dl: Download,
        seg: Download.Segment,
        pos: Long,
    ): Long {
        val builder = Request.Builder().url(url)
        builder.header("Range", if (to >= from) "bytes=$from-$to" else "bytes=$from-")
        client.newCall(builder.build()).execute().use { resp ->
            if (resp.code == 404) {
                dl.failed = true
                throw IllegalStateException("трек недоступен")
            }
            if (resp.code != 200 && resp.code != 206) {
                throw IllegalStateException("HTTP ${resp.code}")
            }
            val body = resp.body ?: throw IllegalStateException("empty body")
            val input = body.byteStream()
            val buf = ByteArray(32 * 1024)
            var written = 0L
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                synchronized(dl.lock) {
                    dl.raf.seek(pos + written)
                    dl.raf.write(buf, 0, n)
                    written += n
                    // прогресс сегмента считаем от его начала
                    seg.filled = (pos + written) - seg.from
                    dl.lock.notifyAll()
                }
            }
            return written
        }
    }

    private fun headSize(url: String): Long? = runCatching {
        client.newCall(Request.Builder().url(url).head().build()).execute().use { resp ->
            val cl = resp.header("Content-Length")
            val cr = resp.header("Content-Range")
            when {
                cr != null && cr.contains("/") -> cr.substringAfter("/").trim().toLongOrNull()
                resp.code == 200 && cl != null -> cl.toLongOrNull()
                else -> null
            }
        }
    }.getOrNull()
}
