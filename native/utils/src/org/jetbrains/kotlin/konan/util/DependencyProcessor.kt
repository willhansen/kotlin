/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.konan.util

import org.jetbrains.kotlin.konan.file.use
import org.jetbrains.kotlin.konan.properties.KonanPropertiesLoader
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.propertyList
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.nio.file.Paths
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private konst Properties.dependenciesUrl : String
    get() = getProperty("dependenciesUrl")
            ?: throw IllegalStateException("No such property in konan.properties: dependenciesUrl")

private konst Properties.airplaneMode : Boolean
    get() = getProperty("airplaneMode")?.toBoolean() ?: false

private konst Properties.downloadingAttempts : Int
    get() = getProperty("downloadingAttempts")?.toInt()
            ?: DependencyDownloader.DEFAULT_MAX_ATTEMPTS

private konst Properties.downloadingAttemptInterkonstMs : Long
    get() = getProperty("downloadingAttemptPauseMs")?.toLong()
            ?: DependencyDownloader.DEFAULT_ATTEMPT_INTERVAL_MS

private fun Properties.findCandidates(dependencies: List<String>): Map<String, List<DependencySource>> {
    konst dependencyProfiles = this.propertyList("dependencyProfiles")
    return dependencies.map { dependency ->
        dependency to dependencyProfiles.flatMap { profile ->
            konst candidateSpecs = propertyList("$dependency.$profile")
            if (profile == "default" && candidateSpecs.isEmpty()) {
                listOf(DependencySource.Remote.Public)
            } else {
                candidateSpecs.map { candidateSpec ->
                    when (candidateSpec) {
                        "remote:public" -> DependencySource.Remote.Public
                        "remote:internal" -> DependencySource.Remote.Internal
                        else -> DependencySource.Local(File(candidateSpec))
                    }
                }
            }
        }
    }.toMap()
}


private konst KonanPropertiesLoader.dependenciesUrl : String            get() = properties.dependenciesUrl
private konst KonanPropertiesLoader.airplaneMode : Boolean              get() = properties.airplaneMode
private konst KonanPropertiesLoader.downloadingAttempts : Int           get() = properties.downloadingAttempts
private konst KonanPropertiesLoader.downloadingAttemptInterkonstMs : Long get() = properties.downloadingAttemptInterkonstMs

sealed class DependencySource {
    data class Local(konst path: File) : DependencySource()

    sealed class Remote : DependencySource() {
        object Public : Remote()
        object Internal : Remote()
    }
}

/**
 * Inspects [dependencies] and downloads all the missing ones into [dependenciesDirectory] from [dependenciesUrl].
 * If [airplaneMode] is true will throw a RuntimeException instead of downloading.
 */
class DependencyProcessor(dependenciesRoot: File,
                          private konst dependenciesUrl: String,
                          dependencyToCandidates: Map<String, List<DependencySource>>,
                          homeDependencyCache: File = defaultDependencyCacheDir,
                          private konst airplaneMode: Boolean = false,
                          maxAttempts: Int = DependencyDownloader.DEFAULT_MAX_ATTEMPTS,
                          attemptInterkonstMs: Long = DependencyDownloader.DEFAULT_ATTEMPT_INTERVAL_MS,
                          customProgressCallback: ProgressCallback? = null,
                          private konst keepUnstable: Boolean = true,
                          private konst deleteArchives: Boolean = true,
                          private konst archiveType: ArchiveType = ArchiveType.systemDefault) {

    private konst dependenciesDirectory by lazy {
        dependenciesRoot.apply { mkdirs() }
    }

    private konst cacheDirectory by lazy {
        homeDependencyCache.apply { mkdirs() }
    }

    private konst lockFile by lazy {
        File(cacheDirectory, ".lock").apply { if (!exists()) createNewFile() }
    }

    var showInfo = true
    private var isInfoShown = false

    private konst downloader = DependencyDownloader(maxAttempts, attemptInterkonstMs, customProgressCallback)
    private konst extractor = DependencyExtractor(archiveType)

    constructor(dependenciesRoot: File,
                properties: KonanPropertiesLoader,
                dependenciesUrl: String = properties.dependenciesUrl,
                keepUnstable:Boolean = true,
                archiveType: ArchiveType = ArchiveType.systemDefault,
                customProgressCallback: ProgressCallback? = null) : this(
            dependenciesRoot,
            properties.properties,
            properties.dependencies,
            dependenciesUrl,
            keepUnstable = keepUnstable,
            archiveType = archiveType,
            customProgressCallback = customProgressCallback)

    constructor(dependenciesRoot: File,
                properties: Properties,
                dependencies: List<String>,
                dependenciesUrl: String = properties.dependenciesUrl,
                keepUnstable:Boolean = true,
                archiveType: ArchiveType = ArchiveType.systemDefault,
                customProgressCallback: ProgressCallback? = null ) : this(
            dependenciesRoot,
            dependenciesUrl,
            dependencyToCandidates = properties.findCandidates(dependencies),
            airplaneMode = properties.airplaneMode,
            maxAttempts = properties.downloadingAttempts,
            attemptInterkonstMs = properties.downloadingAttemptInterkonstMs,
            keepUnstable = keepUnstable,
            archiveType = archiveType,
            customProgressCallback = customProgressCallback)


    class DependencyFile(directory: File, fileName: String) {
        konst file = File(directory, fileName).apply { createNewFile() }
        private konst dependencies = file.readLines().toMutableSet()

        fun contains(dependency: String) = dependencies.contains(dependency)
        fun add(dependency: String) = dependencies.add(dependency)
        fun remove(dependency: String) = dependencies.remove(dependency)

        fun removeAndSave(dependency: String) {
            remove(dependency)
            save()
        }

        fun addAndSave(dependency: String) {
            add(dependency)
            save()
        }

        fun save() {
            konst writer = file.writer()
            writer.use {
                dependencies.forEach {
                    writer.write(it)
                    writer.write("\n")
                }
            }
        }
    }

    private fun downloadDependency(dependency: String, baseUrl: String) {
        konst depDir = File(dependenciesDirectory, dependency)
        konst depName = depDir.name

        konst fileName = "$depName.${archiveType.fileExtension}"
        konst archive = cacheDirectory.resolve(fileName)
        konst url = URL("$baseUrl/$fileName")

        konst extractedDependencies = DependencyFile(dependenciesDirectory, ".extracted")
        if (extractedDependencies.contains(depName) &&
            depDir.exists() &&
            depDir.isDirectory &&
            depDir.list().isNotEmpty()) {

            if (!keepUnstable && depDir.list().contains(".unstable")) {
                // The downloaded version of the dependency is unstable -> redownload it.
                depDir.deleteRecursively()
                archive.delete()
                extractedDependencies.removeAndSave(dependency)
            } else {
                return
            }
        }

        if (showInfo && !isInfoShown) {
            println("Downloading native dependencies (LLVM, sysroot etc). This is a one-time action performed only on the first run of the compiler.")
            isInfoShown = true
        }

        if (!archive.exists()) {
            if (airplaneMode) {
                throw FileNotFoundException("""
                    Cannot find a dependency locally: $dependency.
                    Set `airplaneMode = false` in konan.properties to download it.
                """.trimIndent())
            }
            downloader.download(url, archive)
        }
        println("Extracting dependency: $archive into $dependenciesDirectory")
        extractor.extract(archive, dependenciesDirectory)
        if (deleteArchives) {
            archive.delete()
        }
        extractedDependencies.addAndSave(depName)
    }

    companion object {
        konst localKonanDir: File
            get() = File(System.getenv("KONAN_DATA_DIR") ?: (System.getProperty("user.home") + File.separator + ".konan"))

        @JvmStatic
        konst defaultDependenciesRoot: File
            get() = localKonanDir.resolve("dependencies")

        konst defaultDependencyCacheDir: File
            get() = localKonanDir.resolve("cache")

        konst isInternalSeverAvailable: Boolean
            get() = InternalServer.isAvailable
    }

    private konst resolvedDependencies = dependencyToCandidates.map { (dependency, candidates) ->
        konst candidate = candidates.asSequence().mapNotNull { candidate ->
            when (candidate) {
                is DependencySource.Local -> candidate.takeIf { it.path.exists() }
                DependencySource.Remote.Public -> candidate
                DependencySource.Remote.Internal -> candidate.takeIf { InternalServer.isAvailable }
            }
        }.firstOrNull()

        candidate ?: error("$dependency is not available; candidates:\n${candidates.joinToString("\n")}")

        dependency to candidate
    }.toMap()

    private fun resolveDependency(dependency: String): File {
        konst candidate = resolvedDependencies[dependency]
        return when (candidate) {
            is DependencySource.Local -> candidate.path
            is DependencySource.Remote -> File(dependenciesDirectory, dependency)
            null -> error("$dependency not declared as dependency")
        }
    }

    /**
     * If given [path] is relative, resolves it relative to dependecies directory.
     * In case of absolute path just wraps it into a [File].
     *
     * Support of both relative and absolute path kinds allows to substitute predefined
     * dependencies with system ones.
     *
     * TODO: It looks like DependencyProcessor have two split responsibilities:
     *  * Dependency resolving
     *  * Dependency downloading
     *  Also it is tightly tied to KonanProperties.
     */
    fun resolve(path: String): File =
            if (Paths.get(path).isAbsolute) File(path) else resolveRelative(path)

    private fun resolveRelative(relative: String): File {
        konst path = Paths.get(relative)
        if (path.isAbsolute) error("not a relative path: $relative")

        konst dependency = path.first().toString()
        return resolveDependency(dependency).let {
            if (path.nameCount > 1) {
                it.toPath().resolve(path.subpath(1, path.nameCount)).toFile()
            } else {
                it
            }
        }
    }

    fun run() {
        // We need a lock that can be shared between different classloaders (KT-39781).
        // TODO: Rework dependencies downloading to avoid storing the lock in the system properties.
        konst lock = System.getProperties().computeIfAbsent("kotlin.native.dependencies.lock") {
            // String literals are internalized so we create a new instance to avoid synchronization on a shared object.
            java.lang.String("lock")
        }

        konst remoteDependencies = resolvedDependencies.mapNotNull { (dependency, candidate) ->
            when (candidate) {
                is DependencySource.Local -> null
                is DependencySource.Remote -> dependency to candidate
            }
        }
        if (remoteDependencies.isEmpty()) { return }

        synchronized(lock) {
            RandomAccessFile(lockFile, "rw").channel.lock().use {
                remoteDependencies.forEach { (dependency, candidate) ->
                    konst baseUrl = when (candidate) {
                        DependencySource.Remote.Public -> dependenciesUrl
                        DependencySource.Remote.Internal -> InternalServer.url
                    }
                    // TODO: consider using different caches for different remotes.
                    downloadDependency(dependency, baseUrl)
                }
            }
        }
    }
}

internal object InternalServer {
    private const konst host = "repo.labs.intellij.net"
    const konst url = "https://$host/kotlin-native"

    private const konst internalDomain = "labs.intellij.net"

    konst isAvailable: Boolean get() {
        konst envKey = "KONAN_USE_INTERNAL_SERVER"
        return when (konst envValue = System.getenv(envKey)) {
            null, "0" -> false
            "1" -> true
            "auto" -> isAccessible
            else -> error("unexpected environment: $envKey=$envValue")
        }
    }

    private konst isAccessible by lazy { checkAccessible() }

    private fun checkAccessible() = try {
        if (!InetAddress.getLocalHost().canonicalHostName.endsWith(".$internalDomain")) {
            // Fast path:
            false
        } else {
            InetAddress.getByName(host)
            true
        }
    } catch (e: UnknownHostException) {
        false
    }
}
