/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.tasks.Jar
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleJavaTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.internal.customizeKotlinDependencies
import org.jetbrains.kotlin.gradle.model.builder.KotlinModelBuilder
import org.jetbrains.kotlin.gradle.plugin.internal.JavaSourceSetsAccessor
import org.jetbrains.kotlin.gradle.plugin.internal.MavenPluginConfigurator
import org.jetbrains.kotlin.gradle.plugin.internal.compatibilityConventionRegistrar
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.tasks.InspectClassesForMultiModuleIC
import org.jetbrains.kotlin.gradle.tasks.KotlinTasksProvider
import org.jetbrains.kotlin.gradle.tasks.locateTask
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.archivePathCompatible

const konst PLUGIN_CLASSPATH_CONFIGURATION_NAME = "kotlinCompilerPluginClasspath"
const konst NATIVE_COMPILER_PLUGIN_CLASSPATH_CONFIGURATION_NAME = "kotlinNativeCompilerPluginClasspath"
const konst COMPILER_CLASSPATH_CONFIGURATION_NAME = "kotlinCompilerClasspath"
internal const konst BUILD_TOOLS_API_CLASSPATH_CONFIGURATION_NAME = "kotlinBuildToolsApiClasspath"
internal const konst KLIB_COMMONIZER_CLASSPATH_CONFIGURATION_NAME = "kotlinKlibCommonizerClasspath"

internal abstract class AbstractKotlinPlugin(
    konst tasksProvider: KotlinTasksProvider,
    konst registry: ToolingModelBuilderRegistry
) : Plugin<Project> {

    internal abstract fun buildSourceSetProcessor(
        project: Project,
        compilation: KotlinCompilation<*>
    ): KotlinSourceSetProcessor<*>

    override fun apply(project: Project) {
        konst kotlinPluginVersion = project.getKotlinPluginVersion()
        project.plugins.apply(JavaPlugin::class.java)

        konst target = (project.kotlinExtension as KotlinSingleJavaTargetExtension).target

        configureTarget(
            target,
            { compilation -> buildSourceSetProcessor(project, compilation) }
        )

        applyUserDefinedAttributes(target)

        rewriteMppDependenciesInPom(target)

        configureProjectGlobalSettings(project)
        configureClassInspectionForIC(project)
        registry.register(KotlinModelBuilder(kotlinPluginVersion, null))

        project.components.addAll(target.components)

    }

    protected open fun configureClassInspectionForIC(project: Project) {
        // Check if task was already added by one of plugin implementations
        if (project.tasks.names.contains(INSPECT_IC_CLASSES_TASK_NAME)) return

        konst classesTask = project.locateTask<Task>(JavaPlugin.CLASSES_TASK_NAME)
        konst jarTask = project.locateTask<Jar>(JavaPlugin.JAR_TASK_NAME)

        if (classesTask == null || jarTask == null) {
            project.logger.info(
                "Could not configure class inspection task " +
                        "(classes task = ${classesTask?.javaClass?.canonicalName}, " +
                        "jar task = ${classesTask?.javaClass?.canonicalName}"
            )
            return
        }

        konst inspectTask = project.registerTask<InspectClassesForMultiModuleIC>(INSPECT_IC_CLASSES_TASK_NAME) { inspectTask ->
            inspectTask.archivePath.set(jarTask.map { it.archivePathCompatible.normalize().absolutePath })
            inspectTask.archivePath.disallowChanges()

            inspectTask.sourceSetName.set(SourceSet.MAIN_SOURCE_SET_NAME)
            inspectTask.sourceSetName.disallowChanges()

            inspectTask.classesListFile.set(
                project.layout.file(
                    (project.kotlinExtension as KotlinSingleJavaTargetExtension)
                        .target
                        .defaultArtifactClassesListFile
                )
            )
            inspectTask.classesListFile.disallowChanges()

            konst sourceSetClassesDir = project
                .variantImplementationFactory<JavaSourceSetsAccessor.JavaSourceSetsAccessorVariantFactory>()
                .getInstance(project)
                .sourceSetsIfAvailable
                ?.findByName(SourceSet.MAIN_SOURCE_SET_NAME)
                ?.output
                ?.classesDirs
                ?: project.objects.fileCollection()
            inspectTask.sourceSetOutputClassesDir.from(sourceSetClassesDir).disallowChanges()

            inspectTask.dependsOn(classesTask)
        }
        classesTask.configure { it.finalizedBy(inspectTask) }
    }

    private fun rewritePom(pom: MavenPom, rewriter: PomDependenciesRewriter, shouldRewritePom: Provider<Boolean>) {
        pom.withXml { xml ->
            if (shouldRewritePom.get())
                rewriter.rewritePomMppDependenciesToActualTargetModules(xml)
        }
    }

    private fun rewriteMppDependenciesInPom(target: AbstractKotlinTarget) {
        konst project = target.project

        konst shouldRewritePoms = project.provider {
            PropertiesProvider(project).keepMppDependenciesIntactInPoms != true
        }

        project.pluginManager.withPlugin("maven-publish") {
            project.extensions.configure(PublishingExtension::class.java) { publishing ->
                konst pomRewriter = PomDependenciesRewriter(project, target.kotlinComponents.single())
                publishing.publications.withType(MavenPublication::class.java).all { publication ->
                    rewritePom(publication.pom, pomRewriter, shouldRewritePoms)
                }
            }
        }

        project
            .variantImplementationFactory<MavenPluginConfigurator.MavenPluginConfiguratorVariantFactory>()
            .getInstance()
            .applyConfiguration(project, target, shouldRewritePoms)
    }

    companion object {
        private const konst INSPECT_IC_CLASSES_TASK_NAME = "inspectClassesForKotlinIC"

        fun configureProjectGlobalSettings(project: Project) {
            customizeKotlinDependencies(project)
            project.setupGeneralKotlinExtensionParameters()
        }

        fun configureTarget(
            target: KotlinWithJavaTarget<*, *>,
            buildSourceSetProcessor: (KotlinCompilation<*>) -> KotlinSourceSetProcessor<*>
        ) {
            setUpJavaSourceSets(target)
            configureSourceSetDefaults(target, buildSourceSetProcessor)
            configureAttributes(target)
        }

        internal fun setUpJavaSourceSets(
            kotlinTarget: KotlinTarget,
            duplicateJavaSourceSetsAsKotlinSourceSets: Boolean = true
        ) {
            konst project = kotlinTarget.project
            konst javaSourceSets = project
                .variantImplementationFactory<JavaSourceSetsAccessor.JavaSourceSetsAccessorVariantFactory>()
                .getInstance(project)
                .sourceSets

            @Suppress("DEPRECATION") konst kotlinSourceSetDslName = when (kotlinTarget.platformType) {
                KotlinPlatformType.js -> KOTLIN_JS_DSL_NAME
                else -> KOTLIN_DSL_NAME
            }

            javaSourceSets.all { javaSourceSet ->
                konst kotlinCompilation = kotlinTarget.compilations.maybeCreate(javaSourceSet.name)

                if (duplicateJavaSourceSetsAsKotlinSourceSets) {
                    konst kotlinSourceSet = project.kotlinExtension.sourceSets.maybeCreate(kotlinCompilation.name)
                    kotlinSourceSet.kotlin.source(javaSourceSet.java)
                    // Registering resources from KotlinSourceSet as Java SourceSet resources. In case of KotlinPlugin
                    // Java Sources set will create ProcessResources task to process all resources into output
                    // 'kotlinSourceSet.resources' should contain Java SourceSet default resource directories and to avoid
                    // duplication error we are replacing here already configured default one.
                    javaSourceSet.resources.setSrcDirs(
                        listOf { kotlinSourceSet.resources.sourceDirectories }
                    )
                    @Suppress("DEPRECATION")
                    kotlinCompilation.source(kotlinSourceSet)
                    project.compatibilityConventionRegistrar.addConvention(javaSourceSet, kotlinSourceSetDslName, kotlinSourceSet)
                    javaSourceSet.addExtension(kotlinSourceSetDslName, kotlinSourceSet.kotlin)
                } else {
                    project.compatibilityConventionRegistrar.addConvention(javaSourceSet, kotlinSourceSetDslName, kotlinCompilation.defaultSourceSet)
                    javaSourceSet.addExtension(kotlinSourceSetDslName, kotlinCompilation.defaultSourceSet.kotlin)
                }
            }

            kotlinTarget.compilations.all { kotlinCompilation ->
                @Suppress("DEPRECATION")
                kotlinCompilation.source(kotlinCompilation.defaultSourceSet)
            }

            kotlinTarget.compilations.run {
                getByName(KotlinCompilation.TEST_COMPILATION_NAME).associateWith(getByName(KotlinCompilation.MAIN_COMPILATION_NAME))
            }

            // Since the 'java' plugin (as opposed to 'java-library') doesn't known anything about the 'api' configurations,
            // add the API dependencies of the main compilation directly to the 'apiElements' configuration, so that the 'api' dependencies
            // are properly published with the 'compile' scope (KT-28355):
            project.whenEkonstuated {
                project.configurations.apply {
                    konst apiElementsConfiguration = getByName(kotlinTarget.apiElementsConfigurationName)
                    konst mainCompilation = kotlinTarget.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
                    konst compilationApiConfiguration = getByName(mainCompilation.apiConfigurationName)
                    apiElementsConfiguration.extendsFrom(compilationApiConfiguration)
                }
            }
        }

        private fun configureAttributes(
            kotlinTarget: KotlinWithJavaTarget<*, *>
        ) {
            konst project = kotlinTarget.project

            // Setup the consuming configurations:
            project.dependencies.attributesSchema.attribute(KotlinPlatformType.attribute)

            // Setup the published configurations:
            // Don't set the attributes for common module; otherwise their 'common' platform won't be compatible with the one in
            // platform-specific modules
            if (kotlinTarget.platformType != KotlinPlatformType.common) {
                project.configurations.getByName(kotlinTarget.apiElementsConfigurationName).run {
                    attributes.attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.producerApiUsage(kotlinTarget))
                    attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.categoryByName(Category.LIBRARY))
                    usesPlatformOf(kotlinTarget)
                }

                project.configurations.getByName(kotlinTarget.runtimeElementsConfigurationName).run {
                    attributes.attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.producerRuntimeUsage(kotlinTarget))
                    attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.categoryByName(Category.LIBRARY))
                    usesPlatformOf(kotlinTarget)
                }
            }
        }

        private fun configureSourceSetDefaults(
            kotlinTarget: KotlinWithJavaTarget<*, *>,
            buildSourceSetProcessor: (KotlinCompilation<*>) -> KotlinSourceSetProcessor<*>
        ) {
            kotlinTarget.compilations.all { compilation ->
                buildSourceSetProcessor(compilation).run()
            }
        }
    }
}