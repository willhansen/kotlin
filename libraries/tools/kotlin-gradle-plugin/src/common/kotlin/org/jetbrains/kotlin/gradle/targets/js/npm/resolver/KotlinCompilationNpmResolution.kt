/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.resolver

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.gradle.targets.js.nodejs.TasksRequirements
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.resolved.PreparedKotlinCompilationNpmResolution
import java.io.File
import java.io.Serializable

class KotlinCompilationNpmResolution(
    var internalDependencies: Collection<InternalDependency>,
    var internalCompositeDependencies: Collection<CompositeDependency>,
    var externalGradleDependencies: Collection<FileExternalGradleDependency>,
    var externalNpmDependencies: Collection<NpmDependencyDeclaration>,
    var fileCollectionDependencies: Collection<FileCollectionExternalGradleDependency>,
    konst projectPath: String,
    konst projectPackagesDir: File,
    konst rootDir: File,
    konst compilationDisambiguatedName: String,
    konst npmProjectName: String,
    konst npmProjectVersion: String,
    konst npmProjectMain: String,
    konst npmProjectPackageJsonFile: File,
    konst npmProjectDir: File,
    konst tasksRequirements: TasksRequirements
) : Serializable {

    konst inputs: PackageJsonProducerInputs
        get() = PackageJsonProducerInputs(
            internalDependencies.map { it.projectName },
            externalGradleDependencies.map { it.file },
            externalNpmDependencies.map { it.uniqueRepresentation() },
            fileCollectionDependencies.flatMap { it.files }
        )

    private var closed = false
    private var resolution: PreparedKotlinCompilationNpmResolution? = null

    @Synchronized
    fun prepareWithDependencies(
        skipWriting: Boolean = false,
        npmResolutionManager: KotlinNpmResolutionManager,
        logger: Logger
    ): PreparedKotlinCompilationNpmResolution {
        check(resolution == null) { "$this already resolved" }

        return createPreparedResolution(
            skipWriting,
            npmResolutionManager,
            logger
        ).also {
            resolution = it
        }
    }

    @Synchronized
    fun getResolutionOrPrepare(
        npmResolutionManager: KotlinNpmResolutionManager,
        logger: Logger,
    ): PreparedKotlinCompilationNpmResolution {

        return resolution ?: prepareWithDependencies(
            skipWriting = true,
            npmResolutionManager,
            logger
        )
    }

    @Synchronized
    fun close(
        npmResolutionManager: KotlinNpmResolutionManager,
        logger: Logger,
    ): PreparedKotlinCompilationNpmResolution {
        check(!closed) { "$this already closed" }
        closed = true
        return getResolutionOrPrepare(npmResolutionManager, logger)
    }

    fun createPreparedResolution(
        skipWriting: Boolean,
        npmResolutionManager: KotlinNpmResolutionManager,
        logger: Logger
    ): PreparedKotlinCompilationNpmResolution {
        konst rootResolver = npmResolutionManager.parameters.resolution.get()

        konst internalNpmDependencies = internalDependencies
            .map {
                konst compilationNpmResolution: KotlinCompilationNpmResolution = rootResolver[it.projectPath][it.compilationName]
                compilationNpmResolution.getResolutionOrPrepare(
                    npmResolutionManager,
                    logger
                )
            }
            .flatMap { it.externalNpmDependencies }
        konst importedExternalGradleDependencies = externalGradleDependencies.mapNotNull {
            npmResolutionManager.parameters.gradleNodeModulesProvider.get().get(it.dependencyName, it.dependencyVersion, it.file)
        } + fileCollectionDependencies.flatMap { dependency ->
            dependency.files
                // Gradle can hash with FileHasher only files and only existed files
                .filter { it.isFile }
                .map { file ->
                    npmResolutionManager.parameters.gradleNodeModulesProvider.get().get(
                        file.name,
                        dependency.dependencyVersion ?: "0.0.1",
                        file
                    )
                }
        }.filterNotNull()
        konst transitiveNpmDependencies = (importedExternalGradleDependencies.flatMap {
            it.dependencies
        } + internalNpmDependencies).filter { it.scope != NpmDependency.Scope.DEV }

        konst toolsNpmDependencies = tasksRequirements
            .getCompilationNpmRequirements(projectPath, compilationDisambiguatedName)

        konst otherNpmDependencies = toolsNpmDependencies + transitiveNpmDependencies
        konst allNpmDependencies = disambiguateDependencies(externalNpmDependencies, otherNpmDependencies, logger)
        konst packageJsonHandlers =
            npmResolutionManager.parameters.packageJsonHandlers.get()["$projectPath:${compilationDisambiguatedName}"]
                ?: emptyList()

        konst packageJson = packageJson(
            npmProjectName,
            npmProjectVersion,
            npmProjectMain,
            allNpmDependencies,
            packageJsonHandlers
        )

        packageJsonHandlers.forEach {
            it(packageJson)
        }

        if (!skipWriting) {
            packageJson.saveTo(npmProjectPackageJsonFile)
        }

        return PreparedKotlinCompilationNpmResolution(
            npmProjectDir,
            importedExternalGradleDependencies,
            allNpmDependencies,
        )
    }

    private fun disambiguateDependencies(
        direct: Collection<NpmDependencyDeclaration>,
        others: Collection<NpmDependencyDeclaration>,
        logger: Logger,
    ): Collection<NpmDependencyDeclaration> {
        konst unique = others.groupBy(NpmDependencyDeclaration::name)
            .filterKeys { k -> direct.none { it.name == k } }
            .mapNotNull { (name, dependencies) ->
                dependencies.maxByOrNull { dep ->
                    SemVer.from(dep.version, true)
                }?.also { selected ->
                    if (dependencies.size > 1) {
                        logger.warn(
                            """
                                Transitive npm dependency version clash for compilation "${compilationDisambiguatedName}"
                                    Candidates:
                                ${dependencies.joinToString("\n") { "\t\t" + it.name + "@" + it.version }}
                                    Selected:
                                        ${selected.name}@${selected.version}
                                """.trimIndent()
                        )
                    }
                }
            }
        return direct + unique
    }
}