package ru.topbun.panelcontrol

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

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
        var editMode by remember { mutableStateOf(false) }
        ButtonList(editMode)
        Spacer(Modifier.weight(1f))
        IconButton(
            modifier = Modifier.align(Alignment.End),
            onClick = { editMode = !editMode },
            colors = IconButtonDefaults.iconButtonColors(Color(0xff302F34))
        ) {
            Icon(
                imageVector = if (editMode) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ButtonList(
    editMode: Boolean
) {

    val view = LocalView.current
    val context = LocalContext.current

    val orderPreferencesManager = OrderPreferencesManager(context)
    var list by remember { mutableStateOf(emptyList<ControlButtons>()) }

    LaunchedEffect(Unit) {
        orderPreferencesManager.getOrder().collect { savedOrder ->
            if (savedOrder.isNotEmpty()) {
                val buttons = ControlButtons.entries
                list = savedOrder.map { btnStr -> buttons.first { btnStr == it.name } }
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    if (list.isNotEmpty()) {
        val lazyGridState = rememberLazyGridState()
        val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
            list = list.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }

            coroutineScope.launch {
                orderPreferencesManager.saveOrder(list.map { it.name })
            }

            ViewCompat.performHapticFeedback(
                view,
                HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
            )
        }

        val statusManager = StatusManager()
        var statusWifi by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, WIFI))
        }
        var statusMobileInternet by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, MOBILE_INTERNET))
        }
        var statusBluetooth by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, BLUETOOTH))
        }
        var statusNoDisturb by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, NO_DISTURB))
        }
        var statusFlashlight by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, FLASHLIGHT))
        }
        var statusLocation by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, LOCATION))
        }
        var statusModem by rememberSaveable {
            mutableStateOf(statusManager.defineStatus(context, MODEM))
        }

        LaunchedEffect(Unit) {
            while (true) {
                statusWifi = statusManager.defineStatus(context, WIFI)
                statusMobileInternet = statusManager.defineStatus(context, MOBILE_INTERNET)
                statusBluetooth = statusManager.defineStatus(context, BLUETOOTH)
                statusNoDisturb = statusManager.defineStatus(context, NO_DISTURB)
                statusFlashlight = statusManager.defineStatus(context, FLASHLIGHT)
                statusLocation = statusManager.defineStatus(context, LOCATION)
                statusModem = statusManager.defineStatus(context, MODEM)

                delay(500)
            }
        }

        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list, key = { it.name }) { item ->
                ReorderableItem(reorderableLazyGridState, key = item.name) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isActive = when (item) {
                        WIFI -> statusWifi
                        MOBILE_INTERNET -> statusMobileInternet
                        BLUETOOTH -> statusBluetooth
                        NO_DISTURB -> statusNoDisturb
                        FLASHLIGHT -> statusFlashlight
                        USB -> false
                        OTG -> false
                        LOCATION -> statusLocation
                        MODEM -> statusModem
                        SIMCARD -> false
                        SOUND -> false
                        SCREEN -> false
                    }

                    ControlButton(
                        label = item.label,
                        icon = item.icon,
                        isActive = isActive,
                        onClick = {
                            ControlManager.Click.defineClick(context, item)
                        },
                        onLongClick = {
                            ControlManager.LongClick.defineLongClick(context, item)
                        },
                        onHandleContent = {
                            if (editMode){
                                IconButton(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .draggableHandle(
                                            onDragStarted = {
                                                ViewCompat.performHapticFeedback(
                                                    view,
                                                    HapticFeedbackConstantsCompat.GESTURE_START
                                                )
                                            },
                                            onDragStopped = {
                                                ViewCompat.performHapticFeedback(
                                                    view,
                                                    HapticFeedbackConstantsCompat.GESTURE_END
                                                )
                                            },
                                            interactionSource = interactionSource,
                                        ),
                                    onClick = {}
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Menu,
                                        contentDescription = "Reorder",
                                        tint = if (isActive) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onHandleContent: @Composable () -> Unit = {}
) {
    val bgColor = if (isActive) Color(0xffDCE1FF) else Color(0xff302F34)
    val animateBgColor by animateColorAsState(bgColor)
    val contentColor = if (isActive) Color(0xff1A1B20) else Color(0xffEDECF1)
    val animateContentColor by animateColorAsState(contentColor)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(animateBgColor)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(start = 20.dp, top = 22.dp, bottom = 22.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = animateContentColor
        )
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
        onHandleContent()
    }
}
