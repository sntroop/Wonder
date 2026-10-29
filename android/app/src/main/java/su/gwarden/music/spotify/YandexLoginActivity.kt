package su.gwarden.music.spotify

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

/**
 * Публичная сборка Wonder: вход через Яндекс Музыку отключён.
 */
class YandexLoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "Вход через Яндекс Музыку недоступен в публичной сборке Wonder."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        })
    }
}
