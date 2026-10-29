package su.gwarden.music.model

/**
 * Шортс: трек + «лучший» кусок внутри него.
 *
 * Границы (start/end) считаются по громкости и ударам: это не случайные
 * секунды, а самый плотный/жирный участок трека (обычно припев или дроп).
 */
data class Short(
    val track: Track = Track(),
    val start: Float = 0f,
    val end: Float = 30f,
    /** drop - резкий скачок после тишины, flow - ускорение читки */
    val kind: String = "drop",
)
