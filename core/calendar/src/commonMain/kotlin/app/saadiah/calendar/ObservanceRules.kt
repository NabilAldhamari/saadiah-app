package app.saadiah.calendar

import app.saadiah.model.HijriDate
import app.saadiah.model.ObservanceKind
import app.saadiah.model.Tradition

private const val MUHARRAM = 1
private const val RAMADAN = 9
private const val SHAWWAL = 10
private const val DHU_AL_HIJJAH = 12

private const val TASUA = 9
private const val ASHURA = 10
private const val ARAFAH = 9
private const val EID_AL_ADHA_DAY = 10

private val TASHRIQ_DAYS = 11..13
private val WHITE_DAYS = 13..15
private val SIX_OF_SHAWWAL_DAYS = 2..7
private val HIJAMA_DAYS = setOf(17, 19, 21)

private val BOTH = setOf(Tradition.SUNNI)
private val SUNNI_ONLY = setOf(Tradition.SUNNI)

enum class Observance {
    RAMADAN,
    EID_AL_FITR,
    EID_AL_ADHA,
    TASHRIQ,
    ARAFAH,
    TASUA,
    ASHURA,
    AYYAM_AL_BID,
    SIX_OF_SHAWWAL,
    HIJAMA,
}

data class ObservanceRule(
    val observance: Observance,
    val kind: ObservanceKind,
    val traditions: Set<Tradition>,
    val applies: (HijriDate) -> Boolean,
)

/**
 * Ashura is the divergence that catches people: a recommended fast in Sunni practice,
 * while for Twelvers it falls in the mourning of Muharram and is not offered as one.
 * Twelver mourning observances have no [ObservanceKind] yet, so nothing is emitted
 * for them rather than something wrong.
 */
internal val OBSERVANCE_RULES: List<ObservanceRule> =
    listOf(
        ObservanceRule(Observance.RAMADAN, ObservanceKind.OBLIGATORY_FAST, BOTH) { it.month == RAMADAN },
        ObservanceRule(Observance.EID_AL_FITR, ObservanceKind.EID, BOTH) { it.isEidAlFitr() },
        ObservanceRule(Observance.EID_AL_FITR, ObservanceKind.PROHIBITED_FAST, BOTH) { it.isEidAlFitr() },
        ObservanceRule(Observance.EID_AL_ADHA, ObservanceKind.EID, BOTH) { it.isEidAlAdha() },
        ObservanceRule(Observance.EID_AL_ADHA, ObservanceKind.PROHIBITED_FAST, BOTH) { it.isEidAlAdha() },
        ObservanceRule(Observance.TASHRIQ, ObservanceKind.PROHIBITED_FAST, BOTH) {
            it.month == DHU_AL_HIJJAH && it.day in TASHRIQ_DAYS
        },
        ObservanceRule(Observance.AYYAM_AL_BID, ObservanceKind.RECOMMENDED_FAST, BOTH) { it.day in WHITE_DAYS },
        ObservanceRule(Observance.ARAFAH, ObservanceKind.RECOMMENDED_FAST, BOTH) {
            it.month == DHU_AL_HIJJAH && it.day == ARAFAH
        },
        ObservanceRule(Observance.TASUA, ObservanceKind.RECOMMENDED_FAST, SUNNI_ONLY) {
            it.month == MUHARRAM && it.day == TASUA
        },
        ObservanceRule(Observance.ASHURA, ObservanceKind.RECOMMENDED_FAST, SUNNI_ONLY) {
            it.month == MUHARRAM && it.day == ASHURA
        },
        ObservanceRule(Observance.SIX_OF_SHAWWAL, ObservanceKind.RECOMMENDED_FAST, SUNNI_ONLY) {
            it.month == SHAWWAL && it.day in SIX_OF_SHAWWAL_DAYS
        },
        ObservanceRule(Observance.HIJAMA, ObservanceKind.RECOMMENDED_HIJAMA, BOTH) { it.day in HIJAMA_DAYS },
    )

/**
 * A recommended fast is dropped when the same day already prohibits fasting, or when
 * it is already obligatory, so Ayyam al-Bid never appears on a day of Tashriq or inside
 * Ramadan.
 */
fun observancesOn(
    date: HijriDate,
    tradition: Tradition,
): List<ObservanceRule> {
    val matching = OBSERVANCE_RULES.filter { tradition in it.traditions && it.applies(date) }
    val fastingSettled =
        matching.any {
            it.kind == ObservanceKind.PROHIBITED_FAST || it.kind == ObservanceKind.OBLIGATORY_FAST
        }
    return if (fastingSettled) matching.filterNot { it.kind == ObservanceKind.RECOMMENDED_FAST } else matching
}

private fun HijriDate.isEidAlFitr(): Boolean = month == SHAWWAL && day == 1

private fun HijriDate.isEidAlAdha(): Boolean = month == DHU_AL_HIJJAH && day == EID_AL_ADHA_DAY
