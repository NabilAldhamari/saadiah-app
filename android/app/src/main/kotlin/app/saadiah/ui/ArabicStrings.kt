package app.saadiah.ui

/**
 * Written in Modern Standard Arabic. Religious terms keep the form they are read in rather
 * than a transliteration, so ʿĀshūrāʾ is عاشوراء here and not "Ashura".
 *
 * A native reader should still go over this: the wording is serviceable, not idiomatic, and
 * the app's primary audience reads Arabic first.
 *
 * The table is mostly parameterised strings, so splitting it to satisfy a function count
 * would scatter one language across several files for no reader's benefit.
 */
@Suppress("TooManyFunctions")
object ArabicStrings : Strings {
    override val rendersArabic = true

    override val back = "رجوع"
    override val cancel = "إلغاء"
    override val open = "افتح"
    override val chosen = "مختار"
    override val change = "تغيير"
    override val comingSoon = "قادم في تحديث لاحق"

    override val tabToday = "اليوم"
    override val tabCalendar = "التقويم"
    override val tabAdhkar = "الأذكار"
    override val tabMore = "المزيد"

    override val titleChooseCity = "اختر مدينتك"
    override val titleAlertCheck = "هل ستصلني التنبيهات؟"
    override val titleWhyThisTime = "لماذا هذا التوقيت"
    override val titleBaqarah = "سورة البقرة"
    override val titlePrayer = "الصلاة"
    override val titleSettings = "الإعدادات"

    override val todaysReading = "سورة البقرة"
    override val todaysReadingHint = "قراءة اليوم. اضغط لمعرفة سبب المداومة عليها."
    override val fastingAhead = "صيام قادم"

    override val nextPrayer = "الصلاة القادمة"
    override val whyThisTimeQuestion = "لماذا هذا التوقيت؟"

    override val ante = "ص"
    override val post = "م"

    override val sectionLocation = "الموقع"
    override val sectionLanguage = "اللغة"
    override val sectionCalculation = "حساب أوقات الصلاة"
    override val sectionAlerts = "التنبيهات"
    override val sectionMadhab = "مذهب العصر"
    override val sectionMadhabWhy = "يغيّر وقت دخول العصر."
    override val sectionCombining = "الجمع بين الصلوات"
    override val sectionWhichPrayers = "الصلوات التي تنبهك"
    override val sectionWarnBefore = "نبهني قبل كل صلاة"
    override val sectionWarnClosing = "نبهني قبل خروج الوقت"
    override val sectionBaqarah = "سورة البقرة"
    override val sectionBaqarahWhy = "تذكير بالمداومة على القراءة اليومية."
    override val sectionChecks = "الفحوصات"
    override val sectionAbout = "عن التطبيق"

    override val languageHint = "العربية تعرض التطبيق من اليمين إلى اليسار."
    override val everyPrayerSilent = "جميع الصلوات صامتة. لن ينبهك التطبيق إطلاقًا."
    override val privacyNote = "تُحفظ كل الإعدادات على هذا الجهاز. لا يُرسل شيء إلى أي جهة."
    override val geoNamesCredit = "بيانات المدن والبلدات من GeoNames‏ (geonames.org) بموجب رخصة CC BY 4.0."
    override val whyItIsRead = "لماذا تُقرأ"
    override val alerting = "ينبه"
    override val silent = "صامت"

    override val traditionSunni = "سني"
    override val madhabStandard = "المعتاد — الشافعي والمالكي والحنبلي"
    override val madhabHanafi = "الحنفي — يدخل العصر متأخرًا"
    override val combineNone = "إظهار الصلوات الخمس"
    override val combineZuhraynIshaayn = "الجمع في الظهرين والعشاءين"
    override val doNotWarnMe = "لا تنبهني"
    override val doNotRemindMe = "لا تذكرني"
    override val everyDay = "كل يوم"
    override val onceAWeek = "مرة كل أسبوع"
    override val followMyPhone = "حسب إعداد الهاتف"
    override val arabicLanguage = "العربية"
    override val englishLanguage = "English — الإنجليزية"

    override fun minutesBefore(minutes: Long) = "قبل $minutes دقيقة"

    override fun minutesBeforeClosing(minutes: Long) = "قبل خروج الوقت بـ $minutes دقيقة"

    override val searchForYourCity = "ابحث عن مدينتك"
    override val loadingCityList = "جارٍ تحميل قائمة المدن…"
    override val typeYourCity = "اكتب اسم مدينتك أو بلدتك."
    override val noCityMatches = "لا توجد مدينة أو بلدة بهذا الاسم."
    override val currentlyCity = "تُحسب أوقات الصلاة لهذا الموقع. حاليًا"

    override val alertsArriveQuestion = "هل ستصلني التنبيهات؟"
    override val notificationsAllowed = "الإشعارات مسموحة"
    override val exactAlarmsAllowed = "المنبهات الدقيقة مسموحة"
    override val openBackgroundSettings = "افتح إعدادات التشغيل في الخلفية"
    override val recentAlerts = "التنبيهات الأخيرة"
    override val noAlertYet = "لم يصل أي تنبيه بعد. عند وصول أول تنبيه سيُسجل وقته هنا."
    override val backgroundAdviceGeneric = "إذا تأخر أي تنبيه، اسمح للتطبيق بالعمل في الخلفية."

    override fun backgroundAdviceRestrictive(manufacturer: String) =
        "هواتف $manufacturer توقف التطبيقات في الخلفية افتراضيًا، وقد يؤخر ذلك تنبيهات الصلاة. " +
            "افتح شاشة الإعدادات واسمح للتطبيق بالعمل."

    override val yes = "نعم"
    override val no = "لا"

    override val adhkarMorning = "الصباح"
    override val adhkarEvening = "المساء"
    override val adhkarTapRing = "اضغط الدائرة للعد"
    override val adhkarPrevious = "السابق"
    override val adhkarNext = "التالي"
    override val adhkarNotBundled =
        "لم تُضف أذكار هذا المذهب بعد. ستُضاف عند توفر مجموعة موثقة ومرخّصة بشكل مفتوح. " +
            "ولا تُعرض هنا مجموعة مذهب آخر مكانها."

    override val baqarahSubtitle = "تُقرأ يوميًا، وتُحفظ مع الوقت."
    override val baqarahNotBundled =
        "وردت فضائل قراءة سورة البقرة في الحديث. ستُضاف هنا عند توفر مجموعة موثقة ومرخّصة بشكل " +
            "مفتوح، كل فضيلة مع روايتها. ولا يُعرض شيء في هذه الأثناء، لأن فضيلة تُكتب من الذاكرة " +
            "وتُنسب إلى النبي ﷺ ستبدو تمامًا كفضيلة موثقة."

    override val meritNarration = "حديث"
    override val meritReflection = "خاطرة"
    override val translationOfMeaning = "ترجمة المعنى"

    override val nawafil = "النوافل"
    override val noNawafil = "لا نوافل مع هذا الوقت."
    override val duaAndAdhkar = "الدعاء والأذكار"
    override val duaWordingWithheld = "لا يُعرض النص حتى تُضاف مجموعة الأذكار من مصدرها."

    override val checkPassing = "سليم"
    override val checkNeedsAttention = "يحتاج انتباهًا"

    override val calendarList = "قائمة"
    override val calendarGrid = "شبكة"
    override val calendarLegend = "كل يوم مميز، حسب الشكل"
    override val previousMonth = "الشهر السابق"
    override val nextMonth = "الشهر التالي"
    override val legendFast = "يوم صيام"
    override val legendDoNotFast = "يوم لا يُصام"
    override val legendHijamah = "يوم حجامة"

    override val prayerNames = ARABIC_PRAYER_NAMES
    override val prayerNamesArabic = ARABIC_PRAYER_NAMES
    override val hijriMonths = ARABIC_HIJRI_MONTHS
    override val gregorianMonths =
        listOf(
            "يناير",
            "فبراير",
            "مارس",
            "أبريل",
            "مايو",
            "يونيو",
            "يوليو",
            "أغسطس",
            "سبتمبر",
            "أكتوبر",
            "نوفمبر",
            "ديسمبر",
        )
    override val weekdays =
        listOf("الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

    override fun hoursAndMinutes(
        hours: Long,
        minutes: Long,
    ): String =
        listOfNotNull(
            hours.takeIf { it > 0 }?.let { "$it ${if (it == 1L) "ساعة" else "ساعات"}" },
            minutes.takeIf { it > 0 }?.let { "$it دقيقة" },
        ).joinToString(" و")

    override val now = "الآن"

    override val itIsTimeForThisPrayer = "حان وقت هذه الصلاة."

    override fun prayerBeginsIn(
        prayer: String,
        duration: String,
    ) = "يدخل وقت $prayer بعد $duration."

    override fun prayerWindowEndsIn(
        prayer: String,
        duration: String,
    ) = "يخرج وقت $prayer بعد $duration."

    override val baqarahReminderTitle = "سورة البقرة"
    override val baqarahReminderBody = "حان وقت قراءة اليوم."

    override val fastingObligatory = "الصيام واجب"
    override val fastingRecommended = "الصيام مستحب"
    override val doNotFast = "لا يُصام"
    override val cuppingDay = "يوم مستحب للحجامة"
    override val eid = "عيد"
    override val zuhrayn = "الظهرين"
    override val ishaayn = "العشاءين"
    override val zuhraynArabic = "الظهرين"
    override val ishaaynArabic = "العشاءين"

    override val whyAngle = "زاوية الفجر والعشاء"
    override val whyMadhab = "مذهب العصر"
    override val whyHighLatitude = "خطوط العرض العالية"
    override val whyYourTuning = "تعديلاتك"
    override val tuningNone = "لا شيء"
    override val tuningSet = "مضبوطة"
    override val madhabShortStandard = "المعتاد"
    override val madhabShortHanafi = "الحنفي"
    override val middleOfNight = "منتصف الليل"
    override val seventhOfNight = "سُبع الليل"
    override val twilightAngle = "زاوية الشفق"

    override fun alternativeAsr(
        madhab: String,
        time: String,
        difference: String,
        earlier: Boolean,
    ) = "حساب $madhab يجعل العصر في $time — $difference ${if (earlier) "أبكر" else "أكثر تأخرًا"}. " +
        "فإن وافق ذلك مسجدك، فغيّر المذهب."

    override val mondayAndThursday = "الإثنين والخميس"
    override val ayyamAlBid = "أيام البيض"
    override val hijamah = "الحجامة"
    override val today = "اليوم"
    override val tomorrow = "غدًا"
    override val shapeFilledCircle = "دائرة ممتلئة"
    override val shapeHorizontalBar = "شريط أفقي"
    override val shapeRingOutline = "حلقة مفرغة"

    override fun remainingIn(duration: String) = "بعد $duration"

    override fun inDays(days: Int) = "بعد $days أيام"

    override fun whiteDaysTiming(whenText: String) = "الثالث عشر والرابع عشر والخامس عشر — $whenText"

    override fun hijamaTiming(
        day: Int,
        whenText: String,
    ) = "اليوم $day — $whenText، من مغرب الليلة السابقة"

    override fun weeklyTiming(
        weekday: String,
        whenText: String,
    ) = "$weekday — $whenText"

    override val obsRamadan = "رمضان"
    override val obsEidFitr = "عيد الفطر"
    override val obsEidAdha = "عيد الأضحى"
    override val obsTashriq = "التشريق"
    override val obsArafah = "عرفة"
    override val obsTasua = "تاسوعاء"
    override val obsAshura = "عاشوراء"
    override val obsWhiteDays = "أيام البيض"
    override val obsSixOfShawwal = "ست من شوال"
    override val obsHijamahDay = "يوم الحجامة"
    override val tashriqConflictNote =
        "الثالث عشر من أيام البيض عادةً، لكنه يقع في التشريق هذا الشهر."
    override val matchMyMasjid = "طابق مسجدي"

    override fun fastOn(observance: String) = "صيام — $observance"

    override fun doNotFastOn(observance: String) = "لا يُصام — $observance"

    override val observanceLabels =
        mapOf(
            "RAMADAN" to "رمضان — صيام",
            "EID_AL_FITR" to "عيد الفطر",
            "EID_AL_ADHA" to "عيد الأضحى",
            "TASHRIQ" to "أيام التشريق — لا يُصام",
            "ARAFAH" to "يوم عرفة — يُستحب صيامه",
            "TASUA" to "تاسوعاء — يُستحب صيامه",
            "ASHURA" to "عاشوراء — يُستحب صيامه",
            "AYYAM_AL_BID" to "أيام البيض — يُستحب صيامها",
            "SIX_OF_SHAWWAL" to "ست من شوال — يُستحب صيامها",
            "HIJAMA" to "يوم الحجامة",
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

    override fun sunnahOf(prayer: String) = "سنة $prayer"

    override fun nafilahOf(prayer: String) = "نافلة $prayer"

    override val naflBeforeAsr = "نافلة قبل العصر"
    override val wutayrah = "الوتيرة"
    override val before = "قبلية"
    override val after = "بعدية"
    override val beforeNotConfirmed = "قبلية، غير مؤكدة"
    override val seated = "جالسًا"

    override fun rakah(count: Int) = "$count ركعات"

    override val sectionAdhan = "الأذان"
    override val sectionAdhanWhy = "صوت تنبيه الصلاة. يُشغَّل بمستوى المنبه ليُسمع حتى لو أُسكتت الإشعارات."
    override val adhanDefault = "صوت إشعارات هاتفك"
    override val adhanShort = "أذان قصير"
    override val adhanLong = "أذان كامل"

    override val prayerChannelName = "أوقات الصلاة"
    override val prayerChannelWhat = "يعلن كل صلاة عند دخول وقتها."
    override val readingChannelName = "تذكير القراءة"
    override val readingChannelWhat = "يذكّرك بقراءة سورة البقرة."

    override val sectionTheme = "المظهر"
    override val themeSystem = "حسب إعداد الهاتف"
    override val themeLight = "فاتح دافئ"
    override val themeDark = "داكن دافئ"
    override val themeGreen = "أبيض وأخضر"

    override val loadingText = "جارٍ فتح النص…"
    override val textFailedVerification =
        "هذه النسخة من النص لا تطابق البصمات التي نُشرت معها، فلا تُعرض. إعادة تثبيت التطبيق تستبدلها."
    override val readAlBaqarah = "اقرأ سورة البقرة"
    override val readAlImran = "اقرأ سورة آل عمران"
    override val titleAlBaqarah = "سورة البقرة"
    override val titleAlImran = "سورة آل عمران"
    override val backToStart = "العودة إلى الآية الأولى"

    override fun ayahCount(count: Int) = "$count آية"

    override fun positionInSet(
        index: Int,
        total: Int,
    ) = "$index من $total"

    override fun outOf(total: Int) = "من $total"

    override fun counterSpoken(
        current: Int,
        target: Int,
    ) = "$current من $target. اضغط للعد."

    override fun alertOnFor(title: String) = "التنبيه مفعّل لـ $title"

    override fun alertOffFor(title: String) = "التنبيه متوقف لـ $title"

    override val myAdhkar = "أذكاري"
    override val addDhikr = "أضف ذكرًا"
    override val newDhikrHint = "ما تريد تكراره"
    override val save = "حفظ"
    override val remove = "حذف"
    override val noCustomAdhkar = "لم تُضف شيئًا بعد. ما تضيفه هنا يُحفظ على هذا الجهاز."
    override val timesLabel = "عدد المرات"
    override val bundledAdhkar = "أذكار الصباح والمساء"

    override fun whyIsItAt(time: String) = "لماذا $time؟"

    override val jumpToToday = "اليوم"
    override val matchMyMasjidWhy = "أدخل أوقات مسجدك المطبوعة ليجد التطبيق الإعدادات الموافقة لها."
    override val applyProfile = "تطبيق هذه الإعدادات"
    override val matchedProfile = "أقرب تطابق"
    override val methodNames =
        mapOf(
            "MUSLIM_WORLD_LEAGUE" to "رابطة العالم الإسلامي",
            "NORTH_AMERICA" to "أمريكا الشمالية (ISNA)",
            "EGYPTIAN" to "الهيئة المصرية العامة للمساحة",
            "KARACHI" to "جامعة كراتشي",
            "UMM_AL_QURA" to "أم القرى، مكة المكرمة",
            "DIYANET" to "الديانة التركية",
            "TEHRAN" to "معهد الجيوفيزياء، طهران",
            "JAFARI" to "الجعفري",
        )
    override val lowConfidence =
        "هذه الأوقات لا توافق حسابًا واحدًا بوضوح. راجع ما أدخلته، أو طبّق هذا واضبط صلاة يدويًا."
    override val enterYourMasjidTimes = "استبدل أي وقت لديك من مسجدك، واترك البقية كما هي."
    override val correctWhatYouKnow =
        "صحّح الأوقات التي تعرفها فقط. وقت واحد يكفي — تُعاد بقية الأوقات وفق الحساب الموافق له، " +
            "وما تتركه دون تغيير لا يُعدّ وقت مسجدك."
    override val twentyFourHourNotice = "الأوقات بنظام ٢٤ ساعة كما تُطبع في المساجد — ‎18:30 لا ‎6:30 مساءً."
    override val nothingCorrectedYet = "لم تصحّح أي وقت بعد، فلا شيء لمطابقته."
    override val yourMasjidsTime = "وقت مسجدك"
    override val leftAsCalculated = "متروك كما هو محسوب"

    override fun prayerTodayIn(
        prayer: String,
        city: String,
    ) = "$prayer اليوم في $city"

    override fun offsetMinutes(minutes: Long) = if (minutes < 0) "${-minutes} دقيقة قبل" else "$minutes دقيقة بعد"

    override val onTime = "في وقته"

    override fun lateBy(duration: String) = "متأخر بـ $duration"

    override val duaMorningAdhkar = "أذكار الصباح"
    override val duaEveningAdhkar = "أذكار المساء"
    override val duaAyatAlKursi = "آية الكرسي بعد الصلاة"
    override val duaTasbih = "التسبيح بعد الصلاة"
}
