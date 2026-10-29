package su.gwarden.music.offline

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.Track
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Офлайн-скачивание: «оставь этот трек на телефоне».
 *
 * Отличие от обычного кэша плеера: файл лежит в filesDir/downloads и НЕ
 * вытесняется. Играется первым делом — даже без сети и без VK.
 * Лимит (по умолчанию 1.5 ГБ) проверяется при скачивании: старые удаляются.
 */
object Offline {

    data class Item(
        val source: String = "",
        val id: String = "",
        val artist: String = "",
        val title: String = "",
        val artwork: String = "",
        val file: String = "",
        val size: Long = 0,
        val ts: Long = 0,
    )

    private const val PREFS = "offline"
    private const val KEY_LIMIT = "limit_mb"
    private const val DEFAULT_LIMIT_MB = 1536

    private var dir: File? = null
    private var appCtx: Context? = null
    private val gson = Gson()

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    fun init(ctx: Context) {
        appCtx = ctx.applicationContext
        if (dir == null) {
            dir = File(ctx.applicationContext.filesDir, "downloads").apply { mkdirs() }
        }
    }

    private fun prefs() = appCtx?.getSharedPreferences(PREFS, 0)

    fun limitMb(): Int = prefs()?.getInt(KEY_LIMIT, DEFAULT_LIMIT_MB) ?: DEFAULT_LIMIT_MB
    fun setLimitMb(mb: Int) {
        prefs()?.edit()?.putInt(KEY_LIMIT, mb.coerceIn(200, 8000))?.apply()
        trim()
    }

    private fun indexFile(): File = File(dir, "index.json")

    fun list(): List<Item> {
        return try {
            val raw = indexFile().takeIf { it.exists() }?.readText().orEmpty()
            if (raw.isBlank()) emptyList()
            else gson.fromJson(raw, Array<Item>::class.java)?.toList() ?: emptyList()
        } catch (e: Throwable) {
            emptyList()
        }
    }

    private fun save(items: List<Item>) {
        runCatching { indexFile().writeText(gson.toJson(items)) }
    }

    private fun fileName(source: String, id: String): String =
        source + "_" + id.replace(Regex("[^0-9A-Za-z_-]"), "_") + ".mp3"

    /** Файл уже скачан? Возвращает путь для плеера. */
    fun fileFor(source: String, id: String): File? {
        val f = File(dir, fileName(source, id))
        return if (f.exists() && f.length() > 1024) f else null
    }

    fun fileFor(t: Track): File? = fileFor(t.source, t.id)

    fun isDownloaded(t: Track): Boolean = fileFor(t) != null

    fun totalSize(): Long = list().sumOf { it.size }

    /** Удалить всё, что выходит за лимит (сначала самые старые). */
    fun trim() {
        val items = list().toMutableList()
        val limit = limitMb().toLong() * 1024L * 1024L
        var total = items.sumOf { it.size }
        if (total <= limit) return
        items.sortBy { it.ts }
        val remove = mutableListOf<Item>()
        for (it in items) {
            if (total <= limit) break
            total -= it.size
            remove.add(it)
        }
        for (r in remove) {
            runCatching { File(dir, r.file).delete() }
        }
        save(items.filter { it !in remove })
    }

    fun remove(t: Track): Boolean {
        val items = list().toMutableList()
        val hit = items.firstOrNull { it.source == t.source && it.id == t.id }
        if (hit != null) {
            runCatching { File(dir, hit.file).delete() }
            items.remove(hit)
            save(items)
        }
        return hit != null
    }

    /** Скачать трек (фон-поток, ответ в главном). */
    fun download(ctx: Context, t: Track, onDone: (Boolean, String) -> Unit) {
        init(ctx)
        if (isDownloaded(t)) {
            onDone(true, "уже скачан")
            return
        }
        val url = ApiClient.streamUrl(t.source, t.id)
        val fname = fileName(t.source, t.id)
        Thread {
            val res = try {
                fetchToFile(url, fname, t)
            } catch (e: Throwable) {
                false to (e.message ?: "нет сети")
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onDone(res.first, res.second)
            }
        }.apply { isDaemon = true; this.name = "offline-dl" }.start()
    }

    private fun fetchToFile(url: String, name: String, t: Track): Pair<Boolean, String> {
        val req = Request.Builder().url(url).build()
        http.newCall(req).execute().use { r ->
            if (!r.isSuccessful) return false to ("сервер: " + r.code)
            val body = r.body ?: return false to "пустой ответ"
            val tmp = File(dir, name + ".part")
            body.byteStream().use { input ->
                tmp.outputStream().use { out -> input.copyTo(out, 64 * 1024) }
            }
            if (tmp.length() <= 1024) {
                tmp.delete()
                return false to "файл пустой"
            }
            val dst = File(dir, name)
            if (dst.exists()) dst.delete()
            if (!tmp.renameTo(dst)) return false to "не смог сохранить"
            val items = list().toMutableList()
            items.removeAll { it.source == t.source && it.id == t.id }
            items.add(
                Item(
                    t.source, t.id, t.artist, t.title,
                    t.artwork ?: "", name, dst.length(),
                    System.currentTimeMillis(),
                )
            )
            save(items)
            trim()
            return true to "скачано"
        }
    }
}
