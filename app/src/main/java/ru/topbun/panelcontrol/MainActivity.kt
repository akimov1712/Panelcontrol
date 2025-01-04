package ru.topbun.panelcontrol

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.controls.templates.ControlButton
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import ru.topbun.panelcontrol.ControlButtons.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(Color.Black)

            val permissionRequester = PermissionRequester()
            permissionRequester.request(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.CAMERA,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.WRITE_SECURE_SETTINGS,
                Manifest.permission.UPDATE_DEVICE_STATS,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
            )
            ControlPanel()
        }
    }
}

@Composable
fun ControlPanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(vertical = 20.dp, horizontal = 14.dp)
    ) {
        val statusManager = StatusManager()
        val context = LocalContext.current
        var statusWifi by rememberSaveable {
            mutableStateOf(
                statusManager.defineStatus(
                    context,
                    WIFI
                )
            )
        }
        var statusMobileInternet by rememberSaveable {
            mutableStateOf(
                statusManager.defineStatus(
                    context,
                    MOBILE_INTERNET
                )
            )
        }
        var statusBluetooth by rememberSaveable {
            mutableStateOf(
                statusManager.defineStatus(
                    context,
                    BLUETOOTH
                )
            )
        }
        var statusNoDisturb by rememberSaveable {
            mutableStateOf(
                statusManager.defineStatus(
                    context,
                    NO_DISTURB
                )
            )
        }
        var statusFlashlight by rememberSaveable {
            mutableStateOf(
                statusManager.defineStatus(
                    context,
                    FLASHLIGHT
                )
            )
        }
        var statusAirplane by rememberSaveable {
            mutableStateOf(
                statusManager.defineStatus(
                    context,
                    AIRPLANE
                )
            )
        }

        LaunchedEffect(Unit) {
            while (true) {
                statusWifi = statusManager.defineStatus(context, WIFI)
                statusMobileInternet = statusManager.defineStatus(context, MOBILE_INTERNET)
                statusBluetooth = statusManager.defineStatus(context, BLUETOOTH)
                statusNoDisturb = statusManager.defineStatus(context, NO_DISTURB)
                statusFlashlight = statusManager.defineStatus(context, FLASHLIGHT)
                statusAirplane = statusManager.defineStatus(context, AIRPLANE)

                delay(500)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ControlButtons.entries) {
                ControlButton(
                    label = it.label,
                    icon = it.icon,
                    isActive = when (it) {
                        WIFI -> statusWifi
                        MOBILE_INTERNET -> statusMobileInternet
                        BLUETOOTH -> statusBluetooth
                        NO_DISTURB -> statusNoDisturb
                        FLASHLIGHT -> statusFlashlight
                        AIRPLANE -> statusAirplane
                    },
                    onClick = {
                        ControlManager.Click.defineClick(context, it)
                        when (it) {
                            WIFI -> statusWifi = statusManager.defineStatus(context, WIFI)
                            MOBILE_INTERNET -> statusMobileInternet =
                                statusManager.defineStatus(context, MOBILE_INTERNET)

                            BLUETOOTH -> statusBluetooth =
                                statusManager.defineStatus(context, BLUETOOTH)

                            NO_DISTURB -> statusNoDisturb =
                                statusManager.defineStatus(context, NO_DISTURB)

                            FLASHLIGHT -> statusFlashlight =
                                statusManager.defineStatus(context, FLASHLIGHT)

                            AIRPLANE -> statusAirplane =
                                statusManager.defineStatus(context, AIRPLANE)
                        }
                    },
                    onLongClick = {
                        ControlManager.LongClick.defineLongClick(context, it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlButton(
    label: String,
    icon: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if(isActive) Color(0xffDCE1FF) else Color(0xff302F34)
    val animateBgColor by animateColorAsState(bgColor)
    val contentColor = if(isActive) Color(0xff1A1B20) else Color(0xffEDECF1)
    val animateContentColor by animateColorAsState(contentColor)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(animateBgColor)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = animateContentColor
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            modifier = Modifier
                .weight(1f)
                .basicMarquee(Int.MAX_VALUE),
            text = label,
            color = animateContentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
