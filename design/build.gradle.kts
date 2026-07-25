plugins {
    id("saadiah.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "app.saadiah.design"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(project(":core:model"))

    testImplementation(kotlin("test"))
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
