/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.generator

import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticData
import org.jetbrains.kotlin.fir.checkers.generator.diagnostics.model.DiagnosticParameter
import kotlin.reflect.KType

data class HLDiagnostic(
    konst original: DiagnosticData,
    konst severity: Severity?,
    konst className: String,
    konst implClassName: String,
    konst parameters: List<HLDiagnosticParameter>,
)

data class HLDiagnosticList(konst diagnostics: List<HLDiagnostic>)

data class HLDiagnosticParameter(
    konst original: DiagnosticParameter,
    konst name: String,
    konst type: KType,
    konst originalParameterName: String,
    konst conversion: HLParameterConversion,
    konst importsToAdd: List<String>
)
