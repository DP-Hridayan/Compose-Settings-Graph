# Compose Settings Graph

A declarative, type-safe DSL for constructing strictly reactive settings screens in Jetpack Compose. 

Unlike traditional implementations that recompose lists upon state changes, Compose Settings Graph utilizes an internally hoisted layout strategy. Values, interaction states, and themes update locally at the item level. The DSL graph is evaluated dynamically, ensuring minimal recomposition overhead while supporting real-time visibility conditioning and global search indexing.

## Installation

Declare the dependency in your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.dp-hridayan:compose-settings-graph:{{ library_version() }}")
}
```

## Quick Start

Initialize the global routing state, and construct the UI using the `SettingsColumn` DSL.

```kotlin
val graphState = rememberSettingsGraphState {
    onBooleanChanged { key, newValue -> prefs.putBoolean(key.toString(), newValue) }
    onIntChanged { key, newValue -> prefs.putInt(key.toString(), newValue) }
    isChecked { key -> prefs.getBoolean(key.toString(), false) }
    selectedValue { key -> prefs.getInt(key.toString(), 0) }
}

CompositionLocalProvider(LocalSettingGraphState provides graphState) {
    SettingsColumn(modifier = Modifier.fillMaxSize()) {
        group("Application Theme") {
            radioGroupItem("theme_mode") {
                options(
                    RadioButtonOption(0, "System Default"),
                    RadioButtonOption(1, "Light"),
                    RadioButtonOption(2, "Dark")
                )
            }
        }
    }
}
```
