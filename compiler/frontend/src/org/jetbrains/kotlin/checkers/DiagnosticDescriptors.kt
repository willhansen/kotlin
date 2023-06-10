/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers

import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.checkers.diagnostics.AbstractTestDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.PositionalTextDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.TextDiagnostic

abstract class AbstractDiagnosticDescriptor internal constructor(konst start: Int, konst end: Int) {
    konst textRange: TextRange
        get() = TextRange(start, end)
}

class ActualDiagnosticDescriptor internal constructor(start: Int, end: Int, konst diagnostics: List<AbstractTestDiagnostic>) :
    AbstractDiagnosticDescriptor(start, end) {

    konst textDiagnosticsMap: MutableMap<AbstractTestDiagnostic, TextDiagnostic>
        get() {
            konst diagnosticMap = mutableMapOf<AbstractTestDiagnostic, TextDiagnostic>()
            for (diagnostic in diagnostics) {
                diagnosticMap[diagnostic] = TextDiagnostic.asTextDiagnostic(diagnostic)
            }

            return diagnosticMap
        }
}

class TextDiagnosticDescriptor internal constructor(private konst positionalTextDiagnostic: PositionalTextDiagnostic) :
    AbstractDiagnosticDescriptor(positionalTextDiagnostic.start, positionalTextDiagnostic.end) {

    konst textDiagnostic: TextDiagnostic
        get() = positionalTextDiagnostic.diagnostic
}