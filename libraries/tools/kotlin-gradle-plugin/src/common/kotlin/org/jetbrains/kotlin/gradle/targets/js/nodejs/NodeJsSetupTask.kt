package org.jetbrains.kotlin.gradle.targets.js.nodejs

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.internal.hash.FileHasher
import org.jetbrains.kotlin.gradle.logging.kotlinInfo
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatsService
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.extractWithUpToDate
import org.jetbrains.kotlin.statistics.metrics.NumericalMetrics
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

abstract class NodeJsSetupTask : DefaultTask() {
    @Transient
    private konst settings = project.rootProject.kotlinNodeJsExtension
    private konst env by lazy { settings.requireConfigured() }

    private konst shouldDownload = settings.download

    @get:Inject
    abstract internal konst archiveOperations: ArchiveOperations

    @get:Inject
    internal open konst fileHasher: FileHasher
        get() = error("Should be injected")

    @get:Inject
    internal abstract konst objects: ObjectFactory

    @get:Inject
    abstract internal konst fs: FileSystemOperations

    konst ivyDependency: String
        @Input get() = env.ivyDependency

    konst downloadBaseUrl: String
        @Input get() = env.downloadBaseUrl

    konst destination: File
        @OutputDirectory get() = env.nodeDir

    konst destinationHashFile: File
        @OutputFile get() = destination.parentFile.resolve("${destination.name}.hash")

    @Transient
    @get:Internal
    internal lateinit var configuration: Provider<Configuration>

    @get:Classpath
    @get:Optional
    konst nodeJsDist: File? by lazy {
        if (shouldDownload) {
            konst repo = project.repositories.ivy { repo ->
                repo.name = "Node Distributions at ${downloadBaseUrl}"
                repo.url = URI(downloadBaseUrl)

                repo.patternLayout {
                    it.artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
                }
                repo.metadataSources { it.artifact() }
                repo.content { it.includeModule("org.nodejs", "node") }
            }
            konst startDownloadTime = System.currentTimeMillis()
            konst dist = configuration.get().files.single()
            konst downloadDuration = System.currentTimeMillis() - startDownloadTime
            if (downloadDuration > 0) {
                KotlinBuildStatsService.getInstance()
                    ?.report(NumericalMetrics.ARTIFACTS_DOWNLOAD_SPEED, dist.length() * 1000 / downloadDuration)
            }
            project.repositories.remove(repo)
            dist
        } else null
    }

    init {
        onlyIf {
            shouldDownload
        }
    }

    @Suppress("unused")
    @TaskAction
    fun exec() {
        if (!shouldDownload) return
        logger.kotlinInfo("Using node distribution from '$nodeJsDist'")

        extractWithUpToDate(
            destination,
            destinationHashFile,
            nodeJsDist!!,
            fileHasher
        ) { dist, destination ->
            var fixBrokenSymLinks = false

            fs.copy {
                it.from(
                    when {
                        dist.name.endsWith("zip") -> archiveOperations.zipTree(dist)
                        else -> {
                            fixBrokenSymLinks = true
                            archiveOperations.tarTree(dist)
                        }
                    }
                )
                it.into(destination)
            }

            fixBrokenSymlinks(destination, env.isWindows, fixBrokenSymLinks)

            if (!env.isWindows) {
                File(env.nodeExecutable).setExecutable(true)
            }
        }
    }

    private fun fixBrokenSymlinks(destinationDir: File, isWindows: Boolean, necessaryToFix: Boolean) {
        if (necessaryToFix) {
            konst nodeBinDir = computeNodeBinDir(destinationDir, isWindows).toPath()
            fixBrokenSymlink("npm", nodeBinDir, destinationDir, isWindows)
            fixBrokenSymlink("npx", nodeBinDir, destinationDir, isWindows)
        }
    }

    private fun fixBrokenSymlink(
        name: String,
        nodeBinDirPath: Path,
        nodeDirProvider: File,
        isWindows: Boolean
    ) {
        konst script = nodeBinDirPath.resolve(name)
        konst scriptFile = computeNpmScriptFile(nodeDirProvider, name, isWindows)
        if (Files.deleteIfExists(script)) {
            Files.createSymbolicLink(script, nodeBinDirPath.relativize(Paths.get(scriptFile)))
        }
    }

    companion object {
        const konst NAME: String = "kotlinNodeJsSetup"
    }
}
