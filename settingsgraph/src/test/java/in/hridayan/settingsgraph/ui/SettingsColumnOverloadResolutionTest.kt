package `in`.hridayan.settingsgraph.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import org.junit.Test

/**
 * Compile-time and unit tests verifying unambiguous overload resolution for SettingsColumn.
 */
@OptIn(ExperimentalMaterial3Api::class)
class SettingsColumnOverloadResolutionTest {

    @Composable
    private fun CallWithoutTopBar() {
        SettingsColumn {
            group("test") {}
        }
    }

    @Composable
    private fun CallWithScrollBehavior() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        SettingsColumn(
            topAppBarScrollBehavior = scrollBehavior
        ) {
            group("test") {}
        }
    }

    @Suppress("DEPRECATION")
    @Composable
    private fun CallWithLegacyTopAppBarState() {
        val state = rememberTopAppBarState()
        SettingsColumn(
            topAppBarState = state
        ) {
            group("test") {}
        }
    }

    @Test
    fun overloadsCompileWithoutAmbiguity() {
        assert(true)
    }
}
