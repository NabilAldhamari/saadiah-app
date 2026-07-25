package app.saadiah.doctor

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val RESTRICTIVE =
    listOf(
        "Xiaomi",
        "Redmi",
        "POCO",
        "HUAWEI",
        "HONOR",
        "OPPO",
        "realme",
        "vivo",
        "OnePlus",
        "samsung",
        "Meizu",
        "asus",
        "Letv",
    )

@RunWith(RobolectricTestRunner::class)
class OemGuidanceTest {
    @Test
    fun everyKnownRestrictiveManufacturerHasSomewhereToSend() {
        for (manufacturer in RESTRICTIVE) {
            assertTrue(
                autostartComponentsFor(manufacturer).isNotEmpty(),
                "$manufacturer is known for killing alarms but offers no destination",
            )
        }
    }

    @Test
    fun manufacturerMatchingIgnoresCase() {
        assertEquals(
            expected = autostartComponentsFor("Xiaomi"),
            actual = autostartComponentsFor("xIaOmI"),
        )
    }

    @Test
    fun anUnknownManufacturerHasNoSpecificDestination() {
        assertTrue(autostartComponentsFor("Fairphone").isEmpty())
        assertTrue(autostartComponentsFor("").isEmpty())
    }

    @Test
    fun componentsNameARealPackageAndClass() {
        for (manufacturer in RESTRICTIVE) {
            for (component in autostartComponentsFor(manufacturer)) {
                assertTrue(component.packageName.contains("."), "$manufacturer: ${component.packageName}")
                assertTrue(component.className.contains("."), "$manufacturer: ${component.className}")
            }
        }
    }

    @Test
    fun theRestrictiveListIsRecognisedAsRestrictive() {
        for (manufacturer in RESTRICTIVE) {
            assertTrue(isKnownRestrictive(manufacturer), manufacturer)
        }
        assertTrue(!isKnownRestrictive("Fairphone"))
    }
}
