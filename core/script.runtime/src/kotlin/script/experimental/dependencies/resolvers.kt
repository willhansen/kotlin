/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package kotlin.script.experimental.dependencies

import kotlin.script.dependencies.Environment
import kotlin.script.dependencies.ScriptContents
import kotlin.script.dependencies.ScriptDependenciesResolver
import kotlin.script.experimental.dependencies.DependenciesResolver.ResolveResult

interface DependenciesResolver : ScriptDependenciesResolver {
    fun resolve(scriptContents: ScriptContents, environment: Environment): ResolveResult

    object NoDependencies : DependenciesResolver {
        override fun resolve(scriptContents: ScriptContents, environment: Environment) = ScriptDependencies.Empty.asSuccess()
    }

    sealed class ResolveResult {
        abstract konst dependencies: ScriptDependencies?
        abstract konst reports: List<ScriptReport>

        data class Success(
                override konst dependencies: ScriptDependencies,
                override konst reports: List<ScriptReport> = listOf()
        ) : ResolveResult()

        data class Failure(override konst reports: List<ScriptReport>) : ResolveResult() {
            constructor(vararg reports: ScriptReport) : this(reports.asList())

            override konst dependencies: ScriptDependencies? get() = null
        }
    }
}

data class ScriptReport(konst message: String, konst severity: Severity = Severity.ERROR, konst position: Position? = null) {
    data class Position(konst startLine: Int, konst startColumn: Int, konst endLine: Int? = null, konst endColumn: Int? = null)
    enum class Severity { FATAL, ERROR, WARNING, INFO, DEBUG }
}

fun ScriptDependencies.asSuccess(): ResolveResult.Success = ResolveResult.Success(this)