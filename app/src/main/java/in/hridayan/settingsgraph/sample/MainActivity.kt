package `in`.hridayan.settingsgraph.sample

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import `in`.hridayan.settingsgraph.ui.LocalSettingGraphState
import `in`.hridayan.settingsgraph.ui.SettingsColumn
import `in`.hridayan.settingsgraph.ui.rememberSettingsGraphState

/** A generic preference wrapper for the demo app */
class AppPreferences(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val defaultBooleans = mapOf(
        "dynamic_color" to (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S),
        "haptics" to true
    )

    private val defaultInts = mapOf(
        "theme_mode" to 0,
        "view_mode" to 0
    )

    fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    fun getBoolean(key: String): Boolean = prefs.getBoolean(key, defaultBooleans[key] ?: false)

    fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }
    fun getInt(key: String): Int = prefs.getInt(key, defaultInts[key] ?: 0)

    fun clear() = prefs.edit { clear() }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefs = remember { AppPreferences(context) }

            var updateTrigger by remember { mutableIntStateOf(0) }
            DisposableEffect(prefs) {
                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> updateTrigger++ }
                prefs.prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }

            val themeMode = updateTrigger.let { prefs.getInt("theme_mode") }
            val useDynamicColor = updateTrigger.let { prefs.getBoolean("dynamic_color") }
            val enableHaptics = updateTrigger.let { prefs.getBoolean("haptics") }
            val developerMode = updateTrigger.let { prefs.getBoolean("developer_mode") }
            val experimentalFeatures =
                updateTrigger.let { prefs.getBoolean("experimental_features") }

            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            val colorScheme = when {
                useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }

                isDark -> darkColorScheme()
                else -> lightColorScheme()
            }

            val graphState = rememberSettingsGraphState {
                onBooleanChanged { key, newValue -> prefs.putBoolean(key.toString(), newValue) }
                onIntChanged { key, newValue -> prefs.putInt(key.toString(), newValue) }
                isChecked { key -> prefs.getBoolean(key.toString()) }
                selectedValue { key -> prefs.getInt(key.toString()) }
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CompositionLocalProvider(LocalSettingGraphState provides graphState) {
                        Scaffold(
                            topBar = {
                                TopAppBar(title = { Text("Settings Graph Demo") })
                            }
                        ) { padding ->
                            SettingsColumn(
                                contentPadding = padding,
                                modifier = Modifier.fillMaxSize(),
                                hapticsEnabled = enableHaptics
                            ) {
                                item("profile_header") {
                                    ProfileHeader()
                                }

                                group("App Theme") {
                                    radioGroupItem("theme_mode") {
                                        options {
                                            option(0) { label("System Default") }
                                            option(1) { label("Light") }
                                            option(2) { label("Dark") }
                                        }
                                    }
                                }

                                group("Dynamic Theme") {
                                    switchItem("dynamic_color") {
                                        title("Dynamic Color")
                                        description("Use Android 12+ wallpaper colors for the theme")
                                        icon(Icons.Default.Palette)
                                        visible { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }
                                    }
                                }

                                group("View Layout") {
                                    buttonGroupItem("view_mode") {
                                        options {
                                            option(0) {
                                                label("List")
                                                icon(Icons.AutoMirrored.Filled.ViewList)
                                            }
                                            option(1) {
                                                label("Grid")
                                                icon(Icons.AutoMirrored.Filled.ViewList)
                                            }
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

                                group("Developer") {
                                    switchItem("developer_mode") {
                                        title("Developer Mode")
                                        description("Unlock advanced settings for developers")
                                        icon(Icons.Default.Build)
                                    }

                                    switchItem("experimental_features") {
                                        title("Experimental Features")
                                        description("Enable unstable new features that might crash")
                                        icon(Icons.Default.Science)
                                        experimentalFlagText("Alpha")
                                        visible { developerMode }
                                        onClick {
                                            prefs.putBoolean(
                                                "experimental_features",
                                                !experimentalFeatures
                                            )
                                        }
                                    }

                                    clickableItem("clear_cache") {
                                        title("Clear App Preferences")
                                        description("Revert settings back to defaults")
                                        icon(Icons.Default.Delete)
                                        enabled(experimentalFeatures)
                                        visible { developerMode }
                                        onClick {
                                            prefs.clear()
                                            Toast.makeText(
                                                context,
                                                "Preferences cleared!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    clickableItem("app_info") {
                                        title("App Info")
                                        description("Version 1.0.0")
                                        icon(Icons.Default.Info)
                                        onClick {
                                            Toast.makeText(
                                                context,
                                                "aShellYou Settings DSL Demo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Welcome to aShellYou",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "This is a custom item{} slot rendered perfectly inside the graph!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}







