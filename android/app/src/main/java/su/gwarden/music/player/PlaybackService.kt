package su.gwarden.music.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import su.gwarden.music.MainActivity
import su.gwarden.music.R
import su.gwarden.music.model.LocalLibrary
import su.gwarden.music.ui.ImageLoader

/**
 * Уведомление «сейчас играет»: обложка, название, автор и кнопки
 * назад / пауза-играть / вперёд. Живёт в foreground-сервисе, поэтому
 * музыка играет и когда приложение свёрнуто.
 */
class PlaybackService : Service() {

    companion object {
        private const val CHANNEL_ID = "playback"
        private const val NOTIF_ID = 4201
        private const val ACTION_UPDATE = "su.gwarden.music.UPDATE"
        private const val ACTION_PLAY = "su.gwarden.music.ACTION_PLAY"
        private const val ACTION_PAUSE = "su.gwarden.music.ACTION_PAUSE"
        private const val ACTION_NEXT = "su.gwarden.music.ACTION_NEXT"
        private const val ACTION_PREV = "su.gwarden.music.ACTION_PREV"
        private const val ACTION_ADD = "su.gwarden.music.ACTION_ADD"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_ARTIST = "artist"
        private const val EXTRA_COVER = "cover"
        private const val EXTRA_PLAYING = "playing"
        /** Пришли из уведомления - сразу открыть экран «сейчас играет». */
        const val EXTRA_OPEN_NOW = "open_now"

        /** Показать/обновить уведомление. */
        fun show(ctx: Context, title: String, artist: String, cover: String?, playing: Boolean) {
            val i = Intent(ctx, PlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_COVER, cover)
                putExtra(EXTRA_PLAYING, playing)
            }
            runCatching {
                if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i)
                else ctx.startService(i)
            }
        }

        /** Убрать уведомление и остановить сервис (трек закрыли в нижнем баре). */
        fun hide(ctx: Context) {
            runCatching { ctx.stopService(Intent(ctx, PlaybackService::class.java)) }
        }
    }

    private var title: String = ""
    private var artist: String = ""
    private var coverUrl: String? = null
    private var cover: Bitmap? = null
    private var playing: Boolean = false
    private lateinit var session: MediaSession

    /** Раз в секунду обновляем позицию в медиасессии: ползунок в уведомлении
     *  и на экране блокировки должен ехать сам. */
    private val ticker = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            if (!playing) return
            updateSessionState()
            ticker.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        session = MediaSession(this, "Wonder").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    PlayerHub.controller?.play()
                    playing = true
                    push()
                }

                override fun onPause() {
                    PlayerHub.controller?.pause()
                    playing = false
                    push()
                }

                override fun onSkipToNext() {
                    PlayerHub.controller?.next()
                    syncFromPlayer()
                    push()
                }

                override fun onSkipToPrevious() {
                    PlayerHub.controller?.prev()
                    syncFromPlayer()
                    push()
                }

                /** Пользователь потянул ползунок в уведомлении/на локскрине. */
                override fun onSeekTo(pos: Long) {
                    PlayerHub.controller?.seekTo(pos.toInt())
                    updateSessionState()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                PlayerHub.controller?.play()
                playing = true
            }
            ACTION_PAUSE -> {
                PlayerHub.controller?.pause()
                playing = false
            }
            ACTION_NEXT -> {
                PlayerHub.controller?.next()
                syncFromPlayer()
            }
            ACTION_PREV -> {
                PlayerHub.controller?.prev()
                syncFromPlayer()
            }
            ACTION_ADD -> {
                PlayerHub.controller?.current()?.let { t ->
                    LocalLibrary.init(this)
                    LocalLibrary.add(t)
                }
            }
            else -> {
                intent?.getStringExtra(EXTRA_TITLE)?.let { title = it }
                intent?.getStringExtra(EXTRA_ARTIST)?.let { artist = it }
                intent?.getBooleanExtra(EXTRA_PLAYING, playing)?.let { playing = it }
                val c = intent?.getStringExtra(EXTRA_COVER)
                if (c != coverUrl) {
                    coverUrl = c
                    cover = null
                    loadCover(c)
                }
            }
        }
        push()
        return START_STICKY
    }

    private fun syncFromPlayer() {
        val p = PlayerHub.controller ?: return
        playing = p.isPlaying
        p.current()?.let {
            title = it.title
            artist = it.artist
            val url = su.gwarden.music.api.ApiClient.img(it.artwork, 400)
            if (url != coverUrl) {
                coverUrl = url
                cover = null
                loadCover(url)
            }
        }
    }

    private fun loadCover(url: String?) {
        if (url.isNullOrEmpty()) return
        ImageLoader.bitmap(url, 400) { bmp ->
            if (bmp != null && url == coverUrl) {
                cover = bmp
                push()
            }
        }
    }

    private fun push() {
        updateSessionState()
        val n = build()
        runCatching { startForeground(NOTIF_ID, n) }
        ticker.removeCallbacks(tick)
        if (playing) ticker.postDelayed(tick, 1000)
    }

    private fun updateSessionState() {
        val c = PlayerHub.controller
        val dur = (runCatching { c?.duration }.getOrNull() ?: 0).coerceAtLeast(0).toLong()
        val pos = (runCatching { c?.position }.getOrNull() ?: 0).toLong().coerceIn(0L, maxOf(dur, 0L))
        val state = if (playing) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED

        // ГРАБЛЯ: без METADATA_KEY_DURATION система рисует в уведомлении
        // серую полоску вместо ползунка, и он не двигается.
        session.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title.ifEmpty { "Wonder" })
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, dur.coerceAtLeast(1))
                .build()
        )

        val actions = (PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE
            or PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_SKIP_TO_NEXT
            or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_SEEK_TO).toLong()
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(state, pos, 1f)
                .setBufferedPosition(pos)
                .build()
        )
    }

    private fun build(): Notification {
        // ВАЖНО: без SINGLE_TOP/CLEAR_TOP тап по уведомлению создавал ВТОРУЮ
        // MainActivity со своим PlayerController: интерфейс пустой («ничего не
        // играет»), а старый MediaPlayer продолжал играть сам по себе.
        val openIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_OPEN_NOW, true)
        }
        val open = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID)
        else @Suppress("DEPRECATION") Notification.Builder(this)

        b.setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title.ifEmpty { "Wonder" })
            .setContentText(artist)
            .setContentIntent(open)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(playing)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(action(ACTION_PREV, R.drawable.ic_prev, "Назад"))
            .addAction(
                action(
                    if (playing) ACTION_PAUSE else ACTION_PLAY,
                    if (playing) R.drawable.ic_pause else R.drawable.ic_play,
                    if (playing) "Пауза" else "Играть"
                )
            )
            .addAction(action(ACTION_NEXT, R.drawable.ic_next, "Дальше"))
            .addAction(action(ACTION_ADD, R.drawable.ic_plus, "В мою музыку"))
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        cover?.let { b.setLargeIcon(it) }
        return b.build()
    }

    private fun action(act: String, iconRes: Int, label: String): Notification.Action {
        val i = Intent(this, PlaybackService::class.java).setAction(act)
        val pi = PendingIntent.getService(
            this, act.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val icon = Icon.createWithResource(this, iconRes)
        return Notification.Action.Builder(icon, label, pi).build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            CHANNEL_ID, "Воспроизведение", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Текущий трек и управление"
            setShowBadge(false)
        }
        nm.createNotificationChannel(ch)
    }

    override fun onDestroy() {
        ticker.removeCallbacks(tick)
        runCatching { session.isActive = false }
        // закрыли трек - уведомление не должно висеть
        runCatching {
            if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE)
            else @Suppress("DEPRECATION") stopForeground(true)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        PlayerHub.controller?.pause()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }
}
