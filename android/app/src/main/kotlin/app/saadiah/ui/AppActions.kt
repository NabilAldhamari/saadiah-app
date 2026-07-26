package app.saadiah.ui

import app.saadiah.data.Settings
import app.saadiah.model.City

/**
 * What the app can do that outlives a screen. Navigation is deliberately absent: that
 * belongs to [Navigator], so the phone's back button and the on-screen one cannot drift.
 */
data class AppActions(
    val onChangeSettings: (Settings) -> Unit,
    val onChangeCity: (City) -> Unit,
    val onOpenBackgroundSettings: () -> Unit,
    val onApplyMatchedProfile: (app.saadiah.prayer.SolveResult) -> Unit,
)
