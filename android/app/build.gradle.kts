plugins {
    id("saadiah.android.application")
}

android {
    // The Kotlin package, and what relative class names in the manifest resolve against.
    // It stays app.saadiah because that is where the source actually lives.
    namespace = "app.saadiah"

    defaultConfig {
        // What Play knows the app as, and the only identifier a user's device ever sees.
        // It is deliberately not the namespace: the listing was created as com.saadiah, and
        // renaming every source package to match would be a large diff to change one string
        // that Gradle already models separately.
        applicationId = "com.saadiah"
    }

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
            // The keystore alone is not enough to sign with — the passwords come from the
            // environment and only the release workflow and the maintainer have them. Keying
            // off the file alone meant that once a keystore existed in a working tree, every
            // release build there failed on a missing storePassword, including the ones the
            // guards need: check-apk-size and check-forbidden-strings both read an assembled
            // release APK, and neither cares whether it was signed.
            //
            // An unsigned build announces itself — AGP names the output *-release-unsigned.apk
            // — and Play refuses unsigned uploads, so a forgotten variable cannot get past
            // either. Silently unsigned is recoverable; unbuildable is not.
            signingConfig =
                signingConfigs
                    .getByName("release")
                    .takeIf { it.storeFile != null && it.storePassword != null }
        }
    }

    testOptions {
        unitTests {
            // Robolectric shadows what the alarm tests exercise; the remaining android.jar
            // stubs should return defaults rather than throw.
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

// The Compose test manifest that hosts a composable under test is a debug artefact, so the
// release unit test cannot run the same suite and there is nothing gained by trying.
androidComponents {
    beforeVariants(selector().withBuildType("release")) { variant ->
        variant.enableUnitTest = false
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
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
}
