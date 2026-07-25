package app.saadiah.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class AlarmSpecTest {
    @Test
    fun retainsPrayerAndKind() {
        val spec =
            AlarmSpec(
                prayer = Prayer.FAJR,
                triggerAt = Instant.fromEpochSeconds(0L),
                kind = AlarmKind.AT_TIME,
            )
        assertEquals(Prayer.FAJR, spec.prayer)
        assertEquals(AlarmKind.AT_TIME, spec.kind)
    }
}
