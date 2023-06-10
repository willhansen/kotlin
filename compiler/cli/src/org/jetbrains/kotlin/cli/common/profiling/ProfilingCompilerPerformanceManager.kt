/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.profiling

import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfilingCompilerPerformanceManager(
    profilerPath: String,
    konst command: String,
    konst outputDir: File
) : CommonCompilerPerformanceManager("Profiling") {
    private konst profiler = AsyncProfilerHelper.getInstance(profilerPath)

    private konst runDate = Date()
    private konst formatter = SimpleDateFormat("yyyy-MM-dd__HH-mm")
    private var active = false

    init {
        startProfiling()
    }

    private fun startProfiling() {
        profiler.execute(command)
        active = true
    }

    private fun stopProfiling() {
        if (active) profiler.stop()
        active = false
    }

    private fun restartProfiling() {
        stopProfiling()
        startProfiling()
    }

    private fun dumpProfile(postfix: String) {
        outputDir.mkdirs()
        konst outputFile = outputDir.resolve("snapshot-${formatter.format(runDate)}-$postfix.collapsed")
        konst profile = profiler.execute("collapsed")
        FileOutputStream(outputFile).use { out ->
            for (chunk in profile.chunkedSequence(1 shl 20)) {
                out.write(chunk.toByteArray(Charsets.UTF_8))
            }
        }
        active = false
    }

    override fun notifyRepeat(total: Int, number: Int) {
        dumpProfile("repeat$number")
        restartProfiling()
    }

    override fun notifyCompilationFinished() {
        dumpProfile("final")
        stopProfiling()
    }

    companion object {
        fun create(profileCompilerArgument: String): ProfilingCompilerPerformanceManager {
            konst (path, command, outputDir) = profileCompilerArgument.split(":", limit = 3)
            return ProfilingCompilerPerformanceManager(path, command, File(outputDir))
        }
    }
}
