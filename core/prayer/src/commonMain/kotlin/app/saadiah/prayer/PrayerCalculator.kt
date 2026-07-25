package app.saadiah.prayer

import app.saadiah.model.City
import app.saadiah.model.DayTimings
import app.saadiah.model.MaghribMode
import app.saadiah.model.Prayer
import app.saadiah.model.TimingProfile
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.PrayerAdjustments
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.batoulapps.adhan2.data.TimeComponents
import com.batoulapps.adhan2.internal.SolarTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import app.saadiah.model.HighLatitudeRule as ModelHighLatitudeRule
import app.saadiah.model.Madhab as ModelMadhab
import com.batoulapps.adhan2.Coordinates as AdhanCoordinates
import com.batoulapps.adhan2.HighLatitudeRule as AdhanHighLatitudeRule
import com.batoulapps.adhan2.Madhab as AdhanMadhab

private const val SECONDS_PER_MINUTE = 60L
private const val HALF_MINUTE = 30L

class PrayerCalculator {
    fun compute(
        city: City,
        date: LocalDate,
        profile: TimingProfile,
    ): DayTimings {
        val coordinates = AdhanCoordinates(city.coordinates.latitude, city.coordinates.longitude)
        val dateComponents = DateComponents(date.year, date.monthNumber, date.dayOfMonth)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, profile.toCalculationParameters())

        val maghrib =
            when (profile.maghribMode) {
                MaghribMode.SUNSET -> prayerTimes.maghrib
                MaghribMode.ANGLE -> maghribByAngle(coordinates, dateComponents, profile)
            }

        return DayTimings(
            date = date,
            times =
                mapOf(
                    Prayer.FAJR to prayerTimes.fajr,
                    Prayer.SUNRISE to prayerTimes.sunrise,
                    Prayer.DHUHR to prayerTimes.dhuhr,
                    Prayer.ASR to prayerTimes.asr,
                    Prayer.MAGHRIB to maghrib,
                    Prayer.ISHA to prayerTimes.isha,
                ),
        )
    }

    private fun maghribByAngle(
        coordinates: AdhanCoordinates,
        dateComponents: DateComponents,
        profile: TimingProfile,
    ): Instant {
        val solarTime = SolarTime(dateComponents, coordinates)
        val hours = solarTime.timeForSolarAngle(-profile.angles.maghrib, afterTransit = true)
        val components =
            requireNotNull(TimeComponents.fromDouble(hours)) {
                "maghrib angle ${profile.angles.maghrib} is unsolvable at this location"
            }
        val evening = components.dateComponents(dateComponents).toInstant(TimeZone.UTC)
        val offset = profile.adjustments[Prayer.MAGHRIB]?.inWholeMinutes ?: 0L
        return evening.plusMinutes(offset).roundedToMinute()
    }
}

private fun TimingProfile.toCalculationParameters(): CalculationParameters =
    CalculationParameters(
        fajrAngle = angles.fajr,
        ishaAngle = angles.isha,
        ishaInterval = angles.ishaInterval?.inWholeMinutes?.toInt() ?: 0,
        method = CalculationMethod.OTHER,
        madhab = madhab.toAdhan(),
        highLatitudeRule = highLatitudeRule.toAdhan(),
        prayerAdjustments = adjustments.toPrayerAdjustments(),
    )

private fun ModelMadhab.toAdhan(): AdhanMadhab =
    when (this) {
        ModelMadhab.SHAFI -> AdhanMadhab.SHAFI
        ModelMadhab.HANAFI -> AdhanMadhab.HANAFI
    }

private fun ModelHighLatitudeRule.toAdhan(): AdhanHighLatitudeRule =
    when (this) {
        ModelHighLatitudeRule.MIDDLE_OF_NIGHT -> AdhanHighLatitudeRule.MIDDLE_OF_THE_NIGHT
        ModelHighLatitudeRule.SEVENTH_OF_NIGHT -> AdhanHighLatitudeRule.SEVENTH_OF_THE_NIGHT
        ModelHighLatitudeRule.TWILIGHT_ANGLE -> AdhanHighLatitudeRule.TWILIGHT_ANGLE
    }

private fun Map<Prayer, Duration>.toPrayerAdjustments(): PrayerAdjustments =
    PrayerAdjustments(
        fajr = minutesFor(Prayer.FAJR),
        sunrise = minutesFor(Prayer.SUNRISE),
        dhuhr = minutesFor(Prayer.DHUHR),
        asr = minutesFor(Prayer.ASR),
        maghrib = minutesFor(Prayer.MAGHRIB),
        isha = minutesFor(Prayer.ISHA),
    )

private fun Map<Prayer, Duration>.minutesFor(prayer: Prayer): Int = this[prayer]?.inWholeMinutes?.toInt() ?: 0

private fun Instant.plusMinutes(minutes: Long): Instant =
    Instant.fromEpochSeconds(epochSeconds + minutes * SECONDS_PER_MINUTE)

private fun Instant.roundedToMinute(): Instant =
    Instant.fromEpochSeconds((epochSeconds + HALF_MINUTE) / SECONDS_PER_MINUTE * SECONDS_PER_MINUTE)
