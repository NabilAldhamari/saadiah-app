package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.CityId
import app.saadiah.model.Coordinates
import app.saadiah.model.CountryCode
import app.saadiah.model.Madhab
import app.saadiah.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * Checked against a real published timetable rather than against this app's own output. Every
 * other prayer fixture in this module is adhan's, which proves only that we drive the library the
 * way its authors do — that suite was green throughout while London was two hours wrong.
 *
 * The London Unified Prayer Timetable takes sunrise, Ẓuhr, ʿAṣr and Maghrib from HMNAO and adds
 * safety margins it publishes: sunrise −3, Ẓuhr +5 from transit, Maghrib +3. Those margins are
 * exactly what per-prayer tuning is for, so they go in as tuning and the astronomy is then
 * asserted against the printed table across both British Summer Time and GMT.
 *
 * Fajr and ʿIshāʾ are not asserted against a method. The timetable takes them from Hizbul Ulama,
 * not from a sun-depression angle, so no method, madhhab or high-latitude rule can reproduce
 * them, and a test claiming otherwise would be bending the reference to fit us.
 * [theDistanceFromHizbulUlamaAtFajrAndIshaIsPinned] records the measured gap instead.
 */
class LondonUnifiedTest {
    private val calculator = PrayerCalculator()

    private val london =
        City(
            id = CityId(value = 2643743),
            name = "London",
            country = CountryCode(value = "GB"),
            admin1 = "England",
            coordinates = Coordinates(latitude = 51.5085, longitude = -0.1257),
            timeZone = TimeZone.of("Europe/London"),
        )

    // Ẓuhr is +5 from transit, and the Muslim World League preset already carries +1 of it.
    private val publishedMargins =
        mapOf(Prayer.SUNRISE to (-3).minutes, Prayer.DHUHR to 4.minutes, Prayer.MAGHRIB to 3.minutes)

    private fun profile(madhab: Madhab) = inferProfile(london).copy(madhab = madhab, adjustments = publishedMargins)

    private val days =
        Json
            .parseToJsonElement(readResource("/fixtures/london-unified.json"))
            .jsonObject
            .getValue("days")
            .jsonArray
            .map { it.jsonObject }

    @Test
    fun theAstronomicalPrayersTrackTheLondonTimetable() {
        val drift = days.flatMap { driftOn(it) }

        assertTrue(drift.isEmpty(), "drifted from the printed London timetable: " + drift.joinToString("; "))
    }

    private fun driftOn(day: JsonObject): List<String> {
        val date = LocalDate.parse(day.string("date"))
        val shafi = calculator.compute(london, date, profile(Madhab.SHAFI))
        val hanafi = calculator.compute(london, date, profile(Madhab.HANAFI))
        return listOf(
            Triple("sunrise", day.string("sunrise"), shafi[Prayer.SUNRISE]),
            Triple("zuhr", day.string("zuhr"), shafi[Prayer.DHUHR]),
            Triple("asr mithl 1", day.string("asr1"), shafi[Prayer.ASR]),
            Triple("asr mithl 2", day.string("asr2"), hanafi[Prayer.ASR]),
            Triple("maghrib", day.string("maghrib"), shafi[Prayer.MAGHRIB]),
        ).mapNotNull { (name, printed, ours) ->
            val off = minutesOf(ours) - clockToMinutes(printed)
            if (abs(off) > EPHEMERIS_TOLERANCE) "$date $name printed $printed, ours ${clock(ours)}" else null
        }
    }

    /**
     * Fajr swings from twenty-two minutes early in midwinter to fifty-five late in early July, so
     * no fixed offset closes it — matching this masjid's Fajr exactly would mean carrying Hizbul
     * Ulama's own table, not tuning a method. ʿIshāʾ stays inside twenty minutes all year, which
     * tuning does close. Before the high-latitude rule was taken from the latitude these were
     * eighty-one and ninety-seven minutes out in July; that is what these bounds guard against.
     */
    @Test
    fun theDistanceFromHizbulUlamaAtFajrAndIshaIsPinned() {
        val gaps = days.flatMap { gapsOn(it) }

        assertTrue(gaps.isEmpty(), "further from Hizbul Ulama than measured: " + gaps.joinToString("; "))
    }

    private fun gapsOn(day: JsonObject): List<String> {
        val date = LocalDate.parse(day.string("date"))
        val ours = calculator.compute(london, date, profile(Madhab.SHAFI))
        return listOf(
            Triple("fajr", Prayer.FAJR, FAJR_BOUND),
            Triple("isha", Prayer.ISHA, ISHA_BOUND),
        ).mapNotNull { (name, prayer, bound) ->
            val off = minutesOf(ours[prayer]) - clockToMinutes(day.string(name))
            if (abs(off) > bound) "$date $name is $off minutes from the printed table" else null
        }
    }

    private fun minutesOf(instant: Instant): Int {
        val time = instant.toLocalDateTime(london.timeZone).time
        return time.hour * MINUTES_PER_HOUR + time.minute
    }

    private fun clock(instant: Instant): String {
        val time = instant.toLocalDateTime(london.timeZone).time
        return time.hour.toString().padStart(CLOCK_DIGITS, '0') + ":" +
            time.minute.toString().padStart(CLOCK_DIGITS, '0')
    }

    private fun clockToMinutes(value: String): Int {
        val (hour, minute) = value.split(":").map { it.toInt() }
        return hour * MINUTES_PER_HOUR + minute
    }

    private fun readResource(path: String): String {
        val stream = requireNotNull(javaClass.getResourceAsStream(path)) { "missing resource $path" }
        return stream.bufferedReader().use { it.readText() }
    }

    private companion object {
        const val MINUTES_PER_HOUR = 60
        const val CLOCK_DIGITS = 2

        // HMNAO's ephemeris against adhan's solar model. One minute on seven of the eight sampled
        // days; two on 31 October, where the sun is low and ʿAṣr moves slowly against its angle.
        const val EPHEMERIS_TOLERANCE = 2
        const val FAJR_BOUND = 60
        const val ISHA_BOUND = 20
    }
}
