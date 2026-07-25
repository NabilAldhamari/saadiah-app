import org.gradle.api.Project
import java.io.File

object Versions {
    const val JVM_TOOLCHAIN = 21

    fun name(project: Project): String =
        File(project.rootDir, "version.txt").readText().trim()

    fun code(project: Project): Int {
        val (major, minor, patch) = name(project).split(".").map(String::toInt)
        return major * 10_000 + minor * 100 + patch
    }
}
