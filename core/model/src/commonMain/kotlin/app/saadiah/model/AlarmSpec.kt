package app.saadiah.model

import kotlinx.datetime.Instant

enum class AlarmKind {
    PRE_ALERT,
    AT_TIME,
    END_OF_WINDOW,

    // Wakes the app to arm the next batch when the slot budget truncates the horizon.
    RE_ARM,
}

data class AlarmSpec(
    val prayer: Prayer,
    val triggerAt: Instant,
    val kind: AlarmKind,
)
