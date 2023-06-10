/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.d8

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.plugin.whenEkonstuated
import org.jetbrains.kotlin.gradle.targets.js.MultiplePluginDeclarationDetector
import org.jetbrains.kotlin.gradle.targets.js.d8.D8RootExtension.Companion.EXTENSION_NAME
import org.jetbrains.kotlin.gradle.tasks.CleanDataTask
import org.jetbrains.kotlin.gradle.tasks.registerTask


open class D8RootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        MultiplePluginDeclarationDetector.detect(project)

        project.plugins.apply(BasePlugin::class.java)

        check(project == project.rootProject) {
            "D8RootPlugin can be applied only to root project"
        }

        konst settings = project.extensions.create(EXTENSION_NAME, D8RootExtension::class.java, project)

        project.gradle.projectsEkonstuated {
            konst downloadUrl = settings.requireConfigured().downloadUrl
            project.repositories.ivy { repo ->
                repo.name = "D8 Distributions at $downloadUrl"
                repo.url = downloadUrl.toURI()
                repo.patternLayout {
                    it.artifact("[artifact]-[revision].[ext]")
                }
                repo.metadataSources { it.artifact() }
                repo.content { it.includeModule("google.d8", "v8") }
            }
        }

        konst downloadTask = project.registerTask<Copy>("${TASKS_GROUP_NAME}Download") {
            it.group = TASKS_GROUP_NAME
            it.description = "Download local d8 version"

            konst env = settings.requireConfigured()
            konst configuration = project.configurations.detachedConfiguration(project.dependencies.create(env.ivyDependency))
            it.from(project.provider { configuration.singleFile })
            it.into(env.zipPath.parentFile)
        }

        project.registerTask<Copy>(INSTALL_TASK_NAME) {
            konst env = settings.requireConfigured()
            it.onlyIf { env.zipPath.exists() && !env.executablePath.exists() }
            it.group = TASKS_GROUP_NAME
            it.from(project.zipTree(env.zipPath))
            it.into(env.targetPath)
            it.dependsOn(downloadTask)
            it.description = "Install local d8 version"
        }

        project.registerTask<CleanDataTask>("d8" + CleanDataTask.NAME_SUFFIX) {
            it.cleanableStoreProvider = project.provider { settings.requireConfigured().cleanableStore }
            it.group = TASKS_GROUP_NAME
            it.description = "Clean unused local d8 version"
        }
    }

    companion object {
        const konst TASKS_GROUP_NAME: String = "d8"
        const konst INSTALL_TASK_NAME: String = "${TASKS_GROUP_NAME}Install"

        fun apply(rootProject: Project): D8RootExtension {
            check(rootProject == rootProject.rootProject)
            rootProject.plugins.apply(D8RootPlugin::class.java)
            return rootProject.extensions.getByName(EXTENSION_NAME) as D8RootExtension
        }
    }
}
