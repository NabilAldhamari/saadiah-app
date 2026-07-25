package app.saadiah.ui

// A translation table is mostly parameterised strings; splitting it by arity would
// scatter one language across several files for no reader's benefit.
@Suppress("TooManyFunctions")
object EnglishStrings : Strings {
    override val back = "Back"
    override val open = "open"
    override val chosen = "chosen"
    override val change = "change"
    override val comingSoon = "Coming in a future update"

    override val tabToday = "Today"
    override val tabCalendar = "Calendar"
    override val tabAdhkar = "Adhkār"
    override val tabMore = "More"

    override val titleChooseCity = "Choose your city"
    override val titleAlertCheck = "Will my alerts arrive?"
    override val titleAsk = "Ask"
    override val titleWhyThisTime = "Why this time"
    override val titleBaqarah = "Sūrat al-Baqarah"
    override val titlePrayer = "Prayer"
    override val titleSettings = "Settings"

    override val todaysReading = "سورة البقرة — Sūrat al-Baqarah"
    override val todaysReadingHint = "Today's reading. Tap to read why it is kept up."
    override val fastingAhead = "Fasting ahead"

    override val sectionLocation = "Location"
    override val sectionLanguage = "Language"
    override val sectionMadhab = "ʿAṣr madhhab"
    override val sectionMadhabWhy = "Changes when ʿAṣr begins."
    override val sectionCombining = "Combining prayers"
    override val sectionWhichPrayers = "Which prayers alert you"
    override val sectionWarnBefore = "Warn me before each prayer"
    override val sectionWarnClosing = "Warn me before each window closes"
    override val sectionBaqarah = "Sūrat al-Baqarah"
    override val sectionBaqarahWhy = "A reminder to keep up the daily reading."
    override val sectionChecks = "Checks"
    override val sectionAbout = "About"

    override val languageHint = "Arabic lays the app out right to left."
    override val everyPrayerSilent = "Every prayer is silent. Saadiah will not alert you at all."
    override val privacyNote = "Every setting is kept on this device. Nothing is sent anywhere."
    override val geoNamesCredit = "City and town data from GeoNames (geonames.org), used under CC BY 4.0."
    override val whyItIsRead = "Why it is read"
    override val alerting = "alerting"
    override val silent = "silent"

    override val traditionSunni = "Sunni"
    override val madhabStandard = "Standard — Shāfiʿī, Mālikī, Ḥanbalī"
    override val madhabHanafi = "Ḥanafī — ʿAṣr begins later"
    override val combineNone = "Show all five prayers"
    override val combineZuhraynIshaayn = "Combine into Ẓuhrayn and ʿIshāʾayn"
    override val doNotWarnMe = "Do not warn me"
    override val doNotRemindMe = "Do not remind me"
    override val everyDay = "Every day"
    override val onceAWeek = "Once a week"
    override val followMyPhone = "Follow my phone"
    override val arabicLanguage = "العربية — Arabic"
    override val englishLanguage = "English"

    override fun minutesBefore(minutes: Long) = "$minutes minutes before"

    override fun minutesBeforeClosing(minutes: Long) = "$minutes minutes before it closes"

    override val searchForYourCity = "Search for your city"
    override val loadingCityList = "Loading the city list…"
    override val typeYourCity = "Type the name of your city or town."
    override val noCityMatches = "No city or town matches that name."
    override val currentlyCity = "Prayer times are calculated for this location. Currently"

    override val alertsArriveQuestion = "Will my alerts arrive?"
    override val notificationsAllowed = "Notifications allowed"
    override val exactAlarmsAllowed = "Exact alarms allowed"
    override val openBackgroundSettings = "Open background settings"
    override val recentAlerts = "Recent alerts"
    override val noAlertYet = "No alert has arrived yet. Once one does, its timing is recorded here."
    override val backgroundAdviceGeneric = "If an alert ever arrives late, allow Saadiah to run in the background."

    override fun backgroundAdviceRestrictive(manufacturer: String) =
        "$manufacturer phones stop background apps by default, which can hold prayer alerts back. " +
            "Open the settings screen and allow Saadiah to run."

    override val yes = "yes"
    override val no = "no"

    override val askAQuestion = "Ask a question"
    override val askNotConnected = "Answering is not connected yet. Nothing you type leaves this device."
    override val askDisclaimerTitle = "An answer here may be wrong."

    override fun askDisclaimerBody(tradition: String) =
        "Answers are generated, not verified, and they can be confidently mistaken. Your tradition " +
            "($tradition) changes the answer. Nothing here is a fatwa. For anything that matters, " +
            "ask someone qualified."

    override val adhkarMorning = "Morning"
    override val adhkarEvening = "Evening"
    override val adhkarTapRing = "Tap the ring to count"
    override val adhkarPrevious = "Previous"
    override val adhkarNext = "Next"
    override val adhkarNotBundled =
        "Adhkār for this tradition are not bundled yet. They will be added once a verified, openly " +
            "licensed collection is available. Another tradition's compilation is not shown here in its place."

    override val baqarahSubtitle = "Read daily, and memorised over time."
    override val baqarahNotBundled =
        "The merits of reciting al-Baqarah are reported in hadith. They will be added here once a " +
            "verified, openly licensed collection is bundled, each with the narration it comes from. " +
            "Nothing is shown in the meantime, because a merit written from memory and attributed to " +
            "the Prophet ﷺ would read exactly like a sourced one."

    override val nawafil = "Nawāfil"
    override val noNawafil = "No nawāfil accompany this time."
    override val duaAndAdhkar = "Duʿāʾ and adhkār"
    override val duaWordingWithheld = "Wording is not shown until the adhkār corpus is bundled from its source."

    override val calendarList = "List"
    override val calendarGrid = "Grid"
    override val calendarLegend = "Every marked day, by shape"
    override val legendFast = "A day to fast"
    override val legendDoNotFast = "A day not to fast"
    override val legendHijamah = "A day for ḥijāmah"

    override val prayerNames = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    override val prayerNamesArabic = ARABIC_PRAYER_NAMES
    override val hijriMonths = ARABIC_HIJRI_MONTHS
    override val gregorianMonths =
        listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December",
        )
    override val weekdays =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun hoursAndMinutes(
        hours: Long,
        minutes: Long,
    ): String =
        listOfNotNull(
            hours.takeIf { it > 0 }?.let { "$it ${if (it == 1L) "hour" else "hours"}" },
            minutes.takeIf { it > 0 }?.let { "$it ${if (it == 1L) "minute" else "minutes"}" },
        ).joinToString(" ")

    override val now = "now"

    override val itIsTimeForThisPrayer = "It is time for this prayer."

    override fun prayerBeginsIn(
        prayer: String,
        duration: String,
    ) = "$prayer begins in $duration."

    override fun prayerWindowEndsIn(
        prayer: String,
        duration: String,
    ) = "The time for $prayer ends in $duration."

    override val baqarahReminderTitle = "سورة البقرة — Sūrat al-Baqarah"
    override val baqarahReminderBody = "Time for today's reading."

    override val fastingObligatory = "Fasting is obligatory"
    override val fastingRecommended = "Fasting is recommended"
    override val doNotFast = "Do not fast"
    override val cuppingDay = "A recommended day for cupping"
    override val eid = "Eid"
    override val zuhrayn = "Ẓuhrayn"
    override val ishaayn = "ʿIshāʾayn"
    override val zuhraynArabic = "الظهرين"
    override val ishaaynArabic = "العشاءين"

    override val whyAngle = "Fajr / ʿIshāʾ angle"
    override val whyMadhab = "ʿAṣr madhhab"
    override val whyHighLatitude = "High latitude"
    override val whyYourTuning = "Your tuning"
    override val tuningNone = "none"
    override val tuningSet = "set"
    override val madhabShortStandard = "Standard"
    override val madhabShortHanafi = "Ḥanafī"
    override val middleOfNight = "Middle of the night"
    override val seventhOfNight = "One seventh of the night"
    override val twilightAngle = "Twilight angle"

    override fun alternativeAsr(
        madhab: String,
        time: String,
        difference: String,
        earlier: Boolean,
    ) = "The $madhab calculation would put ʿAṣr at $time — $difference ${if (earlier) "earlier" else "later"}. " +
        "If that matches your masjid, switch the madhhab."

    override val mondayAndThursday = "Monday and Thursday"
    override val ayyamAlBid = "Ayyām al-Bīḍ"
    override val hijamah = "Ḥijāmah"
    override val today = "today"
    override val tomorrow = "tomorrow"
    override val shapeFilledCircle = "filled circle"
    override val shapeHorizontalBar = "horizontal bar"
    override val shapeRingOutline = "ring outline"

    override fun remainingIn(duration: String) = "in $duration"

    override fun inDays(days: Int) = "in $days days"

    override fun whiteDaysTiming(whenText: String) = "the 13th, 14th and 15th — $whenText"

    override fun hijamaTiming(
        day: Int,
        whenText: String,
    ) = "the ${day}th — $whenText, from Maghrib the evening before"

    override fun weeklyTiming(
        weekday: String,
        whenText: String,
    ) = "$weekday — $whenText"

    override val obsRamadan = "Ramaḍān"
    override val obsEidFitr = "Eid al-Fiṭr"
    override val obsEidAdha = "Eid al-Aḍḥā"
    override val obsTashriq = "Tashrīq"
    override val obsArafah = "ʿArafah"
    override val obsTasua = "Tāsūʿāʾ"
    override val obsAshura = "ʿĀshūrāʾ"
    override val obsWhiteDays = "Ayyām al-Bīḍ"
    override val obsSixOfShawwal = "the six of Shawwāl"
    override val obsHijamahDay = "Ḥijāmah day"
    override val tashriqConflictNote =
        "The 13th is normally Ayyām al-Bīḍ, but it falls in Tashrīq this month."
    override val matchMyMasjid = "Match my masjid"

    override fun fastOn(observance: String) = "Fast — $observance"

    override fun doNotFastOn(observance: String) = "Do not fast — $observance"

    override fun askDisclaimerFor(school: String) =
        "Answers are generated, not verified, and they can be confidently mistaken. They follow " +
            "the $school school you have selected, so changing that setting changes the answer. " +
            "Nothing here is a fatwa. For anything that matters, ask someone qualified."

    override val observanceLabels =
        mapOf(
            "RAMADAN" to "Ramaḍān — fasting",
            "EID_AL_FITR" to "Eid al-Fiṭr",
            "EID_AL_ADHA" to "Eid al-Aḍḥā",
            "TASHRIQ" to "Days of Tashrīq — do not fast",
            "ARAFAH" to "Day of ʿArafah — fasting recommended",
            "TASUA" to "Tāsūʿāʾ — fasting recommended",
            "ASHURA" to "ʿĀshūrāʾ — fasting recommended",
            "AYYAM_AL_BID" to "White days — fasting recommended",
            "SIX_OF_SHAWWAL" to "Six of Shawwāl — fasting recommended",
            "HIJAMA" to "Cupping day",
        )
    override val observanceShortLabels =
        mapOf(
            "RAMADAN" to obsRamadan,
            "EID_AL_FITR" to obsEidFitr,
            "EID_AL_ADHA" to obsEidAdha,
            "TASHRIQ" to obsTashriq,
            "ARAFAH" to obsArafah,
            "TASUA" to obsTasua,
            "ASHURA" to obsAshura,
            "AYYAM_AL_BID" to obsWhiteDays,
            "SIX_OF_SHAWWAL" to obsSixOfShawwal,
            "HIJAMA" to hijamah,
        )

    override fun sunnahOf(prayer: String) = "Sunnah of $prayer"

    override fun nafilahOf(prayer: String) = "Nāfilah of $prayer"

    override val naflBeforeAsr = "Nafl before ʿAṣr"
    override val wutayrah = "Wutayrah"
    override val before = "before"
    override val after = "after"
    override val beforeNotConfirmed = "before, not confirmed"
    override val seated = "seated"

    override fun rakah(count: Int) = "$count rakʿah"

    override val readToday = "Mark today's reading"
    override val readTodayDone = "Read today"
    override val quoteBy = "—"

    override fun daysKeptUp(count: Int) = if (count == 1) "1 day kept up" else "$count days kept up"

    override val prayerChannelName = "Prayer times"
    override val prayerChannelWhat = "Announces each prayer as its time enters."
    override val readingChannelName = "Reading reminders"
    override val readingChannelWhat = "Reminds you to read Sūrat al-Baqarah."

    override val sectionTheme = "Theme"
    override val themeSystem = "Follow my phone"
    override val themeLight = "Warm light"
    override val themeDark = "Warm dark"
    override val themeGreen = "White and green"

    override val loadingText = "Opening the text…"
    override val textFailedVerification =
        "This copy of the text does not match the checksums it was published with, so it is not " +
            "shown. Reinstalling the app will replace it."
    override val readAlBaqarah = "Read Sūrat al-Baqarah"
    override val readAlImran = "Read Sūrat Āl ʿImrān"
    override val titleAlBaqarah = "سورة البقرة"
    override val titleAlImran = "سورة آل عمران"

    override val myAdhkar = "My adhkār"
    override val addDhikr = "Add a dhikr"
    override val newDhikrHint = "What you want to repeat"
    override val save = "Save"
    override val remove = "Remove"
    override val noCustomAdhkar = "Nothing added yet. What you add here is kept on this device."
    override val timesLabel = "Times"
}
