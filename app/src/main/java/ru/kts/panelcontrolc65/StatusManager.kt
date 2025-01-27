package ru.kts.panelcontrolc65

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import ru.kts.panelcontrolc65.ControlButtons.*
import java.lang.reflect.Method
import java.security.AccessController.getContext


class StatusManager {

    fun defineStatus(context: Context, button: ControlButtons): Boolean = when (button) {
        WIFI -> isWifiEnabled(context)
        MOBILE_INTERNET -> isMobileInternetEnabled(context)
        BLUETOOTH -> isBluetoothEnabled()
        NO_DISTURB -> isDoNotDisturbEnabled(context)
        FLASHLIGHT -> isFlashlightEnabled(context)
        USB -> false
        OTG -> false
        LOCATION -> isLocationEnabled(context)
        MODEM -> isModemEnabled(context)
        SIMCARD -> false
        SOUND -> false
        SCREEN -> false
    }

    private fun isSilentModeEnabled(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    private fun isModemEnabled(context: Context): Boolean {
        val wifimanager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method = wifimanager.javaClass.getDeclaredMethod("isWifiApEnabled");
            method.isAccessible = true;
            method.invoke(wifimanager) as Boolean
        } catch (e: Exception) { false }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = getSystemService(context, WifiManager::class.java)
        return wifiManager?.isWifiEnabled ?: false
    }

    private fun isMobileInternetEnabled(context: Context): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.dataState == TelephonyManager.DATA_CONNECTED
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

    private fun isDoNotDisturbEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.currentInterruptionFilter == android.app.NotificationManager.INTERRUPTION_FILTER_NONE
        } else {
            false
        }
    }

    private fun isFlashlightEnabled(context: Context): Boolean {
        return ControlManager.Click.checkFlashlightStatus()
    }

    private fun isAirplaneModeEnabled(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

}