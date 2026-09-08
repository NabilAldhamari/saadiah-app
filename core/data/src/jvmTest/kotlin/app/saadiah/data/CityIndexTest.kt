package app.saadiah.data

import app.saadiah.model.CityId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun loadFixture(): CityIndex {
    val stream =
        checkNotNull(CityIndexTest::class.java.getResourceAsStream("/cities-fixture.bin")) {
            "cities-fixture.bin is missing; regenerate it with tools/gen-city-db.py"
        }
    return CityIndex(stream.readBytes())
}

class CityIndexTest {
    private val index = loadFixture()

    @Test
    fun readsEveryRecordInTheFixture() {
        assertEquals(expected = 30, actual = index.size)
    }

    @Test
    fun rejectsABlobThatIsNotACityDatabase() {
        val notADatabase = ByteArray(64) { 0 }

        val failure = runCatching { CityIndex(notADatabase) }.exceptionOrNull()

        assertNotNull(failure, "a blob with the wrong magic must not be read as a city database")
    }

    @Test
    fun findsACityByExactName() {
        val results = index.search("London")

        assertTrue(results.any { it.name == "London" && it.country.value == "GB" }, "London GB was not found")
    }

    @Test
    fun findsACityByPrefix() {
        assertTrue(index.search("Lond").any { it.name == "London" })
    }

    @Test
    fun matchingIgnoresCase() {
        assertEquals(
            expected = index.search("London").map { it.id },
            actual = index.search("lOnDoN").map { it.id },
        )
    }

    @Test
    fun theSameNameInTwoCountriesReturnsBoth() {
        val countries = index.search("London").map { it.country.value }.toSet()

        assertTrue(countries.containsAll(setOf("GB", "CA")), "expected London in both GB and CA, got $countries")
    }

    @Test
    fun searchPrioritizesCitiesInPreferredTimeZone() {
        // In the fixture, London exists in both GB (Europe/London) and CA (America/Toronto).
        // With America/Toronto preferred, London CA should rank above London GB despite lower population.
        val canadianResults =
            index.search(
                "London",
                preferredTimeZone = kotlinx.datetime.TimeZone.of("America/Toronto"),
            )
        assertEquals(expected = "CA", actual = canadianResults.first().country.value)

        // With Europe/London preferred, London GB should rank first.
        val britishResults = index.search("London", preferredTimeZone = kotlinx.datetime.TimeZone.of("Europe/London"))
        assertEquals(expected = "GB", actual = britishResults.first().country.value)
    }

    @Test
    fun anUnknownNameFindsNothing() {
        assertTrue(index.search("Zzzznowhere").isEmpty())
    }

    @Test
    fun anEmptyQueryReturnsNothingRatherThanEverything() {
        assertTrue(index.search("   ").isEmpty())
    }

    @Test
    fun aCityCarriesItsCoordinatesAndZone() {
        val london = index.search("London").first { it.country.value == "GB" }

        assertEquals(expected = 51.5085, actual = london.coordinates.latitude, absoluteTolerance = 0.001)
        assertEquals(expected = -0.1257, actual = london.coordinates.longitude, absoluteTolerance = 0.001)
        assertEquals(expected = "Europe/London", actual = london.timeZone.id)
    }

    @Test
    fun aCityCarriesItsRegion() {
        val london = index.search("London").first { it.country.value == "GB" }

        assertTrue(london.admin1.isNotBlank(), "London should carry its admin1 name")
    }

    @Test
    fun anArabicNameIsExposedWhenTheSourceHasOne() {
        val makkah = index.search("Makkah").firstOrNull()

        assertNotNull(makkah, "Makkah is in the fixture")
        assertNotNull(makkah.arabicName, "Makkah should carry an Arabic name")
        assertTrue(makkah.arabicName.orEmpty().any { it in '؀'..'ۿ' }, "expected Arabic script")
    }

    @Test
    fun lookupByIdReturnsTheSameCityThatSearchFound() {
        val found = index.search("Makkah").first()

        assertEquals(expected = found, actual = index.byId(found.id))
    }

    @Test
    fun lookupByAnUnknownIdReturnsNull() {
        assertNull(index.byId(CityId(1)))
    }

    /**
     * The generator sorts on lower(asciiname) and the reader binary-searches the same key.
     * If the two ever drift the search misses silently, so the ordering is asserted rather
     * than trusted.
     */
    @Test
    fun everyRecordIsOrderedBySearchKey() {
        val keys = (0 until index.size).map { index.searchKeyAt(it) }

        assertEquals(expected = keys.sorted(), actual = keys, message = "records are not in search-key order")
    }
}
