package su.gwarden.music.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.BaseAudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Один биквад-фильтр (RBJ cookbook). Коэффициенты уже нормированы на a0,
 * состояние — два прошлых входа и два прошлых выхода.
 */
internal class Biquad {
    var b0 = 1f; private set
    var b1 = 0f; private set
    var b2 = 0f; private set
    var a1 = 0f; private set
    var a2 = 0f; private set

    private var x1 = 0f
    private var x2 = 0f
    private var y1 = 0f
    private var y2 = 0f

    /** Пик-фильтр: поднять/опустить одну полосу. */
    fun peaking(fs: Float, f0: Float, q: Float, gainDb: Float) {
        if (abs(gainDb) < 0.01f) {
            identity()
            return
        }
        val a = 10.0.pow(gainDb / 40.0).toFloat()
        val w0 = 2.0 * PI * (f0 / fs).toDouble()
        val cosW = cos(w0).toFloat()
        val alpha = (sin(w0) / (2.0 * q)).toFloat()
        val a0 = 1f + alpha / a
        b0 = (1f + alpha * a) / a0
        b1 = (-2f * cosW) / a0
        b2 = (1f - alpha * a) / a0
        a1 = (-2f * cosW) / a0
        a2 = (1f - alpha / a) / a0
        clearState()
    }

    /** Низкая полка: «усиление басов» одним фильтром, без бугра на СЧ. */
    fun lowShelf(fs: Float, f0: Float, gainDb: Float, s: Float = 0.9f) {
        if (abs(gainDb) < 0.01f) {
            identity()
            return
        }
        val a = 10.0.pow(gainDb / 40.0).toFloat()
        val w0 = 2.0 * PI * (f0 / fs).toDouble()
        val cosW = cos(w0).toFloat()
        val sinW = sin(w0).toFloat()
        val alpha = sinW / 2f * sqrt((a + 1f / a) * (1f / s - 1f) + 2f)
        val beta = 2f * sqrt(a) * alpha
        val a0 = (a + 1f) + (a - 1f) * cosW + beta
        b0 = (a * ((a + 1f) - (a - 1f) * cosW + beta)) / a0
        b1 = (2f * a * ((a - 1f) - (a + 1f) * cosW)) / a0
        b2 = (a * ((a + 1f) - (a - 1f) * cosW - beta)) / a0
        a1 = (-2f * ((a - 1f) + (a + 1f) * cosW)) / a0
        a2 = ((a + 1f) + (a - 1f) * cosW - beta) / a0
        clearState()
    }

    private fun identity() {
        b0 = 1f; b1 = 0f; b2 = 0f; a1 = 0f; a2 = 0f
        clearState()
    }

    fun reset() = clearState()

    private fun clearState() {
        x1 = 0f; x2 = 0f; y1 = 0f; y2 = 0f
    }

    fun process(x: Float): Float {
        val y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1; x1 = x
        y2 = y1; y1 = y
        return y
    }

    /** АЧХ на частоте — для кривой в редакторе эквалайзера. */
    fun magnitudeDb(freq: Float, fs: Float): Float {
        if (freq <= 0f || fs <= 0f) return 0f
        val w = 2.0 * PI * (freq / fs).toDouble()
        val cosW = cos(w)
        val sinW = sin(w)
        val cos2 = cos(2 * w)
        val sin2 = sin(2 * w)
        val numRe = b0 + b1 * cosW + b2 * cos2
        val numIm = b1 * sinW * -1.0 + b2 * sin2 * -1.0
        val denRe = 1.0 + a1 * cosW + a2 * cos2
        val denIm = a1 * sinW * -1.0 + a2 * sin2 * -1.0
        val num = sqrt(numRe * numRe + numIm * numIm)
        val den = sqrt(denRe * denRe + denIm * denIm)
        if (den <= 0.0) return 0f
        return (20.0 * kotlin.math.log10(num / den)).toFloat()
    }
}

/** Гребёнка Шрёдера (Freeverb). */
private class Comb(size: Int) {
    private val buf = FloatArray(size.coerceAtLeast(1))
    private var idx = 0
    private var store = 0f

    fun process(x: Float, feedback: Float, damp: Float): Float {
        val y = buf[idx]
        store = y * (1f - damp) + store * damp
        buf[idx] = x + store * feedback
        if (++idx >= buf.size) idx = 0
        return y
    }

    fun reset() {
        java.util.Arrays.fill(buf, 0f)
        store = 0f
        idx = 0
    }
}

/** Всепропускающее звено (Freeverb). */
private class Allpass(size: Int) {
    private val buf = FloatArray(size.coerceAtLeast(1))
    private var idx = 0

    fun process(x: Float): Float {
        val buffered = buf[idx]
        val y = -x + buffered
        buf[idx] = x + buffered * 0.5f
        if (++idx >= buf.size) idx = 0
        return y
    }

    fun reset() {
        java.util.Arrays.fill(buf, 0f)
        idx = 0
    }
}

/**
 * Один канал Freeverb: 8 гребёнок + 4 всепропускающих.
 * Даёт плотный «зал» без внешних библиотек и без задержки на старте.
 */
private class ReverbChannel(sampleRate: Int, spread: Int) {
    private val scale = sampleRate / 44100f
    private val combs = intArrayOf(1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617)
        .map { Comb((it * scale).toInt() + spread) }
    private val aps = intArrayOf(556, 441, 341, 225)
        .map { Allpass((it * scale).toInt() + spread) }

    fun process(x: Float, feedback: Float, damp: Float): Float {
        var out = 0f
        for (c in combs) out += c.process(x, feedback, damp)
        out *= 0.125f
        for (a in aps) out = a.process(out)
        return out
    }

    fun reset() {
        for (c in combs) c.reset()
        for (a in aps) a.reset()
    }
}

/**
 * Вся обработка звука одним проходом по PCM:
 *
 *   preamp -> 10 полос EQ -> бас-полка -> дистошн -> реверб -> лимитер
 *
 * Работает как [androidx.media3.common.audio.AudioProcessor] внутри
 * ExoPlayer: звук любого источника (VK, SoundCloud, свой файл, офлайн)
 * проходит через одну и ту же цепочку.
 *
 * Настройки читаются из [Fx.settings] на каждый буфер; при смене [Fx.version]
 * пересчитываются коэффициенты. Никаких блокировок между UI и аудиопотоком.
 */
class FxProcessor private constructor() : BaseAudioProcessor() {

    companion object {
        /** Единственный экземпляр: DSP-состояние должно жить вместе с плеером. */
        val INSTANCE: FxProcessor by lazy { FxProcessor() }
    }

    private var channels = 2
    private var sampleRate = 44100
    private var passthrough = false

    private var appliedVersion = -1
    private var preampGain = 1f

    private val bandL = Array(Fx.BANDS.size) { Biquad() }
    private val bandR = Array(Fx.BANDS.size) { Biquad() }
    private val bassL = Biquad()
    private val bassR = Biquad()

    private var revL = ReverbChannel(44100, 0)
    private var revR = ReverbChannel(44100, 23)

    private var lpL = 0f
    private var lpR = 0f

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT || inputAudioFormat.channelCount > 2) {
            // Неизвестный формат: не ломаем воспроизведение, просто пропускаем.
            passthrough = true
            return inputAudioFormat
        }
        passthrough = false
        channels = inputAudioFormat.channelCount.coerceAtLeast(1)
        sampleRate = inputAudioFormat.sampleRate.coerceAtLeast(8000)
        revL = ReverbChannel(sampleRate, 0)
        revR = ReverbChannel(sampleRate, 23)
        appliedVersion = -1
        rebuild(Fx.settings)
        return inputAudioFormat
    }

    override fun onFlush() {
        for (f in bandL) f.reset()
        for (f in bandR) f.reset()
        bassL.reset()
        bassR.reset()
        revL.reset()
        revR.reset()
        lpL = 0f
        lpR = 0f
    }

    override fun onReset() {
        onFlush()
    }

    override fun isActive(): Boolean = true

    override fun queueInput(inputBuffer: ByteBuffer) {
        val size = inputBuffer.remaining()
        if (size <= 0) return
        val out = replaceOutputBuffer(size)
        if (passthrough || size % (2 * channels) != 0) {
            out.put(inputBuffer)
            out.flip()
            inputBuffer.position(inputBuffer.limit())
            return
        }

        val s = Fx.settings
        if (Fx.version != appliedVersion) rebuild(s)

        val inShorts = inputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val outShorts = out.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val frames = size / (2 * channels)

        if (!s.enabled) {
            // Выключено: побитовая копия без единого умножения.
            outShorts.put(inShorts)
            out.position(size)
            out.flip()
            inputBuffer.position(inputBuffer.limit())
            return
        }

        val fb = 0.70f + 0.28f * s.reverbSize
        val damp = 0.20f + 0.35f * (1f - s.reverbSize)
        val revMix = s.reverbMix
        val dry = 1f - revMix * 0.55f
        val drive = s.distDrive
        val toneCoef = toneCoefficient(s.distTone)

        for (i in 0..<frames) {
            var l = (inShorts.get().toFloat()) / 32768f
            var r = if (channels == 2) (inShorts.get().toFloat()) / 32768f else l

            // preamp
            l *= preampGain
            r *= preampGain

            // 10 полос
            for (b in bandL) l = b.process(l)
            for (b in bandR) r = b.process(r)

            // бас
            l = bassL.process(l)
            r = bassR.process(r)

            // дистошн + тон
            if (drive > 0.001f) {
                val d = 1f + drive * 22f
                val sl = tanh(l * d) * 0.7f
                val sr = tanh(r * d) * 0.7f
                lpL = lpL + toneCoef * (sl - lpL)
                lpR = lpR + toneCoef * (sr - lpR)
                l = l * (1f - drive) + lpL * drive
                r = r * (1f - drive) + lpR * drive
            }

            // реверб
            if (revMix > 0.001f) {
                val wl = revL.process(l, fb, damp)
                val wr = revR.process(r, fb, damp)
                l = l * dry + wl * revMix * 0.9f
                r = r * dry + wr * revMix * 0.9f
            }

            // мягкий лимитер: бусты не должны хрипеть клиппингом
            l = limit(l)
            r = limit(r)

            outShorts.put((l * 32767f).toInt().coerceIn(-32768, 32767).toShort())
            if (channels == 2) {
                outShorts.put((r * 32767f).toInt().coerceIn(-32768, 32767).toShort())
            }
        }
        out.position(size)
        out.flip()
        inputBuffer.position(inputBuffer.limit())
    }

    /** Пересчёт коэффициентов под текущие настройки. */
    private fun rebuild(s: FxSettings) {
        appliedVersion = Fx.version
        preampGain = 10.0.pow((s.preamp / 20.0)).toFloat()
        for (i in Fx.BANDS.indices) {
            val g = s.bands.getOrElse(i) { 0f }
            bandL[i].peaking(sampleRate.toFloat(), Fx.BANDS[i], 1.4f, g)
            bandR[i].peaking(sampleRate.toFloat(), Fx.BANDS[i], 1.4f, g)
        }
        bassL.lowShelf(sampleRate.toFloat(), 90f, s.bass)
        bassR.lowShelf(sampleRate.toFloat(), 90f, s.bass)
    }

    private fun toneCoefficient(tone: Float): Float {
        val fc = 600f + tone.coerceIn(0f, 1f) * 11400f
        val dt = 1f / sampleRate.toFloat()
        val rc = 1f / (2f * PI.toFloat() * fc)
        return (dt / (rc + dt)).coerceIn(0.001f, 1f)
    }

    private fun limit(x: Float): Float {
        val a = abs(x)
        if (a <= 0.92f) return x
        val over = a - 0.92f
        val soft = 0.92f + 0.08f * tanh(over / 0.08f)
        return if (x < 0f) -soft else soft
    }

    /** Громкость 0..1 для плавного старта/затухания — на случай отладки. */
    fun resetState() = onFlush()
}

/** Экспонента для расчёта питча из полутонов (используется в плеере). */
fun semitonesToRatio(semitones: Float): Float =
    2.0.pow(semitones.coerceIn(-24f, 24f) / 12.0).toFloat()
