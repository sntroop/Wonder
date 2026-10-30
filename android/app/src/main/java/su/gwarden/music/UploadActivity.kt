package su.gwarden.music

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import su.gwarden.music.api.ApiClient

/**
 * Загрузка своего трека: аудио + обложка + название + жанр + дроп для шортсов.
 * Сервер разрешает один трек в 24 часа.
 */
class UploadActivity : AppCompatActivity() {

    private var audio: Uri? = null
    private var cover: Uri? = null
    private var durationSec = 0
    private var genre = "фонк"

    private lateinit var title: EditText
    private lateinit var drop: SeekBar
    private lateinit var len: SeekBar
    private lateinit var dropLabel: TextView
    private lateinit var lenLabel: TextView
    private lateinit var audioName: TextView
    private lateinit var coverImg: ImageView
    private lateinit var go: TextView
    private lateinit var chips: LinearLayout

    private val genres = listOf(
        "фонк", "хардтекк", "рэп", "трэп", "дрилл", "техно",
        "хаус", "драм-н-бейс", "поп", "инди", "другое",
    )
    private val chipViews = LinkedHashMap<String, TextView>()

    private val pickAudio =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            audio = uri
            takePermission(uri)
            audioName.text = displayName(uri) ?: "Файл выбран"
            durationSec = readDuration(uri)
            drop.max = (durationSec - 5).coerceAtLeast(1)
            drop.progress = (drop.max / 3).coerceAtLeast(0)
            updateDropLabel()
            toast("Длительность: " + time(durationSec))
        }

    private val pickCover =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            cover = uri
            takePermission(uri)
            coverImg.imageTintList = null
            runCatching {
                com.bumptech.glide.Glide.with(this).load(uri)
                    .centerCrop().into(coverImg)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        su.gwarden.music.ui.Skin.applyTheme(this)
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        setContentView(R.layout.activity_upload)
        su.gwarden.music.ui.Skin.apply(this)

        findViewById<View>(R.id.upload_back).setOnClickListener { finish() }
        findViewById<View>(R.id.upload_pick).setOnClickListener {
            runCatching { pickAudio.launch(arrayOf("audio/*")) }
        }
        findViewById<View>(R.id.upload_pick_cover).setOnClickListener {
            runCatching { pickCover.launch(arrayOf("image/*")) }
        }
        title = findViewById(R.id.upload_title)
        drop = findViewById(R.id.upload_drop)
        len = findViewById(R.id.upload_len)
        dropLabel = findViewById(R.id.upload_drop_label)
        lenLabel = findViewById(R.id.upload_len_label)
        audioName = findViewById(R.id.upload_audio_name)
        coverImg = findViewById(R.id.upload_cover)
        go = findViewById(R.id.upload_go)
        chips = findViewById(R.id.upload_genres)

        buildChips()
        setGenre(genre)

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) =
                updateDropLabel()

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }
        drop.setOnSeekBarChangeListener(listener)
        len.setOnSeekBarChangeListener(listener)
        updateDropLabel()

        go.setOnClickListener { publish() }

        // если 24 часа ещё не прошли - честно говорим заранее
        ApiClient.profileInfo(0) { p ->
            if (p == null || p.canUploadIn <= 0) return@profileInfo
            val h = p.canUploadIn / 3600
            val m = (p.canUploadIn % 3600) / 60
            val left = if (h > 0) "$h ч $m мин" else "$m мин"
            findViewById<TextView>(R.id.upload_cooldown).apply {
                visibility = View.VISIBLE
                text = "Следующий трек можно выложить через $left.\n" +
                        "Пока послушай, что уже есть, или собери новый дроп в шортсах."
            }
            go.alpha = 0.4f
            go.isEnabled = false
        }
    }

    private fun buildChips() {
        val dens = resources.displayMetrics.density
        for (g in genres) {
            val tv = TextView(this).apply {
                text = g
                textSize = 13f
                setPadding((14 * dens).toInt(), (8 * dens).toInt(), (14 * dens).toInt(), (8 * dens).toInt())
                setTextColor(0xFFE0E0E0.toInt())
                setBackgroundResource(R.drawable.bg_chip)
                setOnClickListener { setGenre(g) }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.marginEnd = (8 * dens).toInt()
            chips.addView(tv, lp)
            chipViews[g] = tv
        }
    }

    private fun setGenre(g: String) {
        genre = g
        for ((name, v) in chipViews) {
            val on = name == g
            v.setBackgroundResource(if (on) R.drawable.bg_listen else R.drawable.bg_chip)
            v.setTextColor(if (on) 0xFF0A0A0A.toInt() else 0xFFE0E0E0.toInt())
        }
    }

    private fun updateDropLabel() {
        val start = drop.progress
        val length = 8 + len.progress
        val end = if (durationSec > 0) (start + length).coerceAtMost(durationSec) else start + length
        dropLabel.text = "Дроп: " + time(start) + " — " + time(end)
        lenLabel.text = "Длина куска: $length сек"
    }

    private fun publish() {
        val a = audio
        if (a == null) {
            toast("Сначала выбери файл трека")
            return
        }
        val name = title.text.toString().trim()
        if (name.isEmpty()) {
            toast("Введи название трека")
            return
        }
        go.isEnabled = false
        go.alpha = 0.6f
        go.text = "Загружаю…"
        ApiClient.uploadTrack(
            audio = a,
            cover = cover,
            title = name,
            genre = genre,
            dropStart = drop.progress.toFloat(),
            dropLen = (8 + len.progress).toFloat(),
            duration = durationSec,
        ) { body, err ->
            go.isEnabled = true
            go.alpha = 1f
            go.text = "Опубликовать"
            if (body != null) {
                toast("Трек опубликован")
                setResult(RESULT_OK)
                finish()
            } else {
                toast(err ?: "Не получилось загрузить")
            }
        }
    }

    // ------------------------------------------------------------------ файлы

    private fun takePermission(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun displayName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) c.getString(i) else null
        }
    }.getOrNull()

    private fun readDuration(uri: Uri): Int {
        return runCatching {
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(this, uri)
                val ms = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
                (ms / 1000L).toInt()
            } finally {
                runCatching { mmr.release() }
            }
        }.getOrDefault(0)
    }

    private fun time(sec: Int): String {
        val s = sec.coerceAtLeast(0)
        return (s / 60).toString() + ":" + (s % 60).toString().padStart(2, '0')
    }

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
