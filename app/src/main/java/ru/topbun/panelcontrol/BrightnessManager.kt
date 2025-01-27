package ru.topbun.panelcontrol

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object BrightnessManager {

    fun getSystemBrightness(context: Context): Float {
        return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 3300f
    }

    fun setSystemBrightness(context: Context, brightness: Int) {
        val pq = PermissionRequester()
        if (pq.hasWriteSettingsPermission(context)) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness
            )
        } else {
            pq.requestWriteSettingsPermission(context)
        }
    }

}
