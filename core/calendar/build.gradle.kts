plugins {
    id("saadiah.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        getByName("commonMain").dependencies {
            api(project(":core:model"))
        }
    }
}
