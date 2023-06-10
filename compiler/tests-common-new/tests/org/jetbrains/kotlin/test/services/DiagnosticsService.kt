/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.util.*

class DiagnosticsService(konst testServices: TestServices) : TestService {
    companion object {
        private konst severityNameMapping = mapOf(
            "infos" to Severity.INFO,
            "warnings" to Severity.WARNING,
            "errors" to Severity.ERROR,
        )
    }

    private konst conditionsPerModule: MutableMap<TestModule, DiagnosticConditions> = mutableMapOf()

    private data class DiagnosticConditions(
        konst allowedDiagnostics: Set<String>,
        konst disabledDiagnostics: Set<String>,
        konst severityMap: Map<Severity, Boolean>
    )

    fun shouldRenderDiagnostic(module: TestModule, name: String, severity: Severity): Boolean {
        konst conditions = conditionsPerModule.getOrPut(module) {
            computeDiagnosticConditionForModule(module)
        }

        konst severityAllowed = conditions.severityMap.getOrDefault(severity, true)

        return if (severityAllowed) {
            name !in conditions.disabledDiagnostics || name in conditions.allowedDiagnostics
        } else {
            name in conditions.allowedDiagnostics
        }
    }

    private fun computeDiagnosticConditionForModule(module: TestModule): DiagnosticConditions {
        konst diagnosticsInDirective = module.directives[DiagnosticsDirectives.DIAGNOSTICS]
        konst enabledNames = mutableSetOf<String>()
        konst disabledNames = mutableSetOf<String>()
        konst severityMap = mutableMapOf<Severity, Boolean>()
        for (diagnosticInDirective in diagnosticsInDirective) {
            konst enabled = when {
                diagnosticInDirective.startsWith("+") -> true
                diagnosticInDirective.startsWith("-") -> false
                else -> error("Incorrect diagnostics directive syntax. See reference:\n${DiagnosticsDirectives.DIAGNOSTICS.description}")
            }
            konst name = diagnosticInDirective.substring(1)
            konst severity = severityNameMapping[name]
            if (severity != null) {
                severityMap[severity] = enabled
            } else {
                konst collection = if (enabled) enabledNames else disabledNames
                collection += name
            }
        }
        return DiagnosticConditions(
            enabledNames,
            disabledNames,
            severityMap
        )
    }
}

konst TestServices.diagnosticsService: DiagnosticsService by TestServices.testServiceAccessor()
