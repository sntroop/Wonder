package su.gwarden.music.player

/** Доступ к активному плееру из сервиса уведомления. */
object PlayerHub {
    @Volatile
    var controller: PlayerController? = null
}
