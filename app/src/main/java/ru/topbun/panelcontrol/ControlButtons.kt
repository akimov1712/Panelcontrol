package ru.topbun.panelcontrol

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

    AIRPLANE(
        label = "Режим полета",
        icon = R.drawable.ic_airplane
    ),

}