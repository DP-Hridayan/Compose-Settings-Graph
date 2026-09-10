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

## Scroll Behavior Integration

The `SettingsColumn` is fully compatible with Material 3 nested scrolling protocols. Pass a `TopAppBarState` instance directly into the root configuration to synchronize list displacement vectors with custom collapsed app bars.

```kotlin
val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = { TopAppBar(...) }
) { paddingValues ->
    SettingsColumn(
        modifier = Modifier,
        contentPadding = paddingValues,
        topAppBarState = scrollBehavior.state,
        hapticsEnabled = true,
    ) {
        // ...
    }
}
```
