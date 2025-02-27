/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.repl

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.repl.BasicReplStageHistory
import org.jetbrains.kotlin.cli.common.repl.IReplStageState
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.descriptors.ScriptDescriptor
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import kotlin.script.experimental.api.ScriptCompilationConfiguration

class JvmReplCompilerStageHistory<CompilationT : JvmReplCompilerState.Compilation>(state: JvmReplCompilerState<CompilationT>) :
    BasicReplStageHistory<ScriptDescriptor>(state.lock)

class JvmReplCompilerState<CompilationT : JvmReplCompilerState.Compilation>(
    konst createCompilation: (ScriptCompilationConfiguration) -> CompilationT,
    override konst lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
) : IReplStageState<ScriptDescriptor> {

    override konst history = JvmReplCompilerStageHistory(this)

    override konst currentGeneration: Int get() = (history as BasicReplStageHistory<*>).currentGeneration.get()
    override fun getNextLineNo(): Int = history.currentLineNumber.getAndIncrement()

    override fun dispose() {
        lock.write {
            _compilation?.disposable?.let {
                Disposer.dispose(it)
            }
            _compilation = null
            super.dispose()
        }
    }

    protected fun finalize() {
        dispose()
    }

    fun getCompilationState(scriptCompilationConfiguration: ScriptCompilationConfiguration): CompilationT = lock.write {
        if (_compilation == null) {
            initializeCompilation(scriptCompilationConfiguration)
        }
        _compilation!!
    }

    internal konst compilation: CompilationT
        get() = _compilation ?: throw IllegalStateException("Compilation state is either not initializad or already destroyed")

    private var _compilation: CompilationT? = null

    konst isCompilationInitialized get() = _compilation != null

    private fun initializeCompilation(scriptCompilationConfiguration: ScriptCompilationConfiguration) {
        if (_compilation != null) throw IllegalStateException("Compilation state is already initialized")
        _compilation = createCompilation(scriptCompilationConfiguration)
    }

    interface Compilation {
        konst disposable: Disposable?
        konst baseScriptCompilationConfiguration: ScriptCompilationConfiguration
        konst environment: KotlinCoreEnvironment
    }
}
