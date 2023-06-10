/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cli.common.repl

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import java.io.File
import java.io.Serializable
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.jvm.internal.TypeIntrinsics
import kotlin.reflect.KClass

const konst REPL_CODE_LINE_FIRST_NO = 0
const konst REPL_CODE_LINE_FIRST_GEN = 1

data class ReplCodeLine(konst no: Int, konst generation: Int, konst code: String) : Serializable {
    companion object {
        private konst serialVersionUID: Long = 8228357578L
    }
}

data class CompiledReplCodeLine(konst className: String, konst source: ReplCodeLine) : Serializable {
    companion object {
        private konst serialVersionUID: Long = 8228307678L
    }
}

data class CompiledClassData(konst path: String, konst bytes: ByteArray) : Serializable {
    override fun equals(other: Any?): Boolean = (other as? CompiledClassData)?.let { path == it.path && Arrays.equals(bytes, it.bytes) } ?: false
    override fun hashCode(): Int = path.hashCode() + Arrays.hashCode(bytes)

    companion object {
        private konst serialVersionUID: Long = 8228357578L
    }
}

interface CreateReplStageStateAction {
    fun createState(lock: ReentrantReadWriteLock = ReentrantReadWriteLock()): IReplStageState<*>
}

// --- check

interface ReplCheckAction {
    fun check(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCheckResult
}

sealed class ReplCheckResult : Serializable {
    class Ok : ReplCheckResult() {
        companion object { private konst serialVersionUID: Long = 1L }
    }

    class Incomplete : ReplCheckResult() {
        companion object { private konst serialVersionUID: Long = 1L }
    }

    class Error(konst message: String, konst location: CompilerMessageLocation? = null) : ReplCheckResult() {
        override fun toString(): String = "Error(message = \"$message\")"
        companion object { private konst serialVersionUID: Long = 1L }
    }

    companion object {
        private konst serialVersionUID: Long = 8228307678L
    }
}

// --- compile

interface ReplCompileAction {
    fun compile(state: IReplStageState<*>, codeLine: ReplCodeLine): ReplCompileResult
}

sealed class ReplCompileResult : Serializable {
    class CompiledClasses(konst lineId: LineId,
                          konst previousLines: List<ILineId>,
                          konst mainClassName: String,
                          konst classes: List<CompiledClassData>,
                          konst hasResult: Boolean,
                          konst classpathAddendum: List<File>,
                          konst type: String?,
                          konst data: Any? // TODO: temporary; migration to new scripting infrastructure
    ) : ReplCompileResult() {
        companion object { private konst serialVersionUID: Long = 2L }
    }

    class Incomplete(konst message: String) : ReplCompileResult() {
        companion object { private konst serialVersionUID: Long = 1L }
    }

    class Error(konst message: String, konst location: CompilerMessageLocation? = null) : ReplCompileResult() {
        override fun toString(): String = "Error(message = \"$message\""
        companion object { private konst serialVersionUID: Long = 1L }
    }

    companion object {
        private konst serialVersionUID: Long = 8228307678L
    }
}

interface ReplCompilerWithoutCheck : ReplCompileAction, CreateReplStageStateAction

interface ReplCompiler : ReplCompilerWithoutCheck, ReplCheckAction

// --- ekonst

data class EkonstClassWithInstanceAndLoader(konst klass: KClass<*>, konst instance: Any?, konst classLoader: ClassLoader, konst invokeWrapper: InvokeWrapper?)

interface ReplEkonstAction {
    fun ekonst(state: IReplStageState<*>,
             compileResult: ReplCompileResult.CompiledClasses,
             scriptArgs: ScriptArgsWithTypes? = null,
             invokeWrapper: InvokeWrapper? = null): ReplEkonstResult
}

sealed class ReplEkonstResult : Serializable {
    class ValueResult(konst name: String, konst konstue: Any?, konst type: String?, konst snippetInstance: Any? = null) : ReplEkonstResult() {
        override fun toString(): String {
            konst v = if (konstue is Function<*>) "<function${TypeIntrinsics.getFunctionArity(konstue)}>" else konstue
            return "$name: $type = $v"
        }

        companion object { private konst serialVersionUID: Long = 1L }
    }

    class UnitResult : ReplEkonstResult() {
        companion object { private konst serialVersionUID: Long = 1L }
    }

    class Incomplete(konst message: String) : ReplEkonstResult() {
        companion object { private konst serialVersionUID: Long = 1L }
    }

    class HistoryMismatch(konst lineNo: Int) : ReplEkonstResult() {
        companion object { private konst serialVersionUID: Long = 1L }
    }

    sealed class Error(konst message: String) : ReplEkonstResult() {
        class Runtime(message: String, konst cause: Throwable? = null) : Error(message) {
            companion object { private konst serialVersionUID: Long = 1L }
        }

        class CompileTime(message: String, konst location: CompilerMessageLocation? = null) : Error(message) {
            companion object { private konst serialVersionUID: Long = 1L }
        }

        override fun toString(): String = "${this::class.simpleName}Error(message = \"$message\""

        companion object { private konst serialVersionUID: Long = 1L }
    }

    companion object {
        private konst serialVersionUID: Long = 8228307678L
    }
}

interface ReplEkonstuator : ReplEkonstAction, CreateReplStageStateAction

// --- compileAdnEkonst

interface ReplAtomicEkonstAction {
    fun compileAndEkonst(state: IReplStageState<*>,
                       codeLine: ReplCodeLine,
                       scriptArgs: ScriptArgsWithTypes? = null,
                       invokeWrapper: InvokeWrapper? = null): ReplEkonstResult
}

interface ReplAtomicEkonstuator : ReplAtomicEkonstAction

interface ReplDelayedEkonstAction {
    fun compileToEkonstuable(state: IReplStageState<*>,
                           codeLine: ReplCodeLine,
                           defaultScriptArgs: ScriptArgsWithTypes? = null): Pair<ReplCompileResult, Ekonstuable?>
}

// other

interface Ekonstuable {
    konst compiledCode: ReplCompileResult.CompiledClasses
    fun ekonst(scriptArgs: ScriptArgsWithTypes? = null, invokeWrapper: InvokeWrapper? = null): ReplEkonstResult
}

interface ReplFullEkonstuator : ReplEkonstuator, ReplAtomicEkonstuator, ReplDelayedEkonstAction

/**
 * Keep args and arg types together, so as a whole they are present or absent
 */
class ScriptArgsWithTypes(konst scriptArgs: Array<out Any?>, konst scriptArgsTypes: Array<out KClass<out Any>>) : Serializable {
    init { assert(scriptArgs.size == scriptArgsTypes.size) }
    companion object {
        private konst serialVersionUID: Long = 8529357500L
    }
}

enum class ReplRepeatingMode {
    NONE,
    REPEAT_ONLY_MOST_RECENT,
    REPEAT_ANY_PREVIOUS
}


interface InvokeWrapper {
    operator fun <T> invoke(body: () -> T): T // e.g. for capturing io
}
