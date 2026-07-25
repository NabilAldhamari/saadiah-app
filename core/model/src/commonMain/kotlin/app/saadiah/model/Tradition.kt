package app.saadiah.model

/**
 * Only one entry for now. It is kept as a type rather than deleted because observances,
 * adhkār and nawāfil are all tagged with it, and those tags are what stop content reaching
 * a reader it was not written for. Adding a tradition later means adding a case here, not
 * reinstating the concept everywhere.
 */
enum class Tradition {
    SUNNI,
}

enum class Madhab {
    SHAFI,
    HANAFI,
}

enum class ObservanceKind {
    OBLIGATORY_FAST,
    RECOMMENDED_FAST,
    PROHIBITED_FAST,
    RECOMMENDED_HIJAMA,
    EID,
}
