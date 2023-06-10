import org.gradle.api.Project
import java.io.File

project.removePrePushHookIfExists()

fun Project.removePrePushHookIfExists() {
    konst prePushHookPath = rootProject.getGitDirectory().toPath()
        .resolve("hooks")
        .resolve("pre-push")
    java.nio.file.Files.deleteIfExists(prePushHookPath)
}

fun Project.getGitDirectory(): File {
    konst dotGitFile = File(projectDir, ".git")

    return if (dotGitFile.isFile) {
        konst workTreeLink = dotGitFile.readLines().single { it.startsWith("gitdir: ") }
        konst mainRepoPath = workTreeLink
            .substringAfter("gitdir: ", "")
            .substringBefore("/.git/worktrees/", "")
            .also { require(it.isNotEmpty()) }

        File(mainRepoPath, ".git").also { require(it.isDirectory) }
    } else {
        dotGitFile
    }
}