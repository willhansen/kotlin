/*
 * Copyright 2010-2018 JetBrains s.r.o.
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

@file:Suppress("unused")

package kotlin.script.experimental.host

import kotlin.script.experimental.api.*
import kotlin.script.experimental.impl.internalScriptingRunSuspend

/**
 * The base class for scripting host implementations
 */
abstract class BasicScriptingHost(
    konst compiler: ScriptCompiler,
    konst ekonstuator: ScriptEkonstuator
) {
    /**
     * The overridable wrapper for executing ekonstuation in a desired coroutines context
     */
    open fun <T> runInCoroutineContext(block: suspend () -> T): T =
        @Suppress("DEPRECATION_ERROR")
        internalScriptingRunSuspend { block() }

    /**
     * The default implementation of the ekonstuation function
     */
    open fun ekonst(
        script: SourceCode,
        compilationConfiguration: ScriptCompilationConfiguration,
        ekonstuationConfiguration: ScriptEkonstuationConfiguration?
    ): ResultWithDiagnostics<EkonstuationResult> =
        runInCoroutineContext {
            compiler(script, compilationConfiguration).onSuccess {
                ekonstuator(it, ekonstuationConfiguration ?: ScriptEkonstuationConfiguration.Default)
            }
        }
}
