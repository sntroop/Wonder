package su.gwarden.music.model

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * «Моя музыка» внутри приложения: треки, добавленные плюсиком.
 * Хранится локально в filesDir/my_music.json (в VK ничего не пишем).
 */
object LocalLibrary {

    private val gson = Gson()
    private var file: File? = null
    private val items = mutableListOf<Track>()

    // треки из VK-библиотеки, которые пользователь убрал из «Моей музыки»
    private val hidden = LinkedHashSet<String>()
    private var hiddenFile: File? = null

    // порядок, который пользователь задал перетаскиванием (для любых треков)
    private var orderKeys: MutableList<String> = mutableListOf()
    private var orderFile: File? = null

    /** Сменить аккаунт: у каждого своя «Моя музыка» и свой порядок. */
    fun reset(ctx: Context, prefix: String) {
        file = null
        hiddenFile = null
        orderFile = null
        items.clear()
        hidden.clear()
        orderKeys.clear()
        init(ctx, prefix)
    }

    fun init(ctx: Context, prefix: String = "") {
        if (file != null) return
        val suf = if (prefix.isEmpty()) "" else "_$prefix"
        file = File(ctx.filesDir, "my_music$suf.json")
        hiddenFile = File(ctx.filesDir, "my_music_hidden$suf.json")
        orderFile = File(ctx.filesDir, "my_music_order$suf.json")
        runCatching {
            hiddenFile?.takeIf { it.exists() && it.length() > 0 }?.let { f ->
                gson.fromJson(f.readText(), Array<String>::class.java)?.forEach { hidden.add(it) }
            }
            orderFile?.takeIf { it.exists() && it.length() > 0 }?.let { f ->
                gson.fromJson(f.readText(), Array<String>::class.java)?.forEach { orderKeys.add(it) }
            }
        }
        runCatching {
            val f = file ?: return
            if (f.exists() && f.length() > 0) {
                val arr = gson.fromJson(f.readText(), Array<Track>::class.java) ?: emptyArray()
                items.clear()
                items.addAll(arr.filter { it.id.isNotEmpty() })
            }
        }
    }

    fun all(): List<Track> = items.toList()

    fun contains(track: Track): Boolean =
        items.any { it.id == track.id && it.source == track.source }

    /** true - добавлено, false - уже было. */
    fun add(track: Track): Boolean {
        if (track.id.isEmpty() || contains(track)) return false
        items.add(0, track)
        save()
        return true
    }

    /** Новый порядок локальных треков (drag&drop). */
    fun reorder(order: List<Track>) {
        val byKey = LinkedHashMap<String, Track>()
        items.forEach { byKey[it.source + it.id] = it }
        val newList = ArrayList<Track>(items.size)
        order.forEach { t -> byKey.remove(t.source + t.id)?.let { newList.add(it) } }
        // что осталось (например, скрыто фильтром) - в конец, ничего не теряем
        newList.addAll(byKey.values)
        items.clear()
        items.addAll(newList)
        save()
    }

    fun remove(track: Track) {
        items.removeAll { it.id == track.id && it.source == track.source }
        save()
    }

    private fun key(t: Track) = t.source + "|" + t.id

    /** Убрать трек из «Моей музыки». Локальный - удаляем, из VK - скрываем (в VK не пишем). */
    fun hide(track: Track) {
        hidden.add(key(track))
        runCatching { hiddenFile?.writeText(gson.toJson(hidden.toList())) }
    }

    /** Локальные треки + серверная библиотека без дублей и без скрытых. */
    fun merge(server: List<Track>): List<Track> {
        val localKeys = items.map { key(it) }.toSet()
        return items + server.filter { key(it) !in localKeys && key(it) !in hidden }
    }

    /** Применить порядок, заданный перетаскиванием (работает и для VK-треков). */
    fun applyOrder(list: List<Track>): List<Track> {
        if (orderKeys.isEmpty()) return list
        val idx = HashMap<String, Int>(orderKeys.size)
        orderKeys.forEachIndexed { i, k -> idx[k] = i }
        return list.sortedBy { idx[key(it)] ?: Int.MAX_VALUE }
    }

    fun saveOrder(list: List<Track>) {
        orderKeys = list.map { key(it) }.toMutableList()
        runCatching { orderFile?.writeText(gson.toJson(orderKeys)) }
    }

    private fun save() {
        runCatching { file?.writeText(gson.toJson(items)) }
    }
}
