# Customization

For use-cases that exceed standard list configurations, the DSL exposes `item` block injection for rendering pure `Composable` structures directly within the hierarchical column stream. 

This enables the integration of custom spacer constraints, complex visual headers, or specialized layout controls that maintain standard `SettingsColumn` scroll positioning.

## Custom Composables

You can inject any external Composable directly into the node list. An arbitrary `key` string must be provided for the `LazyColumn` reconciliation engine.

```kotlin
SettingsColumn(
    modifier = Modifier.fillMaxWidth()
) {
    // Top Spacer
    item(key = "spacer_top") {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
        )
    }

    group(R.string.file_actions) {
        switchItem(SettingsKeys.SaveWholeOutput) {
            title(R.string.save_whole_output)
        }
    }
}
```

## Architectural Complex Layouts

Custom nodes can manage complex nested animations and graphic rendering without compromising the strict declarative nature of the DSL tree. 

Below is a production example of a specialized `SpinningGears` and custom header implementation:

```kotlin
SettingsColumn(...) {
    item("HEADER_ANIMATION") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(300.dp)
        ) {
            FloatingIconsBackground(...)
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    SpinningGears(modifier = Modifier.size(175.dp))
                }

                AutoResizeableText(
                    text = stringResource(R.string.settings),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displayLargeEmphasized,
                )
            }
        }
    }
    
    group {
        clickableItem(SettingsKeys.Behavior) { ... }
    }
}
```

## App Bar & Scroll Behavior Integration

`SettingsColumn` is designed to work seamlessly with any type of top app bar or custom header.

### Standard & Pinned App Bars

For standard single-row app bars (`TopAppBar`, `CenterAlignedTopAppBar`), simply apply `contentPadding` from `Scaffold`. Pass `topAppBarScrollBehavior` when using Material 3 scroll behaviors; pinned app bars are automatically detected and will never be collapsed off-screen during highlight navigation:

```kotlin
val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        TopAppBar(
            title = { Text("Settings") },
            scrollBehavior = scrollBehavior,
        )
    }
) { paddingValues ->
    SettingsColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
        topAppBarScrollBehavior = scrollBehavior,
        hapticsEnabled = true,
    ) {
        // ...
    }
}
```

### Collapsible Two-Row App Bars (`LargeTopAppBar` / `MediumTopAppBar`)

When using two-row app bars with `exitUntilCollapsedScrollBehavior`, you can optionally collapse the large title down to the pinned compact bar when scrolling to a highlighted search result:

```kotlin
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        LargeTopAppBar(
            title = { Text("Settings") },
            scrollBehavior = scrollBehavior,
        )
    }
) { paddingValues ->
    SettingsColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
        topAppBarScrollBehavior = scrollBehavior,
        collapseTopAppBarOnHighlight = true,
    ) {
        // ...
    }
}
```

### Custom & Static Headers

For custom headers, non-Material 3 components, or custom animation coordinators, use `onHighlightScroll` to trigger custom collapse or transition effects:

```kotlin
SettingsColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = paddingValues,
    onHighlightScroll = { customHeaderState.collapse() },
) {
    // ...
}
```
