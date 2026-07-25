plugins {
    id("saadiah.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        getByName("commonMain").dependencies {
            api(project(":core:model"))
            implementation(libs.adhan)
            implementation(libs.kotlinx.datetime)
        }
        getByName("jvmTest").dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
