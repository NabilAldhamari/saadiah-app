plugins {
    id("saadiah.android.application")
}

android {
    namespace = "app.saadiah"

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore.jks").takeIf { it.exists() }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(project(":design"))
    implementation(project(":core:model"))
    implementation(project(":core:prayer"))
    implementation(project(":core:calendar"))
    implementation(project(":core:schedule"))
    implementation(project(":core:content"))
    implementation(project(":core:data"))

    testImplementation(kotlin("test"))
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
}
