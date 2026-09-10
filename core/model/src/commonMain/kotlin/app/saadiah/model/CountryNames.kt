package app.saadiah.model

data class CountryEntry(
    val code: CountryCode,
    val englishName: String,
    val arabicNames: List<String>,
    val aliases: List<String> = emptyList(),
)

object CountryNames {
    private const val ISO_CODE_LENGTH = 2
    private const val MIN_ARABIC_STRIP_LENGTH = 3
    private const val TASHKEEL_START = 0x064B
    private const val TASHKEEL_END = 0x0652
    private const val TATWEEL = 0x0640

    val ALL: List<CountryEntry> =
        listOf(
            CountryEntry(
                code = CountryCode("SA"),
                englishName = "Saudi Arabia",
                arabicNames = listOf("السعودية", "المملكة العربية السعودية"),
                aliases = listOf("KSA"),
            ),
            CountryEntry(
                code = CountryCode("YE"),
                englishName = "Yemen",
                arabicNames = listOf("اليمن", "الجمهورية اليمنية"),
            ),
            CountryEntry(
                code = CountryCode("AE"),
                englishName = "United Arab Emirates",
                arabicNames = listOf("الإمارات", "الامارات", "دولة الإمارات العربية المتحدة"),
                aliases = listOf("UAE"),
            ),
            CountryEntry(
                code = CountryCode("QA"),
                englishName = "Qatar",
                arabicNames = listOf("قطر", "دولة قطر"),
            ),
            CountryEntry(
                code = CountryCode("KW"),
                englishName = "Kuwait",
                arabicNames = listOf("الكويت", "دولة الكويت"),
            ),
            CountryEntry(
                code = CountryCode("BH"),
                englishName = "Bahrain",
                arabicNames = listOf("البحرين", "مملكة البحرين"),
            ),
            CountryEntry(
                code = CountryCode("OM"),
                englishName = "Oman",
                arabicNames = listOf("عمان", "سلطنة عمان"),
            ),
            CountryEntry(
                code = CountryCode("EG"),
                englishName = "Egypt",
                arabicNames = listOf("مصر", "جمهورية مصر العربية"),
            ),
            CountryEntry(
                code = CountryCode("JO"),
                englishName = "Jordan",
                arabicNames = listOf("الأردن", "المملكة الأردنية الهاشمية"),
            ),
            CountryEntry(
                code = CountryCode("PS"),
                englishName = "Palestine",
                arabicNames = listOf("فلسطين", "دولة فلسطين"),
            ),
            CountryEntry(
                code = CountryCode("IQ"),
                englishName = "Iraq",
                arabicNames = listOf("العراق", "جمهورية العراق"),
            ),
            CountryEntry(
                code = CountryCode("SY"),
                englishName = "Syria",
                arabicNames = listOf("سوريا", "سورية", "الجمهورية العربية السورية"),
            ),
            CountryEntry(
                code = CountryCode("LB"),
                englishName = "Lebanon",
                arabicNames = listOf("لبنان", "الجمهورية اللبنانية"),
            ),
            CountryEntry(
                code = CountryCode("TR"),
                englishName = "Turkey",
                arabicNames = listOf("تركيا", "الجمهورية التركية"),
                aliases = listOf("Turkiye"),
            ),
            CountryEntry(
                code = CountryCode("IR"),
                englishName = "Iran",
                arabicNames = listOf("إيران", "الجمهورية الإسلامية الإيرانية"),
            ),
            CountryEntry(
                code = CountryCode("PK"),
                englishName = "Pakistan",
                arabicNames = listOf("باكستان", "جمهورية باكستان الإسلامية"),
            ),
            CountryEntry(
                code = CountryCode("AF"),
                englishName = "Afghanistan",
                arabicNames = listOf("أفغانستان"),
            ),
            CountryEntry(
                code = CountryCode("BD"),
                englishName = "Bangladesh",
                arabicNames = listOf("بنغلاديش", "بنغلادش"),
            ),
            CountryEntry(
                code = CountryCode("IN"),
                englishName = "India",
                arabicNames = listOf("الهند"),
            ),
            CountryEntry(
                code = CountryCode("ID"),
                englishName = "Indonesia",
                arabicNames = listOf("إندونيسيا"),
            ),
            CountryEntry(
                code = CountryCode("MY"),
                englishName = "Malaysia",
                arabicNames = listOf("ماليزيا"),
            ),
            CountryEntry(
                code = CountryCode("SG"),
                englishName = "Singapore",
                arabicNames = listOf("سنغافورة"),
            ),
            CountryEntry(
                code = CountryCode("MA"),
                englishName = "Morocco",
                arabicNames = listOf("المغرب", "المملكة المغربية"),
            ),
            CountryEntry(
                code = CountryCode("DZ"),
                englishName = "Algeria",
                arabicNames = listOf("الجزائر", "الجمهورية الجزائرية"),
            ),
            CountryEntry(
                code = CountryCode("TN"),
                englishName = "Tunisia",
                arabicNames = listOf("تونس", "الجمهورية التونسية"),
            ),
            CountryEntry(
                code = CountryCode("LY"),
                englishName = "Libya",
                arabicNames = listOf("ليبيا", "دولة ليبيا"),
            ),
            CountryEntry(
                code = CountryCode("SD"),
                englishName = "Sudan",
                arabicNames = listOf("السودان", "جمهورية السودان"),
            ),
            CountryEntry(
                code = CountryCode("SO"),
                englishName = "Somalia",
                arabicNames = listOf("الصومال", "جمهورية الصومال"),
            ),
            CountryEntry(
                code = CountryCode("DJ"),
                englishName = "Djibouti",
                arabicNames = listOf("جيبوتي", "جمهورية جيبوتي"),
            ),
            CountryEntry(
                code = CountryCode("MR"),
                englishName = "Mauritania",
                arabicNames = listOf("موريتانيا", "الجمهورية الإسلامية الموريتانية"),
            ),
            CountryEntry(
                code = CountryCode("KM"),
                englishName = "Comoros",
                arabicNames = listOf("جزر القمر"),
            ),
            CountryEntry(
                code = CountryCode("TD"),
                englishName = "Chad",
                arabicNames = listOf("تشاد"),
            ),
            CountryEntry(
                code = CountryCode("NE"),
                englishName = "Niger",
                arabicNames = listOf("النيجر"),
            ),
            CountryEntry(
                code = CountryCode("ML"),
                englishName = "Mali",
                arabicNames = listOf("مالي"),
            ),
            CountryEntry(
                code = CountryCode("SN"),
                englishName = "Senegal",
                arabicNames = listOf("السنغال"),
            ),
            CountryEntry(
                code = CountryCode("NG"),
                englishName = "Nigeria",
                arabicNames = listOf("نيجيريا"),
            ),
            CountryEntry(
                code = CountryCode("ZA"),
                englishName = "South Africa",
                arabicNames = listOf("جنوب أفريقيا"),
            ),
            CountryEntry(
                code = CountryCode("UZ"),
                englishName = "Uzbekistan",
                arabicNames = listOf("أوزبكستان"),
            ),
            CountryEntry(
                code = CountryCode("KZ"),
                englishName = "Kazakhstan",
                arabicNames = listOf("كازاخستان"),
            ),
            CountryEntry(
                code = CountryCode("KG"),
                englishName = "Kyrgyzstan",
                arabicNames = listOf("قيرغيزستان"),
            ),
            CountryEntry(
                code = CountryCode("TJ"),
                englishName = "Tajikistan",
                arabicNames = listOf("طاجيكستان"),
            ),
            CountryEntry(
                code = CountryCode("TM"),
                englishName = "Turkmenistan",
                arabicNames = listOf("تركمانستان"),
            ),
            CountryEntry(
                code = CountryCode("AZ"),
                englishName = "Azerbaijan",
                arabicNames = listOf("أذربيجان"),
            ),
            CountryEntry(
                code = CountryCode("GB"),
                englishName = "United Kingdom",
                arabicNames = listOf("المملكة المتحدة", "بريطانيا", "إنجلترا"),
                aliases = listOf("UK", "Britain", "Great Britain", "England", "Scotland", "Wales"),
            ),
            CountryEntry(
                code = CountryCode("US"),
                englishName = "United States",
                arabicNames = listOf("الولايات المتحدة", "الولايات المتحدة الأمريكية", "أمريكا"),
                aliases = listOf("USA", "America"),
            ),
            CountryEntry(
                code = CountryCode("CA"),
                englishName = "Canada",
                arabicNames = listOf("كندا"),
            ),
            CountryEntry(
                code = CountryCode("FR"),
                englishName = "France",
                arabicNames = listOf("فرنسا"),
            ),
            CountryEntry(
                code = CountryCode("DE"),
                englishName = "Germany",
                arabicNames = listOf("ألمانيا"),
            ),
            CountryEntry(
                code = CountryCode("IT"),
                englishName = "Italy",
                arabicNames = listOf("إيطاليا"),
            ),
            CountryEntry(
                code = CountryCode("ES"),
                englishName = "Spain",
                arabicNames = listOf("إسبانيا", "الأندلس"),
            ),
            CountryEntry(
                code = CountryCode("NL"),
                englishName = "Netherlands",
                arabicNames = listOf("هولندا"),
                aliases = listOf("Holland"),
            ),
            CountryEntry(
                code = CountryCode("BE"),
                englishName = "Belgium",
                arabicNames = listOf("بلجيكا"),
            ),
            CountryEntry(
                code = CountryCode("CH"),
                englishName = "Switzerland",
                arabicNames = listOf("سويسرا"),
            ),
            CountryEntry(
                code = CountryCode("AT"),
                englishName = "Austria",
                arabicNames = listOf("النمسا"),
            ),
            CountryEntry(
                code = CountryCode("SE"),
                englishName = "Sweden",
                arabicNames = listOf("السويد"),
            ),
            CountryEntry(
                code = CountryCode("NO"),
                englishName = "Norway",
                arabicNames = listOf("النرويج"),
            ),
            CountryEntry(
                code = CountryCode("DK"),
                englishName = "Denmark",
                arabicNames = listOf("الدنمارك"),
            ),
            CountryEntry(
                code = CountryCode("FI"),
                englishName = "Finland",
                arabicNames = listOf("فنلندا"),
            ),
            CountryEntry(
                code = CountryCode("IE"),
                englishName = "Ireland",
                arabicNames = listOf("أيرلندا"),
            ),
            CountryEntry(
                code = CountryCode("PT"),
                englishName = "Portugal",
                arabicNames = listOf("البرتغال"),
            ),
            CountryEntry(
                code = CountryCode("GR"),
                englishName = "Greece",
                arabicNames = listOf("اليونان"),
            ),
            CountryEntry(
                code = CountryCode("CY"),
                englishName = "Cyprus",
                arabicNames = listOf("قبرص"),
            ),
            CountryEntry(
                code = CountryCode("RU"),
                englishName = "Russia",
                arabicNames = listOf("روسيا"),
            ),
            CountryEntry(
                code = CountryCode("UA"),
                englishName = "Ukraine",
                arabicNames = listOf("أوكرانيا"),
            ),
            CountryEntry(
                code = CountryCode("PL"),
                englishName = "Poland",
                arabicNames = listOf("بولندا"),
            ),
            CountryEntry(
                code = CountryCode("RO"),
                englishName = "Romania",
                arabicNames = listOf("رومانيا"),
            ),
            CountryEntry(
                code = CountryCode("BA"),
                englishName = "Bosnia and Herzegovina",
                arabicNames = listOf("البوسنة والهرسك", "البوسنة"),
                aliases = listOf("Bosnia"),
            ),
            CountryEntry(
                code = CountryCode("AL"),
                englishName = "Albania",
                arabicNames = listOf("ألبانيا"),
            ),
            CountryEntry(
                code = CountryCode("XK"),
                englishName = "Kosovo",
                arabicNames = listOf("كوسوفو"),
            ),
            CountryEntry(
                code = CountryCode("ME"),
                englishName = "Montenegro",
                arabicNames = listOf("الجبل الأسود"),
            ),
            CountryEntry(
                code = CountryCode("MK"),
                englishName = "North Macedonia",
                arabicNames = listOf("مقدونيا"),
                aliases = listOf("Macedonia"),
            ),
            CountryEntry(
                code = CountryCode("AU"),
                englishName = "Australia",
                arabicNames = listOf("أستراليا"),
            ),
            CountryEntry(
                code = CountryCode("NZ"),
                englishName = "New Zealand",
                arabicNames = listOf("نيوزيلندا"),
            ),
            CountryEntry(
                code = CountryCode("JP"),
                englishName = "Japan",
                arabicNames = listOf("اليابان"),
            ),
            CountryEntry(
                code = CountryCode("CN"),
                englishName = "China",
                arabicNames = listOf("الصين"),
            ),
            CountryEntry(
                code = CountryCode("KR"),
                englishName = "South Korea",
                arabicNames = listOf("كوريا الجنوبية"),
                aliases = listOf("Korea"),
            ),
            CountryEntry(
                code = CountryCode("TH"),
                englishName = "Thailand",
                arabicNames = listOf("تايلاند"),
            ),
            CountryEntry(
                code = CountryCode("PH"),
                englishName = "Philippines",
                arabicNames = listOf("الفلبين"),
            ),
            CountryEntry(
                code = CountryCode("VN"),
                englishName = "Vietnam",
                arabicNames = listOf("فيتنام"),
            ),
            CountryEntry(
                code = CountryCode("BR"),
                englishName = "Brazil",
                arabicNames = listOf("البرازيل"),
            ),
            CountryEntry(
                code = CountryCode("AR"),
                englishName = "Argentina",
                arabicNames = listOf("الأرجنتين"),
            ),
            CountryEntry(
                code = CountryCode("CL"),
                englishName = "Chile",
                arabicNames = listOf("تشيلي"),
            ),
            CountryEntry(
                code = CountryCode("CO"),
                englishName = "Colombia",
                arabicNames = listOf("كولومبيا"),
            ),
            CountryEntry(
                code = CountryCode("MX"),
                englishName = "Mexico",
                arabicNames = listOf("المكسيك"),
            ),
            CountryEntry(
                code = CountryCode("LK"),
                englishName = "Sri Lanka",
                arabicNames = listOf("سريلانكا"),
            ),
            CountryEntry(
                code = CountryCode("NP"),
                englishName = "Nepal",
                arabicNames = listOf("نيبال"),
            ),
            CountryEntry(
                code = CountryCode("MM"),
                englishName = "Myanmar",
                arabicNames = listOf("ميانمار", "بورما"),
                aliases = listOf("Burma"),
            ),
            CountryEntry(
                code = CountryCode("ET"),
                englishName = "Ethiopia",
                arabicNames = listOf("إثيوبيا", "الحبشة"),
            ),
            CountryEntry(
                code = CountryCode("KE"),
                englishName = "Kenya",
                arabicNames = listOf("كينيا"),
            ),
            CountryEntry(
                code = CountryCode("TZ"),
                englishName = "Tanzania",
                arabicNames = listOf("تنزانيا"),
            ),
            CountryEntry(
                code = CountryCode("GH"),
                englishName = "Ghana",
                arabicNames = listOf("غانا"),
            ),
            CountryEntry(
                code = CountryCode("CI"),
                englishName = "Ivory Coast",
                arabicNames = listOf("ساحل العاج"),
                aliases = listOf("Cote d'Ivoire"),
            ),
        )

    private const val ALEF_CHARS = "أإآاٱ"

    private val aliasMap: Map<String, CountryCode> by lazy {
        ALL
            .flatMap { entry ->
                entry.aliases.map { normalize(it) to entry.code }
            }.toMap()
    }

    /**
     * Resolves country codes matching a query in English, Arabic, alias, or 2-letter ISO code.
     */
    fun resolveCountryCodes(query: String): Set<CountryCode> {
        val trimmed = query.trim()
        if (trimmed.length < ISO_CODE_LENGTH) return emptySet()

        val normalized = normalize(trimmed)
        val matches = mutableSetOf<CountryCode>()

        aliasMap[normalized]?.let { matches.add(it) }

        if (trimmed.length == ISO_CODE_LENGTH && trimmed.all { it.isIsoLetter() }) {
            val upper = trimmed.uppercase()
            if (ALL.any { it.code.value == upper }) {
                matches.add(CountryCode(upper))
            }
        }

        val stripped = stripArabicArticle(normalized)
        for (entry in ALL) {
            if (matchesEntry(entry, normalized, stripped)) {
                matches.add(entry.code)
            }
        }
        return matches
    }

    private fun Char.isIsoLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

    private fun matchesEntry(
        entry: CountryEntry,
        normalized: String,
        stripped: String,
    ): Boolean =
        matchesEnglish(entry, normalized) ||
            matchesAlias(entry, normalized) ||
            matchesArabic(entry, normalized, stripped)

    private fun matchesEnglish(
        entry: CountryEntry,
        normalized: String,
    ): Boolean {
        val norm = normalize(entry.englishName)
        return norm.startsWith(normalized) || normalized.startsWith(norm)
    }

    private fun matchesAlias(
        entry: CountryEntry,
        normalized: String,
    ): Boolean =
        entry.aliases.any { alias ->
            val norm = normalize(alias)
            norm.equals(normalized, ignoreCase = true) || norm.startsWith(normalized)
        }

    private fun matchesArabic(
        entry: CountryEntry,
        normalized: String,
        stripped: String,
    ): Boolean =
        entry.arabicNames.any { arabic ->
            val norm = normalize(arabic)
            val strippedArabic = stripArabicArticle(norm)
            norm.startsWith(normalized) ||
                strippedArabic.startsWith(stripped) ||
                (normalized.length > ISO_CODE_LENGTH && norm.contains(normalized))
        }

    fun normalize(text: String): String =
        buildString(text.length) {
            for (char in text.trim().lowercase()) {
                val mapped = mapArabicChar(char)
                if (mapped != null) append(mapped)
            }
        }

    private fun mapArabicChar(char: Char): Char? =
        when {
            char in ALEF_CHARS -> 'ا'
            char == 'ة' -> 'ه'
            char == 'ى' -> 'ي'
            char.code in TASHKEEL_START..TASHKEEL_END || char.code == TATWEEL -> null
            else -> char
        }

    private fun stripArabicArticle(text: String): String =
        if (text.startsWith("ال") && text.length > MIN_ARABIC_STRIP_LENGTH) {
            text.substring(2)
        } else {
            text
        }
}
