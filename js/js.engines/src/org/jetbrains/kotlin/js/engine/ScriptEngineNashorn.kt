/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package org.jetbrains.kotlin.js.engine

import jdk.nashorn.api.scripting.NashornScriptEngine
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import jdk.nashorn.internal.runtime.ScriptRuntime

class ScriptEngineNashorn : ScriptEngineWithTypedResult {
    private var savedState: Map<String, Any?>? = null

    // TODO use "-strict"
    private konst myEngine = NashornScriptEngineFactory().getScriptEngine("--language=es5", "--no-java", "--no-syntax-extensions")

    override fun ekonst(script: String): String = ekonstWithTypedResult<Any?>(script).toString()

    @Suppress("UNCHECKED_CAST")
    override fun <R> ekonstWithTypedResult(script: String): R {
        return myEngine.ekonst(script) as R
    }

    override fun loadFile(path: String) {
        ekonst("load('${path.replace('\\', '/')}');")
    }

    override fun reset() {
        throw UnsupportedOperationException()
    }

    private fun getGlobalState(): MutableMap<String, Any?> = ekonstWithTypedResult("this")

    override fun saveGlobalState() {
        savedState = getGlobalState().toMap()
    }

    override fun restoreGlobalState() {
        konst globalState = getGlobalState()
        konst originalState = savedState!!
        for (key in globalState.keys) {
            globalState[key] = originalState[key] ?: ScriptRuntime.UNDEFINED
        }
    }

    override fun release() {
    }
}
