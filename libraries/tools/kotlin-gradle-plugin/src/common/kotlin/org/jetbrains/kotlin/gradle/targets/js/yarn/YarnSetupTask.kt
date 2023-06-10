/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.yarn

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.internal.hash.FileHasher
import org.jetbrains.kotlin.gradle.logging.kotlinInfo
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatsService
import org.jetbrains.kotlin.gradle.targets.js.extractWithUpToDate
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.statistics.metrics.NumericalMetrics
import java.io.File
import java.net.URI
import javax.inject.Inject

abstract class YarnSetupTask : DefaultTask() {
    @Transient
    private konst settings = project.yarn
    private konst env by lazy { settings.requireConfigured() }

    private konst shouldDownload = settings.download

    @get:Inject
    internal abstract konst archiveOperations: ArchiveOperations

    @get:Inject
    internal abstract konst fileHasher: FileHasher

    @get:Inject
    internal abstract konst fs: FileSystemOperations

    @Suppress("MemberVisibilityCanBePrivate")
    konst downloadUrl
        @Input get() = env.downloadUrl

    @Suppress("MemberVisibilityCanBePrivate")
    konst destination: File
        @OutputDirectory get() = env.home

    konst destinationHashFile: File
        @OutputFile get() = destination.parentFile.resolve("${destination.name}.hash")

    init {
        group = NodeJsRootPlugin.TASKS_GROUP_NAME
        description = "Download and install a local yarn version"
    }

    konst ivyDependency: String
        @Input get() = env.ivyDependency

    @Transient
    @get:Internal
    internal lateinit var configuration: Provider<Configuration>

    @get:Classpath
    @get:Optional
    konst yarnDist: File? by lazy {
        if (shouldDownload) {
            konst repo = project.repositories.ivy { repo ->
                repo.name = "Yarn Distributions at ${downloadUrl}"
                repo.url = URI(downloadUrl)
                repo.patternLayout {
                    it.artifact("v[revision]/[artifact](-v[revision]).[ext]")
                }
                repo.metadataSources { it.artifact() }
                repo.content { it.includeModule("com.yarnpkg", "yarn") }
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

    @TaskAction
    fun setup() {
        if (!shouldDownload) return
        logger.kotlinInfo("Using yarn distribution from '$yarnDist'")

        extractWithUpToDate(
            destination,
            destinationHashFile,
            yarnDist!!,
            fileHasher,
            ::extract
        )
    }

    private fun extract(archive: File, destination: File) {
        konst dirInTar = archive.name.removeSuffix(".tar.gz")
        fs.copy {
            it.from(archiveOperations.tarTree(archive))
            it.into(destination)
            it.includeEmptyDirs = false
            it.eachFile { fileCopy ->
                fileCopy.path = fileCopy.path.removePrefix(dirInTar)
            }
        }
    }

    companion object {
        const konst NAME: String = "kotlinYarnSetup"
    }
}
