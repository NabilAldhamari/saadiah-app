package app.saadiah.schedule

import app.saadiah.model.AlarmKind
import app.saadiah.model.AlarmSpec

/**
 * Fits [specs] into [max] slots, keeping the nearest ones. When some are dropped, the
 * last slot becomes a re-arm alarm timed to the first spec that did not fit, so the app
 * wakes to arm the next batch instead of letting the horizon lapse silently.
 */
fun budget(
    specs: List<AlarmSpec>,
    max: Int,
): List<AlarmSpec> {
    require(max > 0) { "at least one slot is required" }
    val ordered = specs.sortedBy { it.triggerAt }
    if (ordered.size <= max) {
        return ordered
    }
    val kept = ordered.take(max - 1)
    val reArm = ordered[max - 1]
    return kept + AlarmSpec(prayer = reArm.prayer, triggerAt = reArm.triggerAt, kind = AlarmKind.RE_ARM)
}
