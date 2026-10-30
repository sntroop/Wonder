package su.gwarden.music.audio

import android.content.Context

/**
 * Настройки звукового движка Wonder.
 *
 * Всё хранится локально в SharedPreferences («wonder_fx»), читается один раз
 * при старте и живёт в памяти как неизменяемый снимок: аудиопоток читает
 * ссылку [settings] без блокировок, UI подменяет её целиком.
 *
 * Значения намеренно в «человеческих» единицах: дБ, проценты, полутона.
 * Перевод в коэффициенты — забота [FxProcessor].
 */
data class FxSettings(
    /** Общий выключатель обработки: выкл = звук как с источника. */
    var enabled: Boolean = false,
    /** Предусиление, дБ -12..+12 (компенсация буста полос). */
    var preamp: Float = 0f,
    /** 10 полос, дБ -12..+12 (31 Гц … 16 кГц). */
    var bands: FloatArray = FloatArray(10),
    /** Усиление басов, дБ 0..15 (низкая полка 90 Гц). */
    var bass: Float = 0f,
    /** Доля реверба в сигнале 0..1. */
    var reverbMix: Float = 0f,
    /** «Размер комнаты» реверба 0..1. */
    var reverbSize: Float = 0.5f,
    /** Глубина искажения 0..1. */
    var distDrive: Float = 0f,
    /** Тон дистошна 0..1 (0 = темно, 1 = ярко). */
    var distTone: Float = 0.5f,
    /** Темп 0.5..2.0, не меняет высоту тона. */
    var tempo: Float = 1f,
    /** Высота тона в полутонах -12..+12, не меняет темп. */
    var semitones: Float = 0f,
    /** Кроссфейд между треками, секунды 0..12. */
    var crossfade: Int = 0,
) {
    fun copyOf(): FxSettings = copy(bands = bands.copyOf())

    /** Приводим всё в допустимые рамки — и после чтения с диска, и после UI. */
    fun normalize(): FxSettings {
        if (bands.size != Fx.BANDS.size) {
            bands = FloatArray(Fx.BANDS.size) { bands.getOrElse(it) { 0f } }
        }
        for (i in bands.indices) bands[i] = bands[i].coerceIn(-12f, 12f)
        preamp = preamp.coerceIn(-12f, 12f)
        bass = bass.coerceIn(0f, 15f)
        reverbMix = reverbMix.coerceIn(0f, 1f)
        reverbSize = reverbSize.coerceIn(0f, 1f)
        distDrive = distDrive.coerceIn(0f, 1f)
        distTone = distTone.coerceIn(0f, 1f)
        tempo = tempo.coerceIn(0.5f, 2f)
        semitones = semitones.coerceIn(-12f, 12f)
        crossfade = crossfade.coerceIn(0, 12)
        return this
    }
}

/** Готовый пресет эквалайзера. */
data class FxPreset(
    val id: String,
    val name: String,
    val bands: FloatArray,
    val preamp: Float = 0f,
    val bass: Float = 0f,
)

object Fx {

    /** Полосы стандартного 10-полосного эквалайзера (ISO, октава). */
    val BANDS = floatArrayOf(31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
    val BAND_LABELS = arrayOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    private const val PREFS = "wonder_fx"

    @Volatile
    var settings: FxSettings = FxSettings()
        private set

    /** Растёт при каждом изменении — DSP пересчитывает коэффициенты. */
    @Volatile
    var version: Int = 0
        private set

    /** Встроенные пресеты. Значения подобраны на слух под музыку из Wonder. */
    val presets: List<FxPreset> = listOf(
        FxPreset("flat", "Плоский", FloatArray(10)),
        FxPreset(
            "bass", "Бас", floatArrayOf(7f, 6f, 4.5f, 2.5f, 0.5f, 0f, -0.5f, 0f, 1f, 1.5f),
            preamp = -2f, bass = 4f,
        ),
        FxPreset(
            "vocal", "Вокал", floatArrayOf(-3f, -2f, 0f, 2f, 4.5f, 4.5f, 3f, 1.5f, 0f, -1f),
        ),
        FxPreset(
            "rock", "Рок", floatArrayOf(4f, 3f, 2f, 0.5f, -1f, -0.5f, 1.5f, 3f, 4f, 3.5f),
            preamp = -1.5f,
        ),
        FxPreset(
            "pop", "Поп", floatArrayOf(-1f, 0f, 1.5f, 3f, 2.5f, 0.5f, -0.5f, 1f, 2f, 2.5f),
        ),
        FxPreset(
            "electronic", "Электро", floatArrayOf(5f, 4f, 2f, 0f, -1.5f, 1f, 2f, 3f, 4f, 4.5f),
            preamp = -1f, bass = 3f,
        ),
        FxPreset(
            "night", "Ночь", floatArrayOf(2f, 1f, 0f, -1f, -2f, -2.5f, -2f, -1f, 0f, 1f),
            preamp = 1f,
        ),
        FxPreset(
            "hard", "Хардстайл", floatArrayOf(8f, 7f, 4f, 1f, -2f, -1f, 1f, 3f, 5f, 4f),
            preamp = -3f, bass = 5f,
        ),
    )

    /** Пользовательские пресеты (3 слота), как их видит UI. */
    fun customPresets(ctx: Context): List<FxPreset> = (1..3).mapNotNull { slot ->
        val raw = prefs(ctx).getString("custom_$slot", null) ?: return@mapNotNull null
        val parts = raw.split("|")
        if (parts.size < 4) return@mapNotNull null
        val bands = parts[3].split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
        if (bands.size != BANDS.size) return@mapNotNull null
        FxPreset(
            id = "custom_$slot",
            name = parts[0].ifBlank { "Мой $slot" },
            bands = bands,
            preamp = parts[1].toFloatOrNull() ?: 0f,
            bass = parts[2].toFloatOrNull() ?: 0f,
        )
    }

    fun saveCustom(ctx: Context, slot: Int, name: String) {
        val s = settings
        val csv = s.bands.joinToString(",") { "%.2f".format(java.util.Locale.US, it) }
        prefs(ctx).edit()
            .putString("custom_$slot", "$name|${s.preamp}|${s.bass}|$csv")
            .apply()
    }

    fun deleteCustom(ctx: Context, slot: Int) {
        prefs(ctx).edit().remove("custom_$slot").apply()
    }

    fun load(ctx: Context) {
        val p = prefs(ctx)
        val s = FxSettings(
            enabled = p.getBoolean("enabled", false),
            preamp = p.getFloat("preamp", 0f),
            bands = parseBands(p.getString("bands", null)),
            bass = p.getFloat("bass", 0f),
            reverbMix = p.getFloat("reverb", 0f),
            reverbSize = p.getFloat("reverb_size", 0.5f),
            distDrive = p.getFloat("dist", 0f),
            distTone = p.getFloat("dist_tone", 0.5f),
            tempo = p.getFloat("tempo", 1f),
            semitones = p.getFloat("semitones", 0f),
            crossfade = p.getInt("crossfade", 0),
        ).normalize()
        settings = s
        version++
    }

    /**
     * Изменить настройки: копия -> mutate -> сохранить -> опубликовать.
     * Именно копия, а не мутация на месте: аудиопоток не должен увидеть
     * половину применённых ползунков.
     */
    fun update(ctx: Context, block: (FxSettings) -> Unit) {
        val next = settings.copyOf()
        block(next)
        next.normalize()
        settings = next
        version++
        save(ctx, next)
    }

    fun applyPreset(ctx: Context, preset: FxPreset) {
        update(ctx) {
            it.bands = preset.bands.copyOf()
            it.preamp = preset.preamp
            it.bass = preset.bass
        }
    }

    fun resetAll(ctx: Context) {
        settings = FxSettings().normalize()
        version++
        save(ctx, settings)
    }

    private fun save(ctx: Context, s: FxSettings) {
        prefs(ctx).edit()
            .putBoolean("enabled", s.enabled)
            .putFloat("preamp", s.preamp)
            .putString("bands", s.bands.joinToString(",") { "%.2f".format(java.util.Locale.US, it) })
            .putFloat("bass", s.bass)
            .putFloat("reverb", s.reverbMix)
            .putFloat("reverb_size", s.reverbSize)
            .putFloat("dist", s.distDrive)
            .putFloat("dist_tone", s.distTone)
            .putFloat("tempo", s.tempo)
            .putFloat("semitones", s.semitones)
            .putInt("crossfade", s.crossfade)
            .apply()
    }

    private fun parseBands(raw: String?): FloatArray {
        if (raw.isNullOrBlank()) return FloatArray(BANDS.size)
        val list = raw.split(",").mapNotNull { it.toFloatOrNull() }
        if (list.size != BANDS.size) return FloatArray(BANDS.size)
        return list.toFloatArray()
    }

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
