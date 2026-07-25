package app.saadiah.model

import kotlinx.datetime.Instant

enum class AlarmKind {
    PRE_ALERT,
    AT_TIME,
    END_OF_WINDOW,
}

data class AlarmSpec(
    val prayer: Prayer,
    val triggerAt: Instant,
    val kind: AlarmKind,
)
