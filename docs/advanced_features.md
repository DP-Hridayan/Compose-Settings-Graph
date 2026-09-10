# Advanced Features

## Dynamic Property Evaluation

The DSL evaluates block conditions at runtime, preventing the need for complex reactive architecture when updating UI states.

### Visibility
To dynamically insert or remove an item based on remote configurations or contextual flags, pass a pure evaluation lambda to `visible`. The background parsing engine excludes disabled items from the global search index entirely.

```kotlin
clickableItem(SettingsKeys.CloudModels) {
    title(R.string.ai_models)
    icon(Icons.Outlined.AutoAwesome)
    visible { FeatureConfig.isAiEnabled }
    onClick { navController.navigate(NavRoutes.AiModelsScreen) }
}
```

### State Enabling
Control interaction access based on dependent states. Disabled items automatically apply a generic `0.5f` alpha layout overlay and intercept touch events.

```kotlin
switchItem(SettingsKeys.AdvancedAnalytics) {
    title(R.string.advanced_analytics)
    enabled { FeatureConfig.isTelemetryActive }
}
```

## Manual Event Overrides

By default, the `SwitchItem` and `SwitchBannerItem` delegate their touch interactions exclusively to the global `onBooleanChanged` callback. 

However, you can specify an `onClick` override on the item to assume total manual control over the interaction event. When an `onClick` block is present on a switch node, both the row click and the switch thumb click will execute the override, bypassing the global change emitter.

```kotlin
switchItem(SettingsKeys.DevMode) {
    title("Developer Mode")
    onClick {
        // Evaluate condition before mutating state
        if (!hasSufficientPermissions()) {
            dialogManager.show(WarningDialog)
        } else {
            // Trigger manual state mutation
            settingsViewModel.setBoolean(SettingsKeys.DevMode, true)
        }
    }
}
```

## Badges

Render localized visual indicators for specific configurations using `experimentalFlagText`.

```kotlin
switchItem(SettingsKeys.BetaRenderer) {
    title("Use Vulkun Engine")
    experimentalFlagText("Beta")
}
```
