# Customization

Compose Settings Graph provides several methods for deep customization, ensuring your settings screen fits perfectly within your app's design language.

## Custom Slots

You can inject completely custom Composable sections anywhere inside your graph using the `item` block:

```kotlin
item("profile_header") {
    ProfileHeaderCard()
}
```

## Dynamic UI Conditioning

Elements can be dynamically shown, hidden, or disabled based on state, completely without triggering full graph recompositions.

```kotlin
clickableItem("clear_cache") {
    title("Clear App Cache")
    description("Frees up local storage space")
    icon(Icons.Default.Delete)
    enabled(!experimentalFeaturesActive)
    visible { developerModeEnabled }
}
```
