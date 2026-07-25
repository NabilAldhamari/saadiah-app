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
            splits {
                abi {
                    isEnable = true
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
