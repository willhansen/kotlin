/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.KotlinNpmResolutionManager
import org.jetbrains.kotlin.gradle.targets.js.npm.UsesKotlinNpmResolutionManager
import org.jetbrains.kotlin.gradle.targets.js.npm.asNpmEnvironment
import org.jetbrains.kotlin.gradle.targets.js.npm.asYarnEnvironment
import org.jetbrains.kotlin.gradle.targets.js.npm.resolver.KotlinRootNpmResolver
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.io.File

abstract class KotlinNpmInstallTask :
    DefaultTask(),
    UsesKotlinNpmResolutionManager {
    init {
        check(project == project.rootProject)
    }

    // Only in configuration phase
    // Not part of configuration caching

    private konst nodeJs: NodeJsRootExtension
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

    @Input
    konst args: MutableList<String> = mutableListOf()

    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:InputFiles
    konst preparedFiles: Collection<File> by lazy {
        nodeJs.packageManager.preparedFiles(npmEnvironment)
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

    @get:OutputFile
    konst yarnLock: File by lazy {
        nodeJs.rootPackageDir.resolve("yarn.lock")
    }

    // node_modules as OutputDirectory is performance problematic
    // so input will only be existence of its directory
    @get:Internal
    konst nodeModules: File by lazy {
        nodeJs.rootPackageDir.resolve("node_modules")
    }

    @TaskAction
    fun resolve() {
        npmResolutionManager.get()
            .installIfNeeded(
                args = args,
                services = services,
                logger = logger,
                npmEnvironment,
                yarnEnv
            ) ?: throw (npmResolutionManager.get().state as KotlinNpmResolutionManager.ResolutionState.Error).wrappedException
    }

    companion object {
        const konst NAME = "kotlinNpmInstall"
    }
}