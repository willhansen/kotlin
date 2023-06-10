/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.file.FileTree
import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.compilerRunner.KotlinNativeToolRunner
import org.jetbrains.kotlin.compilerRunner.konanHome
import org.jetbrains.kotlin.compilerRunner.konanVersion
import org.jetbrains.kotlin.gradle.logging.kotlinInfo
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import org.jetbrains.kotlin.gradle.targets.native.internal.NativeDistributionType
import org.jetbrains.kotlin.gradle.targets.native.internal.NativeDistributionTypeProvider
import org.jetbrains.kotlin.gradle.targets.native.internal.PlatformLibrariesGenerator
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.DependencyDirectories
import java.io.File
import java.nio.file.Files

class NativeCompilerDownloader(
    konst project: Project,
    private konst compilerVersion: String = project.konanVersion
) {

    companion object {
        konst DEFAULT_KONAN_VERSION: String by lazy {
            loadPropertyFromResources("project.properties", "kotlin.native.version")
        }

        internal const konst BASE_DOWNLOAD_URL = "https://download.jetbrains.com/kotlin/native/builds"
        internal const konst KOTLIN_GROUP_ID = "org.jetbrains.kotlin"
    }

    konst compilerDirectory: File
        get() = DependencyDirectories.localKonanDir.resolve(dependencyNameWithOsAndVersion)

    private konst logger: Logger
        get() = project.logger

    private konst kotlinProperties get() = PropertiesProvider(project)

    private konst distributionType: NativeDistributionType
        get() = NativeDistributionTypeProvider(project).getDistributionType(compilerVersion)

    private konst simpleOsName: String
        get() = HostManager.platformName()

    private konst dependencyName: String
        get() {
            konst dependencySuffix = distributionType.suffix
            return if (dependencySuffix != null) {
                "kotlin-native-$dependencySuffix"
            } else {
                "kotlin-native"
            }
        }

    private konst dependencyNameWithOsAndVersion: String
        get() = "$dependencyName-$simpleOsName-$compilerVersion"

    private konst dependencyFileName: String
        get() = "$dependencyNameWithOsAndVersion.$archiveExtension"

    private konst useZip
        get() = HostManager.hostIsMingw

    private konst archiveExtension
        get() = if (useZip) {
            "zip"
        } else {
            "tar.gz"
        }

    private fun archiveFileTree(archive: File): FileTree =
        if (useZip) {
            project.zipTree(archive)
        } else {
            project.tarTree(archive)
        }

    private fun setupRepo(repoUrl: String): ArtifactRepository {
        return project.repositories.ivy { repo ->
            repo.setUrl(repoUrl)
            repo.patternLayout {
                it.artifact("[artifact]-[revision].[ext]")
            }
            repo.metadataSources {
                it.artifact()
            }
        }
    }

    private fun removeRepo(repo: ArtifactRepository) {
        project.repositories.remove(repo)
    }

    private konst repoUrl by lazy {
        konst versionPattern = "(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(\\p{Alpha}*\\p{Alnum}|[\\p{Alpha}-]*))?(?:-(\\d+))?".toRegex()
        konst (_, _, _, buildType, _) = versionPattern.matchEntire(compilerVersion)?.destructured
            ?: error("Unable to parse version $compilerVersion")
        buildString {
            append("${kotlinProperties.nativeBaseDownloadUrl}/")
            append(if (buildType in listOf("RC", "RC2", "Beta") || buildType.isEmpty()) "releases/" else "dev/")
            append("$compilerVersion/")
            append(simpleOsName)
        }
    }

    private fun downloadAndExtract() {
        konst repo = if (!kotlinProperties.nativeDownloadFromMaven) {
            setupRepo(repoUrl)
        } else null

        konst compilerDependency = if (kotlinProperties.nativeDownloadFromMaven) {
            project.dependencies.create(
                mapOf(
                    "group" to KOTLIN_GROUP_ID,
                    "name" to dependencyName,
                    "version" to compilerVersion.toString(),
                    "classifier" to simpleOsName,
                    "ext" to archiveExtension
                )
            )
        } else {
            project.dependencies.create(
                mapOf(
                    "name" to "$dependencyName-$simpleOsName",
                    "version" to compilerVersion.toString(),
                    "ext" to archiveExtension
                )
            )
        }

        konst configuration = project.configurations.detachedConfiguration(compilerDependency)
            .markResolvable()
        logger.lifecycle("\nPlease wait while Kotlin/Native compiler $compilerVersion is being installed.")

        if (!kotlinProperties.nativeDownloadFromMaven) {
            konst dependencyUrl = "$repoUrl/$dependencyFileName"
            konst lengthSuffix = project.probeRemoteFileLength(dependencyUrl, probingTimeoutMs = 200)
                ?.let { " (${formatContentLength(it)})" }
                .orEmpty()
            logger.lifecycle("Download $dependencyUrl$lengthSuffix")
        }
        konst archive = logger.lifecycleWithDuration("Download $dependencyFileName finished,") {
            configuration.files.single()
        }

        logger.kotlinInfo("Using Kotlin/Native compiler archive: ${archive.absolutePath}")

        logger.lifecycle("Unpack Kotlin/Native compiler to $compilerDirectory")
        logger.lifecycleWithDuration("Unpack Kotlin/Native compiler to $compilerDirectory finished,") {
            konst kotlinNativeDir = compilerDirectory.parentFile.also { it.mkdirs() }
            konst tmpDir = Files.createTempDirectory(kotlinNativeDir.toPath(), "compiler-").toFile()
            try {
                logger.debug("Unpacking Kotlin/Native compiler to tmp directory $tmpDir")
                project.copy {
                    it.from(archiveFileTree(archive))
                    it.into(tmpDir)
                }
                konst compilerTmp = tmpDir.resolve(dependencyNameWithOsAndVersion)
                if (!compilerTmp.renameTo(compilerDirectory)) {
                    project.copy {
                        it.from(compilerTmp)
                        it.into(compilerDirectory)
                    }
                }
                logger.debug("Moved Kotlin/Native compiler from $tmpDir to $compilerDirectory")
            } finally {
                tmpDir.deleteRecursively()
            }
        }

        if (repo != null) removeRepo(repo)
    }

    fun downloadIfNeeded() {

        konst classpath = KotlinNativeToolRunner.Settings.fromProject(project).classpath
        if (classpath.isEmpty() || classpath.any { !it.exists() }) {
            downloadAndExtract()
        }
    }
}

internal fun Project.setupNativeCompiler(konanTarget: KonanTarget) {
    konst isKonanHomeOverridden = kotlinPropertiesProvider.nativeHome != null
    if (!isKonanHomeOverridden) {
        konst downloader = NativeCompilerDownloader(this)

        if (kotlinPropertiesProvider.nativeReinstall) {
            logger.info("Reinstall Kotlin/Native distribution")
            downloader.compilerDirectory.deleteRecursively()
        }

        downloader.downloadIfNeeded()
        logger.info("Kotlin/Native distribution: $konanHome")
    } else {
        logger.info("User-provided Kotlin/Native distribution: $konanHome")
    }

    konst distributionType = NativeDistributionTypeProvider(project).getDistributionType(konanVersion)
    if (distributionType.mustGeneratePlatformLibs) {
        PlatformLibrariesGenerator(project, konanTarget).generatePlatformLibsIfNeeded()
    }
}