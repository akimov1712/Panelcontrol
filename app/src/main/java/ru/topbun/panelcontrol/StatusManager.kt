package ru.topbun.panelcontrol

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat.getSystemService
import java.security.AccessController.getContext


class StatusManager {

    fun defineStatus(context: Context, button: ControlButtons): Boolean = when (button) {
        ControlButtons.WIFI -> isWifiEnabled(context)
        ControlButtons.MOBILE_INTERNET -> isMobileInternetEnabled(context)
        ControlButtons.BLUETOOTH -> isBluetoothEnabled()
        ControlButtons.NO_DISTURB -> isDoNotDisturbEnabled(context)
        ControlButtons.FLASHLIGHT -> isFlashlightEnabled(context)
        ControlButtons.AIRPLANE -> isAirplaneModeEnabled(context)
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