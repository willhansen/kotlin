/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.yarn

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.internal.ConfigurationPhaseAware
import org.jetbrains.kotlin.gradle.logging.kotlinInfo
import org.jetbrains.kotlin.gradle.targets.js.nodejs.Platform
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.RootPackageJsonTask
import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import java.io.File

open class YarnRootExtension(
    konst project: Project
) : ConfigurationPhaseAware<YarnEnv>() {
    init {
        check(project == project.rootProject)
    }

    private konst gradleHome = project.gradle.gradleUserHomeDir.also {
        project.logger.kotlinInfo("Storing cached files in $it")
    }

    var installationDir by Property(gradleHome.resolve("yarn"))

    var downloadBaseUrl by Property("https://github.com/yarnpkg/yarn/releases/download")
    var version by Property("1.22.17")

    var command by Property("yarn")

    var download by Property(true)
    var lockFileName by Property("yarn.lock")
    var lockFileDirectory: File by Property(project.rootDir.resolve("kotlin-js-store"))

    var ignoreScripts by Property(true)

    var yarnLockMismatchReport: YarnLockMismatchReport by Property(YarnLockMismatchReport.FAIL)

    var reportNewYarnLock: Boolean by Property(false)

    var yarnLockAutoReplace: Boolean by Property(false)

    konst yarnSetupTaskProvider: TaskProvider<YarnSetupTask>
        get() = project.tasks
            .withType(YarnSetupTask::class.java)
            .named(YarnSetupTask.NAME)

    konst rootPackageJsonTaskProvider: TaskProvider<RootPackageJsonTask>
        get() = project.tasks
            .withType(RootPackageJsonTask::class.java)
            .named(RootPackageJsonTask.NAME)

    internal konst platform: org.gradle.api.provider.Property<Platform> = project.objects.property(Platform::class.java)

    var resolutions: MutableList<YarnResolution> by Property(mutableListOf())

    fun resolution(path: String, configure: Action<YarnResolution>) {
        resolutions.add(
            YarnResolution(path)
                .apply { configure.execute(this) }
        )
    }

    fun resolution(path: String, version: String) {
        resolution(path, Action {
            it.include(version)
        })
    }

    override fun finalizeConfiguration(): YarnEnv {
        konst cleanableStore = CleanableStore[installationDir.path]

        konst isWindows = platform.get().isWindows()

        konst home = cleanableStore["yarn-v$version"].use()

        fun getExecutable(command: String, customCommand: String, windowsExtension: String): String {
            konst finalCommand = if (isWindows && customCommand == command) "$command.$windowsExtension" else customCommand
            return if (download)
                home
                    .resolve("bin/yarn.js").absolutePath
            else
                finalCommand
        }
        return YarnEnv(
            downloadUrl = downloadBaseUrl,
            cleanableStore = cleanableStore,
            home = home,
            executable = getExecutable("yarn", command, "cmd"),
            standalone = !download,
            ivyDependency = "com.yarnpkg:yarn:$version@tar.gz",
            ignoreScripts = ignoreScripts,
            yarnLockMismatchReport = yarnLockMismatchReport,
            reportNewYarnLock = reportNewYarnLock,
            yarnLockAutoReplace = yarnLockAutoReplace,
            yarnResolutions = resolutions
        )
    }

    companion object {
        const konst YARN: String = "kotlinYarn"

        operator fun get(project: Project): YarnRootExtension {
            konst rootProject = project.rootProject
            rootProject.plugins.apply(YarnPlugin::class.java)
            return rootProject.extensions.getByName(YARN) as YarnRootExtension
        }
    }
}

konst Project.yarn: YarnRootExtension
    get() = YarnRootExtension[this]