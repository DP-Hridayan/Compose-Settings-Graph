# Item Types

The DSL provides distinct builder blocks for rendering common UI components. All builders support property configurations for titles, descriptions, and leading icons. Text nodes can be supplied as raw strings or localized XML resource IDs (`@StringRes`, `@DrawableRes`).

## Groups
`group` acts as the structural container for all child nodes. Items within a group are visually structured inside adjacent bounded cards. The group accepts an optional header string or string resource.

```kotlin
group(R.string.local_adb_shell) {
    // Child items
}
```

## Interactive Elements

### Clickable Item
Renders a standard row intended for navigation or discrete action triggering.

```kotlin
clickableItem(SettingsKeys.OutputSaveDirectory) {
    title(R.string.configure_save_directory)
    description(R.string.des_configure_save_directory)
    icon(R.drawable.ic_directory)
    onClick { dialogManager.show(SettingsDialogKey.ConfigureSaveDir) }
}
```

### Switch Item
Renders a standard row with a trailing `Switch` composable.

```kotlin
switchItem(SettingsKeys.DisableSoftKeyboard) {
    title(R.string.disable_softkey)
    description(R.string.des_disable_softkey)
    icon(R.drawable.ic_disable_keyboard)
}
```

### Switch Banner
Renders a high-emphasis, pill-shaped banner designed for primary feature toggles. Modifies the underlying background color scheme to `primaryContainer`.

```kotlin
switchBannerItem(SettingsKeys.MasterFeatureToggle) {
    title("Enable advanced tools")
}
```

## Selection Elements

### Radio Group
Renders a vertically stacked list of cards containing `RadioButton` instances. The system tracks internal selection through the active `selectedValue`.

```kotlin
radioGroupItem(SettingsKeys.LocalAdbWorkingMode) {
    options(
        RadioButtonOption(0, R.string.mode_standard),
        RadioButtonOption(1, R.string.mode_legacy),
        RadioButtonOption(2, R.string.mode_compatibility)
    )
}
```

### Button Group
Renders a segmented horizontal control layout using interconnected `ToggleButton` instances. Useful for compact selection enumerations.

```kotlin
buttonGroupItem(SettingsKeys.TerminalFontStyle) {
    options(
        ButtonGroupOption(TerminalFontStyle.MONOSPACE, R.string.monospace),
        ButtonGroupOption(TerminalFontStyle.SYSTEM_FONT, R.string.system_font),
    )
}
```
