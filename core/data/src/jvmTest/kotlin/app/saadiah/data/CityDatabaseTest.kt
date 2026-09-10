package app.saadiah.data

import java.io.File
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val EXPECTED_CITIES = 170493
private const val KEYSTROKE_BUDGET_MILLIS = 8.0
private const val WARMUP_ROUNDS = 50
private const val MEASURED_ROUNDS = 200
private const val NANOS_PER_MILLI = 1_000_000.0

/**
 * Exercises the database the app actually ships rather than the fixture, because the
 * budget in README.md is stated against the real one and a thirty-row file
 * would meet it no matter how the search were written.
 */
class CityDatabaseTest {
    private val index: CityIndex by lazy {
        val file = File("../../android/app/src/main/assets/cities.bin")
        assertTrue(file.exists(), "cities.bin not found at ${file.absolutePath}; run tools/gen-city-db.py")
        CityIndex(file.readBytes())
    }

    @Test
    fun carriesEveryPlaceInTheDump() {
        assertEquals(expected = EXPECTED_CITIES, actual = index.size)
    }

    @Test
    fun theWholeFileIsInSearchKeyOrder() {
        var previous = index.searchKeyAt(0)
        for (record in 1 until index.size) {
            val current = index.searchKeyAt(record)
            assertTrue(previous <= current, "record $record breaks the ordering: '$previous' then '$current'")
            previous = current
        }
    }

    @Test
    fun theBiggestPlaceWinsAName() {
        val london = index.search("London").first()

        assertEquals(expected = "GB", actual = london.country.value, message = "London GB must outrank London CA")
    }

    @Test
    fun findsASmallTownNotJustACapital() {
        assertTrue(index.search("Todmorden").any { it.country.value == "GB" }, "a town of 15,000 should be findable")
    }

    @Test
    fun findsAPlaceByItsArabicName() {
        val results = index.search("مكة")

        assertTrue(results.isNotEmpty(), "an Arabic query must reach the Arabic names")
    }

    @Test
    fun aKeystrokeStaysInsideItsBudget() {
        // A single letter is the worst case: it matches the most records before ranking.
        val queries = listOf("l", "lo", "lon", "lond", "londo", "london", "s", "sa", "ba", "new", "م", "مكة")
        repeat(WARMUP_ROUNDS) { queries.forEach { index.search(it) } }

        val slowest =
            queries.maxOf { query ->
                val nanos = measureNanoTime { repeat(MEASURED_ROUNDS) { index.search(query) } }
                val perKeystroke = nanos / MEASURED_ROUNDS / NANOS_PER_MILLI
                println("  \"$query\" -> %.3f ms".format(perKeystroke))
                perKeystroke
            }

        assertTrue(
            slowest <= KEYSTROKE_BUDGET_MILLIS,
            "slowest keystroke was %.3f ms, over the %.1f ms budget".format(slowest, KEYSTROKE_BUDGET_MILLIS),
        )
    }
}
