package app.saadiah.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

enum class Prayer {
    FAJR,
    SUNRISE,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA,
}

data class DayTimings(
    val date: LocalDate,
    val times: Map<Prayer, Instant>,
) {
    init {
        require(Prayer.entries.all { it in times }) { "timings must include every prayer" }
    }

    operator fun get(prayer: Prayer): Instant = times.getValue(prayer)
}
