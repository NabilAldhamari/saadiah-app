package app.saadiah.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import app.saadiah.model.Language
import java.util.Locale

/**
 * Every word the app shows. This is an interface rather than a map so that adding a string
 * without translating it fails to compile: a missing entry cannot fall back silently to
 * English and go unnoticed.
 *
 * These are Compose values rather than Android string resources on purpose. Switching a
 * resource locale means recreating the activity, which is invisible to the JVM screenshot
 * tests DESIGN.md asks for in both directions, and it cannot be exercised from a test at
 * font scale 2.0. A composition local changes the whole tree in place instead.
 */
@Suppress("TooManyFunctions")
interface Strings {
    val back: String
    val open: String
    val chosen: String
    val change: String
    val comingSoon: String

    val tabToday: String
    val tabCalendar: String
    val tabAdhkar: String
    val tabMore: String

    val titleChooseCity: String
    val titleAlertCheck: String
    val titleWhyThisTime: String
    val titleBaqarah: String
    val titlePrayer: String
    val titleSettings: String

    val todaysReading: String
    val todaysReadingHint: String
    val fastingAhead: String

    val sectionLocation: String
    val sectionLanguage: String
    val sectionMadhab: String
    val sectionMadhabWhy: String
    val sectionCombining: String
    val sectionWhichPrayers: String
    val sectionWarnBefore: String
    val sectionWarnClosing: String
    val sectionBaqarah: String
    val sectionBaqarahWhy: String
    val sectionChecks: String
    val sectionAbout: String

    val languageHint: String
    val everyPrayerSilent: String
    val privacyNote: String
    val geoNamesCredit: String
    val whyItIsRead: String
    val alerting: String
    val silent: String

    val traditionSunni: String
    val madhabStandard: String
    val madhabHanafi: String
    val combineNone: String
    val combineZuhraynIshaayn: String
    val doNotWarnMe: String
    val doNotRemindMe: String
    val everyDay: String
    val onceAWeek: String
    val followMyPhone: String
    val arabicLanguage: String
    val englishLanguage: String

    fun minutesBefore(minutes: Long): String

    fun minutesBeforeClosing(minutes: Long): String

    val searchForYourCity: String
    val loadingCityList: String
    val typeYourCity: String
    val noCityMatches: String
    val currentlyCity: String

    val alertsArriveQuestion: String
    val notificationsAllowed: String
    val exactAlarmsAllowed: String
    val openBackgroundSettings: String
    val recentAlerts: String
    val noAlertYet: String
    val backgroundAdviceGeneric: String

    fun backgroundAdviceRestrictive(manufacturer: String): String

    val yes: String
    val no: String

    val adhkarMorning: String
    val adhkarEvening: String
    val adhkarTapRing: String
    val adhkarPrevious: String
    val adhkarNext: String
    val adhkarNotBundled: String

    val baqarahSubtitle: String
    val baqarahNotBundled: String

    val nawafil: String
    val noNawafil: String
    val duaAndAdhkar: String
    val duaWordingWithheld: String

    val calendarList: String
    val calendarGrid: String
    val calendarLegend: String
    val legendFast: String
    val legendDoNotFast: String
    val legendHijamah: String

    val prayerNames: List<String>
    val prayerNamesArabic: List<String>
    val hijriMonths: List<String>
    val gregorianMonths: List<String>
    val weekdays: List<String>

    fun hoursAndMinutes(
        hours: Long,
        minutes: Long,
    ): String

    val now: String

    val itIsTimeForThisPrayer: String

    fun prayerBeginsIn(
        prayer: String,
        duration: String,
    ): String

    fun prayerWindowEndsIn(
        prayer: String,
        duration: String,
    ): String

    val baqarahReminderTitle: String
    val baqarahReminderBody: String

    val fastingObligatory: String
    val fastingRecommended: String
    val doNotFast: String
    val cuppingDay: String
    val eid: String
    val zuhrayn: String
    val ishaayn: String
    val zuhraynArabic: String
    val ishaaynArabic: String

    val whyAngle: String
    val whyMadhab: String
    val whyHighLatitude: String
    val whyYourTuning: String
    val tuningNone: String
    val tuningSet: String
    val madhabShortStandard: String
    val madhabShortHanafi: String
    val middleOfNight: String
    val seventhOfNight: String
    val twilightAngle: String

    fun alternativeAsr(
        madhab: String,
        time: String,
        difference: String,
        earlier: Boolean,
    ): String

    val mondayAndThursday: String
    val ayyamAlBid: String
    val hijamah: String
    val today: String
    val tomorrow: String
    val shapeFilledCircle: String
    val shapeHorizontalBar: String
    val shapeRingOutline: String

    fun remainingIn(duration: String): String

    fun inDays(days: Int): String

    fun whiteDaysTiming(whenText: String): String

    fun hijamaTiming(
        day: Int,
        whenText: String,
    ): String

    fun weeklyTiming(
        weekday: String,
        whenText: String,
    ): String

    val obsRamadan: String
    val obsEidFitr: String
    val obsEidAdha: String
    val obsTashriq: String
    val obsArafah: String
    val obsTasua: String
    val obsAshura: String
    val obsWhiteDays: String
    val obsSixOfShawwal: String
    val obsHijamahDay: String
    val tashriqConflictNote: String
    val matchMyMasjid: String

    fun fastOn(observance: String): String

    fun doNotFastOn(observance: String): String

    val observanceLabels: Map<String, String>

    val observanceShortLabels: Map<String, String>

    fun sunnahOf(prayer: String): String

    fun nafilahOf(prayer: String): String

    val naflBeforeAsr: String
    val wutayrah: String
    val before: String
    val after: String
    val beforeNotConfirmed: String
    val seated: String

    fun rakah(count: Int): String

    val prayerChannelName: String
    val prayerChannelWhat: String
    val readingChannelName: String
    val readingChannelWhat: String

    val sectionTheme: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val themeGreen: String

    val loadingText: String
    val textFailedVerification: String
    val readAlBaqarah: String
    val readAlImran: String
    val titleAlBaqarah: String
    val titleAlImran: String

    val myAdhkar: String
    val addDhikr: String
    val newDhikrHint: String
    val save: String
    val remove: String
    val noCustomAdhkar: String
    val timesLabel: String
    val bundledAdhkar: String

    fun whyIsItAt(time: String): String

    fun prayerTodayIn(
        prayer: String,
        city: String,
    ): String

    val onTime: String

    fun lateBy(duration: String): String

    val duaMorningAdhkar: String
    val duaEveningAdhkar: String
    val duaAyatAlKursi: String
    val duaTasbih: String
}

val LocalStrings: ProvidableCompositionLocal<Strings> = staticCompositionLocalOf { EnglishStrings }

/** Arabic is the app's primary locale, so [Language.SYSTEM] resolves to it when the phone is Arabic. */
fun stringsFor(language: Language): Strings =
    when (language) {
        Language.ARABIC -> ArabicStrings
        Language.ENGLISH -> EnglishStrings
        Language.SYSTEM -> if (Locale.getDefault().language == "ar") ArabicStrings else EnglishStrings
    }

fun layoutDirectionFor(language: Language): LayoutDirection =
    if (stringsFor(language) === ArabicStrings) LayoutDirection.Rtl else LayoutDirection.Ltr

val strings: Strings
    @Composable
    get() = LocalStrings.current
