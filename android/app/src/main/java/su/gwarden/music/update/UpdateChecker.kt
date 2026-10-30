package su.gwarden.music.update

import android.content.Context
import java.io.File

/** Что сервер рассказал про свежую сборку. */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val url: String,
    val sha256: String,
    val notes: String,
)

/**
 * Публичная сборка Wonder.
 *
 * OTA-обновления отключены: здесь нет адреса сервера и загрузки APK.
 * Интерфейс баннера оставлен только для сборки UI.
 */
object UpdateChecker {

    @Volatile
    var downloading: Boolean = false
        private set

    fun installedCode(ctx: Context): Int = 0

    fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences("wonder_update", Context.MODE_PRIVATE)

    fun check(ctx: Context, force: Boolean = false, cb: (UpdateInfo?) -> Unit) {
        cb(null)
    }

    fun download(
        ctx: Context,
        info: UpdateInfo,
        onProgress: (Int) -> Unit,
        onDone: (File?) -> Unit,
    ) {
        onDone(null)
    }

    fun canInstall(ctx: Context): Boolean = false

    fun openInstallSettings(ctx: Context) { }

    fun install(ctx: Context, file: File) { }
}
