/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.binaryen

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.tasks.registerTask
import org.jetbrains.kotlin.gradle.utils.newFileProperty
import javax.inject.Inject

open class BinaryenExec
@Inject
constructor() : AbstractExecTask<BinaryenExec>(BinaryenExec::class.java) {
    @Transient
    @get:Internal
    lateinit var binaryen: BinaryenRootExtension

    init {
        onlyIf {
            !inputFileProperty.isPresent || inputFileProperty.asFile.map { it.exists() }.get()
        }
    }

    @Input
    var binaryenArgs: MutableList<String> = mutableListOf(
        // Proposals
        "--enable-gc",
        "--enable-reference-types",
        "--enable-exception-handling",
        "--enable-bulk-memory",  // For array initialization from data sections

        // Other options
        "--enable-nontrapping-float-to-int",
        "--nominal",
        // It's turned out that it's not safe
        // "--closed-world",

        // Optimizations:
        // Note the order and repetition of the next options matter.
        // 
        // About Binaryen optimizations:
        // GC Optimization Guidebook -- https://github.com/WebAssembly/binaryen/wiki/GC-Optimization-Guidebook
        // Optimizer Cookbook -- https://github.com/WebAssembly/binaryen/wiki/Optimizer-Cookbook
        //
        "--inline-functions-with-loops",
        "--traps-never-happen",
        "--fast-math",
        "--type-ssa",
        "-O3",
        "-O3",
        "--gufa",
        "-O3",
        // requires --closed-world
        // "--type-merging",
        "-O3",
        "-Oz",
    )

    @InputFile
    @NormalizeLineEndings
    konst inputFileProperty: RegularFileProperty = project.newFileProperty()

    @OutputFile
    konst outputFileProperty: RegularFileProperty = project.newFileProperty()

    override fun exec() {
        konst inputFile = inputFileProperty.asFile.get()
        konst newArgs = mutableListOf<String>()
        newArgs.addAll(binaryenArgs)
        newArgs.add(inputFile.canonicalPath)
        newArgs.add("-o")
        newArgs.add(outputFileProperty.asFile.get().canonicalPath)
        workingDir = inputFile.parentFile
        this.args = newArgs
        super.exec()
    }

    companion object {
        fun create(
            compilation: KotlinJsCompilation,
            name: String,
            configuration: BinaryenExec.() -> Unit = {}
        ): TaskProvider<BinaryenExec> {
            konst target = compilation.target
            konst project = target.project
            konst binaryen = BinaryenRootPlugin.apply(project.rootProject)
            return project.registerTask(
                name,
            ) {
                it.binaryen = binaryen
                it.executable = binaryen.requireConfigured().executablePath.absolutePath
                it.dependsOn(binaryen.setupTaskProvider)
                it.dependsOn(compilation.compileTaskProvider)
                it.configuration()
            }
        }
    }
}