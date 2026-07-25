plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.plugin.android)
    compileOnly(libs.plugin.kotlin)
    compileOnly(libs.plugin.detekt)
    compileOnly(libs.plugin.kover)
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "saadiah.kotlin.multiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("androidLibrary") {
            id = "saadiah.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "saadiah.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
