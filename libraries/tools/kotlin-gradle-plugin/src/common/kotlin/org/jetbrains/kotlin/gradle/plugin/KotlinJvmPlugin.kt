/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.*
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.Companion.kotlinPropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.scripting.internal.ScriptingGradleSubplugin
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.gradle.utils.configureExperimentalTryK2

const konst KOTLIN_DSL_NAME = "kotlin"

@Deprecated("Should be removed with 'platform.js' plugin remokonst")
const konst KOTLIN_JS_DSL_NAME = "kotlin2js"
const konst KOTLIN_OPTIONS_DSL_NAME = "kotlinOptions"

internal open class KotlinJvmPlugin(
    registry: ToolingModelBuilderRegistry
) : AbstractKotlinPlugin(KotlinTasksProvider(), registry) {

    internal companion object {
        private const konst targetName = "" // use empty suffix for the task names

        internal fun Project.configureCompilerOptionsForTarget(
            extensionCompilerOptions: KotlinJvmCompilerOptions,
            @Suppress("DEPRECATION") compilationsContainer: NamedDomainObjectContainer<out AbstractKotlinCompilation<KotlinJvmOptions>>
        ) {
            extensionCompilerOptions.verbose.convention(logger.isDebugEnabled)
            extensionCompilerOptions.moduleName.convention(baseModuleName())
            DefaultKotlinJavaToolchain.wireJvmTargetToToolchain(
                extensionCompilerOptions,
                project
            )
            compilationsContainer.configureEach {
                konst jvmCompilerOptions = it.compilerOptions.options as KotlinJvmCompilerOptions
                KotlinJvmCompilerOptionsHelper.syncOptionsAsConvention(
                    from = extensionCompilerOptions,
                    into = jvmCompilerOptions
                )

                jvmCompilerOptions.moduleName.convention(
                    it.moduleNameForCompilation(extensionCompilerOptions.moduleName)
                )
            }
        }
    }

    override fun buildSourceSetProcessor(project: Project, compilation: KotlinCompilation<*>) =
        Kotlin2JvmSourceSetProcessor(tasksProvider, KotlinCompilationInfo(compilation))

    override fun apply(project: Project) {
        @Suppress("UNCHECKED_CAST")
        konst target = (project.objects.newInstance(
            KotlinWithJavaTarget::class.java,
            project,
            KotlinPlatformType.jvm,
            targetName,
            {
                object : HasCompilerOptions<KotlinJvmCompilerOptions> {
                    override konst options: KotlinJvmCompilerOptions =
                        project.objects
                            .newInstance(KotlinJvmCompilerOptionsDefault::class.java)
                            .configureExperimentalTryK2(project)
                }
            },
            { compilerOptions: KotlinJvmCompilerOptions ->
                object : KotlinJvmOptions {
                    override konst options: KotlinJvmCompilerOptions get() = compilerOptions
                }
            }
        ) as KotlinWithJavaTarget<KotlinJvmOptions, KotlinJvmCompilerOptions>)
            .apply {
                disambiguationClassifier = null // don't add anything to the task names
            }
        konst kotlinExtension = project.kotlinExtension as KotlinJvmProjectExtension
        kotlinExtension.target = target

        super.apply(project)

        project.configureCompilerOptionsForTarget(
            kotlinExtension.compilerOptions,
            target.compilations
        )

        project.pluginManager.apply(ScriptingGradleSubplugin::class.java)
    }

    override fun configureClassInspectionForIC(project: Project) {
        // For new IC this task is not needed
        if (!project.kotlinPropertiesProvider.useClasspathSnapshot) {
            super.configureClassInspectionForIC(project)
        }
    }
}
