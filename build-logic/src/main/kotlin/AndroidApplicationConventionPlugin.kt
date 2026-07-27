import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        extensions.configure(ApplicationExtension::class.java) {
            compileSdk = 35
            defaultConfig {
                minSdk = 26
                targetSdk = 35
                versionCode = Versions.code(target)
                versionName = Versions.name(target)
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
            buildFeatures {
                compose = true
                buildConfig = true
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                }
            }
            // Per-ABI APKs and an app bundle cannot be built in the same invocation: AGP
            // refuses with "Multiple shrunk-resources files found" because the shrinker has
            // produced one per split and a bundle expects exactly one. The release workflow
            // asks for both in a single ./gradlew call, so this turns splitting off whenever
            // a bundle is among the requested tasks.
            //
            // Splitting stays on for assembleRelease, because check-apk-size.sh measures the
            // arm64 APK and there would be no such file without it.
            val buildingABundle =
                gradle.startParameter.taskNames.any { it.contains("undle", ignoreCase = false) }
            splits {
                abi {
                    isEnable = !buildingABundle
                    reset()
                    include("arm64-v8a", "armeabi-v7a", "x86_64")
                    isUniversalApk = false
                }
            }
            packaging {
                jniLibs.useLegacyPackaging = false
            }
            lint {
                warningsAsErrors = true
                abortOnError = true
            }
        }
    }
}
