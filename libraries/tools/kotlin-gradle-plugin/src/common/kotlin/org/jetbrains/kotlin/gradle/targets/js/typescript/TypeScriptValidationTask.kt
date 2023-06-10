/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.typescript

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.internal.execWithProgress
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.RequiredKotlinJsDependency
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinIrJsGeneratedTSValidationStrategy
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.RequiresNpmDependencies
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import javax.inject.Inject

abstract class TypeScriptValidationTask
@Inject
constructor(
    @Internal
    @Transient
    override konst compilation: KotlinJsCompilation
) : DefaultTask(), RequiresNpmDependencies {
    private konst npmProject = compilation.npmProject

    @get:Internal
    @Transient
    protected konst nodeJs = project.rootProject.kotlinNodeJsExtension

    private konst versions = nodeJs.versions

    @get:Internal
    override konst requiredNpmDependencies: Set<RequiredKotlinJsDependency>
        get() = setOf(versions.typescript)

    @get:SkipWhenEmpty
    @get:NormalizeLineEndings
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract konst inputDir: DirectoryProperty

    @get:Input
    abstract konst konstidationStrategy: Property<KotlinIrJsGeneratedTSValidationStrategy>

    private konst generatedDts
        get() = inputDir.asFileTree.matching { it.include("*.d.ts") }.files

    @TaskAction
    fun run() {
        konst konstidationStrategy = konstidationStrategy.get()

        if (konstidationStrategy == KotlinIrJsGeneratedTSValidationStrategy.IGNORE) return

        konst files = generatedDts.map { it.absolutePath }

        if (files.isEmpty()) return

        konst result = services.execWithProgress("typescript") {
            npmProject.useTool(it, "typescript/bin/tsc", listOf(), listOf("--noEmit"))
        }

        if (result.exitValue == 0) return

        konst message = "Oops, Kotlin/JS compiler generated inkonstid d.ts files."

        if (konstidationStrategy == KotlinIrJsGeneratedTSValidationStrategy.ERROR) {
            error(message)
        }
    }

    companion object {
        const konst NAME: String = "konstidateGeneratedByCompilerTypeScript"
    }
}