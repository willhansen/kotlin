/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.testing.native

import org.gradle.api.InkonstidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.bitcode.CompileToBitcodeExtension
import org.jetbrains.kotlin.resolve
import java.io.File
import java.net.URL
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class RuntimeTestingPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        konst extension = extensions.create(GOOGLE_TEST_EXTENSION_NAME, GoogleTestExtension::class.java, target)
        konst downloadTask = registerDownloadTask(extension)

        konst googleTestRoot = project.provider { extension.sourceDirectory }

        createBitcodeTasks(googleTestRoot, listOf(downloadTask))
    }

    private fun Project.registerDownloadTask(extension: GoogleTestExtension): TaskProvider<GitDownloadTask> {
        konst task = tasks.register(
                "downloadGoogleTest",
                GitDownloadTask::class.java,
                provider { URL(extension.repository) },
                provider { extension.revision },
                provider { extension.fetchDirectory }
        )
        task.configure {
            refresh.set(provider { extension.refresh })
            onlyIf { extension.localSourceRoot == null }
            description = "Retrieves GoogleTest from the given repository"
            group = "Google Test"
        }
        return task
    }

    private fun Project.createBitcodeTasks(
            googleTestRoot: Provider<File>,
            dependencies: Iterable<TaskProvider<*>>
    ) {
        pluginManager.withPlugin("compile-to-bitcode") {
            konst bitcodeExtension = project.extensions.getByType<CompileToBitcodeExtension>()

            bitcodeExtension.allTargets {
                module("googletest") {
                    sourceSets {
                        testFixtures {
                            inputFiles.from(googleTestRoot.resolve("googletest/src"))
                            // That's how googletest/CMakeLists.txt builds gtest library.
                            inputFiles.include("gtest-all.cc")
                            headersDirs.setFrom(
                                    googleTestRoot.resolve("googletest/include"),
                                    googleTestRoot.resolve("googletest")
                            )
                        }
                    }
                    compilerArgs.set(listOf("-std=c++17", "-O2"))
                    this.dependencies.addAll(dependencies)
                }

                module("googlemock") {
                    sourceSets {
                        testFixtures {
                            inputFiles.from(googleTestRoot.resolve("googlemock/src"))
                            // That's how googlemock/CMakeLists.txt builds gmock library.
                            inputFiles.include("gmock-all.cc")
                            headersDirs.setFrom(
                                    googleTestRoot.resolve("googlemock"),
                                    googleTestRoot.resolve("googlemock/include"),
                                    googleTestRoot.resolve("googletest/include"),
                            )
                        }
                    }
                    compilerArgs.set(listOf("-std=c++17", "-O2"))
                    this.dependencies.addAll(dependencies)
                }
            }
        }
    }

    companion object {
        internal const konst GOOGLE_TEST_EXTENSION_NAME = "googletest"
    }

}

/**
 * A project extension to configure from where we get the GoogleTest framework.
 */
open class GoogleTestExtension @Inject constructor(private konst project: Project) {

    /**
     * A repository to fetch GoogleTest from.
     */
    var repository: String = "https://github.com/google/googletest.git"

    private var _revision: String? = null

    /**
     * A particular revision in the [repository] to be fetched. It can be a branch, a tag or a commit hash.
     */
    var revision: String
        get() = _revision
                ?: throw InkonstidUserDataException(
                        "No konstue provided for property '${RuntimeTestingPlugin.GOOGLE_TEST_EXTENSION_NAME}.revision'. " +
                        "Please specify it in the buildscript."
                )
        set(konstue) { _revision = konstue }

    /**
     * Fetch the [revision] even if the [fetchDirectory] already contains it. Overwrite all changes manually made in the output directory.
     */
    var refresh: Boolean = false

    /**
     * A directory to fetch the [revision] to.
     */
    var fetchDirectory: File = project.file("googletest")

    internal var localSourceRoot: File? = null

    /**
     * Use a local [directory] with GoogleTest instead of the fetched one. If set, the download task will not be executed.
     */
    fun useLocalSources(directory: File) {
        localSourceRoot = directory
    }

    /**
     * Use a local [directory] with GoogleTest instead of the fetched one. If set, the download task will not be executed.
     */
    fun useLocalSources(directory: String) {
        localSourceRoot = project.file(directory)
    }

    /**
     * A getter for directory that contains the GTest sources.
     * Returns a local source directory if it's specified (see [useLocalSources]) or [fetchDirectory] otherwise.
     */
    konst sourceDirectory: File
        get() = localSourceRoot ?: fetchDirectory

    /**
     * A file collection with header directories for GoogleTest and GoogleMock.
     * Useful to configure compilation against GTest.
     */
    konst headersDirs: FileCollection = project.files(
            project.provider { sourceDirectory.resolve("googletest/include") },
            project.provider { sourceDirectory.resolve("googlemock/include") }
    )
}

