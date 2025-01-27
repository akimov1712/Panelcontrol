package ru.kts.panelcontrolc65

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext

class PermissionRequester {

    @Composable
    fun request(vararg permission: String){
        val context = LocalContext.current
        val contract = ActivityResultContracts.RequestMultiplePermissions()
        val launcher = rememberLauncherForActivityResult(contract = contract) {}
        SideEffect {
            launcher.launch(permission.toList().toTypedArray())
            requestWriteSettingsPermission(context)
        }
    }

    fun hasWriteSettingsPermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun requestWriteSettingsPermission(context: Context) {
        if (!hasWriteSettingsPermission(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

}