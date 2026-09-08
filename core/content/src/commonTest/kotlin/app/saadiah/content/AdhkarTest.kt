package app.saadiah.content

import app.saadiah.model.Tradition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val EXPECTED_MORNING = 26
private const val EXPECTED_EVENING = 24

class AdhkarTest {
    private val morning = adhkar(DhikrCollection.MORNING, Tradition.SUNNI)
    private val evening = adhkar(DhikrCollection.EVENING, Tradition.SUNNI)

    /** DESIGN.md §6.4: no entry renders without a source reference. */
    @Test
    fun everyEntryCarriesASourceReference() {
        val all = morning + evening

        assertTrue(all.isNotEmpty())
        for (dhikr in all) {
            assertTrue(dhikr.source.isNotBlank(), "${dhikr.id} has no source")
        }
    }

    @Test
    fun anEntryWithoutASourceCannotBeConstructed() {
        assertFailsWith<IllegalArgumentException> {
            Dhikr(
                id = "x",
                order = 1,
                arabic = "ا",
                transliteration = null,
                translation = null,
                repetitions = 1,
                repetitionsInWords = null,
                virtue = null,
                source = "  ",
                collections = setOf(DhikrCollection.MORNING),
                traditions = setOf(Tradition.SUNNI),
            )
        }
    }

    @Test
    fun anUntaggedEntryCannotBeConstructed() {
        assertFailsWith<IllegalArgumentException> {
            Dhikr(
                id = "x",
                order = 1,
                arabic = "ا",
                transliteration = null,
                translation = null,
                repetitions = 1,
                repetitionsInWords = null,
                virtue = null,
                source = "Ṣaḥīḥ Muslim",
                collections = setOf(DhikrCollection.MORNING),
                traditions = emptySet(),
            )
        }
    }

    @Test
    fun bothSetsAreWhatTheSourceDescribes() {
        assertEquals(expected = EXPECTED_MORNING, actual = morning.size)
        assertEquals(expected = EXPECTED_EVENING, actual = evening.size)
    }

    @Test
    fun afterPrayerCollectionIsPopulatedAndTagged() {
        val afterPrayer = adhkar(DhikrCollection.AFTER_PRAYER, Tradition.SUNNI)
        assertTrue(afterPrayer.isNotEmpty(), "after-prayer adhkār collection must not be empty")
        for (dhikr in afterPrayer) {
            assertTrue(dhikr.source.isNotBlank(), "${dhikr.id} has no source")
            assertTrue(dhikr.arabic.isNotBlank(), "${dhikr.id} has no Arabic")
            assertTrue(Tradition.SUNNI in dhikr.traditions)
        }
    }

    @Test
    fun theSharedEntriesAppearInBothSets() {
        val shared = morning.filter { DhikrCollection.EVENING in it.collections }

        assertTrue(shared.isNotEmpty())
        for (dhikr in shared) {
            assertTrue(evening.any { it.id == dhikr.id }, "${dhikr.id} is missing from the evening set")
        }
    }

    @Test
    fun nothingLeaksToATraditionItWasNotTaggedFor() {
        for (collection in DhikrCollection.entries) {
            for (tradition in Tradition.entries) {
                val returned = adhkar(collection, tradition)

                assertTrue(returned.all { tradition in it.traditions }, "$collection leaked to $tradition")
            }
        }
    }

    @Test
    fun everyEntryCarriesItsArabicAndItsRepetitionCount() {
        for (dhikr in morning + evening) {
            assertTrue(dhikr.arabic.isNotBlank(), dhikr.id)
            assertTrue(dhikr.repetitions >= 1, dhikr.id)
        }
    }

    @Test
    fun eachSetIsOrdered() {
        assertEquals(expected = morning.map { it.order }.sorted(), actual = morning.map { it.order })
    }

    @Test
    fun searchingFindsTheUthmaniTextFromAnUnvocalisedQuery() {
        val results = searchAdhkar("امسينا", Tradition.SUNNI)

        assertTrue(results.isNotEmpty(), "an unvocalised query must reach the vocalised text")
    }

    @Test
    fun searchingAlsoMatchesTheTranslation() {
        assertTrue(searchAdhkar("Allah", Tradition.SUNNI).isNotEmpty())
    }

    @Test
    fun anEmptyQueryReturnsNothingRatherThanEverything() {
        assertTrue(searchAdhkar("   ", Tradition.SUNNI).isEmpty())
    }
}
