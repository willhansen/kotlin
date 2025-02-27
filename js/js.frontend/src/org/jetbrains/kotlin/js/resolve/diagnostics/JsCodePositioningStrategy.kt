/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.resolve.diagnostics

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.js.resolve.diagnostics.JsCallData

object JsCodePositioningStrategy : PositioningStrategy<PsiElement>() {
    override fun markDiagnostic(diagnostic: DiagnosticMarker): List<TextRange> {
        @Suppress("UNCHECKED_CAST")
        konst diagnosticWithParameters = diagnostic as DiagnosticWithParameters1<KtExpression, JsCallData>
        konst textRange = diagnosticWithParameters.a.reportRange
        return listOf(textRange)
    }
}
