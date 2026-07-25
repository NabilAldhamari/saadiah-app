package app.saadiah.schedule

import app.saadiah.model.City
import app.saadiah.model.CombineMode
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import kotlin.time.Duration

data class AlertSettings(
    val city: City,
    val profile: TimingProfile,
    val enabled: Set<Prayer>,
    val preAlert: Duration? = null,
    val endOfWindow: Duration? = null,
    val combineMode: CombineMode = CombineMode.NONE,
)
