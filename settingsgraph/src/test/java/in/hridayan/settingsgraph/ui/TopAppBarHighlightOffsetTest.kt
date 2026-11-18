package `in`.hridayan.settingsgraph.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying top app bar offset resolution during highlight scrolling.
 */
@OptIn(ExperimentalMaterial3Api::class)
class TopAppBarHighlightOffsetTest {

    @Test
    fun pinnedScrollBehavior_neverCollapses_evenWhenCollapseRequested() {
        val state = TopAppBarState(
            initialHeightOffsetLimit = -100f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )
        val behavior = FakeTopAppBarScrollBehavior(isPinned = true, state = state)

        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = behavior,
            legacyState = null,
            collapseOnHighlight = true,
            onHighlightScroll = null
        )

        assertEquals(0f, state.heightOffset, 0.001f)
    }

    @Test
    fun unpinnedScrollBehavior_collapses_whenCollapseRequested() {
        val state = TopAppBarState(
            initialHeightOffsetLimit = -100f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )
        val behavior = FakeTopAppBarScrollBehavior(isPinned = false, state = state)

        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = behavior,
            legacyState = null,
            collapseOnHighlight = true,
            onHighlightScroll = null
        )

        assertEquals(-100f, state.heightOffset, 0.001f)
    }

    @Test
    fun unpinnedScrollBehavior_doesNotCollapse_whenCollapseDisabled() {
        val state = TopAppBarState(
            initialHeightOffsetLimit = -100f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )
        val behavior = FakeTopAppBarScrollBehavior(isPinned = false, state = state)

        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = behavior,
            legacyState = null,
            collapseOnHighlight = false,
            onHighlightScroll = null
        )

        assertEquals(0f, state.heightOffset, 0.001f)
    }

    @Test
    fun customAppBar_invokesOnHighlightScrollCallback() {
        var callbackInvoked = false

        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = null,
            legacyState = null,
            collapseOnHighlight = false,
            onHighlightScroll = { callbackInvoked = true }
        )

        assertTrue(callbackInvoked)
    }

    @Test
    fun legacyTopAppBarState_doesNotCollapse_byDefault() {
        val state = TopAppBarState(
            initialHeightOffsetLimit = -100f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )

        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = null,
            legacyState = state,
            collapseOnHighlight = false,
            onHighlightScroll = null
        )

        assertEquals(0f, state.heightOffset, 0.001f)
    }

    @Test
    fun legacyTopAppBarState_collapses_whenExplicitlyRequested() {
        val state = TopAppBarState(
            initialHeightOffsetLimit = -100f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f
        )

        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = null,
            legacyState = state,
            collapseOnHighlight = true,
            onHighlightScroll = null
        )

        assertEquals(-100f, state.heightOffset, 0.001f)
    }

    @Test
    fun allNull_noOp() {
        resolveTopAppBarOffsetOnHighlight(
            scrollBehavior = null,
            legacyState = null,
            collapseOnHighlight = true,
            onHighlightScroll = null
        )
    }

    private class FakeTopAppBarScrollBehavior(
        override val isPinned: Boolean,
        override val state: TopAppBarState,
    ) : TopAppBarScrollBehavior {
        override val snapAnimationSpec: AnimationSpec<Float>? = null
        override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
        override val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {}
    }
}
