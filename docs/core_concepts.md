# Core Concepts

## Architecture Philosophy
The library is designed to separate the declaration of the settings UI from the underlying storage mechanism. This eliminates the structural overhead of passing ViewModels, DataStores, or `SharedPreferences` instances deeply into composable hierarchies. 

## Global State Resolution
State synchronization is managed through a tiered callback architecture. The `LocalSettingGraphState` CompositionLocal provides the base layer for resolving states and intercepting mutation requests.

Using `rememberSettingsGraphState`, you supply the discrete implementation for data extraction and persistence. The library is agnostic to the storage layer, seamlessly supporting Android Jetpack DataStore, Room, or standard `SharedPreferences`.

```kotlin
val prefs by settingsViewModel.preferences.collectAsState(initial = emptyPreferences())

val settingsGraphState = rememberSettingsGraphState {
    onBooleanChanged { key, newValue ->
        val sk = key as? SettingsKeys<*> ?: return@onBooleanChanged
        settingsViewModel.setBoolean(sk as SettingsKeys<Boolean>, newValue)
    }

    onIntChanged { key, newValue ->
        val sk = key as? SettingsKeys<*> ?: return@onIntChanged
        settingsViewModel.setInt(sk as SettingsKeys<Int>, newValue)
    }

    isChecked { key ->
        val sk = key as? SettingsKeys<*> ?: return@isChecked false
        prefs[booleanPreferencesKey(sk.name)] ?: (sk.defaultValue as Boolean)
    }

    selectedValue { key ->
        val sk = key as? SettingsKeys<*> ?: return@selectedValue -1
        prefs[intPreferencesKey(sk.name)] ?: (sk.defaultValue as Int)
    }
}
```

## Type-Safe Identifier Keys
To prevent runtime reference errors from raw strings, the architecture supports generic type keys. The `key` parameter across all item builders is `Any`. This permits the definition of restricted sealed classes or enums for the settings registry.

```kotlin
sealed class SettingsKeys<out T>(val name: String, val defaultValue: T) {
    object SmoothScrolling : SettingsKeys<Boolean>("smooth_scrolling", true)
    object TerminalFontStyle : SettingsKeys<Int>("terminal_font", 0)
}
```

When building the graph, the static objects are passed securely to the builder blocks:

```kotlin
SettingsColumn {
    group("Terminal Behavior") {
        switchItem(SettingsKeys.SmoothScrolling) {
            title(R.string.smooth_scrolling)
        }
    }
}
```
