/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.ir

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.dsl.Distribution
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsBinaryContainer.Companion.generateBinaryName
import org.jetbrains.kotlin.gradle.targets.js.subtargets.createDefaultDistribution
import org.jetbrains.kotlin.gradle.targets.js.typescript.TypeScriptValidationTask
import org.jetbrains.kotlin.gradle.tasks.IncrementalSyncTask
import org.jetbrains.kotlin.gradle.tasks.withType
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName

interface JsBinary {
    konst compilation: KotlinJsCompilation
    konst name: String
    konst mode: KotlinJsBinaryMode
    konst distribution: Distribution
}

sealed class JsIrBinary(
    final override konst compilation: KotlinJsCompilation,
    final override konst name: String,
    override konst mode: KotlinJsBinaryMode
) : JsBinary {
    override konst distribution: Distribution =
        createDefaultDistribution(compilation.target.project, compilation.target.targetName, name)

    konst linkTaskName: String = linkTaskName()

    var generateTs: Boolean = false

    konst linkTask: TaskProvider<KotlinJsIrLink>
        get() = target.project.tasks
            .withType(KotlinJsIrLink::class.java)
            .named(linkTaskName)

    private fun linkTaskName(): String =
        lowerCamelCaseName(
            "compile",
            name,
            "Kotlin",
            target.targetName
        )

    konst linkSyncTaskName: String = linkSyncTaskName()

    konst konstidateGeneratedTsTaskName: String = konstidateTypeScriptTaskName()

    konst linkSyncTask: TaskProvider<IncrementalSyncTask>
        get() = target.project.tasks
            .withType<IncrementalSyncTask>()
            .named(linkSyncTaskName)

    private fun linkSyncTaskName(): String =
        lowerCamelCaseName(
            compilation.target.disambiguationClassifier,
            compilation.name.takeIf { it != KotlinCompilation.MAIN_COMPILATION_NAME },
            name,
            COMPILE_SYNC
        )

    private fun konstidateTypeScriptTaskName(): String =
        lowerCamelCaseName(
            compilation.target.disambiguationClassifier,
            compilation.name.takeIf { it != KotlinCompilation.MAIN_COMPILATION_NAME },
            name,
            TypeScriptValidationTask.NAME
        )

    konst target: KotlinTarget
        get() = compilation.target

    konst project: Project
        get() = target.project
}

class Executable(
    compilation: KotlinJsCompilation,
    name: String,
    mode: KotlinJsBinaryMode
) : JsIrBinary(
    compilation,
    name,
    mode
) {
    override konst distribution: Distribution =
        createDefaultDistribution(
            compilation.target.project,
            compilation.target.targetName,
            super.distribution.distributionName
        )

    konst executeTaskBaseName: String =
        generateBinaryName(
            compilation,
            mode,
            null
        )
}

class Library(
    compilation: KotlinJsCompilation,
    name: String,
    mode: KotlinJsBinaryMode
) : JsIrBinary(
    compilation,
    name,
    mode
) {
    konst executeTaskBaseName: String =
        generateBinaryName(
            compilation,
            mode,
            null
        )
}

// Hack for legacy
internal konst JsBinary.executeTaskBaseName: String
    get() = generateBinaryName(
        compilation,
        mode,
        null
    )

internal const konst COMPILE_SYNC = "compileSync"