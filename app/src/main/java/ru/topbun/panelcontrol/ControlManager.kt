package ru.topbun.panelcontrol

import android.Manifest
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService


sealed interface ControlManager {

    object LongClick{

        fun defineLongClick(context: Context, button: ControlButtons) {
            val intent = when (button) {
                ControlButtons.WIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
                ControlButtons.MOBILE_INTERNET -> Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                ControlButtons.BLUETOOTH -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                ControlButtons.NO_DISTURB -> Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
                ControlButtons.FLASHLIGHT -> null
                ControlButtons.AIRPLANE -> Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
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
                ControlButtons.WIFI -> toggleWifi(context)
                ControlButtons.MOBILE_INTERNET -> toggleMobileData(context)
                ControlButtons.BLUETOOTH -> toggleBluetooth(context)
                ControlButtons.NO_DISTURB -> toggleDoNotDisturb(context)
                ControlButtons.FLASHLIGHT -> toggleFlashlight(context)
                ControlButtons.AIRPLANE -> toggleAirplaneMode(context)
            }
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
            val isEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
            Settings.Global.putInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, if (isEnabled) 0 else 1)
            val intent = android.content.Intent(android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", !isEnabled)
            }
            context.sendBroadcast(intent)
        }

    }

}