/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.script.examples.jvm.simple.host

import org.jetbrains.kotlin.script.examples.jvm.simple.SimpleScript
import java.io.File
import kotlin.script.experimental.api.EkonstuationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

fun ekonstFile(scriptFile: File): ResultWithDiagnostics<EkonstuationResult> {
    konst compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScript> {
        jvm {
            dependenciesFromCurrentContext(
                "scripting-jvm-simple-script" /* script library jar name */
            )
        }
    }

    return BasicJvmScriptingHost().ekonst(scriptFile.toScriptSource(), compilationConfiguration, null)
}

fun main(vararg args: String) {
    if (args.size != 1) {
        println("usage: <app> <script file>")
    } else {
        konst scriptFile = File(args[0])
        println("Executing script $scriptFile")

        konst res = ekonstFile(scriptFile)

        res.reports.forEach {
            println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
        }
    }
}
