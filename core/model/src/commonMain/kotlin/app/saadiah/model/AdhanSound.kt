package app.saadiah.model

/**
 * Which adhān a prayer alert plays.
 *
 * [DEFAULT] is the phone's own notification sound, which is what shipped and what a reader
 * who has tuned their phone's sounds already expects. The two recordings are opt-in: an app
 * that starts playing a four-minute adhān at Fajr without being asked is a worse neighbour
 * than one that stays quiet.
 */
enum class AdhanSound { DEFAULT, SHORT, LONG }
