package su.gwarden.music.player

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Локальный HTTP-сервер на 127.0.0.1, который отдаёт РАСТУЩИЙ файл.
 *
 * Зачем: MediaPlayer умеет играть только по одному долгоживущему соединению
 * (обычный HTTP GET с Range). Прямо с сервера это не работает - путь
 * владельца рвёт длинные ответы (~250 КБ) и MediaPlayer падает с -38.
 *
 * Решение: файл качается чанками в фоне (короткие запросы, доезжают),
 * а MediaPlayer подключается к ЭТОМУ локальному серверу, который отдаёт
 * уже скачанные байты и держит соединение, пока файл дополняется.
 * Так playback начинается с первого же чанка (~1%).
 */
class LocalStreamServer(
    private val download: ParallelDownloader.Download,
) {
    companion object {
        private const val TAG = "LocalStream"
    }

    private var serverSocket: ServerSocket? = null
    private val running = AtomicBoolean(false)

    /** Порт локального сервера (0 = не запущен). */
    var port: Int = 0
        private set

    fun start(): Boolean {
        return try {
            val ss = ServerSocket(0, 16, InetAddress.getByName("127.0.0.1"))
            serverSocket = ss
            port = ss.localPort
            running.set(true)
            Thread({ loop(ss) }, "local-stream-server").apply {
                isDaemon = true
                start()
            }
            Log.i(TAG, "started on 127.0.0.1:$port")
            true
        } catch (e: Exception) {
            Log.e(TAG, "start failed: ${e.message}")
            false
        }
    }

    private fun loop(ss: ServerSocket) {
        while (running.get()) {
            try {
                val socket = ss.accept()
                Thread({ handle(socket) }, "local-stream-conn").apply {
                    isDaemon = true
                    start()
                }
            } catch (e: Exception) {
                if (running.get()) Log.w(TAG, "accept failed: ${e.message}")
                else break
            }
        }
    }

    private fun handle(socket: Socket) {
        socket.use { s ->
            try {
                s.soTimeout = 30_000
                val input = s.getInputStream().bufferedReader()
                val requestLine = input.readLine() ?: return
                Log.i(TAG, "req: $requestLine")

                // Читаем заголовки
                var range: String? = null
                while (true) {
                    val line = input.readLine() ?: break
                    if (line.isEmpty()) break
                    val idx = line.indexOf(':')
                    if (idx > 0) {
                        val name = line.substring(0, idx).trim().lowercase()
                        val value = line.substring(idx + 1).trim()
                        if (name == "range") range = value
                    }
                }

                val total = download.totalSize()
                var start = 0L
                var end = total - 1
                var status = "200 OK"
                if (range != null && range.startsWith("bytes=")) {
                    val spec = range.removePrefix("bytes=").substringBefore(',')
                    val parts = spec.split('-')
                    start = parts.getOrNull(0)?.trim()?.toLongOrNull() ?: 0L
                    val e = parts.getOrNull(1)?.trim()?.toLongOrNull()
                    if (e != null && e >= start) end = e
                    status = "206 Partial Content"
                }
                if (total > 0 && end > total - 1) end = total - 1
                if (start > 0 && total > 0 && start >= total) {
                    val out = s.getOutputStream()
                    out.write("HTTP/1.1 416 Range Not Satisfiable\r\nContent-Range: bytes */$total\r\n\r\n".toByteArray())
                    out.flush()
                    return
                }

                val out = s.getOutputStream()
                val sb = StringBuilder()
                sb.append("HTTP/1.1 $status\r\n")
                sb.append("Content-Type: audio/mpeg\r\n")
                sb.append("Accept-Ranges: bytes\r\n")
                if (total > 0) {
                    sb.append("Content-Length: ${end - start + 1}\r\n")
                    if (status == "206 Partial Content") {
                        sb.append("Content-Range: bytes $start-$end/$total\r\n")
                    }
                } else {
                    sb.append("Transfer-Encoding: chunked\r\n")
                }
                sb.append("Connection: close\r\n\r\n")
                out.write(sb.toString().toByteArray())
                out.flush()

                if (total > 0) {
                    // Отдаём байты по мере появления
                    var pos = start
                    val buf = ByteArray(64 * 1024)
                    while (pos <= end) {
                        val want = minOf(buf.size.toLong(), end - pos + 1).toInt()
                        val got = download.readAt(pos, want)
                        if (got.isEmpty()) {
                            // Ждём следующий чанк
                            val avail = download.awaitBytes(pos + 1, 20_000)
                            if (avail < 0) {
                                Log.w(TAG, "timeout waiting for data at $pos")
                                break
                            }
                            if (avail <= pos) continue
                            continue
                        }
                        out.write(got, 0, got.size)
                        out.flush()
                        pos += got.size
                    }
                } else {
                    // Размер неизвестен -> чанковая передача
                    val buf = ByteArray(64 * 1024)
                    var pos = 0L
                    while (true) {
                        val got = download.readAt(pos, buf.size)
                        if (got.isEmpty()) {
                            val avail = download.awaitBytes(pos + 1, 20_000)
                            if (avail < 0) break
                            continue
                        }
                        out.write((got.size.toString(16) + "\r\n").toByteArray())
                        out.write(got)
                        out.write("\r\n".toByteArray())
                        out.flush()
                        pos += got.size
                    }
                    out.write("0\r\n\r\n".toByteArray())
                    out.flush()
                }
            } catch (e: Exception) {
                Log.w(TAG, "handle error: ${e.message}")
            }
        }
    }

    fun stop() {
        running.set(false)
        runCatching { serverSocket?.close() }
        serverSocket = null
        port = 0
    }
}
