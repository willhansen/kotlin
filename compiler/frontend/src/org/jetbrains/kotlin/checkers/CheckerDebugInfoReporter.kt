/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.checkers.diagnostics.ActualDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.DebugInfoDiagnostic
import org.jetbrains.kotlin.checkers.diagnostics.factories.DebugInfoDiagnosticFactory0
import org.jetbrains.kotlin.checkers.utils.DebugInfoUtil
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtReferenceExpression

class CheckerDebugInfoReporter(
    private konst dynamicCallDescriptors: MutableList<DeclarationDescriptor>,
    private konst markDynamicCalls: Boolean,
    private konst debugAnnotations: MutableList<ActualDiagnostic>,
    private konst withNewInference: Boolean,
    private konst platform: String?
) : DebugInfoUtil.DebugInfoReporter() {
    override fun reportElementWithErrorType(expression: KtReferenceExpression) {
        newDiagnostic(
            expression,
            DebugInfoDiagnosticFactory0.ELEMENT_WITH_ERROR_TYPE
        )
    }

    override fun reportMissingUnresolved(expression: KtReferenceExpression) {
        newDiagnostic(
            expression,
            DebugInfoDiagnosticFactory0.MISSING_UNRESOLVED
        )
    }

    override fun reportUnresolvedWithTarget(
        expression: KtReferenceExpression,
        target: String
    ) {
        newDiagnostic(expression, DebugInfoDiagnosticFactory0.UNRESOLVED_WITH_TARGET)
    }

    override fun reportDynamicCall(
        element: KtElement,
        declarationDescriptor: DeclarationDescriptor
    ) {
        dynamicCallDescriptors.add(declarationDescriptor)

        if (markDynamicCalls) {
            newDiagnostic(element, DebugInfoDiagnosticFactory0.DYNAMIC)
        }
    }

    private fun newDiagnostic(
        element: KtElement,
        factory: DebugInfoDiagnosticFactory0
    ) {
        debugAnnotations.add(
            ActualDiagnostic(
                DebugInfoDiagnostic(element, factory), platform, withNewInference
            )
        )
    }
}
