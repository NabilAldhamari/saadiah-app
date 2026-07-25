package app.saadiah.ui

enum class Destination { TODAY, CALENDAR, ADHKAR, MORE }

data class AppActions(
    val onChangeSettings: (app.saadiah.data.Settings) -> Unit,
    val onChangeCity: () -> Unit,
    val onOpenDoctor: () -> Unit,
)
