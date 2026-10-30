package su.gwarden.music.eco

import android.content.Context

/**
 * Энергосбережение. По умолчанию ВЫКЛЮЧЕНО (как просили): приложение
 * работает максимально быстро и агрессивно кэширует.
 *
 * Когда включено:
 *  - мост VK опрашивает сервер реже (телефон не держит соединение постоянно);
 *  - предзагрузка в плеере качает только следующий трек, а не два;
 *  - тяжёлые анимации эквалайзера выключаются.
 */
object Eco {

    private const val PREFS = "music_player"
    private const val KEY = "eco_mode"

    private var ctx: Context? = null

    fun init(c: Context) {
        ctx = c.applicationContext
    }

    fun enabled(): Boolean =
        ctx?.getSharedPreferences(PREFS, 0)?.getBoolean(KEY, false) ?: false

    fun set(on: Boolean) {
        ctx?.getSharedPreferences(PREFS, 0)?.edit()?.putBoolean(KEY, on)?.apply()
    }
}
