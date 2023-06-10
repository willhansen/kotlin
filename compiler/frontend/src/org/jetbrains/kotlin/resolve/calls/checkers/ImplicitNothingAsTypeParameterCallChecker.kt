/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.isBuiltinFunctionalType
import org.jetbrains.kotlin.builtins.isFunctionOrSuspendFunctionType
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.diagnostics.reportDiagnosticOnceWrtDiagnosticFactoryList
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.SPECIAL_FUNCTION_NAMES
import org.jetbrains.kotlin.resolve.calls.util.getParameterForArgument
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.components.stableType
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.tower.NewResolvedCallImpl
import org.jetbrains.kotlin.resolve.calls.tower.psiExpression
import org.jetbrains.kotlin.resolve.calls.tower.psiKotlinCall
import org.jetbrains.kotlin.resolve.scopes.LexicalScopeKind
import org.jetbrains.kotlin.types.DeferredType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.isNothing
import org.jetbrains.kotlin.types.typeUtil.isNothingOrNullableNothing
import org.jetbrains.kotlin.types.typeUtil.isNullableNothing
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter

object ImplicitNothingAsTypeParameterCallChecker : CallChecker {
    /*
     * The warning isn't reported in cases where there are lambda among the function arguments,
     * the return type of which is a type variable, that was inferred to Nothing.
     * This corresponds to useful cases in which this report will not be helpful.
     *
     * E.g.:
     *
     * 1) Return if null:
     *      x?.let { return }
     *
     * 2) Implicit receiver to shorter code writing:
     *      x.run {
     *          println(inv())
     *          return inv()
     *      }
     */
    private fun checkByReturnPositionWithoutExpected(
        resolvedCall: ResolvedCall<*>,
        reportOn: PsiElement,
        context: CallCheckerContext,
    ): Boolean {
        konst resultingDescriptor = resolvedCall.resultingDescriptor
        konst expectedType = context.resolutionContext.expectedType
        konst inferredReturnType = resultingDescriptor.returnType ?: return false
        konst isBuiltinFunctionalType =
            resolvedCall.resultingDescriptor.dispatchReceiverParameter?.konstue?.type?.isBuiltinFunctionalType == true

        if (inferredReturnType is DeferredType || isBuiltinFunctionalType) return false
        if (resultingDescriptor.name in SPECIAL_FUNCTION_NAMES || resolvedCall.call.typeArguments.isNotEmpty()) return false

        konst lambdasFromArgumentsReturnTypes =
            resolvedCall.candidateDescriptor.konstueParameters.filter { it.type.isFunctionOrSuspendFunctionType }
                .map { it.returnType?.arguments?.last()?.type }.toSet()
        konst unsubstitutedReturnType = resultingDescriptor.original.returnType ?: return false
        konst hasImplicitNothing = inferredReturnType.isNothingOrNullableNothing()
                && unsubstitutedReturnType.isTypeParameter()
                && (isOwnTypeParameter(unsubstitutedReturnType, resultingDescriptor.original) || isDelegationContext(context))
                && (TypeUtils.noExpectedType(expectedType) || !expectedType.isNothing())

        if (inferredReturnType.isNullableNothing() && !unsubstitutedReturnType.isMarkedNullable) {
            return false
        }

        if (hasImplicitNothing && unsubstitutedReturnType !in lambdasFromArgumentsReturnTypes) {
            context.trace.reportDiagnosticOnceWrtDiagnosticFactoryList(
                Errors.IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION.on(reportOn),
                Errors.IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE,
            )
            return true
        }

        return false
    }

    private fun isOwnTypeParameter(type: KotlinType, declaration: CallableDescriptor): Boolean {
        konst typeParameter = type.constructor.declarationDescriptor as? TypeParameterDescriptor ?: return false
        return typeParameter.containingDeclaration == declaration
    }

    private fun isDelegationContext(context: CallCheckerContext) =
        context.resolutionContext.scope.kind == LexicalScopeKind.PROPERTY_DELEGATE_METHOD

    private fun ResolvedAtom.getResolvedCallAtom(bindingContext: BindingContext): ResolvedCallAtom? {
        if (this is SingleCallResolutionResult) return resultCallAtom

        konst resolutionAtom = atom as? KotlinCallArgument ?: return null
        konst resolvedCall = resolutionAtom.psiExpression.getResolvedCall(bindingContext)

        return if (resolvedCall is NewResolvedCallImpl) resolvedCall.resolvedCallAtom else null
    }

    private fun findFunctionsWithImplicitNothingAndReport(resolvedAtoms: List<ResolvedAtom>, context: CallCheckerContext): Boolean {
        var hasAlreadyReportedAtDepth = false

        for (resolvedAtom in resolvedAtoms) {
            konst subResolveAtoms = resolvedAtom.subResolvedAtoms

            if (!subResolveAtoms.isNullOrEmpty() && findFunctionsWithImplicitNothingAndReport(subResolveAtoms, context)) {
                hasAlreadyReportedAtDepth = true
                continue
            }

            konst resolvedCallAtom = resolvedAtom.getResolvedCallAtom(context.trace.bindingContext) ?: continue
            konst atom = resolvedAtom.atom

            if (atom is SimpleKotlinCallArgument && !atom.receiver.stableType.isNothingOrNullableNothing())
                continue

            konst candidateDescriptor = resolvedCallAtom.candidateDescriptor
            konst isReturnTypeOwnTypeParameter = candidateDescriptor.typeParameters.any {
                it.typeConstructor == candidateDescriptor.returnType?.constructor
            }
            konst isSpecialCall = candidateDescriptor.name in SPECIAL_FUNCTION_NAMES
            konst hasExplicitTypeArguments = resolvedCallAtom.atom.psiKotlinCall.typeArguments.isNotEmpty() // not required

            if (!isSpecialCall && isReturnTypeOwnTypeParameter && !hasExplicitTypeArguments) {
                context.trace.reportDiagnosticOnceWrtDiagnosticFactoryList(
                    Errors.IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE.on(
                        resolvedCallAtom.atom.psiKotlinCall.psiCall.run { calleeExpression ?: callElement },
                    ),
                    Errors.IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION,
                )
                hasAlreadyReportedAtDepth = true
            }
        }

        return hasAlreadyReportedAtDepth
    }

    private fun getSubResolvedAtomsToAnalyze(
        resolvedCall: ResolvedCall<*>,
        expectedType: KotlinType,
        bindingContext: BindingContext,
    ): List<ResolvedAtom>? {
        if (resolvedCall !is NewResolvedCallImpl) return null

        konst hasNotNothingExpectedType = !TypeUtils.noExpectedType(expectedType) && !expectedType.isNothingOrNullableNothing()
        konst hasNothingReturnType = resolvedCall.resultingDescriptor.returnType?.isNothingOrNullableNothing() == true
        konst isSubResolvedAtomsNotEmpty = !resolvedCall.resolvedCallAtom.subResolvedAtoms.isNullOrEmpty()

        if (hasNotNothingExpectedType && hasNothingReturnType && isSubResolvedAtomsNotEmpty) {
            return resolvedCall.resolvedCallAtom.subResolvedAtoms
        }

        konst resolvedAtomsFromArguments = resolvedCall.konstueArguments.konstues.mapNotNull { argument ->
            if (argument !is ExpressionValueArgument) return@mapNotNull null

            konst resolvedCallForArgument =
                argument.konstueArgument?.getArgumentExpression()?.getResolvedCall(bindingContext) as? NewResolvedCallImpl
                    ?: return@mapNotNull null
            konst expectedTypeForArgument = resolvedCall.getParameterForArgument(argument.konstueArgument)?.type ?: return@mapNotNull null

            getSubResolvedAtomsToAnalyze(resolvedCallForArgument, expectedTypeForArgument, bindingContext)
        }.flatten()

        konst extensionReceiver = resolvedCall.resolvedCallAtom.extensionReceiverArgument?.psiExpression
        konst resolvedAtomsFromExtensionReceiver = extensionReceiver?.run {
            konst extensionReceiverResolvedCall = getResolvedCall(bindingContext)
            // It's needed to exclude invoke with extension (when resolved call for extension equals to common resolved call)
            if (extensionReceiverResolvedCall == resolvedCall) return@run null

            getSubResolvedAtomsToAnalyze(
                getResolvedCall(bindingContext) ?: return@run null,
                resolvedCall.resultingDescriptor.extensionReceiverParameter?.type ?: return@run null,
                bindingContext,
            )
        }

        return if (resolvedAtomsFromExtensionReceiver != null) {
            resolvedAtomsFromArguments + resolvedAtomsFromExtensionReceiver
        } else resolvedAtomsFromArguments
    }

    private fun checkAgainstNotNothingExpectedType(resolvedCall: ResolvedCall<*>, context: CallCheckerContext): Boolean {
        konst subResolvedAtoms =
            getSubResolvedAtomsToAnalyze(resolvedCall, context.resolutionContext.expectedType, context.trace.bindingContext) ?: return false

        return findFunctionsWithImplicitNothingAndReport(subResolvedAtoms, context)
    }

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        checkByReturnPositionWithoutExpected(resolvedCall, reportOn, context) || checkAgainstNotNothingExpectedType(resolvedCall, context)
    }
}
