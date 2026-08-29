import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.changelog") version "2.5.0"
}

group = "dev.tailwindrainbow"
version = "0.1.0"

val minimumIntellijVersion = "2025.2"

val pluginVerificationTarget =
    providers
        .gradleProperty("pluginVerificationTarget")
        .orElse("baseline")

kotlin {
    jvmToolchain(21)

    compilerOptions {
        allWarningsAsErrors = true
    }
}

configurations.testRuntimeClasspath {
    exclude(
        group = "org.jetbrains.kotlin",
        module = "kotlin-stdlib",
    )
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")

    intellijPlatform {
        intellijIdeaCommunity(minimumIntellijVersion)
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
            untilBuild = provider { null }
        }

        /*
         * CHANGELOG.md remains the single source of release notes.
         *
         * Use the matching version when it exists. Otherwise, use the
         * Unreleased section so development builds still contain useful notes.
         */
        val pluginVersion = project.version.toString()

        changeNotes =
            changelog.instance.map { log ->
                val item =
                    if (log.has(pluginVersion)) {
                        log.get(pluginVersion)
                    } else {
                        log.unreleasedItem
                    } ?: error(
                        "CHANGELOG.md has no $pluginVersion section " +
                            "and no Unreleased section",
                    )

                log.renderItem(
                    item
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
    }

    pluginVerification {
        ides {
            when (val target = pluginVerificationTarget.get()) {
                "baseline" -> current()

                "latest" ->
                    latest {
                        types =
                            listOf(
                                IntelliJPlatformType.IntellijIdeaCommunity,
                            )

                        channels =
                            listOf(
                                ProductRelease.Channel.RELEASE,
                            )
                    }

                else ->
                    throw GradleException(
                        "Unsupported pluginVerificationTarget '$target'. " +
                            "Expected 'baseline' or 'latest'.",
                    )
            }
        }
    }

    signing {
        certificateChain =
            providers.environmentVariable("CERTIFICATE_CHAIN")

        privateKey =
            providers.environmentVariable("PRIVATE_KEY")

        password =
            providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token =
            providers.environmentVariable("PUBLISH_TOKEN")

        channels = listOf("default")
    }
}

changelog {
    repositoryUrl =
        "https://github.com/Jacobgh00/tailwindrainbow"
}

detekt {
    parallel = true
}

tasks {
    test {
        useJUnitPlatform()

        systemProperty(
            "junit.jupiter.extensions.autodetection.enabled",
            "false",
        )

        systemProperty("ide.slow.operations.assertion", "true")

        systemProperty(
            "java.util.prefs.userRoot",
            layout
                .buildDirectory
                .dir("test-prefs")
                .get()
                .asFile
                .absolutePath,
        )
    }
}
