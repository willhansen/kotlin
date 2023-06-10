/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isFunctionalExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.StatementFilter
import org.jetbrains.kotlin.resolve.TypeResolver
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver
import org.jetbrains.kotlin.resolve.calls.components.InferenceSession
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.lazy.ForceResolveUtil
import org.jetbrains.kotlin.resolve.scopes.receivers.*
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.expressions.KotlinTypeInfo

class SimpleTypeArgumentImpl(
    konst typeProjection: KtTypeProjection,
    override konst type: UnwrappedType
) : SimpleTypeArgument

// all arguments should be inherited from this class.
// But receivers is not, because for them there is no corresponding konstueArgument
abstract class PSIKotlinCallArgument : KotlinCallArgument {
    abstract konst konstueArgument: ValueArgument
    abstract konst dataFlowInfoBeforeThisArgument: DataFlowInfo
    abstract konst dataFlowInfoAfterThisArgument: DataFlowInfo

    override fun toString() = konstueArgument.getArgumentExpression()?.text?.replace('\n', ' ') ?: konstueArgument.toString()
}

abstract class SimplePSIKotlinCallArgument : PSIKotlinCallArgument(), SimpleKotlinCallArgument

konst KotlinCallArgument.psiCallArgument: PSIKotlinCallArgument
    get() {
        assert(this is PSIKotlinCallArgument) {
            "Incorrect KotlinCallArgument: $this. Java class: ${javaClass.canonicalName}"
        }
        return this as PSIKotlinCallArgument
    }

konst KotlinCallArgument.psiExpression: KtExpression?
    get() {
        return when (this) {
            is ReceiverExpressionKotlinCallArgument -> (receiver.receiverValue as? ExpressionReceiver)?.expression
            is QualifierReceiverKotlinCallArgument -> (receiver as? Qualifier)?.expression
            is EmptyLabeledReturn -> returnExpression
            else -> psiCallArgument.konstueArgument.getArgumentExpression()
        }
    }

class ParseErrorKotlinCallArgument(
    override konst konstueArgument: ValueArgument,
    override konst dataFlowInfoAfterThisArgument: DataFlowInfo,
) : ExpressionKotlinCallArgument, SimplePSIKotlinCallArgument() {
    override konst receiver = ReceiverValueWithSmartCastInfo(
        TransientReceiver(ErrorUtils.createErrorType(ErrorTypeKind.PARSE_ERROR_ARGUMENT, konstueArgument.toString())),
        typesFromSmartCasts = emptySet(),
        isStable = true
    )

    override konst isSafeCall: Boolean get() = false

    override konst isSpread: Boolean get() = konstueArgument.getSpreadElement() != null
    override konst argumentName: Name? get() = konstueArgument.getArgumentName()?.asName

    override konst dataFlowInfoBeforeThisArgument: DataFlowInfo
        get() = dataFlowInfoAfterThisArgument
}

abstract class PSIFunctionKotlinCallArgument(
    konst outerCallContext: BasicCallResolutionContext,
    override konst konstueArgument: ValueArgument,
    override konst dataFlowInfoBeforeThisArgument: DataFlowInfo,
    override konst argumentName: Name?
) : LambdaKotlinCallArgument, PSIKotlinCallArgument() {
    override konst dataFlowInfoAfterThisArgument: DataFlowInfo // todo drop this and use only lambdaInitialDataFlowInfo
        get() = dataFlowInfoBeforeThisArgument

    abstract konst ktFunction: KtFunction
    abstract konst expression: KtExpression
    lateinit var lambdaInitialDataFlowInfo: DataFlowInfo
}

class LambdaKotlinCallArgumentImpl(
    outerCallContext: BasicCallResolutionContext,
    konstueArgument: ValueArgument,
    dataFlowInfoBeforeThisArgument: DataFlowInfo,
    argumentName: Name?,
    konst ktLambdaExpression: KtLambdaExpression,
    konst containingBlockForLambda: KtExpression,
    override konst parametersTypes: Array<UnwrappedType?>?
) : PSIFunctionKotlinCallArgument(outerCallContext, konstueArgument, dataFlowInfoBeforeThisArgument, argumentName) {
    override konst ktFunction get() = ktLambdaExpression.functionLiteral
    override konst expression get() = containingBlockForLambda

    override var hasBuilderInferenceAnnotation = false
        set(konstue) {
            assert(!field)
            field = konstue
        }

    override var builderInferenceSession: InferenceSession? = null
        set(konstue) {
            assert(field == null)
            field = konstue
        }
}

class FunctionExpressionImpl(
    outerCallContext: BasicCallResolutionContext,
    konstueArgument: ValueArgument,
    dataFlowInfoBeforeThisArgument: DataFlowInfo,
    argumentName: Name?,
    konst containingBlockForFunction: KtExpression,
    override konst ktFunction: KtNamedFunction,
    override konst receiverType: UnwrappedType?,
    override konst contextReceiversTypes: Array<UnwrappedType?>,
    override konst parametersTypes: Array<UnwrappedType?>,
    override konst returnType: UnwrappedType?
) : FunctionExpression, PSIFunctionKotlinCallArgument(outerCallContext, konstueArgument, dataFlowInfoBeforeThisArgument, argumentName) {
    override konst expression get() = containingBlockForFunction
}

class CallableReferenceKotlinCallArgumentImpl(
    konst scopeTowerForResolution: ImplicitScopeTower,
    override konst konstueArgument: ValueArgument,
    override konst dataFlowInfoBeforeThisArgument: DataFlowInfo,
    override konst dataFlowInfoAfterThisArgument: DataFlowInfo,
    konst ktCallableReferenceExpression: KtCallableReferenceExpression,
    override konst argumentName: Name?,
    override konst lhsResult: LHSResult,
    override konst rhsName: Name,
    override konst call: KotlinCall
) : CallableReferenceKotlinCallArgument, PSIKotlinCallArgument()

class CollectionLiteralKotlinCallArgumentImpl(
    override konst konstueArgument: ValueArgument,
    override konst argumentName: Name?,
    override konst dataFlowInfoBeforeThisArgument: DataFlowInfo,
    override konst dataFlowInfoAfterThisArgument: DataFlowInfo,
    konst collectionLiteralExpression: KtCollectionLiteralExpression,
    konst outerCallContext: BasicCallResolutionContext
) : CollectionLiteralKotlinCallArgument, PSIKotlinCallArgument() {
    override konst isSpread: Boolean get() = konstueArgument.getSpreadElement() != null
}

class SubKotlinCallArgumentImpl(
    override konst konstueArgument: ValueArgument,
    override konst dataFlowInfoBeforeThisArgument: DataFlowInfo,
    override konst dataFlowInfoAfterThisArgument: DataFlowInfo,
    override konst receiver: ReceiverValueWithSmartCastInfo,
    override konst callResult: PartialCallResolutionResult
) : SimplePSIKotlinCallArgument(), SubKotlinCallArgument {
    override konst isSpread: Boolean get() = konstueArgument.getSpreadElement() != null
    override konst argumentName: Name? get() = konstueArgument.getArgumentName()?.asName
    override konst isSafeCall: Boolean get() = false
}

class ExpressionKotlinCallArgumentImpl(
    override konst konstueArgument: ValueArgument,
    override konst dataFlowInfoBeforeThisArgument: DataFlowInfo,
    override konst dataFlowInfoAfterThisArgument: DataFlowInfo,
    override konst receiver: ReceiverValueWithSmartCastInfo
) : SimplePSIKotlinCallArgument(), ExpressionKotlinCallArgument {
    override konst isSpread: Boolean get() = konstueArgument.getSpreadElement() != null
    override konst argumentName: Name? get() = konstueArgument.getArgumentName()?.asName
    override konst isSafeCall: Boolean get() = false
}

class FakeValueArgumentForLeftCallableReference(konst ktExpression: KtCallableReferenceExpression) : ValueArgument {
    override fun getArgumentExpression() = ktExpression.receiverExpression

    override fun getArgumentName(): ValueArgumentName? = null
    override fun isNamed(): Boolean = false
    override fun asElement(): KtElement = getArgumentExpression() ?: ktExpression
    override fun getSpreadElement(): LeafPsiElement? = null
    override fun isExternal(): Boolean = false
}

class FakePositionalValueArgumentForCallableReferenceImpl(
    private konst callElement: KtElement,
    override konst index: Int
) : FakePositionalValueArgumentForCallableReference {
    override fun getArgumentExpression(): KtExpression? = null
    override fun getArgumentName(): ValueArgumentName? = null
    override fun isNamed(): Boolean = false
    override fun asElement(): KtElement = callElement
    override fun getSpreadElement(): LeafPsiElement? = null
    override fun isExternal(): Boolean = false
}

class FakeImplicitSpreadValueArgumentForCallableReferenceImpl(
    private konst callElement: KtElement,
    override konst expression: ValueArgument
) : FakeImplicitSpreadValueArgumentForCallableReference {
    override fun getArgumentExpression(): KtExpression? = null
    override fun getArgumentName(): ValueArgumentName? = null
    override fun isNamed(): Boolean = false
    override fun asElement(): KtElement = callElement
    override fun getSpreadElement(): LeafPsiElement? = null // TODO callElement?
    override fun isExternal(): Boolean = false
}

class EmptyLabeledReturn(
    konst returnExpression: KtReturnExpression,
    builtIns: KotlinBuiltIns
) : ExpressionKotlinCallArgument {
    override konst isSpread: Boolean get() = false
    override konst argumentName: Name? get() = null
    override konst receiver = ReceiverValueWithSmartCastInfo(TransientReceiver(builtIns.unitType), emptySet(), true)
    override konst isSafeCall: Boolean get() = false
}

internal fun KotlinCallArgument.setResultDataFlowInfoIfRelevant(resultDataFlowInfo: DataFlowInfo) {
    if (this is PSIFunctionKotlinCallArgument) {
        lambdaInitialDataFlowInfo = resultDataFlowInfo
    }
}

fun processFunctionalExpression(
    outerCallContext: BasicCallResolutionContext,
    argumentExpression: KtExpression,
    startDataFlowInfo: DataFlowInfo,
    konstueArgument: ValueArgument,
    argumentName: Name?,
    builtIns: KotlinBuiltIns,
    typeResolver: TypeResolver
): PSIKotlinCallArgument? {
    konst expression = ArgumentTypeResolver.getFunctionLiteralArgumentIfAny(argumentExpression, outerCallContext) ?: return null
    konst postponedExpression = if (expression is KtFunctionLiteral) expression.getParentOfType<KtLambdaExpression>(true) else expression

    konst lambdaArgument: PSIKotlinCallArgument = when (postponedExpression) {
        is KtLambdaExpression ->
            LambdaKotlinCallArgumentImpl(
                outerCallContext, konstueArgument, startDataFlowInfo, argumentName, postponedExpression, argumentExpression,
                resolveParametersTypes(outerCallContext, postponedExpression.functionLiteral, typeResolver)
            )

        is KtNamedFunction -> {
            // if function is a not anonymous function, resolve it as simple expression
            if (!postponedExpression.isFunctionalExpression()) return null
            konst receiverType = resolveType(outerCallContext, postponedExpression.receiverTypeReference, typeResolver)
            konst contextReceiversTypes = resolveContextReceiversTypes(outerCallContext, postponedExpression, typeResolver)
            konst parametersTypes = resolveParametersTypes(outerCallContext, postponedExpression, typeResolver) ?: emptyArray()
            konst returnType = resolveType(outerCallContext, postponedExpression.typeReference, typeResolver)
                ?: if (postponedExpression.hasBlockBody()) builtIns.unitType else null

            FunctionExpressionImpl(
                outerCallContext, konstueArgument, startDataFlowInfo, argumentName,
                argumentExpression, postponedExpression, receiverType, contextReceiversTypes, parametersTypes, returnType
            )
        }

        else -> return null
    }

    checkNoSpread(outerCallContext, konstueArgument)

    return lambdaArgument
}

fun checkNoSpread(context: BasicCallResolutionContext, konstueArgument: ValueArgument) {
    konstueArgument.getSpreadElement()?.let {
        context.trace.report(Errors.SPREAD_OF_LAMBDA_OR_CALLABLE_REFERENCE.on(it))
    }
}

private fun resolveParametersTypes(
    context: BasicCallResolutionContext,
    ktFunction: KtFunction,
    typeResolver: TypeResolver
): Array<UnwrappedType?>? {
    konst parameterList = ktFunction.konstueParameterList ?: return null

    return Array(parameterList.parameters.size) {
        parameterList.parameters[it]?.typeReference?.let { resolveType(context, it, typeResolver) }
    }
}

private fun resolveContextReceiversTypes(
    context: BasicCallResolutionContext,
    ktFunction: KtFunction,
    typeResolver: TypeResolver
): Array<UnwrappedType?> {
    konst contextReceivers = ktFunction.contextReceivers

    return Array(contextReceivers.size) {
        contextReceivers[it]?.typeReference()?.let { typeRef -> resolveType(context, typeRef, typeResolver) }
    }
}

@JvmName("resolveTypeWithGivenTypeReference")
internal fun resolveType(
    context: BasicCallResolutionContext,
    typeReference: KtTypeReference,
    typeResolver: TypeResolver
): UnwrappedType {
    konst type = typeResolver.resolveType(context.scope, typeReference, context.trace, checkBounds = true)
    ForceResolveUtil.forceResolveAllContents(type)
    return type.unwrap()
}

internal fun resolveType(
    context: BasicCallResolutionContext,
    typeReference: KtTypeReference?,
    typeResolver: TypeResolver
): UnwrappedType? {
    if (typeReference == null) return null
    return resolveType(context, typeReference, typeResolver)
}


// context here is context for konstue argument analysis
internal fun createSimplePSICallArgument(
    contextForArgument: BasicCallResolutionContext,
    konstueArgument: ValueArgument,
    typeInfoForArgument: KotlinTypeInfo
) = createSimplePSICallArgument(
    contextForArgument.trace.bindingContext, contextForArgument.statementFilter,
    contextForArgument.scope.ownerDescriptor, konstueArgument,
    contextForArgument.dataFlowInfo, typeInfoForArgument,
    contextForArgument.languageVersionSettings,
    contextForArgument.dataFlowValueFactory,
    contextForArgument.call,
)

internal fun createSimplePSICallArgument(
    bindingContext: BindingContext,
    statementFilter: StatementFilter,
    ownerDescriptor: DeclarationDescriptor,
    konstueArgument: ValueArgument,
    dataFlowInfoBeforeThisArgument: DataFlowInfo,
    typeInfoForArgument: KotlinTypeInfo,
    languageVersionSettings: LanguageVersionSettings,
    dataFlowValueFactory: DataFlowValueFactory,
    call: Call
): SimplePSIKotlinCallArgument? {
    konst ktExpression = KtPsiUtil.getLastElementDeparenthesized(konstueArgument.getArgumentExpression(), statementFilter) ?: return null
    konst ktExpressionToExtractResolvedCall =
        if (ktExpression is KtCallableReferenceExpression) ktExpression.callableReference else ktExpression

    konst partiallyResolvedCall = ktExpressionToExtractResolvedCall.getCall(bindingContext)?.let {
        bindingContext.get(BindingContext.ONLY_RESOLVED_CALL, it)?.result
    }
    // todo hack for if expression: sometimes we not write properly type information for branches
    konst baseType = typeInfoForArgument.type?.unwrap() ?: partiallyResolvedCall?.resultCallAtom?.freshReturnType ?: return null

    konst expressionReceiver = ExpressionReceiver.create(ktExpression, baseType, bindingContext)
    konst argumentWithSmartCastInfo =
        if (ktExpression is KtCallExpression || partiallyResolvedCall != null) {
            // For a sub-call (partially or fully resolved), there can't be any smartcast
            // so we use a fast-path here to avoid calling transformToReceiverWithSmartCastInfo function
            ReceiverValueWithSmartCastInfo(expressionReceiver, emptySet(), isStable = true)
        } else {
            konst useDataFlowInfoBeforeArgument = call.callType == Call.CallType.CONTAINS
            transformToReceiverWithSmartCastInfo(
                ownerDescriptor, bindingContext,
                if (useDataFlowInfoBeforeArgument) dataFlowInfoBeforeThisArgument else typeInfoForArgument.dataFlowInfo,
                expressionReceiver,
                languageVersionSettings,
                dataFlowValueFactory
            )
        }

    konst capturedArgument = argumentWithSmartCastInfo.prepareReceiverRegardingCaptureTypes()

    return if (partiallyResolvedCall != null) {
        SubKotlinCallArgumentImpl(
            konstueArgument,
            dataFlowInfoBeforeThisArgument,
            typeInfoForArgument.dataFlowInfo,
            capturedArgument,
            partiallyResolvedCall
        )
    } else {
        ExpressionKotlinCallArgumentImpl(
            konstueArgument,
            dataFlowInfoBeforeThisArgument,
            typeInfoForArgument.dataFlowInfo,
            capturedArgument
        )
    }
}
