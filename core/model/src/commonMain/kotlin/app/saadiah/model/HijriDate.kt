package app.saadiah.model

data class HijriDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    init {
        require(year >= MIN_YEAR) { "hijri year must be positive: $year" }
        require(month in MIN_MONTH..MAX_MONTH) { "hijri month out of range: $month" }
        require(day in MIN_DAY..MAX_DAY) { "hijri day out of range: $day" }
    }

    private companion object {
        const val MIN_YEAR = 1
        const val MIN_MONTH = 1
        const val MAX_MONTH = 12
        const val MIN_DAY = 1
        const val MAX_DAY = 30
    }
}
