# Usage

Initialize your settings screen using `rememberSettingsGraphState` to handle the global routing of preference changes.

## Basic Setup

```kotlin
val graphState = rememberSettingsGraphState {
    onBooleanChanged { key, newValue -> prefs.putBoolean(key.toString(), newValue) }
    onIntChanged { key, newValue -> prefs.putInt(key.toString(), newValue) }
    isChecked { key -> prefs.getBoolean(key.toString(), false) }
    selectedValue { key -> prefs.getInt(key.toString(), 0) }
}
```

## Building the Graph

Pass the state to `SettingsColumn` and construct your UI using the DSL builder blocks.

```kotlin
SettingsColumn(
    modifier = Modifier.fillMaxSize()
) {
    group("App Theme") {
        radioGroupItem("theme_mode") {
            options {
                option(0) { label("System Default") }
                option(1) { label("Light") }
                option(2) { label("Dark") }
            }
        }
    }

    group("Interactions") {
        switchItem("haptics") {
            title("Haptic Feedback")
            description("Vibrate on interactions")
            icon(Icons.Default.TouchApp)
        }
    }
}
```
