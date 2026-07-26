package app.saadiah.doctor

import android.content.Context
import androidx.core.content.edit
import app.saadiah.ui.Strings
import app.saadiah.ui.spelledOut
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val PREFS = "saadiah.deliveries"
private const val KEY_RECORDS = "records"
private const val FIELD_SEPARATOR = "|"
private const val RECORD_SEPARATOR = "\n"
private const val FIELD_COUNT = 3
private const val MAX_RECORDS = 20

/** A minute's drift is invisible to a person; beyond that the alert reads as late. */
val TOLERANCE = 1.minutes

enum class Delivery { ON_TIME, LATE }

data class DeliveryRecord(
    val prayerName: String,
    val expected: Instant,
    val actual: Instant,
    val delivery: Delivery,
) {
    fun describeDelay(strings: Strings): String {
        if (delivery == Delivery.ON_TIME) return strings.onTime
        return strings.lateBy((actual - expected).spelledOut(strings))
    }
}

fun classifyDelivery(
    expected: Instant,
    actual: Instant,
    tolerance: Duration = TOLERANCE,
): Delivery = if (actual - expected > tolerance) Delivery.LATE else Delivery.ON_TIME

/**
 * Kept locally and never sent anywhere. It exists so a user can see for themselves
 * whether their phone is holding alarms back.
 */
class DeliveryLog(
    context: Context,
) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun record(
        prayerName: String,
        expected: Instant,
        actual: Instant,
    ) {
        val entry = listOf(prayerName, expected.epochSeconds, actual.epochSeconds).joinToString(FIELD_SEPARATOR)
        val kept = (listOf(entry) + stored()).take(MAX_RECORDS)
        preferences.edit { putString(KEY_RECORDS, kept.joinToString(RECORD_SEPARATOR)) }
    }

    fun recent(): List<DeliveryRecord> = stored().mapNotNull(::parse)

    private fun stored(): List<String> =
        preferences
            .getString(KEY_RECORDS, "")
            .orEmpty()
            .split(RECORD_SEPARATOR)
            .filter { it.isNotBlank() }

    private fun parse(entry: String): DeliveryRecord? {
        val fields = entry.split(FIELD_SEPARATOR)
        if (fields.size != FIELD_COUNT) return null
        val expected = fields[1].toLongOrNull()?.let(Instant::fromEpochSeconds)
        val actual = fields[2].toLongOrNull()?.let(Instant::fromEpochSeconds)
        if (expected == null || actual == null) return null
        return DeliveryRecord(
            prayerName = fields[0],
            expected = expected,
            actual = actual,
            delivery = classifyDelivery(expected, actual),
        )
    }
}
