package app.saadiah.ui

import app.saadiah.data.Settings

data class SettingsActions(
    val onChange: (Settings) -> Unit,
    val onChangeCity: () -> Unit,
    val onOpenDoctor: () -> Unit,
    val onOpenBaqarah: () -> Unit,
    val onMatchMasjid: () -> Unit,
)
