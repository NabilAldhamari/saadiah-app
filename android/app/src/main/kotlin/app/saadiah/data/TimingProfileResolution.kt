package app.saadiah.data

import app.saadiah.model.City
import app.saadiah.model.TimingProfile
import app.saadiah.prayer.inferProfile

/**
 * The one place a [TimingProfile] is decided, because there are two callers that must agree:
 * the screens showing prayer times and the scheduler arming the alarms for them. They each
 * built their own before, from `inferProfile` alone — so a matched masjid timetable could not
 * have reached either, and had it reached one, the alerts would have fired at other times than
 * the screen displayed.
 *
 * A matched profile wins over the country's method. [Settings.madhab] and
 * [Settings.combineMode] are layered on top of whichever is used: they are the reader's own
 * choices, still theirs to change afterwards without discarding the match.
 */
fun Settings.timingProfileFor(city: City): TimingProfile {
    val base = timingProfile ?: inferProfile(city)
    // An unchosen madhhab keeps whichever the base already carries: the one the solve
    // recovered, or the one the country implies. Defaulting to Shāfiʿī here discarded the
    // Ḥanafī that inferProfile had just put there for Pakistan, India and Bangladesh.
    return base.copy(madhab = madhab ?: base.madhab, combineMode = combineMode)
}
