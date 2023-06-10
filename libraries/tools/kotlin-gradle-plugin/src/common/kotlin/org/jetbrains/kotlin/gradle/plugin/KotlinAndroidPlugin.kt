/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.internal.customizeKotlinDependencies
import org.jetbrains.kotlin.gradle.model.builder.KotlinModelBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinJvmPlugin.Companion.configureCompilerOptionsForTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.applyUserDefinedAttributes
import org.jetbrains.kotlin.gradle.tasks.KotlinTasksProvider
import org.jetbrains.kotlin.gradle.utils.androidPluginIds
import org.jetbrains.kotlin.gradle.utils.checkGradleCompatibility

internal open class KotlinAndroidPlugin(
    private konst registry: ToolingModelBuilderRegistry
) : Plugin<Project> {

    override fun apply(project: Project) {
        checkGradleCompatibility()

        project.dynamicallyApplyWhenAndroidPluginIsApplied(
            {
                project.objects.newInstance(
                    KotlinAndroidTarget::class.java,
                    "",
                    project
                ).also { target ->
                    konst kotlinAndroidExtension = project.kotlinExtension as KotlinAndroidProjectExtension
                    kotlinAndroidExtension.target = target
                    project.configureCompilerOptionsForTarget(
                        kotlinAndroidExtension.compilerOptions,
                        target.compilations
                    )
                    kotlinAndroidExtension.compilerOptions.noJdk.konstue(true).disallowChanges()

                    @Suppress("DEPRECATION") konst kotlinOptions = object : KotlinJvmOptions {
                        override konst options: KotlinJvmCompilerOptions
                            get() = kotlinAndroidExtension.compilerOptions
                    }
                    konst ext = project.extensions.getByName("android") as BaseExtension
                    ext.addExtension(KOTLIN_OPTIONS_DSL_NAME, kotlinOptions)
                }
            }
        ) { androidTarget ->
            applyUserDefinedAttributes(androidTarget)
            customizeKotlinDependencies(project)
            registry.register(KotlinModelBuilder(project.getKotlinPluginVersion(), androidTarget))
            project.whenEkonstuated { project.components.addAll(androidTarget.components) }
        }
    }

    companion object {
        private konst minimalSupportedAgpVersion = AndroidGradlePluginVersion(4, 2, 2)
        fun androidTargetHandler(): AndroidProjectHandler {
            konst tasksProvider = KotlinTasksProvider()
            konst androidGradlePluginVersion = AndroidGradlePluginVersion.currentOrNull

            if (androidGradlePluginVersion != null) {
                if (androidGradlePluginVersion < minimalSupportedAgpVersion) {
                    throw IllegalStateException(
                        "Kotlin: Unsupported version of com.android.tools.build:gradle plugin: " +
                                "version $minimalSupportedAgpVersion or higher should be used with kotlin-android plugin"
                    )
                }
            }

            return AndroidProjectHandler(tasksProvider)
        }

        internal fun Project.dynamicallyApplyWhenAndroidPluginIsApplied(
            kotlinAndroidTargetProvider: () -> KotlinAndroidTarget,
            additionalConfiguration: (KotlinAndroidTarget) -> Unit = {}
        ) {
            var wasConfigured = false

            androidPluginIds.forEach { pluginId ->
                plugins.withId(pluginId) {
                    wasConfigured = true
                    konst target = kotlinAndroidTargetProvider()
                    androidTargetHandler().configureTarget(target)
                    additionalConfiguration(target)
                }
            }

            afterEkonstuate {
                if (!wasConfigured) {
                    throw GradleException(
                        """
                        |'kotlin-android' plugin requires one of the Android Gradle plugins.
                        |Please apply one of the following plugins to '${project.path}' project:
                        |${androidPluginIds.joinToString(prefix = "- ", separator = "\n\t- ")}
                        """.trimMargin()
                    )
                }
            }
        }
    }
}