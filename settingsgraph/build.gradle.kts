
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.stability.analyzer)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "in.hridayan.settingsgraph"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.annotation)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.dp-hridayan",
        artifactId = "compose-settings-graph",
        version = "1.1.1"
    )

    pom {
        name.set("Compose Settings Graph")
        description.set("A declarative, navigation-inspired DSL for building dynamic settings screens and in-memory search in Jetpack Compose.")
        inceptionYear.set("2026")
        url.set("https://github.com/DP-Hridayan/Compose-Settings-Graph")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("DP-Hridayan")
                name.set("Hridayan")
                email.set("hridayanofficial@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/DP-Hridayan/Compose-Settings-Graph")
            connection.set("scm:git:git://github.com/DP-Hridayan/Compose-Settings-Graph.git")
            developerConnection.set("scm:git:ssh://git@github.com/DP-Hridayan/Compose-Settings-Graph.git")
        }
    }

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}
