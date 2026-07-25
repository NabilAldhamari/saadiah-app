package app.saadiah.calendar

import app.saadiah.model.DayTimings
import app.saadiah.model.HijriDate
import app.saadiah.model.Prayer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus

/**
 * The Hijri day runs `[maghrib(d), maghrib(d+1))`, so an evening belongs to the
 * following Hijri date. Pass the timings of the civil day `instant` falls on;
 * the boundary follows this profile's Maghrib, which is not always sunset.
 */
fun DayTimings.hijriDateAt(instant: Instant): HijriDate {
    val civilDate = if (instant >= this[Prayer.MAGHRIB]) date.plus(1, DateTimeUnit.DAY) else date
    return civilDate.toHijriDate()
}
