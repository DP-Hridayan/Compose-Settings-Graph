# Compose Settings Graph

A declarative, type-safe DSL for building beautiful, perfectly reactive settings screens in Jetpack Compose.

## 📖 [Read the Full Documentation](https://dp-hridayan.github.io/compose-settings-graph/)

### Quick Start

```kotlin
SettingsColumn(modifier = Modifier.fillMaxSize()) {
    group("App Theme") {
        radioGroupItem("theme_mode") {
            options {
                option(0) { label("System Default") }
                option(1) { label("Light") }
                option(2) { label("Dark") }
            }
        }
    }
}
```
