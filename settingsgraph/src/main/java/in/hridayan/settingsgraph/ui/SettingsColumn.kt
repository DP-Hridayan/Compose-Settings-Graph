package `in`.hridayan.settingsgraph.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.settingsgraph.graph.SettingsGraphBuilder
import `in`.hridayan.settingsgraph.model.ItemBehavior
import `in`.hridayan.settingsgraph.model.SettingsGraph
import `in`.hridayan.settingsgraph.model.SettingsGraphGroup
import `in`.hridayan.settingsgraph.model.SettingsNode
import `in`.hridayan.settingsgraph.ui.card.CustomCardShape
import `in`.hridayan.settingsgraph.ui.card.cardShapeForPosition
import `in`.hridayan.settingsgraph.ui.item.SettingsItemDefaults
import `in`.hridayan.settingsgraph.ui.item.SettingsItemIconColors
import `in`.hridayan.settingsgraph.ui.item.SettingsItemView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

private val NoOpAnyCallback: (Any) -> Unit = {}
private val NoOpAnyIntCallback: (Any, Int) -> Unit = { _, _ -> }
private val NoOpAnyBooleanCallback: (Any, Boolean) -> Unit = { _, _ -> }
private val AlwaysFalse: (Any) -> Boolean = { false }
private val AlwaysNegativeOne: (Any) -> Int = { -1 }

private const val HIGHLIGHT_MATCH_TIMEOUT_MS = 1_000L
private const val HIGHLIGHT_SCROLL_DELAY_MS = 400L
private const val HIGHLIGHT_BLINK_DURATION_MS = 2_500L

private const val HEADER_KEY_PREFIX = "header_"
private const val DIVIDER_KEY_PREFIX = "divider_"
private const val KEY_SEPARATOR = "_"

private val HEADER_HORIZONTAL_PADDING = 20.dp
private val HEADER_TOP_PADDING = 30.dp
private val HEADER_BOTTOM_PADDING = 10.dp
private val DIVIDER_HORIZONTAL_PADDING = 16.dp
private val DIVIDER_VERTICAL_PADDING = 8.dp

/**
 * Resolves the top app bar offset and notifies highlight listeners when scrolling to a highlighted setting.
 *
 * @param scrollBehavior Optional [TopAppBarScrollBehavior] for Material 3 app bars.
 * @param legacyState Optional legacy [TopAppBarState].
 * @param collapseOnHighlight Whether non-pinned app bars should collapse during highlight scroll.
 * @param onHighlightScroll Optional callback invoked during highlight scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun resolveTopAppBarOffsetOnHighlight(
    scrollBehavior: TopAppBarScrollBehavior?,
    legacyState: TopAppBarState?,
    collapseOnHighlight: Boolean,
    onHighlightScroll: (() -> Unit)?,
) {
    onHighlightScroll?.invoke()

    if (!collapseOnHighlight) return

    if (scrollBehavior != null) {
        if (!scrollBehavior.isPinned) {
            scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffsetLimit
        }
    } else if (legacyState != null) {
        legacyState.heightOffset = legacyState.heightOffsetLimit
    }
}

/**
 * Renders a settings screen defined by the [content] graph block.
 *
 * Callbacks are resolved in two tiers for each item:
 * 1. Per-item override declared inside the item builder block (highest priority).
 * 2. Global defaults from [LocalSettingGraphState] (lowest priority).
 *
 * The graph is rebuilt on every recomposition so that [visible] lambdas and dynamic title/
 * description lambdas always reflect the latest captured values from the enclosing composable.
 *
 * Compatible with any app bar type:
 * - When using Material 3 top app bars with scroll behaviors, pass [topAppBarScrollBehavior].
 *   Pinned app bars are automatically protected and will never collapse off-screen.
 * - When using two-row collapsible app bars ([MediumTopAppBar], [LargeTopAppBar]), set
 *   [collapseTopAppBarOnHighlight] to true if you wish to collapse the large title when
 *   scrolling to a search result.
 * - For custom app bars, pass [onHighlightScroll] to execute custom collapse or animation logic.
 * - If no app bar or a static app bar is used, leave top bar parameters null and provide [contentPadding].
 *
 * @param modifier Modifier applied to the [LazyColumn].
 * @param topAppBarScrollBehavior Optional [TopAppBarScrollBehavior] to inspect pinned status and
 *                                optionally coordinate top bar collapse during highlight scrolling.
 * @param collapseTopAppBarOnHighlight When true, collapsible (non-pinned) top app bars are collapsed
 *                                     to maximize screen space when scrolling to a highlighted item.
 *                                     Defaults to true. Pinned app bars are protected and will never collapse.
 * @param onHighlightScroll Optional callback invoked when scrolling to a highlighted item,
 *                           allowing custom app bars to react.
 * @param hapticsEnabled Whether haptic feedback is enabled on item interactions.
 * @param itemPaddingHorizontal Horizontal padding applied to each item card.
 * @param itemPaddingVertical Vertical padding applied to each item card.
 * @param groupHeader Optional composable used to render group header labels. When null, a default
 *                    [Text] styled with [MaterialTheme.typography.labelLarge] is used.
 * @param listState The [LazyListState] controlling scroll position.
 * @param contentPadding Content padding applied to the [LazyColumn].
 * @param content graph block defining the groups and items in this settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsColumn(
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior? = null,
    collapseTopAppBarOnHighlight: Boolean = true,
    onHighlightScroll: (() -> Unit)? = null,
    hapticsEnabled: Boolean = true,
    itemPaddingHorizontal: Dp = 15.dp,
    itemPaddingVertical: Dp = 1.dp,
    groupHeader: (@Composable (String) -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: SettingsGraphBuilder.() -> Unit,
) {
    SettingsColumnInternal(
        modifier = modifier,
        topAppBarScrollBehavior = topAppBarScrollBehavior,
        legacyTopAppBarState = null,
        collapseTopAppBarOnHighlight = collapseTopAppBarOnHighlight,
        onHighlightScroll = onHighlightScroll,
        hapticsEnabled = hapticsEnabled,
        itemPaddingHorizontal = itemPaddingHorizontal,
        itemPaddingVertical = itemPaddingVertical,
        groupHeader = groupHeader,
        listState = listState,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Legacy overload of [SettingsColumn] accepting [TopAppBarState].
 *
 * @param modifier Modifier applied to the [LazyColumn].
 * @param topAppBarState Legacy top app bar state. Kept for backward compatibility.
 * @param hapticsEnabled Whether haptic feedback is enabled on item interactions.
 * @param itemPaddingHorizontal Horizontal padding applied to each item card.
 * @param itemPaddingVertical Vertical padding applied to each item card.
 * @param groupHeader Optional composable used to render group header labels. When null, a default
 *                    [Text] styled with [MaterialTheme.typography.labelLarge] is used.
 * @param listState The [LazyListState] controlling scroll position.
 * @param contentPadding Content padding applied to the [LazyColumn].
 * @param content graph block defining the groups and items in this settings screen.
 */
@Deprecated(
    message = "Use the canonical SettingsColumn overload. If using a Material 3 TopAppBar, pass topAppBarScrollBehavior instead of topAppBarState to preserve pinned app bar behavior.",
    replaceWith = ReplaceWith(
        "SettingsColumn(modifier = modifier, hapticsEnabled = hapticsEnabled, itemPaddingHorizontal = itemPaddingHorizontal, itemPaddingVertical = itemPaddingVertical, groupHeader = groupHeader, listState = listState, contentPadding = contentPadding, content = content)"
    ),
    level = DeprecationLevel.WARNING
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsColumn(
    modifier: Modifier = Modifier,
    topAppBarState: TopAppBarState?,
    hapticsEnabled: Boolean = true,
    itemPaddingHorizontal: Dp = 15.dp,
    itemPaddingVertical: Dp = 1.dp,
    groupHeader: (@Composable (String) -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: SettingsGraphBuilder.() -> Unit,
) {
    SettingsColumnInternal(
        modifier = modifier,
        topAppBarScrollBehavior = null,
        legacyTopAppBarState = topAppBarState,
        collapseTopAppBarOnHighlight = true,
        onHighlightScroll = null,
        hapticsEnabled = hapticsEnabled,
        itemPaddingHorizontal = itemPaddingHorizontal,
        itemPaddingVertical = itemPaddingVertical,
        groupHeader = groupHeader,
        listState = listState,
        contentPadding = contentPadding,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsColumnInternal(
    modifier: Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior?,
    legacyTopAppBarState: TopAppBarState?,
    collapseTopAppBarOnHighlight: Boolean,
    onHighlightScroll: (() -> Unit)?,
    hapticsEnabled: Boolean,
    itemPaddingHorizontal: Dp,
    itemPaddingVertical: Dp,
    groupHeader: (@Composable (String) -> Unit)?,
    listState: LazyListState,
    contentPadding: PaddingValues,
    content: SettingsGraphBuilder.() -> Unit,
) {
    val plan = SettingsGraphBuilder().apply(content).build().toRenderPlan()

    val graphState = LocalSettingGraphState.current
    val highlightState = graphState.highlightState
    val globalDefaults = graphState.onClickDefaults

    val targetIndex = plan.indexOfFirst {
        it is RenderEntry.Node && it.node.keyName == highlightState.activeKey
    }

    val blinkKey = remember { mutableStateOf<String?>(null) }
    val currentTargetIndex by rememberUpdatedState(targetIndex)

    LaunchedEffect(Unit) {
        snapshotFlow { highlightState.activeKey }
            .filterNotNull()
            .collect { key ->
                val matched = withTimeoutOrNull(HIGHLIGHT_MATCH_TIMEOUT_MS.milliseconds) {
                    snapshotFlow { currentTargetIndex }.first { it >= 0 }
                } != null
                if (!matched) return@collect

                val scrollTarget = currentTargetIndex

                blinkKey.value = key
                highlightState.clear()

                delay(HIGHLIGHT_SCROLL_DELAY_MS.milliseconds)

                if (scrollTarget >= 0) {
                    val layoutInfo = listState.layoutInfo
                    val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == scrollTarget }
                    val isFullyVisible = visibleItem != null &&
                            visibleItem.offset >= layoutInfo.viewportStartOffset &&
                            (visibleItem.offset + visibleItem.size) <= layoutInfo.viewportEndOffset

                    if (!isFullyVisible) {
                        resolveTopAppBarOffsetOnHighlight(
                            scrollBehavior = topAppBarScrollBehavior,
                            legacyState = legacyTopAppBarState,
                            collapseOnHighlight = collapseTopAppBarOnHighlight,
                            onHighlightScroll = onHighlightScroll,
                        )
                        val centerOffset = -(layoutInfo.viewportSize.height / 3)
                        listState.animateScrollToItem(scrollTarget, centerOffset)
                    }
                }

                delay(HIGHLIGHT_BLINK_DURATION_MS.milliseconds)
                blinkKey.value = null
            }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
    ) {
        plan.forEach { renderEntry ->
            when (renderEntry) {
                is RenderEntry.Header -> item(key = renderEntry.key) {
                    val headerText = renderEntry.titleRes
                        ?.let { stringResource(it) }
                        ?: renderEntry.titleString
                    if (groupHeader != null) {
                        groupHeader(headerText)
                    } else {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(
                                    start = HEADER_HORIZONTAL_PADDING,
                                    end = HEADER_HORIZONTAL_PADDING,
                                    top = HEADER_TOP_PADDING,
                                    bottom = HEADER_BOTTOM_PADDING,
                                )
                                .animateItem(),
                        )
                    }
                }

                is RenderEntry.Node -> item(key = renderEntry.key) {
                    val node = renderEntry.node
                    SettingsItemEntry(
                        nodeKey = node.key,
                        title = node.resolveTitle(),
                        description = node.resolveDescription(),
                        iconVector = node.iconVector,
                        iconResId = node.iconResId,
                        iconColors = node.resolveIconColors(),
                        shape = cardShapeForPosition(renderEntry.position, renderEntry.count),
                        isHighlighted = node.keyName == blinkKey.value,
                        experimentalFlagText = node.resolveExperimentalFlagText(),
                        behavior = node.behavior,
                        enabled = node.enabled(),
                        hapticsEnabled = hapticsEnabled,
                        isChecked = node.resolveIsChecked(globalDefaults),
                        selectedValue = node.resolveSelectedValue(globalDefaults),
                        itemPaddingHorizontal = itemPaddingHorizontal,
                        itemPaddingVertical = itemPaddingVertical,
                        onClick = node.onClickOverride,
                        onCheckedChange = node.onCheckedChangeOverride
                            ?: globalDefaults.onBooleanChanged ?: NoOpAnyBooleanCallback,
                        onIntChanged = node.onIntChangedOverride
                            ?: globalDefaults.onIntChanged ?: NoOpAnyIntCallback,
                    )
                }

                is RenderEntry.Raw -> item(key = renderEntry.key) {
                    Box(modifier = Modifier.animateItem()) {
                        renderEntry.content()
                    }
                }

                is RenderEntry.Divider -> item(key = renderEntry.key) {
                    HorizontalDivider(
                        modifier = Modifier
                            .animateItem()
                            .padding(
                                horizontal = DIVIDER_HORIZONTAL_PADDING,
                                vertical = DIVIDER_VERTICAL_PADDING,
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.SettingsItemEntry(
    nodeKey: Any,
    title: String,
    description: String,
    iconVector: ImageVector?,
    @DrawableRes iconResId: Int?,
    iconColors: SettingsItemIconColors,
    shape: CustomCardShape,
    isHighlighted: Boolean,
    experimentalFlagText: String,
    behavior: ItemBehavior,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    isChecked: Boolean,
    selectedValue: Int,
    itemPaddingHorizontal: Dp,
    itemPaddingVertical: Dp,
    onClick: ((Any) -> Unit)?,
    onCheckedChange: (Any, Boolean) -> Unit,
    onIntChanged: (Any, Int) -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val currentOnIntChanged by rememberUpdatedState(onIntChanged)

    val onClickLambda = remember(nodeKey, onClick != null) {
        if (onClick != null) {
            { currentOnClick?.invoke(nodeKey) ?: Unit }
        } else null
    }
    val onCheckedChangeLambda =
        remember(nodeKey) { { v: Boolean -> currentOnCheckedChange(nodeKey, v) } }
    val onValueChangeLambda = remember(nodeKey) { { v: Int -> currentOnIntChanged(nodeKey, v) } }

    SettingsItemView(
        modifier = Modifier
            .fillParentMaxWidth()
            .padding(
                horizontal = itemPaddingHorizontal,
                vertical = itemPaddingVertical
            )
            .animateItem(),
        title = title,
        description = description,
        icon = iconVector,
        iconResId = iconResId,
        iconColors = iconColors,
        shape = shape,
        isHighlighted = isHighlighted,
        experimentalFlagText = experimentalFlagText,
        behavior = behavior,
        enabled = enabled,
        hapticsEnabled = hapticsEnabled,
        isChecked = isChecked,
        selectedValue = selectedValue,
        onClick = onClickLambda,
        onCheckedChange = onCheckedChangeLambda,
        onValueChange = onValueChangeLambda,
    )
}

/**
 * One lazy list item in a rendered settings screen.
 *
 * The plan is built once per composition and drives both the highlight index lookup and the
 * [LazyColumn] body, so the two can never disagree about what is on screen.
 */
private sealed interface RenderEntry {
    val key: String

    class Header(
        override val key: String,
        @param:StringRes val titleRes: Int?,
        val titleString: String,
    ) : RenderEntry

    class Node(
        override val key: String,
        val node: SettingsNode,
        val position: Int,
        val count: Int,
    ) : RenderEntry

    class Raw(override val key: String, val content: @Composable () -> Unit) : RenderEntry

    class Divider(override val key: String) : RenderEntry
}

private fun SettingsGraph.toRenderPlan(): List<RenderEntry> {
    val plan = mutableListOf<RenderEntry>()
    val headerOccurrences = mutableMapOf<String, Int>()
    var dividerOrdinal = 0

    groups.forEach { group ->
        when (group) {
            is SettingsGraphGroup.Group -> plan.addGroup(group, headerOccurrences)

            is SettingsGraphGroup.RawItem -> plan.add(
                RenderEntry.Raw(key = group.key.toString(), content = group.content)
            )

            is SettingsGraphGroup.Divider -> {
                plan.add(RenderEntry.Divider(key = DIVIDER_KEY_PREFIX + dividerOrdinal))
                dividerOrdinal++
            }
        }
    }
    return plan
}

private fun MutableList<RenderEntry>.addGroup(
    group: SettingsGraphGroup.Group,
    headerOccurrences: MutableMap<String, Int>,
) {
    val visibleNodes = group.nodes.filter { it.isVisible() }
    if (visibleNodes.isEmpty()) return

    group.headerLabel()?.let { label ->
        val occurrence = headerOccurrences.getOrDefault(label, 0)
        headerOccurrences[label] = occurrence + 1
        add(
            RenderEntry.Header(
                key = HEADER_KEY_PREFIX + label + KEY_SEPARATOR + occurrence,
                titleRes = group.titleResId,
                titleString = group.titleString,
            )
        )
    }

    visibleNodes.forEachIndexed { position, node ->
        add(
            RenderEntry.Node(
                key = node.keyName,
                node = node,
                position = position,
                count = visibleNodes.size,
            )
        )
    }
}

private fun SettingsGraphGroup.Group.headerLabel(): String? = when {
    titleResId != null -> titleResId.toString()
    titleString.isNotEmpty() -> titleString
    else -> null
}

@Composable
private fun SettingsNode.resolveTitle(): String =
    dynamicTitle?.invoke() ?: staticTitleRes?.let { stringResource(it) } ?: staticTitleString

@Composable
private fun SettingsNode.resolveDescription(): String =
    dynamicDescription?.invoke() ?: staticDescRes?.let { stringResource(it) } ?: staticDescString

@Composable
private fun SettingsNode.resolveExperimentalFlagText(): String =
    experimentalFlagTextRes?.let { stringResource(it) } ?: experimentalFlagTextString

private fun SettingsNode.resolveIsChecked(defaults: OnClickDefaults): Boolean = when (behavior) {
    is ItemBehavior.Switch,
    is ItemBehavior.SwitchBanner -> (isCheckedOverride ?: defaults.isChecked ?: AlwaysFalse)(key)

    else -> false
}

private fun SettingsNode.resolveSelectedValue(defaults: OnClickDefaults): Int = when (behavior) {
    is ItemBehavior.RadioGroup,
    is ItemBehavior.ButtonGroup ->
        (selectedValueOverride ?: defaults.selectedValue ?: AlwaysNegativeOne)(key)

    else -> -1
}

@Composable
private fun SettingsNode.resolveIconColors(): SettingsItemIconColors =
    dynamicIconColors?.invoke() ?: SettingsItemDefaults.iconColors()
