package app.saadiah.doctor

import app.saadiah.ui.EnglishStrings
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val EXPECTED = Instant.fromEpochSeconds(1_700_000_000L)

class DeliveryLogTest {
    @Test
    fun arrivingExactlyOnTimeCountsAsOnTime() {
        assertEquals(expected = Delivery.ON_TIME, actual = classifyDelivery(EXPECTED, EXPECTED))
    }

    @Test
    fun arrivingWithinToleranceCountsAsOnTime() {
        assertEquals(expected = Delivery.ON_TIME, actual = classifyDelivery(EXPECTED, EXPECTED + 30.seconds))
    }

    @Test
    fun arrivingBeyondToleranceCountsAsLate() {
        assertEquals(expected = Delivery.LATE, actual = classifyDelivery(EXPECTED, EXPECTED + 5.minutes))
    }

    @Test
    fun theBoundaryItselfIsStillOnTime() {
        assertEquals(expected = Delivery.ON_TIME, actual = classifyDelivery(EXPECTED, EXPECTED + TOLERANCE))
    }

    @Test
    fun aMomentPastTheBoundaryIsLate() {
        assertEquals(expected = Delivery.LATE, actual = classifyDelivery(EXPECTED, EXPECTED + TOLERANCE + 1.seconds))
    }

    @Test
    fun firingEarlyIsNotLate() {
        assertEquals(expected = Delivery.ON_TIME, actual = classifyDelivery(EXPECTED, EXPECTED - 10.seconds))
    }

    @Test
    fun latenessIsReportedInFullWords() {
        val record =
            DeliveryRecord(
                prayerName = "Fajr",
                expected = EXPECTED,
                actual = EXPECTED + 3.minutes,
                delivery = Delivery.LATE,
            )

        assertEquals(expected = "3 minutes late", actual = record.describeDelay(EnglishStrings))
    }

    @Test
    fun anOnTimeRecordSaysSo() {
        val record =
            DeliveryRecord(
                prayerName = "Asr",
                expected = EXPECTED,
                actual = EXPECTED,
                delivery = Delivery.ON_TIME,
            )

        assertEquals(expected = "on time", actual = record.describeDelay(EnglishStrings))
    }
}
