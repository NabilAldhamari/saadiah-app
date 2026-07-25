import com.diffplug.gradle.spotless.SpotlessExtension
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

val appVersion: String = file("version.txt").readText().trim()
val ktlintVersion: String = libs.versions.ktlint.get()
val detektConfigFile = files("$rootDir/config/detekt.yml")

allprojects {
    version = appVersion
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint(ktlintVersion)
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(ktlintVersion)
        }
    }

    plugins.withId("io.gitlab.arturbosch.detekt") {
        extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            config.setFrom(detektConfigFile)
            buildUponDefaultConfig = true
            parallel = true
        }
    }
}

dependencies {
    kover(project(":core:model"))
    kover(project(":core:prayer"))
    kover(project(":core:calendar"))
    kover(project(":core:schedule"))
    kover(project(":core:content"))
    kover(project(":core:data"))
}

kover {
    reports {
        filters {
            excludes {
                packages("*.ui", "*.di")
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule("core line coverage") {
                bound {
                    minValue = 90
                    coverageUnits = CoverageUnit.LINE
                }
            }
            rule("core branch coverage") {
                bound {
                    minValue = 85
                    coverageUnits = CoverageUnit.BRANCH
                }
            }
        }
    }
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(ktlintVersion)
    }
}
