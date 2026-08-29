import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.changelog") version "2.5.0"
}

group = "dev.tailwindrainbow"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")

    intellijPlatform {
        intellijIdeaCommunity("2025.2.6.2")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }
}

intellijPlatform {
    buildSearchableOptions = false

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

// CHANGELOG.md is the single source of the release notes. Keeping them out of plugin.xml means
// the Marketplace "What's New" tab cannot drift from the file reviewers actually read.
changelog {
    repositoryUrl = "https://github.com/Jacobgh00/tailwindrainbow"
}

tasks {
    test {
        useJUnitPlatform()
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "false")
        systemProperty("java.util.prefs.userRoot", layout.buildDirectory.dir("test-prefs").get().asFile.absolutePath)
    }

    patchPluginXml {
        // Reads through changelog.instance rather than the extension itself: the extension is a
        // script object, and capturing one in a provider breaks the configuration cache.
        //
        // Falls back to the Unreleased section so a build cut before the version is stamped still
        // carries notes, rather than shipping an empty What's New tab.
        val pluginVersion = project.version.toString()
        changeNotes =
            changelog.instance.map { log ->
                val item =
                    (if (log.has(pluginVersion)) log.get(pluginVersion) else log.unreleasedItem)
                        ?: error("CHANGELOG.md has no $pluginVersion section and no Unreleased section")
                log.renderItem(
                    item.withHeader(false).withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }

        sinceBuild.set("252")
        // No upper bound: the plugin uses only stable platform API (one Annotator, one
        // Configurable). Setting untilBuild would stop it loading on the next IDE release
        // until a new version shipped, for no benefit.
        untilBuild.set(provider { null })
    }
}
