package app.saadiah.alarm

import android.content.Context
import android.telephony.TelephonyManager
import app.saadiah.model.City
import app.saadiah.model.EXTENDED_CITIES
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlin.math.abs

data class TravelDetection(
    val isTraveling: Boolean,
    val detectedZone: TimeZone,
    val detectedCountry: String?,
    val suggestedCity: City?,
)

object TravelDetector {
    // 15 minutes in seconds: smallest standard timezone offset step globally (e.g. Nepal +5:45)
    private const val MIN_OFFSET_DIFF_SECONDS = 900

    /**
     * Differentiates real physical travel from seasonal Daylight Saving Time (DST) switches.
     * When DST begins or ends, the device's IANA zone ID (e.g., "Europe/London") does not change,
     * so this returns isTraveling = false to prevent annoying the user.
     *
     * Only when the device enters a different IANA zone with an offset difference >= 15 minutes,
     * or when the cellular network country changes, is travel detected.
     */
    fun detectTravel(
        context: Context,
        currentCity: City,
        now: Instant = Clock.System.now(),
    ): TravelDetection {
        val systemZone = runCatching { TimeZone.currentSystemDefault() }.getOrElse { currentCity.timeZone }
        val telephonyManager =
            runCatching {
                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            }.getOrNull()
        val networkCountry =
            runCatching {
                telephonyManager?.networkCountryIso?.takeIf { it.isNotBlank() }?.uppercase()
            }.getOrNull()

        return evaluate(
            systemZone = systemZone,
            networkCountry = networkCountry,
            currentCity = currentCity,
            now = now,
        )
    }

    /**
     * Pure evaluation logic for travel detection, allowing complete unit testing without Android mocks.
     */
    fun evaluate(
        systemZone: TimeZone,
        networkCountry: String?,
        currentCity: City,
        now: Instant = Clock.System.now(),
    ): TravelDetection {
        // Mathematical DST Filter:
        // If the system timezone ID matches the current city's timezone ID,
        // any offset shift is purely seasonal Daylight Saving Time (e.g. London GMT -> BST).
        if (systemZone.id == currentCity.timeZone.id) {
            return TravelDetection(
                isTraveling = false,
                detectedZone = systemZone,
                detectedCountry = networkCountry,
                suggestedCity = null,
            )
        }

        val systemOffset = systemZone.offsetAt(now)
        val cityOffset = currentCity.timeZone.offsetAt(now)
        val offsetDiffSeconds = abs(systemOffset.totalSeconds - cityOffset.totalSeconds)

        val countryChanged =
            networkCountry != null && !networkCountry.equals(currentCity.country.value, ignoreCase = true)
        val isTraveling = offsetDiffSeconds >= MIN_OFFSET_DIFF_SECONDS || countryChanged

        if (!isTraveling) {
            return TravelDetection(
                isTraveling = false,
                detectedZone = systemZone,
                detectedCountry = networkCountry,
                suggestedCity = null,
            )
        }

        val suggestedCity = findSuggestedCity(systemZone, networkCountry, now)

        return TravelDetection(
            isTraveling = true,
            detectedZone = systemZone,
            detectedCountry = networkCountry,
            suggestedCity = suggestedCity,
        )
    }

    private fun findSuggestedCity(
        targetZone: TimeZone,
        countryCode: String?,
        now: Instant,
    ): City? {
        val targetOffset = targetZone.offsetAt(now)

        // 1. Exact zone ID match + matching country (if country known)
        if (countryCode != null) {
            val match =
                EXTENDED_CITIES.firstOrNull {
                    it.timeZone.id == targetZone.id && it.country.value.equals(countryCode, ignoreCase = true)
                }
            if (match != null) return match
        }

        // 2. Exact zone ID match
        val zoneMatch = EXTENDED_CITIES.firstOrNull { it.timeZone.id == targetZone.id }
        if (zoneMatch != null) return zoneMatch

        // 3. Matching country and matching offset
        if (countryCode != null) {
            val countryOffsetMatch =
                EXTENDED_CITIES.firstOrNull {
                    it.country.value.equals(countryCode, ignoreCase = true) && it.timeZone.offsetAt(now) == targetOffset
                }
            if (countryOffsetMatch != null) return countryOffsetMatch
        }

        // 4. Any city in EXTENDED_CITIES with the same offset at now
        return EXTENDED_CITIES.firstOrNull { it.timeZone.offsetAt(now) == targetOffset }
    }
}
