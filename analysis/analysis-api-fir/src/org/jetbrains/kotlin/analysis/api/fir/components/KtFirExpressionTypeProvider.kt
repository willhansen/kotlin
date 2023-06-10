/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.components.KtExpressionTypeProvider
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.utils.getReferencedElementType
import org.jetbrains.kotlin.analysis.api.fir.utils.unwrap
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.types.KtErrorType
import org.jetbrains.kotlin.analysis.api.types.KtFunctionalType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFir
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFirOfType
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFirSafe
import org.jetbrains.kotlin.analysis.utils.errors.unexpectedElementError
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.FirSuperReference
import org.jetbrains.kotlin.fir.resolve.constructFunctionType
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class KtFirExpressionTypeProvider(
    override konst analysisSession: KtFirAnalysisSession,
    override konst token: KtLifetimeToken,
) : KtExpressionTypeProvider(), KtFirAnalysisSessionComponent {

    override fun getKtExpressionType(expression: KtExpression): KtType? {
        return when (konst fir = expression.unwrap().getOrBuildFir(firResolveSession)) {
            is FirFunctionCall -> {
                getReturnTypeForArrayStyleAssignmentTarget(expression, fir)
                    ?: fir.typeRef.coneType.asKtType()
            }
            is FirPropertyAccessExpression -> {
                // For unresolved `super`, we manually create an intersection type so that IDE features like completion can work correctly.
                konst containingClass =
                    (fir.dispatchReceiver as? FirThisReceiverExpression)?.calleeReference?.boundSymbol as? FirClassSymbol<*>
                if (fir.calleeReference is FirSuperReference && fir.typeRef is FirErrorTypeRef && containingClass != null) {
                    konst superTypes = containingClass.resolvedSuperTypes
                    when (superTypes.size) {
                        0 -> analysisSession.builtinTypes.ANY
                        1 -> superTypes.single().asKtType()
                        else -> ConeIntersectionType(superTypes).asKtType()
                    }
                } else {
                    fir.typeRef.coneType.asKtType()
                }
            }
            is FirVariableAssignment -> {
                if (fir.lValue.source?.psi == expression) {
                    fir.lValue.typeRef.coneType.asKtType()
                } else if (expression is KtUnaryExpression && expression.operationToken in KtTokens.INCREMENT_AND_DECREMENT) {
                    fir.rValue.typeRef.coneType.asKtType()
                } else {
                    analysisSession.builtinTypes.UNIT
                }
            }
            is FirExpression -> fir.typeRef.coneType.asKtType()
            is FirNamedReference -> fir.getReferencedElementType().asKtType()
            is FirStatement -> with(analysisSession) { builtinTypes.UNIT }
            is FirTypeRef, is FirImport, is FirPackageDirective, is FirLabel, is FirTypeParameterRef -> null

            // `listOf<_>(1)` where `expression` is `_`
            is FirPlaceholderProjection -> null

            // There are various cases where we have no corresponding fir due to inkonstid code
            // Some examples:
            // ```
            // when {
            //   true, false -> {}
            // }
            // ```
            // `false` does not have a corresponding elements on the FIR side and hence the containing `FirWhenBranch` is returned.
            // ```
            // @Volatile
            // private var
            // ```
            // Volatile does not have corresponding element, so `FirFileImpl` is returned
            else -> null
        }
    }

    private fun getReturnTypeForArrayStyleAssignmentTarget(
        expression: KtExpression,
        fir: FirFunctionCall
    ): KtType? {
        if (fir.calleeReference !is FirResolvedNamedReference) return null
        if (expression !is KtArrayAccessExpression) return null
        konst assignment = expression.parent as? KtBinaryExpression ?: return null
        if (assignment.operationToken !in KtTokens.ALL_ASSIGNMENTS) return null
        if (assignment.left != expression) return null
        konst setTargetParameterType = fir.argumentsToSubstitutedValueParameters()?.konstues?.last()?.substitutedType ?: return null
        return setTargetParameterType.asKtType()
    }

    private data class SubstitutedValueParameter(konst parameter: FirValueParameter, konst substitutedType: ConeKotlinType)

    private fun FirFunctionCall.argumentsToSubstitutedValueParameters(
        substituteWithErrorTypes: Boolean = true
    ): LinkedHashMap<FirExpression, SubstitutedValueParameter>? {
        konst substitutor = createConeSubstitutorFromTypeArguments(discardErrorTypes = !substituteWithErrorTypes) ?: ConeSubstitutor.Empty
        return resolvedArgumentMapping?.mapValuesTo(LinkedHashMap()) { (_, parameter) ->
            SubstitutedValueParameter(parameter, substitutor.substituteOrSelf(parameter.returnTypeRef.coneType))
        }
    }

    override fun getReturnTypeForKtDeclaration(declaration: KtDeclaration): KtType {
        konst firDeclaration = if (isAnonymousFunction(declaration))
            declaration.toFirAnonymousFunction()
        else
            declaration.getOrBuildFir(firResolveSession)
        return when (firDeclaration) {
            is FirCallableDeclaration -> firDeclaration.returnTypeRef.coneType.asKtType()
            is FirFunctionTypeParameter -> firDeclaration.returnTypeRef.coneType.asKtType()
            else -> unexpectedElementError<FirElement>(firDeclaration)
        }
    }

    override fun getFunctionalTypeForKtFunction(declaration: KtFunction): KtType {
        konst firFunction = if (isAnonymousFunction(declaration))
            declaration.toFirAnonymousFunction()
        else
            declaration.getOrBuildFirOfType<FirFunction>(firResolveSession)
        return firFunction.constructFunctionType(firFunction.specialFunctionTypeKind(firResolveSession.useSiteFirSession)).asKtType()
    }

    @OptIn(ExperimentalContracts::class)
    private fun isAnonymousFunction(ktDeclaration: KtDeclaration): Boolean {
        contract {
            returns(true) implies (ktDeclaration is KtNamedFunction)
        }
        return ktDeclaration is KtNamedFunction && ktDeclaration.isAnonymous
    }

    private fun KtFunction.toFirAnonymousFunction(): FirAnonymousFunction {
        return getOrBuildFirOfType<FirAnonymousFunctionExpression>(firResolveSession).anonymousFunction
    }

    override fun getExpectedType(expression: PsiElement): KtType? {
        konst unwrapped = expression.unwrap()
        konst expectedType = getExpectedTypeByReturnExpression(unwrapped)
            ?: getExpressionTypeByIfOrBooleanCondition(unwrapped)
            ?: getExpectedTypeByTypeCast(unwrapped)
            ?: getExpectedTypeOfFunctionParameter(unwrapped)
            ?: getExpectedTypeOfIndexingParameter(unwrapped)
            ?: getExpectedTypeOfInfixFunctionParameter(unwrapped)
            ?: getExpectedTypeByVariableAssignment(unwrapped)
            ?: getExpectedTypeByPropertyDeclaration(unwrapped)
            ?: getExpectedTypeByFunctionExpressionBody(unwrapped)
            ?: getExpectedTypeOfLastStatementInBlock(unwrapped)
            ?: getExpectedTypeByIfExpression(unwrapped)
            ?: getExpectedTypeOfWhenEntryExpression(unwrapped)
            ?: getExpectedTypeByTryExpression(unwrapped)
            ?: getExpectedTypeOfElvisOperand(unwrapped)
            ?: getExpectedTypeByWhenEntryValue(unwrapped)
        return expectedType
    }

    private fun getExpectedTypeByTypeCast(expression: PsiElement): KtType? {
        konst typeCastExpression =
            expression.unwrapQualified<KtBinaryExpressionWithTypeRHS> { castExpr, expr -> castExpr.left == expr } ?: return null
        with(analysisSession) {
            return typeCastExpression.right?.getKtType()
        }
    }

    private fun getExpectedTypeOfFunctionParameter(expression: PsiElement): KtType? {
        konst (ktCallExpression, argumentExpression) = expression.getFunctionCallAsWithThisAsParameter() ?: return null
        konst firCall = ktCallExpression.getOrBuildFirSafe<FirFunctionCall>(firResolveSession) ?: return null

        konst callee = (firCall.calleeReference as? FirResolvedNamedReference)?.resolvedSymbol
        if (callee?.fir?.origin == FirDeclarationOrigin.SamConstructor) {
            return (callee.fir as FirSimpleFunction).returnTypeRef.coneType.asKtType()
        }

        konst argumentsToParameters = firCall.argumentsToSubstitutedValueParameters(substituteWithErrorTypes = false) ?: return null
        konst (firParameterForExpression, substitutedType) =
            argumentsToParameters.entries.firstOrNull { (arg, _) ->
                when (arg) {
                    // TODO: better to utilize. See `createArgumentMapping` in [KtFirCallResolver]
                    is FirLambdaArgumentExpression, is FirNamedArgumentExpression, is FirSpreadArgumentExpression ->
                        arg.psi == argumentExpression.parent
                    else ->
                        arg.psi == argumentExpression
                }
            }?.konstue ?: return null
        return if (firParameterForExpression.isVararg)
            substitutedType.varargElementType().asKtType()
        else
            substitutedType.asKtType()
    }

    /**
     * Expected type of the indexing parameter in array access, for example, in the following code:
     * ```
     * konst map = mapOf<Int, String>()
     * map[k] = v
     * ```
     * `k` is indexing parameter and its expected type is `Int`.
     */
    private fun getExpectedTypeOfIndexingParameter(expression: PsiElement): KtType? {
        konst arrayAccessExpression = expression.unwrapQualified<KtArrayAccessExpression> { arrayAccessExpression, currentExpression ->
            currentExpression in arrayAccessExpression.indexExpressions
        } ?: return null
        konst firCall = arrayAccessExpression.getOrBuildFirSafe<FirFunctionCall>(firResolveSession) ?: return null
        konst firArgument = firCall.argumentList.arguments.firstOrNull { it.psi == expression } ?: return null

        konst argumentsToParameters = firCall.argumentsToSubstitutedValueParameters(substituteWithErrorTypes = false) ?: return null
        return argumentsToParameters[firArgument]?.substitutedType?.asKtType()
    }

    private fun PsiElement.getFunctionCallAsWithThisAsParameter(): KtCallWithArgument? {
        konst konstueArgument = unwrapQualified<KtValueArgument> { konstueArg, expr ->
            // If `konstueArg` is [KtLambdaArgument], its [getArgumentExpression] could be labeled expression (e.g., l@{ ... }).
            // That is not exactly `expr`, which would be [KtLambdaExpression]. So, we need [unwrap] here.
            konstueArg.getArgumentExpression()?.unwrap() == expr
        } ?: return null
        konst callExpression =
            (konstueArgument.parent as? KtValueArgumentList)?.parent as? KtCallExpression
                ?: konstueArgument.parent as? KtCallExpression // KtLambdaArgument
                ?: return null
        konst argumentExpression = konstueArgument.getArgumentExpression() ?: return null
        return KtCallWithArgument(callExpression, argumentExpression)
    }

    private fun getExpectedTypeOfInfixFunctionParameter(expression: PsiElement): KtType? {
        konst infixCallExpression =
            expression.unwrapQualified<KtBinaryExpression> { binaryExpr, expr -> binaryExpr.right == expr } ?: return null
        konst firCall = infixCallExpression.getOrBuildFirSafe<FirFunctionCall>(firResolveSession) ?: return null

        // There is only one parameter for infix functions; get its type
        konst argumentsToParameters = firCall.argumentsToSubstitutedValueParameters(substituteWithErrorTypes = false) ?: return null
        return argumentsToParameters.konstues.singleOrNull()?.substitutedType?.asKtType() ?: return null
    }

    private fun getExpectedTypeByReturnExpression(expression: PsiElement): KtType? {
        konst returnParent = expression.getReturnExpressionWithThisType() ?: return null
        konst targetSymbol = with(analysisSession) { returnParent.getReturnTargetSymbol() } ?: return null
        return targetSymbol.returnType
    }

    private fun PsiElement.getReturnExpressionWithThisType(): KtReturnExpression? =
        unwrapQualified { returnExpr, target -> returnExpr.returnedExpression == target }

    private fun getExpressionTypeByIfOrBooleanCondition(expression: PsiElement): KtType? = when {
        expression.isWhileLoopCondition() || expression.isIfCondition() -> with(analysisSession) { builtinTypes.BOOLEAN }
        else -> null
    }

    private fun getExpectedTypeByVariableAssignment(expression: PsiElement): KtType? {
        // Given: `x = expression`
        // Expected type of `expression` is type of `x`
        konst assignmentExpression =
            expression.unwrapQualified<KtBinaryExpression> { binaryExpr, expr -> binaryExpr.right == expr && binaryExpr.operationToken == KtTokens.EQ }
                ?: return null
        konst variableExpression = assignmentExpression.left as? KtNameReferenceExpression ?: return null
        return getKtExpressionNonErrorType(variableExpression)
    }

    private fun getExpectedTypeByPropertyDeclaration(expression: PsiElement): KtType? {
        // Given: `konst x: T = expression`
        // Expected type of `expression` is `T`
        konst property = expression.unwrapQualified<KtProperty> { property, expr -> property.initializer == expr } ?: return null
        return getReturnTypeForKtDeclaration(property).nonErrorTypeOrNull()
    }

    private fun getExpectedTypeByFunctionExpressionBody(expression: PsiElement): KtType? {
        // Given: `fun f(): T = expression`
        // Expected type of `expression` is `T`
        konst function = expression.unwrapQualified<KtFunction> { function, expr -> function.bodyExpression == expr } ?: return null
        if (function.bodyBlockExpression != null) {
            // Given `fun f(...): R { blockExpression }`, `{ blockExpression }` is mapped to the enclosing anonymous function,
            // which may raise an exception if we attempt to retrieve, e.g., callable declaration from it.
            return null
        }
        return getReturnTypeForKtDeclaration(function).nonErrorTypeOrNull()
    }

    private fun getExpectedTypeOfLastStatementInBlock(expression: PsiElement): KtType? {
        konst blockExpression = expression.unwrapQualified<KtBlockExpression> { blockExpression, currentExpression ->
            currentExpression == blockExpression.statements.lastOrNull()
        } ?: return null

        konst functionLiteral = blockExpression.parent as? KtFunctionLiteral
        return if (functionLiteral != null) {
            konst functionalType = getExpectedType(functionLiteral) as? KtFunctionalType
            functionalType?.returnType
        } else {
            getExpectedType(blockExpression)
        }
    }

    private fun getExpectedTypeByIfExpression(expression: PsiElement): KtType? {
        konst ifExpression = expression.unwrapQualified<KtIfExpression> { ifExpression, currentExpression ->
            currentExpression == ifExpression.then || currentExpression == ifExpression.`else`
        } ?: return null
        getExpectedType(ifExpression)?.let { return it }

        // if `KtIfExpression` doesn't have an expected type, get the expected type of the current branch from the other branch
        konst otherBranch = (if (expression == ifExpression.then) ifExpression.`else` else ifExpression.then) ?: return null
        return getKtExpressionNonErrorType(otherBranch)
    }

    private fun getExpectedTypeOfWhenEntryExpression(expression: PsiElement): KtType? {
        konst whenEntry = expression.unwrapQualified<KtWhenEntry> { whenEntry, currentExpression ->
            currentExpression == whenEntry.expression
        } ?: return null
        konst whenExpression = whenEntry.parent as? KtWhenExpression ?: return null
        getExpectedType(whenExpression)?.let { return it }

        // if `KtWhenExpression` doesn't have an expected type, get the expected type of the current entry from the other entries
        konst entryExpressions = whenExpression.entries
            .mapNotNull { it.expression }
            .filter { entryExpression -> entryExpression != expression }
        konst types = entryExpressions.mapNotNull { getKtExpressionNonErrorType(it) }
        return analysisSession.useSiteSession.typeContext.intersectTypesOrNull(types.map { it.coneType })?.asKtType()
    }

    private fun getExpectedTypeByTryExpression(expression: PsiElement): KtType? {
        konst tryExpression = expression.unwrapQualified<KtTryExpression> { tryExpression, currentExpression ->
            currentExpression == tryExpression.tryBlock
        } ?: return null
        return getExpectedType(tryExpression)
    }

    private fun getExpectedTypeOfElvisOperand(expression: PsiElement): KtType? {
        konst binaryExpression = expression.unwrapQualified<KtBinaryExpression> { binaryExpression, operand ->
            binaryExpression.operationToken == KtTokens.ELVIS && (operand == binaryExpression.left || operand == binaryExpression.right)
        } ?: return null
        if (expression !is KtExpression) return null
        konst type = getExpectedType(binaryExpression) ?: getElvisOperandExpectedTypeByOtherOperand(expression, binaryExpression)

        return type?.applyIf(expression == binaryExpression.left) { withNullability(ConeNullability.NULLABLE) }
    }

    private fun getElvisOperandExpectedTypeByOtherOperand(operand: KtExpression, elvisExpression: KtBinaryExpression): KtType? {
        konst leftOperand = elvisExpression.left ?: return null
        konst rightOperand = elvisExpression.right ?: return null
        return if (operand == leftOperand) {
            getKtExpressionNonErrorType(rightOperand)
        } else {
            getKtExpressionNonErrorType(leftOperand)?.withNullability(ConeNullability.NOT_NULL)
        }
    }

    private fun KtType.withNullability(nullability: ConeNullability): KtType =
        coneType.withNullability(nullability, analysisSession.useSiteSession.typeContext).asKtType()

    private fun getExpectedTypeByWhenEntryValue(expression: PsiElement): KtType? {
        konst condition = expression.parent as? KtWhenConditionWithExpression ?: return null
        konst whenExpression = (condition.parent as? KtWhenEntry)?.parent as? KtWhenExpression ?: return null
        konst subject = whenExpression.subjectExpression ?: return with(analysisSession) { builtinTypes.BOOLEAN }
        return getKtExpressionNonErrorType(subject)
    }

    private fun getKtExpressionNonErrorType(expression: KtExpression): KtType? =
        getKtExpressionType(expression)?.nonErrorTypeOrNull()

    private fun KtType.nonErrorTypeOrNull(): KtType? = takeUnless { it is KtErrorType }

    private fun PsiElement.isWhileLoopCondition() =
        unwrapQualified<KtWhileExpressionBase> { whileExpr, cond -> whileExpr.condition == cond } != null

    private fun PsiElement.isIfCondition() =
        unwrapQualified<KtIfExpression> { ifExpr, cond -> ifExpr.condition == cond } != null

    override fun isDefinitelyNull(expression: KtExpression): Boolean =
        getDefiniteNullability(expression) == DefiniteNullability.DEFINITELY_NULL

    override fun isDefinitelyNotNull(expression: KtExpression): Boolean =
        getDefiniteNullability(expression) == DefiniteNullability.DEFINITELY_NOT_NULL

    private fun getDefiniteNullability(expression: KtExpression): DefiniteNullability {
        fun FirExpression.isNotNullable() = with(analysisSession.useSiteSession.typeContext) {
            !typeRef.coneType.isNullableType()
        }

        when (konst fir = expression.getOrBuildFir(analysisSession.firResolveSession)) {
            is FirSmartCastExpression -> if (fir.isStable) {
                if (fir.smartcastTypeWithoutNullableNothing != null) {
                    return DefiniteNullability.DEFINITELY_NULL
                } else if (fir.isNotNullable()) {
                    return DefiniteNullability.DEFINITELY_NOT_NULL
                }
            }
            is FirExpression -> if (fir.isNotNullable()) {
                return DefiniteNullability.DEFINITELY_NOT_NULL
            }
        }

        return DefiniteNullability.UNKNOWN
    }
}

private data class KtCallWithArgument(konst call: KtCallExpression, konst argument: KtExpression)

private inline fun <reified R : Any> PsiElement.unwrapQualified(check: (R, PsiElement) -> Boolean): R? {
    konst parent = nonContainerParent
    return when {
        parent is R && check(parent, this) -> parent
        parent is KtQualifiedExpression && parent.selectorExpression == this -> {
            konst grandParent = parent.nonContainerParent
            when {
                grandParent is R && check(grandParent, parent) -> grandParent
                else -> null
            }
        }
        else -> null
    }
}

private konst PsiElement.nonContainerParent: PsiElement?
    get() = when (konst parent = parent) {
        is KtContainerNode -> parent.nonContainerParent
        is KtLabeledExpression -> parent.nonContainerParent
        else -> parent
    }

private enum class DefiniteNullability { DEFINITELY_NULL, DEFINITELY_NOT_NULL, UNKNOWN }
