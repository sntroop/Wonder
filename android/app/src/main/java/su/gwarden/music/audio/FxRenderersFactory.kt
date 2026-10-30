package su.gwarden.music.audio

import android.content.Context
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * Фабрика рендереров, которая вставляет [FxProcessor] в аудиосинк ExoPlayer.
 *
 * Штатный `DefaultRenderersFactory` собирает `DefaultAudioSink` со стандартной
 * цепочкой (silence skipping + sonic). Мы оставляем всё это и добавляем свой
 * процессор последним — он видит уже декодированный PCM 16 бит.
 */
class FxRenderersFactory(context: Context) : DefaultRenderersFactory(context) {

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setAudioProcessors(arrayOf<AudioProcessor>(FxProcessor.INSTANCE))
            .setEnableFloatOutput(false)
            // Темп и тон ведёт Sonic внутри ExoPlayer, а не AudioTrack:
            // системный PlaybackParams на части прошивок не умеет питч
            // отдельно от скорости и падает в моно/тишину.
            .setEnableAudioTrackPlaybackParams(false)
            .build()
    }
}
