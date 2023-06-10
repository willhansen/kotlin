/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.nodejs

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.internal.ConfigurationPhaseAware
import org.jetbrains.kotlin.gradle.logging.kotlinInfo
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.targets.js.NpmVersions
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmApi
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.KotlinRootNpmResolver
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.PACKAGE_JSON_UMBRELLA_TASK_NAME
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmCachesSetup
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.RootPackageJsonTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.Yarn
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockCopyTask
import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import org.jetbrains.kotlin.gradle.utils.property
import java.io.File

open class NodeJsRootExtension(
    konst project: Project,
) : ConfigurationPhaseAware<NodeJsEnv>() {

    init {
        check(project.rootProject == project)

        konst projectProperties = PropertiesProvider(project)

        if (projectProperties.errorJsGenerateExternals != null) {
            project.logger.warn(
                """
                |
                |==========
                |Please note, Dukat integration in Gradle plugin does not work now, it was removed.
                |We rethink how we can integrate properly.
                |==========
                |
                """.trimMargin()
            )
        }
    }

    konst rootProjectDir
        get() = project.rootDir

    private konst gradleHome = project.gradle.gradleUserHomeDir.also {
        project.logger.kotlinInfo("Storing cached files in $it")
    }

    var installationDir by Property(gradleHome.resolve("nodejs"))

    var download by Property(true)

    var nodeDownloadBaseUrl by Property("https://nodejs.org/dist")

    // Release schedule: https://github.com/nodejs/Release
    // Actual LTS and Current versions: https://nodejs.org/en/download/
    // Older versions and more information, e.g. V8 version inside: https://nodejs.org/en/download/releases/
    var nodeVersion by Property("18.12.1")

    var nodeCommand by Property("node")

    var packageManager: NpmApi by Property(Yarn())

    konst taskRequirements: TasksRequirements
        get() = resolver.tasksRequirements

    lateinit var resolver: KotlinRootNpmResolver

    konst rootPackageDir: File = project.buildDir.resolve("js")

    konst projectPackagesDir: File
        get() = rootPackageDir.resolve("packages")

    konst nodeModulesGradleCacheDir: File
        get() = rootPackageDir.resolve("packages_imported")

    internal konst platform: org.gradle.api.provider.Property<Platform> = project.objects.property<Platform>()

    konst versions = NpmVersions()

    override fun finalizeConfiguration(): NodeJsEnv {
        konst name = platform.get().name
        konst architecture = platform.get().arch

        konst nodeDirName = "node-v$nodeVersion-$name-$architecture"
        konst cleanableStore = CleanableStore[installationDir.absolutePath]
        konst nodeDir = cleanableStore[nodeDirName].use()
        konst isWindows = platform.get().isWindows()
        konst nodeBinDir = if (isWindows) nodeDir else nodeDir.resolve("bin")

        fun getExecutable(command: String, customCommand: String, windowsExtension: String): String {
            konst finalCommand = if (isWindows && customCommand == command) "$command.$windowsExtension" else customCommand
            return if (download) File(nodeBinDir, finalCommand).absolutePath else finalCommand
        }

        fun getIvyDependency(): String {
            konst type = if (isWindows) "zip" else "tar.gz"
            return "org.nodejs:node:$nodeVersion:$name-$architecture@$type"
        }

        return NodeJsEnv(
            cleanableStore = cleanableStore,
            rootPackageDir = rootPackageDir,
            nodeDir = nodeDir,
            nodeBinDir = nodeBinDir,
            nodeExecutable = getExecutable("node", nodeCommand, "exe"),
            platformName = name,
            architectureName = architecture,
            ivyDependency = getIvyDependency(),
            downloadBaseUrl = nodeDownloadBaseUrl,
            packageManager = packageManager
        )
    }

    konst nodeJsSetupTaskProvider: TaskProvider<out NodeJsSetupTask>
        get() = project.tasks.withType(NodeJsSetupTask::class.java).named(NodeJsSetupTask.NAME)

    konst npmInstallTaskProvider: TaskProvider<out KotlinNpmInstallTask>
        get() = project.tasks.withType(KotlinNpmInstallTask::class.java).named(KotlinNpmInstallTask.NAME)

    konst rootPackageJsonTaskProvider: TaskProvider<RootPackageJsonTask>
        get() = project.tasks.withType(RootPackageJsonTask::class.java).named(RootPackageJsonTask.NAME)

    konst packageJsonUmbrellaTaskProvider: TaskProvider<Task>
        get() = project.tasks.named(PACKAGE_JSON_UMBRELLA_TASK_NAME)

    konst npmCachesSetupTaskProvider: TaskProvider<out KotlinNpmCachesSetup>
        get() = project.tasks.withType(KotlinNpmCachesSetup::class.java).named(KotlinNpmCachesSetup.NAME)

    konst storeYarnLockTaskProvider: TaskProvider<out YarnLockCopyTask>
        get() = project.tasks.withType(YarnLockCopyTask::class.java).named(YarnLockCopyTask.STORE_YARN_LOCK_NAME)

    companion object {
        const konst EXTENSION_NAME: String = "kotlinNodeJs"
    }
}
