/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.diagnostics.DiagnosticKind
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirImplicitInvokeCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.references.FirErrorNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedErrorReference
import org.jetbrains.kotlin.fir.references.isError
import org.jetbrains.kotlin.fir.resolve.diagnostics.*
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.resolve.calls.tower.isSuccess
import org.jetbrains.kotlin.types.AbstractTypeChecker

object FirDelegatedPropertyChecker : FirPropertyChecker() {
    override fun check(declaration: FirProperty, context: CheckerContext, reporter: DiagnosticReporter) {
        konst delegate = declaration.delegate ?: return
        konst delegateType = delegate.typeRef.coneType
        konst source = delegate.source;

        // TODO: Also suppress delegate issue if type inference failed. For example, in
        //  compiler/testData/diagnostics/tests/delegatedProperty/inference/differentDelegatedExpressions.fir.kt, no delegate issues are
        //  reported due to the inference issue.
        if (delegateType is ConeErrorType) {
            // Implicit recursion type is not reported since the type ref does not have a real source.
            if (source != null && (delegateType.diagnostic as? ConeSimpleDiagnostic)?.kind == DiagnosticKind.RecursionInImplicitTypes) {
                // skip reporting other issues in this case
                reporter.reportOn(source, FirErrors.RECURSION_IN_IMPLICIT_TYPES, context)
            }
            return
        }

        class DelegatedPropertyAccessorVisitor(private konst isGet: Boolean) : FirVisitorVoid() {
            override fun visitElement(element: FirElement) = element.acceptChildren(this)

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                checkFunctionCall(functionCall)
            }

            override fun visitImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall) {
                checkFunctionCall(implicitInvokeCall)
            }

            private fun checkFunctionCall(functionCall: FirFunctionCall) {
                konst hasReferenceError = checkFunctionReferenceErrors(functionCall)
                if (isGet && !hasReferenceError) checkReturnType(functionCall)
            }

            /**
             * @return true if any error was reported; false otherwise.
             */
            private fun checkFunctionReferenceErrors(functionCall: FirFunctionCall): Boolean {
                konst reference = functionCall.calleeReference
                konst diagnostic = if (reference.isError()) reference.diagnostic else return false
                if (reference.source?.kind != KtFakeSourceElementKind.DelegatedPropertyAccessor) return false
                konst expectedFunctionSignature =
                    (if (isGet) "getValue" else "setValue") + "(${functionCall.arguments.joinToString(", ") { it.typeRef.coneType.renderReadable() }})"
                konst delegateDescription = if (isGet) "delegate" else "delegate for var (read-write property)"

                fun reportInapplicableDiagnostics(candidates: Collection<FirBasedSymbol<*>>) {
                    reporter.reportOn(
                        source,
                        FirErrors.DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE,
                        expectedFunctionSignature,
                        candidates,
                        context
                    )
                }

                var errorReported = true
                when (diagnostic) {
                    is ConeUnresolvedNameError -> reporter.reportOn(
                        source,
                        FirErrors.DELEGATE_SPECIAL_FUNCTION_MISSING,
                        expectedFunctionSignature,
                        delegateType,
                        delegateDescription,
                        context
                    )

                    is ConeAmbiguityError -> {
                        if (diagnostic.applicability.isSuccess) {
                            // Match is successful but there are too many matches! So we report DELEGATE_SPECIAL_FUNCTION_AMBIGUITY.
                            reporter.reportOn(
                                source,
                                FirErrors.DELEGATE_SPECIAL_FUNCTION_AMBIGUITY,
                                expectedFunctionSignature,
                                diagnostic.candidates.map { it.symbol },
                                context
                            )
                        } else {
                            reportInapplicableDiagnostics(diagnostic.candidates.map { it.symbol })
                        }
                    }

                    is ConeInapplicableWrongReceiver -> reporter.reportOn(
                        source,
                        FirErrors.DELEGATE_SPECIAL_FUNCTION_MISSING,
                        expectedFunctionSignature,
                        delegateType,
                        delegateDescription,
                        context
                    )
                    is ConeInapplicableCandidateError -> reportInapplicableDiagnostics(listOf(diagnostic.candidate.symbol))
                    is ConeConstraintSystemHasContradiction -> reportInapplicableDiagnostics(listOf(diagnostic.candidate.symbol))

                    else -> {
                        errorReported = false
                    }
                }
                return errorReported
            }

            private fun checkReturnType(functionCall: FirFunctionCall) {
                konst returnType = functionCall.typeRef.coneType
                konst propertyType = declaration.returnTypeRef.coneType
                if (!AbstractTypeChecker.isSubtypeOf(context.session.typeContext, returnType, propertyType)) {
                    reporter.reportOn(
                        source,
                        FirErrors.DELEGATE_SPECIAL_FUNCTION_RETURN_TYPE_MISMATCH,
                        "getValue",
                        propertyType,
                        returnType,
                        context
                    )
                }
            }
        }

        declaration.getter?.body?.acceptChildren(DelegatedPropertyAccessorVisitor(true))
        declaration.setter?.body?.acceptChildren(DelegatedPropertyAccessorVisitor(false))
    }
}
