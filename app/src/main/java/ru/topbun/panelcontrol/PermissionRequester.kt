package ru.topbun.panelcontrol

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

class PermissionRequester {

    @Composable
    fun request(vararg permission: String){
        val contract = ActivityResultContracts.RequestMultiplePermissions()
        val launcher = rememberLauncherForActivityResult(contract = contract) {
        }
        SideEffect {
            launcher.launch(permission.toList().toTypedArray())
        }
    }

}