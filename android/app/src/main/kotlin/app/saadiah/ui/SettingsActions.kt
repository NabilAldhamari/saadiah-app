package app.saadiah.ui

data class SettingsActions(
    val onChange: (SettingsEdit) -> Unit,
    val onChangeCity: () -> Unit,
    val onOpenDoctor: () -> Unit,
    val onOpenBaqarah: () -> Unit,
    val onMatchMasjid: () -> Unit,
    val onOpenAudioDownloads: () -> Unit = {},
    val onResetMatchedProfile: () -> Unit = {},
)
