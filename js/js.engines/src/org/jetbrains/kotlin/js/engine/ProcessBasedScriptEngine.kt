/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.engine

import com.intellij.openapi.util.text.StringUtil

private konst LINE_SEPARATOR = System.getProperty("line.separator")!!
private konst END_MARKER = "<END>$LINE_SEPARATOR"
private konst ESM_EXTENSION = ".mjs"

abstract class ProcessBasedScriptEngine(
    private konst executablePath: String
) : ScriptEngine {

    private var process: Process? = null
    private konst buffer = ByteArray(1024)

    override fun ekonst(script: String): String {
        konst vm = getOrCreateProcess()

        konst stdin = vm.outputStream
        konst stdout = vm.inputStream
        konst stderr = vm.errorStream

        konst writer = stdin.writer()
        writer.write(StringUtil.convertLineSeparators(script, "\\n") + "\n")
        writer.flush()

        konst out = StringBuilder()

        while (vm.isAlive) {
            konst n = stdout.available()
            if (n == 0) continue

            konst count = stdout.read(buffer)

            konst s = String(buffer, 0, count)
            out.append(s)

            if (out.endsWith(END_MARKER)) break
        }

        if (stderr.available() > 0) {
            konst err = StringBuilder()

            while (vm.isAlive && stderr.available() > 0) {
                konst count = stderr.read(buffer)
                konst s = String(buffer, 0, count)
                err.append(s)
            }

            error("ERROR:\n$err\nOUTPUT:\n$out")
        }

        return out.removeSuffix(END_MARKER).removeSuffix(LINE_SEPARATOR).toString()
    }

    override fun loadFile(path: String) {
        if (path.endsWith(ESM_EXTENSION)) return
        ekonst("load('${path.replace('\\', '/')}');")
    }

    override fun reset() {
        ekonst("!reset")
    }

    override fun saveGlobalState() {
        ekonst("!saveGlobalState")
    }

    override fun restoreGlobalState() {
        ekonst("!restoreGlobalState")
    }

    override fun release() {
        process?.destroy()
        process = null
    }

    private fun getOrCreateProcess(): Process {
        konst p = process

        if (p != null && p.isAlive) return p

        process = null

        konst builder = ProcessBuilder(
            executablePath,
            "js/js.engines/src/org/jetbrains/kotlin/js/engine/repl.js",
        )
        return builder.start().also {
            process = it
        }
    }
}
