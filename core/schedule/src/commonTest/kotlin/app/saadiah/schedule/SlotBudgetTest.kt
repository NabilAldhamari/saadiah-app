package app.saadiah.schedule

import app.saadiah.model.AlarmKind
import app.saadiah.model.AlarmSpec
import app.saadiah.model.Prayer
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val SEED = 20_260_725
private const val ITERATIONS = 10_000
private const val IOS_SLOTS = 64
private const val SECONDS_PER_MINUTE = 60L

class SlotBudgetTest {
    private fun spec(
        minute: Int,
        kind: AlarmKind = AlarmKind.AT_TIME,
    ): AlarmSpec =
        AlarmSpec(
            prayer = Prayer.entries[minute % Prayer.entries.size],
            triggerAt = Instant.fromEpochSeconds(minute * SECONDS_PER_MINUTE),
            kind = kind,
        )

    private fun specs(count: Int): List<AlarmSpec> = (0 until count).map { spec(it) }

    @Test
    fun returnsEverythingWhenWithinBudget() {
        val input = specs(10)

        val budgeted = budget(input, max = IOS_SLOTS)

        assertEquals(expected = input, actual = budgeted)
        assertTrue(budgeted.none { it.kind == AlarmKind.RE_ARM }, "no re-arm is needed when nothing is dropped")
    }

    @Test
    fun neverExceedsTheMaximum() {
        for (count in listOf(0, 1, 63, 64, 65, 200)) {
            assertTrue(budget(specs(count), max = IOS_SLOTS).size <= IOS_SLOTS, "count $count")
        }
    }

    @Test
    fun prefersNearerSpecs() {
        val input = specs(200)

        val budgeted = budget(input, max = IOS_SLOTS)

        val kept = budgeted.filter { it.kind != AlarmKind.RE_ARM }
        assertEquals(expected = input.take(kept.size), actual = kept, "kept specs must be the nearest ones")
    }

    @Test
    fun neverDropsASpecWhileALaterOneSurvives() {
        val budgeted = budget(specs(200), max = IOS_SLOTS).filter { it.kind != AlarmKind.RE_ARM }

        val times = budgeted.map { it.triggerAt }
        assertEquals(expected = times.sorted(), actual = times)
        assertEquals(expected = times.size, actual = times.distinct().size)
    }

    @Test
    fun reservesASlotForTheReArmReminder() {
        val input = specs(200)

        val budgeted = budget(input, max = IOS_SLOTS)

        assertEquals(expected = IOS_SLOTS, actual = budgeted.size)
        val reArm = budgeted.filter { it.kind == AlarmKind.RE_ARM }
        assertEquals(expected = 1, actual = reArm.size)
        assertEquals(
            expected = input[IOS_SLOTS - 1].triggerAt,
            actual = reArm.single().triggerAt,
            message = "re-arm must fire when the armed batch would otherwise run dry",
        )
    }

    @Test
    fun acceptsASingleSlotByReArmingImmediately() {
        val budgeted = budget(specs(50), max = 1)

        assertEquals(expected = 1, actual = budgeted.size)
        assertEquals(expected = AlarmKind.RE_ARM, actual = budgeted.single().kind)
    }

    @Test
    fun requiresPositiveMaxSlots() {
        assertFailsWith<IllegalArgumentException> {
            budget(specs(10), max = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            budget(specs(10), max = -5)
        }
    }

    @Test
    fun holdsInvariantsOverSeededRandomInput() {
        val random = Random(SEED)
        repeat(ITERATIONS) { iteration ->
            val count = random.nextInt(0, 150)
            val max = random.nextInt(1, 80)
            val input =
                (0 until count)
                    .map { spec(random.nextInt(0, 5_000)) }
                    .distinctBy { it.triggerAt }
                    .sortedBy { it.triggerAt }

            val budgeted = budget(input, max = max)
            val kept = budgeted.filter { it.kind != AlarmKind.RE_ARM }
            val context = "iteration $iteration count=$count max=$max"

            assertTrue(budgeted.size <= max, "$context exceeded the budget")
            assertEquals(expected = input.take(kept.size), actual = kept, "$context dropped a nearer spec")
            if (input.size > max) {
                assertEquals(expected = 1, actual = budgeted.count { it.kind == AlarmKind.RE_ARM }, context)
            } else {
                assertEquals(expected = input, actual = budgeted, "$context should pass through untouched")
            }
        }
    }
}
