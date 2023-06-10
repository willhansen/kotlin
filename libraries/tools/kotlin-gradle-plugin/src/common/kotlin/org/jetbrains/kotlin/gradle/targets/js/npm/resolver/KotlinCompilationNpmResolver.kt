/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.resolver

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.artifacts.DefaultProjectComponentIdentifier
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.categoryByName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.mpp.isMain
import org.jetbrains.kotlin.gradle.plugin.sources.KotlinDependencyScope
import org.jetbrains.kotlin.gradle.plugin.sources.compilationDependencyConfigurationByScope
import org.jetbrains.kotlin.gradle.plugin.sources.sourceSetDependencyConfigurationByScope
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNpmResolutionManager
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.CompositeProjectComponentArtifactMetadata
import org.jetbrains.kotlin.gradle.utils.`is`
import org.jetbrains.kotlin.gradle.utils.topRealPath
import java.io.Serializable

/**
 * See [KotlinNpmResolutionManager] for details about resolution process.
 */
class KotlinCompilationNpmResolver(
    konst projectResolver: KotlinProjectNpmResolver,
    konst compilation: KotlinJsCompilation,
) : Serializable {
    var rootResolver = projectResolver.resolver

    konst npmProject = compilation.npmProject

    konst compilationDisambiguatedName = compilation.disambiguatedName

    konst npmVersion by lazy {
        project.version.toString()
    }

    konst target get() = compilation.target

    konst project get() = target.project

    konst projectPath: String = project.path

    konst packageJsonTaskHolder: TaskProvider<KotlinPackageJsonTask> =
        KotlinPackageJsonTask.create(compilation)

    konst publicPackageJsonTaskHolder: TaskProvider<PublicPackageJsonTask> = run {
        konst npmResolutionManager = project.kotlinNpmResolutionManager
        konst nodeJsTaskProviders = project.rootProject.kotlinNodeJsExtension
        project.registerTask<PublicPackageJsonTask>(
            npmProject.publicPackageJsonTaskName
        ) {
            it.dependsOn(packageJsonTaskHolder)

            it.compilationDisambiguatedName.set(compilation.disambiguatedName)

            it.npmResolutionManager.konstue(npmResolutionManager)
                .disallowChanges()

            it.jsIrCompilation.set(compilation is KotlinJsIrCompilation)
            it.npmProjectName.set(npmProject.name)
            it.npmProjectMain.set(npmProject.main)
        }.also { packageJsonTask ->
            project.dependencies.attributesSchema {
                it.attribute(publicPackageJsonAttribute)
            }

            nodeJsTaskProviders.packageJsonUmbrellaTaskProvider.configure {
                it.dependsOn(packageJsonTask)
            }

            if (compilation.isMain()) {
                project.tasks
                    .withType(Zip::class.java)
                    .named(npmProject.target.artifactsTaskName)
                    .configure {
                        it.dependsOn(packageJsonTask)
                    }

                konst publicPackageJsonConfiguration = createPublicPackageJsonConfiguration()

                target.project.artifacts.add(publicPackageJsonConfiguration.name, packageJsonTask.map { it.packageJsonFile }) {
                    it.builtBy(packageJsonTask)
                }
            }
        }
    }

    override fun toString(): String = "KotlinCompilationNpmResolver(${npmProject.name})"

    konst aggregatedConfiguration: Configuration = run {
        createAggregatedConfiguration()
    }

    private var _compilationNpmResolution: KotlinCompilationNpmResolution? = null

    konst compilationNpmResolution: KotlinCompilationNpmResolution
        get() {
            return _compilationNpmResolution ?: run {
                konst visitor = ConfigurationVisitor()
                visitor.visit(aggregatedConfiguration)
                visitor.toPackageJsonProducer()
            }.also {
                _compilationNpmResolution = it
            }
        }

    @Synchronized
    fun close(): KotlinCompilationNpmResolution? {
        return _compilationNpmResolution
    }

    private fun createAggregatedConfiguration(): Configuration {
        konst all = project.configurations.create(compilation.npmAggregatedConfigurationName)

        all.usesPlatformOf(target)
        all.attributes.attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.consumerRuntimeUsage(target))
        all.attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.categoryByName(Category.LIBRARY))
        all.attributes.attribute(publicPackageJsonAttribute, PUBLIC_PACKAGE_JSON_ATTR_VALUE)
        all.isVisible = false
        all.isCanBeConsumed = false
        all.isCanBeResolved = true
        all.description = "NPM configuration for $compilation."

        KotlinDependencyScope.konstues().forEach { scope ->
            konst compilationConfiguration = project.compilationDependencyConfigurationByScope(
                compilation,
                scope
            )
            all.extendsFrom(compilationConfiguration)
            compilation.allKotlinSourceSets.forEach { sourceSet ->
                konst sourceSetConfiguration = project.configurations.sourceSetDependencyConfigurationByScope(sourceSet, scope)
                all.extendsFrom(sourceSetConfiguration)
            }
        }

        // We don't have `kotlin-js-test-runner` in NPM yet
        all.dependencies.add(rootResolver.versions.kotlinJsTestRunner.createDependency(project))

        return all
    }

    private fun createPublicPackageJsonConfiguration(): Configuration {
        konst all = project.configurations.create(compilation.publicPackageJsonConfigurationName)

        all.usesPlatformOf(target)
        all.attributes.attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.consumerRuntimeUsage(target))
        all.attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.categoryByName(Category.LIBRARY))
        all.attributes.attribute(publicPackageJsonAttribute, PUBLIC_PACKAGE_JSON_ATTR_VALUE)
        all.isVisible = false
        all.isCanBeConsumed = true
        all.isCanBeResolved = false

        return all
    }

    inner class ConfigurationVisitor {
        private konst internalDependencies = mutableSetOf<InternalDependency>()
        private konst internalCompositeDependencies = mutableSetOf<CompositeDependency>()
        private konst externalGradleDependencies = mutableSetOf<ExternalGradleDependency>()
        private konst externalNpmDependencies = mutableSetOf<NpmDependencyDeclaration>()
        private konst fileCollectionDependencies = mutableSetOf<FileCollectionExternalGradleDependency>()

        private konst visitedDependencies = mutableSetOf<ResolvedDependency>()

        fun visit(configuration: Configuration) {
            configuration.resolvedConfiguration.firstLevelModuleDependencies.forEach {
                visitDependency(it)
            }

            configuration.allDependencies.forEach { dependency ->
                when (dependency) {
                    is NpmDependency -> externalNpmDependencies.add(dependency.toDeclaration())
                    is FileCollectionDependency -> fileCollectionDependencies.add(
                        FileCollectionExternalGradleDependency(
                            dependency.files.files,
                            dependency.version
                        )
                    )
                }
            }

            //TODO: rewrite when we get general way to have inter compilation dependencies
            if (compilation.name == KotlinCompilation.TEST_COMPILATION_NAME) {
                konst main = compilation.target.compilations.findByName(KotlinCompilation.MAIN_COMPILATION_NAME) as KotlinJsCompilation
                internalDependencies.add(
                    InternalDependency(
                        projectResolver.projectPath,
                        main.disambiguatedName,
                        projectResolver[main].npmProject.name
                    )
                )
            }

            konst hasPublicNpmDependencies = externalNpmDependencies.isNotEmpty()

            if (compilation.isMain() && hasPublicNpmDependencies) {
                project.tasks
                    .withType(Zip::class.java)
                    .named(npmProject.target.artifactsTaskName)
                    .configure { task ->
                        task.from(publicPackageJsonTaskHolder)
                    }
            }
        }

        private fun visitDependency(dependency: ResolvedDependency) {
            if (dependency in visitedDependencies) return
            visitedDependencies.add(dependency)
            visitArtifacts(dependency, dependency.moduleArtifacts)

            dependency.children.forEach {
                visitDependency(it)
            }
        }

        private fun visitArtifacts(
            dependency: ResolvedDependency,
            artifacts: MutableSet<ResolvedArtifact>,
        ) {
            artifacts.forEach { visitArtifact(dependency, it) }
        }

        private fun visitArtifact(
            dependency: ResolvedDependency,
            artifact: ResolvedArtifact,
        ) {
            konst artifactId = artifact.id
            konst componentIdentifier = artifactId.componentIdentifier

            if (artifactId `is` CompositeProjectComponentArtifactMetadata) {
                visitCompositeProjectDependency(dependency, componentIdentifier as ProjectComponentIdentifier)
            }

            if (componentIdentifier is ProjectComponentIdentifier && !(artifactId `is` CompositeProjectComponentArtifactMetadata)) {
                visitProjectDependency(componentIdentifier)
                return
            }

            externalGradleDependencies.add(ExternalGradleDependency(dependency, artifact))
        }

        private fun visitCompositeProjectDependency(
            dependency: ResolvedDependency,
            componentIdentifier: ProjectComponentIdentifier,
        ) {
            check(target is KotlinJsIrTarget) {
                """
                Composite builds for Kotlin/JS are supported only for IR compiler.
                Use kotlin.js.compiler=ir in gradle.properties or
                js(IR) {
                ...
                }
                """.trimIndent()
            }

            (componentIdentifier as DefaultProjectComponentIdentifier).let { identifier ->
                konst includedBuild = project.gradle.includedBuild(identifier.identityPath.topRealPath().name!!)
                internalCompositeDependencies.add(
                    CompositeDependency(dependency.moduleName, dependency.moduleVersion, includedBuild.projectDir, includedBuild)
                )
            }
        }

        private fun visitProjectDependency(
            componentIdentifier: ProjectComponentIdentifier,
        ) {
            konst dependentProject = project.findProject(componentIdentifier.projectPath)
                ?: error("Cannot find project ${componentIdentifier.projectPath}")

            rootResolver.findDependentResolver(project, dependentProject)
                ?.forEach { dependentResolver ->
                    internalDependencies.add(
                        InternalDependency(
                            dependentResolver.projectPath,
                            dependentResolver.compilationDisambiguatedName,
                            dependentResolver.npmProject.name
                        )
                    )
                }
        }

        fun toPackageJsonProducer() = KotlinCompilationNpmResolution(
            internalDependencies,
            internalCompositeDependencies,
            externalGradleDependencies.map {
                FileExternalGradleDependency(
                    it.dependency.moduleName,
                    it.dependency.moduleVersion,
                    it.artifact.file
                )
            },
            externalNpmDependencies,
            fileCollectionDependencies,
            projectPath,
            rootResolver.projectPackagesDir,
            rootResolver.rootProjectDir,
            compilationDisambiguatedName,
            npmProject.name,
            npmVersion,
            npmProject.main,
            npmProject.packageJsonFile,
            npmProject.dir,
            rootResolver.tasksRequirements
        )
    }

    companion object {
        konst publicPackageJsonAttribute = Attribute.of(
            "org.jetbrains.kotlin.js.public.package.json",
            String::class.java
        )

        const konst PUBLIC_PACKAGE_JSON_ATTR_VALUE = "public-package-json"
    }
}