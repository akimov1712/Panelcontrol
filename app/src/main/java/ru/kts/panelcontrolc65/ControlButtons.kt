package ru.kts.panelcontrolc65

enum class ControlButtons(
    val label: String,
    val icon: Int,
) {

    WIFI(
        label = "Wi-Fi",
        icon = R.drawable.ic_wifi
    ),

    FLASHLIGHT(
        label = "Фонарик",
        icon = R.drawable.ic_flashlight
    ),

    MOBILE_INTERNET(
        label = "Мобильный интернет",
        icon = R.drawable.ic_mobile_internet
    ),

    BLUETOOTH(
        label = "Bluetooth",
        icon = R.drawable.ic_bluetooth
    ),

    NO_DISTURB(
        label = "Не беспокоить",
        icon = R.drawable.ic_disturb
    ),

    USB(
        label = "Настройка USB",
        icon = R.drawable.ic_usb
    ),

    OTG(
        label = "Режим OTG",
        icon = R.drawable.ic_otg
    ),

    LOCATION(
        label = "Геолокация",
        icon = R.drawable.ic_location
    ),

    MODEM(
        label = "Точка доступа",
        icon = R.drawable.ic_modem
    ),

    SIMCARD(
        label = "Сим-карта",
        icon = R.drawable.ic_simcard
    ),

    SOUND(
        label = "Звук и мелодия",
        icon = R.drawable.ic_sound
    ),

    SCREEN(
        label = "Настройка экрана",
        icon = R.drawable.ic_autobrig
    )

}