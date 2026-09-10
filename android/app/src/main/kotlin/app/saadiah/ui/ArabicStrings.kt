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
@Suppress("TooManyFunctions", "MagicNumber", "CyclomaticComplexMethod")
object ArabicStrings : Strings {
    override val rendersArabic = true

    override val back = "رجوع"
    override val cancel = "إلغاء"
    override val open = "فتح"
    override val chosen = "مُحدَّد"
    override val change = "تغيير"
    override val comingSoon = "سيتوفر في تحديث قادم"

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
    override val titleQuran = "القرآن الكريم"

    override val todaysReading = "سورة البقرة"
    override val todaysReadingHint = "ورد اليوم. اضغط لمعرفة فضل المداومة عليها."
    override val fastingAhead = "أيام الصيام القادمة"

    override val nextPrayer = "الصلاة القادمة"
    override val whyThisTimeQuestion = "لماذا هذا التوقيت؟"

    override val ante = "ص"
    override val post = "م"

    override val sectionLocation = "الموقع"
    override val sectionLanguage = "اللغة"
    override val sectionCalculation = "طريقة حساب مواقيت الصلاة"
    override val sectionAlerts = "التنبيهات"
    override val sectionMadhab = "مذهب العصر"
    override val sectionMadhabWhy = "يحدد وقت دخول صلاة العصر."
    override val sectionCombining = "الجمع بين الصلوات"
    override val sectionWhichPrayers = "صلوات التنبيه"
    override val sectionWarnBefore = "التنبيه قبل دخول الوقت"
    override val sectionWarnClosing = "التنبيه قبل خروج الوقت"
    override val sectionBaqarah = "سورة البقرة"
    override val sectionBaqarahWhy = "تذكير بالمداومة على الورد اليومي."
    override val sectionChecks = "الفحوصات"
    override val sectionAbout = "عن التطبيق"

    override val languageHint = "اختيار اللغة العربية يعرض واجهة التطبيق من اليمين إلى اليسار."
    override val everyPrayerSilent = "جميع الصلوات صامتة. لن ينبهك التطبيق إطلاقًا."
    override val privacyNote = "تُحفظ كل الإعدادات على هذا الجهاز. لا يُرسل شيء إلى أي جهة."
    override val geoNamesCredit = "بيانات المدن والبلدات من GeoNames‏ (geonames.org) بموجب رخصة CC BY 4.0."
    override val whyItIsRead = "فضل قراءتها"
    override val alerting = "تنبيه"
    override val silent = "صامت"

    override val traditionSunni = "سني"
    override val madhabStandard = "الجمهور — الشافعي والمالكي والحنبلي"
    override val madhabHanafi = "المذهب الحنفي — يدخل العصر متأخرًا"
    override val combineNone = "إظهار الصلوات الخمس"
    override val combineZuhraynIshaayn = "الجمع في الظهرين والعشاءين"
    override val doNotWarnMe = "بدون تنبيه"
    override val doNotRemindMe = "بدون تذكير"
    override val everyDay = "كل يوم"
    override val onceAWeek = "مرة أسبوعيًا"
    override val followMyPhone = "حسب إعدادات الهاتف"
    override val arabicLanguage = "العربية"
    override val englishLanguage = "English — الإنجليزية"

    override fun minutesBefore(minutes: Long) =
        when {
            minutes == 1L -> "قبل دقيقة واحدة"
            minutes == 2L -> "قبل دقيقتين"
            minutes in 3L..10L -> "قبل $minutes دقائق"
            else -> "قبل $minutes دقيقة"
        }

    override fun minutesBeforeClosing(minutes: Long) =
        when {
            minutes == 1L -> "قبل خروج الوقت بدقيقة واحدة"
            minutes == 2L -> "قبل خروج الوقت بدقيقتين"
            minutes in 3L..10L -> "قبل خروج الوقت بـ $minutes دقائق"
            else -> "قبل خروج الوقت بـ $minutes دقيقة"
        }

    override val searchForYourCity = "ابحث عن مدينتك"
    override val loadingCityList = "جارٍ تحميل قائمة المدن…"
    override val typeYourCity = "اكتب اسم مدينتك أو بلدتك."
    override val noCityMatches = "لا توجد مدينة أو بلدة بهذا الاسم."
    override val currentlyCity = "تُحسب مواقيت الصلاة حاليًا لمدينة"

    override val alertsArriveQuestion = "هل ستصلني التنبيهات؟"
    override val notificationsAllowed = "الإشعارات مسموحة"
    override val exactAlarmsAllowed = "التنبيهات الدقيقة مسموحة"
    override val openBackgroundSettings = "فتح إعدادات العمل في الخلفية"
    override val recentAlerts = "التنبيهات الأخيرة"
    override val noAlertYet = "لم يصل أي تنبيه بعد. عند وصول أول تنبيه سيُسجل وقته هنا."
    override val backgroundAdviceGeneric = "إذا تأخر وصول التنبيهات، يُرجى السماح للتطبيق بالعمل في الخلفية."

    override fun backgroundAdviceRestrictive(manufacturer: String) =
        "توقف أجهزة $manufacturer التطبيقات في الخلفية تلقائيًا، مما قد يؤخر تنبيهات الصلاة. " +
            "افتح الإعدادات واسمح للتطبيق بالعمل في الخلفية."

    override val yes = "نعم"
    override val no = "لا"

    override val adhkarMorning = "الصباح"
    override val adhkarEvening = "المساء"
    override val adhkarAfterPrayer = "بعد الصلاة"
    override val adhkarTapRing = "اضغط على الدائرة للعد"
    override val adhkarPrevious = "السابق"
    override val adhkarNext = "التالي"
    override val adhkarNotBundled =
        "لم تُضف أذكار هذا المذهب بعد. ستُضاف عند توفر مجموعة موثقة ذات ترخيص مفتوح. " +
            "ولا تُعرض مجموعة مذهب آخر بديلةً عنها."

    override val baqarahSubtitle = "تُقرأ يوميًا، وتُحفظ مع المداومة والاستمرار."
    override val baqarahNotBundled =
        "وردت فضائل قراءة سورة البقرة في الأحاديث النبوية. وستُضاف هنا عند توفر مجموعة موثقة ذات ترخيص " +
            "حر، كل فضيلة مقرونة بروايتها ومصدرها. ولا يُعرض شيء حاليًا؛ لأن كتابة الفضائل من الذاكرة " +
            "ونسبتها إلى النبي ﷺ قد تلتبس بالنصوص المحققة."

    override val meritNarration = "حديث شريف"
    override val meritReflection = "تأمّل"
    override val translationOfMeaning = "ترجمة المعنى"

    override val nawafil = "النوافل"
    override val noNawafil = "لا توجد نوافل راتبة لهذا الوقت."
    override val duaAndAdhkar = "الدعاء والأذكار"
    override val duaWordingWithheld = "لا يُعرض النص حتى يتم توثيق مجموعة الأذكار من مصادرها المعتمدة."

    override val checkPassing = "سليم"
    override val checkNeedsAttention = "يحتاج إلى مراجعة"

    override val calendarList = "قائمة"
    override val calendarGrid = "شبكة"
    override val calendarLegend = "دلالات الرموز والمناسبات"
    override val previousMonth = "الشهر السابق"
    override val nextMonth = "الشهر التالي"
    override val legendFast = "يوم صيام"
    override val legendDoNotFast = "يوم يُنهى عن صيامه"
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
            hours.takeIf { it > 0 }?.let {
                when {
                    it == 1L -> "ساعة"
                    it == 2L -> "ساعتان"
                    it in 3L..10L -> "$it ساعات"
                    else -> "$it ساعة"
                }
            },
            minutes.takeIf { it > 0 }?.let {
                when {
                    it == 1L -> "دقيقة"
                    it == 2L -> "دقيقتان"
                    it in 3L..10L -> "$it دقائق"
                    else -> "$it دقيقة"
                }
            },
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
    override val baqarahReminderBody = "حان وقت الورد اليومي."

    override val fastingObligatory = "الصيام واجب"
    override val fastingRecommended = "الصيام مستحب"
    override val doNotFast = "يُنهى عن صيامه"
    override val cuppingDay = "يوم مستحب للحجامة"
    override val eid = "عيد"
    override val zuhrayn = "الظهرين"
    override val ishaayn = "العشاءين"
    override val zuhraynArabic = "الظهرين"
    override val ishaaynArabic = "العشاءين"

    override val whyAngle = "زاوية الفجر والعشاء"
    override val whyMadhab = "مذهب العصر"
    override val whyHighLatitude = "خطوط العرض العالية"
    override val whyYourTuning = "التعديلات اليدوية"
    override val tuningNone = "بدون تعديل"
    override val tuningSet = "مُعدَّل"
    override val madhabShortStandard = "الجمهور"
    override val madhabShortHanafi = "الحنفي"
    override val middleOfNight = "منتصف الليل"
    override val seventhOfNight = "سُبع الليل"
    override val twilightAngle = "زاوية الشفق"

    override fun alternativeAsr(
        madhab: String,
        time: String,
        difference: String,
        earlier: Boolean,
    ) = "حساب المذهب $madhab يجعل صلاة العصر في $time (بفارق $difference ${if (earlier) "أبكر" else "متأخرًا"}). " +
        "فإن كان ذلك يوافق توقيت مسجدك، يمكنك تغيير المذهب."

    override val mondayAndThursday = "الإثنين والخميس"
    override val ayyamAlBid = "أيام البيض"
    override val hijamah = "الحجامة"
    override val today = "اليوم"
    override val tomorrow = "غدًا"
    override val shapeFilledCircle = "دائرة ممتلئة"
    override val shapeHorizontalBar = "شريط أفقي"
    override val shapeRingOutline = "إطار دائري"

    override fun remainingIn(duration: String) = "بعد $duration"

    override fun inDays(days: Int) =
        when {
            days == 1 -> "بعد يوم واحد"
            days == 2 -> "بعد يومين"
            days in 3..10 -> "بعد $days أيام"
            else -> "بعد $days يومًا"
        }

    override fun whiteDaysTiming(whenText: String) = "الثالث عشر والرابع عشر والخامس عشر — $whenText"

    override fun hijamaTiming(
        day: Int,
        whenText: String,
    ) = "يوم $day — $whenText، ويبدأ من مغرب اليوم السابق"

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
        "اليوم الثالث عشر هو من الأيام البيض عادةً، لكنه يوافق أحد أيام التشريق المنهي عن صيامها هذا الشهر."
    override val matchMyMasjid = "مطابقة توقيت المسجد"

    override fun fastOn(observance: String) = "صيام — $observance"

    override fun doNotFastOn(observance: String) = "يُنهى عن صيامه — $observance"

    override val observanceLabels =
        mapOf(
            "RAMADAN" to "شهر رمضان — فريضة الصيام",
            "EID_AL_FITR" to "عيد الفطر",
            "EID_AL_ADHA" to "عيد الأضحى",
            "TASHRIQ" to "أيام التشريق — يُنهى عن صيامها",
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
    override val beforeNotConfirmed = "قبلية (سنة غير مؤكدة)"
    override val seated = "جالسًا"

    override fun rakah(count: Int) =
        when (count) {
            1 -> "ركعة واحدة"
            2 -> "ركعتان"
            in 3..10 -> "$count ركعات"
            else -> "$count ركعة"
        }

    override val sectionAdhan = "الأذان"
    override val sectionAdhanWhy = "صوت تنبيه الصلاة. يُشغَّل بصوت المنبه ليُسمع حتى عند كتم صوت الإشعارات."
    override val adhanDefault = "صوت إشعارات هاتفك"
    override val adhanShort = "أذان قصير"
    override val adhanLong = "أذان كامل"

    override val prayerChannelName = "أوقات الصلاة"
    override val prayerChannelWhat = "ينبهك عند دخول وقت كل صلاة."
    override val readingChannelName = "تذكير القراءة"
    override val readingChannelWhat = "تذكير بقراءة الورد اليومي من سورة البقرة."

    override val sectionTheme = "المظهر"
    override val themeSystem = "حسب إعداد الهاتف"
    override val themeLight = "فاتح دافئ"
    override val themeDark = "داكن"
    override val themeGreen = "أبيض وأخضر"
    override val andConjunction = " و"

    override fun pageNumber(page: Int) = "صفحة $page"

    override val loadingText = "جارٍ فتح النص…"
    override val textFailedVerification =
        "لم يتطابق النص القرآني مع بيانات التحقق المعتمدة، ولذا لا يُعرض حفاظًا على سلامة النص. " +
            "يُرجى إعادة تثبيت التطبيق لتصحيح ذلك."
    override val readAlBaqarah = "اقرأ سورة البقرة"
    override val readAlImran = "اقرأ سورة آل عمران"
    override val titleAlBaqarah = "سورة البقرة"
    override val titleAlImran = "سورة آل عمران"
    override val backToStart = "العودة إلى الآية الأولى"

    override fun ayahCount(count: Int) =
        when (count) {
            1 -> "آية واحدة"
            2 -> "آيتان"
            in 3..10 -> "$count آيات"
            else -> "$count آية"
        }

    override fun positionInSet(
        index: Int,
        total: Int,
    ) = "$index من $total"

    override fun outOf(total: Int) = "من $total"

    override fun counterSpoken(
        current: Int,
        target: Int,
    ) = "$current من أصل $target. اضغط للعد."

    override fun alertOnFor(title: String) = "التنبيه مفعّل لـ $title"

    override fun alertOffFor(title: String) = "التنبيه متوقف لـ $title"

    override val myAdhkar = "أذكاري"
    override val addDhikr = "أضف ذكرًا"
    override val newDhikrHint = "اكتب نص الذكر هنا"
    override val save = "حفظ"
    override val remove = "حذف"
    override val noCustomAdhkar = "لم تضف أي ذكر بعد. ما تضيفه هنا يُحفظ على هذا الجهاز فقط."
    override val timesLabel = "عدد المرات"
    override val bundledAdhkar = "أذكار الصباح والمساء"

    override fun whyIsItAt(time: String) = "لماذا $time؟"

    override val jumpToToday = "اليوم"
    override val matchMyMasjidWhy = "أدخل أوقات مسجدك المطبوعة ليجد التطبيق طريقة الحساب الأقرب لها."
    override val applyProfile = "تطبيق هذه الإعدادات"
    override val matchedProfile = "طريقة الحساب الأقرب"
    override val resetToAutomatic = "إعادة التعيين إلى الحساب التلقائي"
    override val matchedMasjidActive = "توقيت المسجد المخصص مفعّل"
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
            "KEMENAG" to "وزارة الشؤون الدينية، إندونيسيا",
            "DUBAI" to "دبي وأوقاف الإمارات",
            "LONDON_UNIFIED" to "توقيت لندن الموحد",
            "UK_REGIONAL" to "المملكة المتحدة الإقليمي (١٨°/١٢°)",
            "MOONSIGHTING_COMMITTEE" to "لجنة رؤية الهلال",
            "SINGAPORE" to "مجلس أوغاما إسلام سنغافورة (MUIS)",
            "JAKIM" to "دائرة التنمية الإسلامية بماليزيا (JAKIM)",
            "PARIS" to "مسجد باريس الكبير",
            "UOIF" to "مسلمو فرنسا (زاوية ١٢°)",
        )
    override val lowConfidence =
        "لا تتطابق هذه الأوقات بدقة مع أي طريقة حساب معروفة. " +
            "يُرجى مراجعة ما أدخلته، أو تطبيق هذا الحساب وضبط الأوقات الأخرى يدويًا."
    override val enterYourMasjidTimes = "عدّل أوقات الصلوات المعروفة لديك، واترك الباقي دون تغيير."
    override val correctWhatYouKnow =
        "عدّل الأوقات التي تعرفها فقط؛ فيكفي تحديد وقت صلاة واحدة ليُعاد حساب بقية الأوقات " +
            "وفق الطريقة الأقرب لها، " +
            "وما تتركه دون تعديل سيظل محسوبًا تلقائيًا."
    override val twentyFourHourNotice =
        "تُدخل الأوقات بنظام ٢٤ ساعة كما في جداول المساجد (مثال: 18:30 وليس 6:30 مساءً)."
    override val nothingCorrectedYet = "لم يتم تعديل أي وقت بعد؛ أدخل وقتًا واحدًا على الأقل للمطابقة."
    override val yourMasjidsTime = "توقيت مسجدك"
    override val leftAsCalculated = "محسوب تلقائيًا"

    override fun prayerTodayIn(
        prayer: String,
        city: String,
    ) = "$prayer اليوم في $city"

    override fun offsetMinutes(minutes: Long): String {
        val abs = if (minutes < 0) -minutes else minutes
        val formatted =
            when {
                abs == 1L -> "دقيقة واحدة"
                abs == 2L -> "دقيقتين"
                abs in 3L..10L -> "$abs دقائق"
                else -> "$abs دقيقة"
            }
        return if (minutes < 0) "أبكر بـ $formatted" else "متأخر بـ $formatted"
    }

    override val onTime = "في وقته"

    override fun lateBy(duration: String) = "متأخر بـ $duration"

    override val duaMorningAdhkar = "أذكار الصباح"
    override val duaEveningAdhkar = "أذكار المساء"
    override val duaAyatAlKursi = "آية الكرسي بعد الصلاة"
    override val duaTasbih = "التسبيح بعد الصلاة"

    override val fastingChannelName = "تذكير الصيام"
    override val fastingChannelWhat = "تنبيهات عشية أيام الصيام المستحب"
    override val fastingReminderTitle = "تذكير بصيام الغد"

    override fun fastingReminderBody(fastName: String) = "غدًا يوم صيام مستحب: $fastName."

    override val adhkarChannelName = "تذكير الأذكار والدعاء"
    override val adhkarChannelWhat = "تنبيهات أذكار بعد الصلاة والأذكار اليومية"
    override val afterPrayerReminderTitle = "أذكار ما بعد الصلاة"

    override fun afterPrayerReminderBody(prayerName: String) = "حان وقت أذكار ما بعد صلاة $prayerName."

    override val sectionFasting = "تذكير الصيام"
    override val sectionAfterPrayer = "تذكير أذكار بعد الصلاة"
    override val sectionHomeDuas = "دعاء اليوم في الرئيسية"
    override val sectionHomeDuasWhy = "عرض أدعية وأذكار مختارة ومناسبة لوقت اليوم في الشاشة الرئيسية."
    override val sectionVoiceContent = "المقاطع الصوتية"
    override val sectionVoiceContentWhy = "تسجيلات صوتية للأذكار والأدعية النبوية."

    override val cadenceAllNafilah = "جميع أيام الصيام المستحب (الإثنين والخميس والأيام البيض)"
    override val cadenceWhiteDays = "الأيام البيض فقط (١٣، ١٤، ١٥)"
    override val cadenceMondayThursday = "الإثنين والخميس فقط"

    override val delayFiveMinutes = "بعد ٥ دقائق من الصلاة"
    override val delayTenMinutes = "بعد ١٠ دقائق من الصلاة"
    override val delayFifteenMinutes = "بعد ١٥ دقيقة من الصلاة"

    override val downloadVoiceContent = "تحميل المقاطع الصوتية (~١٢ م.ب)"
    override val downloadingVoiceContent = "جارٍ تحميل المقاطع الصوتية…"
    override val voiceContentDownloaded = "تم تحميل المقاطع الصوتية"
    override val deleteVoiceContent = "حذف المقاطع الصوتية لتوفير المساحة"
    override val listen = "استماع"
    override val stop = "إيقاف"

    override val quranViewModeTranslation = "عرض الترجمة"
    override val quranViewModeReading = "عرض القراءة"

    override val copyAyah = "نسخ الآية"
    override val ayahCopied = "تم نسخ الآية"
    override val shareAyah = "مشاركة الآية"
    override val playAyah = "تلاوة"
    override val pauseAyah = "إيقاف مؤقت"

    override val homeDuaCardTitle = "دعاء اليوم"
    override val openInAdhkar = "فتح في الأذكار"

    override val surahTheCow = "البقرة"
    override val surahFamilyOfImran = "آل عمران"
    override val surahMedinan = "مدنية"
    override val surahMeccan = "مكية"

    override fun surahAyahCount(count: Int) = ayahCount(count)

    override val tabAudioDownloads = "تحميل الصوتيات"
    override val audioDownloadsSubtitle = "تحميل تلاوات القرآن الكريم للاستماع دون اتصال"
    override val downloadAudio = "تحميل"
    override val downloadingAudio = "جارٍ التحميل…"
    override val audioDownloaded = "تم التحميل"
    override val audioNotDownloaded = "غير مُحمَّل"
    override val cancelDownload = "إلغاء"
    override val audioDeleted = "تم حذف الملف الصوتي"
    override val audioDownloadSuccess = "تم التحميل بنجاح"
    override val audioDownloadFailed = "فشل التحميل"
    override val surahBaqarahAudioTitle = "سورة البقرة"
    override val surahImranAudioTitle = "سورة آل عمران"

    override fun downloadingProgress(
        current: Int,
        total: Int,
    ) = "$current من أصل $total ${if (total in 3..10) "ملفات" else "ملفًا"}"

    override val reciterHusariMujawwad = "محمود خليل الحصري (مجود)"
    override val reciterHusariMurattal = "محمود خليل الحصري (مرتل)"
    override val reciterMinshawiMujawwad = "محمد صديق المنشاوي (مجود)"
    override val reciterMinshawiMurattal = "محمد صديق المنشاوي (مرتل)"
    override val reciterAbdulbasitMujawwad = "عبد الباسط عبد الصمد (مجود)"
    override val reciterAbdulbasitMurattal = "عبد الباسط عبد الصمد (مرتل)"
    override val sectionReciter = "القارئ والتلاوة"
    override val sectionReciterWhy = "اختر القارئ ونوع التلاوة المفضل لديك"
    override val deleteReciterAudio = "حذف التلاوات المُحمَّلة"
    override val storageUsed = "المساحة المستخدمة"
    override val manageDownloads = "إدارة التنزيلات"
    override val audioNotAvailable = "التسجيل الصوتي غير متوفر"

    override val travelChannelName = "تنبيهات السفر والمنطقة الزمنية"
    override val travelChannelWhat = "ينبهك عندما يدخل هاتفك منطقة زمنية جديدة لتبقى أوقات الصلاة دقيقة."
    override val travelNotificationTitle = "تغيرت المنطقة الزمنية"

    override fun travelNotificationText(
        detected: String,
        current: String,
    ) = "الموقع الحالي: $detected، ومواقيت الصلاة مضبوطة على $current. اضغط لتحديث مدينتك."

    override val travelPromptTitle = "هل أنت مسافر؟"

    override fun travelPromptMessage(
        detected: String,
        current: String,
    ) = "يبدو أنك في $detected، بينما مواقيت صلاتك مضبوطة على $current."

    override fun travelSwitchTo(city: String) = "التبديل إلى $city"

    override val travelDismiss = "تجاهل"
}
