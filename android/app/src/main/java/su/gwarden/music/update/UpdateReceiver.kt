package su.gwarden.music.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import su.gwarden.music.MainActivity

/**
 * Система присылает MY_PACKAGE_REPLACED после установки новой версии.
 * Процесс к этому моменту уже убит, поэтому именно отсюда приложение
 * открывается само — «перезапустилось обновлённым».
 */
class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        runCatching {
            val open = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(open)
        }
    }
}
