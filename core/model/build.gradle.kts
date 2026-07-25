plugins {
    id("saadiah.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        getByName("commonMain").dependencies {
            api(libs.kotlinx.datetime)
        }
    }
}
