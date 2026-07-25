import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("io.gitlab.arturbosch.detekt")
        pluginManager.apply("org.jetbrains.kotlinx.kover")

        extensions.configure(KotlinMultiplatformExtension::class.java) {
            jvmToolchain(Versions.JVM_TOOLCHAIN)
            jvm()

            sourceSets.apply {
                getByName("commonTest").dependencies {
                    implementation(kotlin("test"))
                }
            }

            targets.configureEach {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            allWarningsAsErrors.set(true)
                        }
                    }
                }
            }
        }
    }
}
