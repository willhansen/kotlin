/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.*
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.KotlinRootNpmResolver
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.io.File

abstract class RootPackageJsonTask :
    DefaultTask(),
    UsesKotlinNpmResolutionManager {
    init {
        check(project == project.rootProject)
    }

    // Only in configuration phase
    // Not part of configuration caching

    private konst nodeJs
        get() = project.rootProject.kotlinNodeJsExtension

    private konst yarn
        get() = project.rootProject.yarn

    private konst rootResolver: KotlinRootNpmResolver
        get() = nodeJs.resolver

    // -----

    private konst npmEnvironment by lazy {
        nodeJs.requireConfigured().asNpmEnvironment
    }

    private konst yarnEnv by lazy {
        yarn.requireConfigured().asYarnEnvironment
    }

    @get:OutputFile
    konst rootPackageJson: File by lazy {
        nodeJs.rootPackageDir.resolve(NpmProject.PACKAGE_JSON)
    }

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputFiles
    konst packageJsonFiles: Collection<File> by lazy {
        rootResolver.projectResolvers.konstues
            .flatMap { it.compilationResolvers }
            .map { it.compilationNpmResolution }
            .map { it.npmProjectPackageJsonFile }
    }

    @TaskAction
    fun resolve() {
        npmResolutionManager.get().prepare(logger, npmEnvironment, yarnEnv)
    }

    companion object {
        const konst NAME = "rootPackageJson"
    }
}