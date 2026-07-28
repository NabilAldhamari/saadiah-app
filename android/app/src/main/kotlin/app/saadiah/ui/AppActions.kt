package app.saadiah.ui

import app.saadiah.data.Settings
import app.saadiah.model.City

/**
 * A change to make to whatever is stored, rather than the value to store. A screen composes
 * against a snapshot that is already stale by the time a tap reaches the disk, so handing back
 * the whole snapshot reverts every other change made since it was taken.
 */
typealias SettingsEdit = (Settings) -> Settings

/**
 * What the app can do that outlives a screen. Navigation is deliberately absent: that
 * belongs to [Navigator], so the phone's back button and the on-screen one cannot drift.
 */
data class AppActions(
    val onChangeSettings: (SettingsEdit) -> Unit,
    val onChangeCity: (City) -> Unit,
    val onOpenBackgroundSettings: () -> Unit,
    val onApplyMatchedProfile: (app.saadiah.prayer.SolveResult) -> Unit,
)
