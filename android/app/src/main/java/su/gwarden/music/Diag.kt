package su.gwarden.music

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Кольцевой буфер последних событий приложения.
 *
 * Зачем: если приложение падает или зависает, владелец должен просто вставить
 * текст в чат, а не искать logcat. При крахе/зависании буфер копируется
 * в буфер обмена вместе со стеком.
 */
object Diag {

    private const val MAX = 60
    private val lines = ArrayDeque<String>()
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)

    @Synchronized
    fun add(s: String) {
        runCatching {
            lines.addLast(fmt.format(Date()) + " " + s)
            while (lines.size > MAX) lines.removeFirst()
        }
    }

    @Synchronized
    fun dump(): String = lines.joinToString("\n")

    fun device(): String =
        "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}), " +
            "${Build.MANUFACTURER} ${Build.MODEL}"
}
