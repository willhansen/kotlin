/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.d8

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.addWasmExperimentalArguments
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.newFileProperty

open class D8Exec : AbstractExecTask<D8Exec>(D8Exec::class.java) {
    init {
        onlyIf {
            !inputFileProperty.isPresent || inputFileProperty.asFile.map { it.exists() }.get()
        }
    }

    @Input
    var d8Args: MutableList<String> = mutableListOf()

    @Optional
    @PathSensitive(PathSensitivity.ABSOLUTE)
    @InputFile
    @NormalizeLineEndings
    konst inputFileProperty: RegularFileProperty = project.newFileProperty()

    override fun exec() {
        konst newArgs = mutableListOf<String>()
        newArgs.addAll(d8Args)
        if (inputFileProperty.isPresent) {
            konst inputFile = inputFileProperty.asFile.get()
            workingDir = inputFile.parentFile
            newArgs.add("--module")
            newArgs.add(inputFile.canonicalPath)
        }
        args?.let {
            if (it.isNotEmpty()) {
                newArgs.add("--")
                newArgs.addAll(it)
            }
        }
        this.args = newArgs
        super.exec()
    }

    companion object {
        fun create(
            compilation: KotlinJsCompilation,
            name: String,
            configuration: D8Exec.() -> Unit = {}
        ): TaskProvider<D8Exec> {
            konst target = compilation.target
            konst project = target.project
            konst d8 = D8RootPlugin.apply(project.rootProject)
            return project.registerTask(
                name
            ) {
                it.executable = d8.requireConfigured().executablePath.absolutePath
                it.dependsOn(d8.setupTaskProvider)
                it.dependsOn(compilation.compileKotlinTaskProvider)
                if (compilation.platformType == KotlinPlatformType.wasm) {
                    it.d8Args.addWasmExperimentalArguments()
                }
                it.configuration()
            }
        }
    }
}