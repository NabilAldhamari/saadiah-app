package app.saadiah.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.saadiah.model.Prayer

/**
 * Where the app can be. Tab roots sit at the bottom of the stack and show the tab bar;
 * everything else is pushed on top of one and shows a back button instead.
 */
sealed interface Screen {
    val title: String

    data object Today : Screen {
        override val title = "Today"
    }

    data object Calendar : Screen {
        override val title = "Calendar"
    }

    data object Adhkar : Screen {
        override val title = "Adhkār"
    }

    data object More : Screen {
        override val title = "More"
    }

    data object PickingCity : Screen {
        override val title = "Choose your city"
    }

    data object Doctor : Screen {
        override val title = "Alert check"
    }

    data object Ask : Screen {
        override val title = "Ask"
    }

    data object WhyThisTime : Screen {
        override val title = "Why this time"
    }

    data object Baqarah : Screen {
        override val title = "Sūrat al-Baqarah"
    }

    data class PrayerDetail(
        val prayer: Prayer,
    ) : Screen {
        override val title = "Prayer"
    }
}

val Screen.isTabRoot: Boolean
    get() = this is Screen.Today || this is Screen.Calendar || this is Screen.Adhkar || this is Screen.More

/**
 * One back stack for the whole app, so the phone's back button and the on-screen one can
 * be the same action. They were separate before, which is why the hardware button left the
 * app from every screen.
 */
class Navigator {
    private var stack by mutableStateOf<List<Screen>>(listOf(Screen.Today))

    val current: Screen get() = stack.last()

    val canGoBack: Boolean get() = stack.size > 1 || current != Screen.Today

    fun go(screen: Screen) {
        stack = stack + screen
    }

    /** A tab is a root, not a place you arrive at, so it replaces the stack rather than growing it. */
    fun switchTab(screen: Screen) {
        stack = listOf(screen)
    }

    fun back(): Boolean {
        if (stack.size > 1) {
            stack = stack.dropLast(1)
            return true
        }
        // Back from a tab reaches Today before it reaches the home screen, which is what
        // people expect of a bottom bar and what the hardware button did not do at all.
        if (current != Screen.Today) {
            stack = listOf(Screen.Today)
            return true
        }
        return false
    }
}
