package ru.topbun.panelcontrol

import android.Manifest
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import ru.topbun.panelcontrol.ControlButtons.BLUETOOTH
import ru.topbun.panelcontrol.ControlButtons.FLASHLIGHT
import ru.topbun.panelcontrol.ControlButtons.LOCATION
import ru.topbun.panelcontrol.ControlButtons.MOBILE_INTERNET
import ru.topbun.panelcontrol.ControlButtons.MODEM
import ru.topbun.panelcontrol.ControlButtons.NO_DISTURB
import ru.topbun.panelcontrol.ControlButtons.OTG
import ru.topbun.panelcontrol.ControlButtons.SCREEN
import ru.topbun.panelcontrol.ControlButtons.SIMCARD
import ru.topbun.panelcontrol.ControlButtons.SOUND
import ru.topbun.panelcontrol.ControlButtons.USB
import ru.topbun.panelcontrol.ControlButtons.WIFI


sealed interface ControlManager {

    object LongClick{

        fun defineLongClick(context: Context, button: ControlButtons) {
            val intent = when (button) {
                WIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
                MOBILE_INTERNET -> Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                BLUETOOTH -> Intent().apply {
                    component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$AdvancedConnectedDeviceActivity"
                    )
                }
                NO_DISTURB -> Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
                FLASHLIGHT -> null
                USB -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                OTG -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                LOCATION -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                MODEM -> Intent().apply {
                    component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$WifiTetherSettingsActivity"
                    )
                }
                SIMCARD -> Intent(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS)
                SOUND -> Intent(Settings.ACTION_SOUND_SETTINGS)
                SCREEN -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            }

            intent?.let {
                try {
                    context.startActivity(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LongClick", "Failed to open settings for $button")
                }
            }

        }

    }

    object Click{

        fun defineClick(context: Context, button: ControlButtons) {
            when (button) {
                WIFI -> toggleWifi(context)
                MOBILE_INTERNET -> toggleMobileData(context)
                BLUETOOTH -> toggleBluetooth(context)
                NO_DISTURB -> toggleDoNotDisturb(context)
                FLASHLIGHT -> toggleFlashlight(context)
                USB -> toggleUSB(context)
                OTG -> toggleOTGMode(context)
                LOCATION -> toggleLocation(context)
                MODEM -> toggleModem(context)
                SIMCARD -> toggleSimcard(context)
                SOUND -> toggleSound(context)
                SCREEN -> toggleAutobrig(context)
            }
        }

        private fun toggleAutobrig(context: Context){
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            context.startActivity(intent)
        }

        private fun toggleSound(context: Context){
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
            context.startActivity(intent)
        }

        private fun toggleSimcard(context: Context){
            val intent = Intent(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS)
            context.startActivity(intent)
        }

        private fun toggleModem(context: Context) {
            val tetherSettings = Intent().apply {
                component = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$WifiTetherSettingsActivity"
                )
            }
            context.startActivity(tetherSettings)
        }

        private fun toggleLocation(context: Context) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }

        private fun toggleUSB(context: Context) {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            context.startActivity(intent)
        }

        private fun toggleWifi(context: Context) {
            val wifiManager = getSystemService(context, WifiManager::class.java)
            wifiManager?.let { wifiManager ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                } else {
                    val panelIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    context.startActivity(panelIntent)
                }
            }

        }

        private fun toggleMobileData(context: Context) {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            context.startActivity(intent)
        }

        private fun toggleBluetooth(context: Context) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter.isEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        context.startActivity(intent)
                    } else { bluetoothAdapter.disable() }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        context.startActivity(enableBtIntent)
                    } else { bluetoothAdapter.enable() }
                }
            }

        }

        private fun toggleDoNotDisturb(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
                return
            }
            if (notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALL) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        }

        private var flashLightStatus = false

        private fun toggleFlashlight(context: Context) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                try {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    if (flashAvailable) {
                        if (!flashLightStatus) {
                            cameraManager.setTorchMode(cameraId, true)
                            flashLightStatus = true
                        } else {
                            cameraManager.setTorchMode(cameraId, false)
                            flashLightStatus = false
                        }
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
        }

        fun checkFlashlightStatus(): Boolean {
            return flashLightStatus
        }

        private fun toggleAirplaneMode(context: Context) {
            context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
        }

        private fun toggleOTGMode(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(android.provider.Settings.ACTION_SETTINGS)
                context.startActivity(fallbackIntent)
            }
        }

    }

}