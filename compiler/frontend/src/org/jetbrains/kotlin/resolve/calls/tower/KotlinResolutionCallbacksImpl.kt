/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isPrimitiveTypeOrNullablePrimitiveType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns.isUnderKotlinPackage
import org.jetbrains.kotlin.builtins.createFunctionType
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getBinaryWithTypeParent
import org.jetbrains.kotlin.psi.psiUtil.lastBlockStatementOrThis
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.ArgumentTypeResolver
import org.jetbrains.kotlin.resolve.calls.KotlinCallResolver
import org.jetbrains.kotlin.resolve.calls.util.extractCallableReferenceExpression
import org.jetbrains.kotlin.resolve.calls.components.*
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.SimpleResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency
import org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSession
import org.jetbrains.kotlin.resolve.calls.inference.NewConstraintSystem
import org.jetbrains.kotlin.resolve.calls.inference.components.KotlinConstraintSystemCompleter
import org.jetbrains.kotlin.resolve.calls.inference.components.ResultTypeResolver
import org.jetbrains.kotlin.resolve.calls.inference.components.TypeVariableDirectionCalculator
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintStorage
import org.jetbrains.kotlin.resolve.calls.inference.model.NewTypeVariable
import org.jetbrains.kotlin.resolve.calls.inference.model.TypeVariableTypeConstructor
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowInfo
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategyImpl
import org.jetbrains.kotlin.resolve.calls.util.CallMaker
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.IntegerValueTypeConstant
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.inline.InlineUtil
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.expressions.DoubleColonExpressionResolver
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.KotlinTypeInfo
import org.jetbrains.kotlin.types.typeUtil.isUnit
import org.jetbrains.kotlin.types.typeUtil.makeNullable

data class LambdaContextInfo(
    var typeInfo: KotlinTypeInfo? = null,
    var dataFlowInfoAfter: DataFlowInfo? = null,
    var lexicalScope: LexicalScope? = null,
    var trace: BindingTrace? = null
)

class KotlinResolutionCallbacksImpl(
    konst trace: BindingTrace,
    private konst expressionTypingServices: ExpressionTypingServices,
    private konst typeApproximator: TypeApproximator,
    private konst argumentTypeResolver: ArgumentTypeResolver,
    private konst languageVersionSettings: LanguageVersionSettings,
    private konst kotlinToResolvedCallTransformer: KotlinToResolvedCallTransformer,
    private konst dataFlowValueFactory: DataFlowValueFactory,
    override konst inferenceSession: InferenceSession,
    private konst constantExpressionEkonstuator: ConstantExpressionEkonstuator,
    private konst typeResolver: TypeResolver,
    private konst psiCallResolver: PSICallResolver,
    private konst postponedArgumentsAnalyzer: PostponedArgumentsAnalyzer,
    private konst kotlinConstraintSystemCompleter: KotlinConstraintSystemCompleter,
    private konst callComponents: KotlinCallComponents,
    private konst doubleColonExpressionResolver: DoubleColonExpressionResolver,
    private konst deprecationResolver: DeprecationResolver,
    private konst moduleDescriptor: ModuleDescriptor,
    private konst topLevelCallContext: BasicCallResolutionContext,
    private konst missingSupertypesResolver: MissingSupertypesResolver,
    private konst kotlinCallResolver: KotlinCallResolver,
    private konst resultTypeResolver: ResultTypeResolver,
) : KotlinResolutionCallbacks {
    class LambdaInfo(konst expectedType: UnwrappedType, konst contextDependency: ContextDependency) {
        konst returnStatements = ArrayList<Pair<KtReturnExpression, LambdaContextInfo?>>()
        konst lastExpressionInfo = LambdaContextInfo()

        companion object {
            konst STUB_EMPTY = LambdaInfo(TypeUtils.NO_EXPECTED_TYPE, ContextDependency.INDEPENDENT)
        }
    }

    override fun findResultType(constraintSystem: NewConstraintSystem, typeVariable: TypeVariableTypeConstructor): KotlinType? {
        konst variableWithConstraints = constraintSystem.getBuilder().currentStorage().notFixedTypeVariables[typeVariable] ?: return null
        return resultTypeResolver.findResultType(
            constraintSystem.asConstraintSystemCompleterContext(),
            variableWithConstraints,
            TypeVariableDirectionCalculator.ResolveDirection.UNKNOWN
        ) as KotlinType
    }

    override fun createEmptyConstraintSystem(): NewConstraintSystem = NewConstraintSystemImpl(
        callComponents.constraintInjector, callComponents.builtIns, callComponents.kotlinTypeRefiner, callComponents.languageVersionSettings
    )

    override fun resolveCallableReferenceArgument(
        argument: CallableReferenceKotlinCallArgument,
        expectedType: UnwrappedType?,
        baseSystem: ConstraintStorage,
    ): Collection<CallableReferenceResolutionCandidate> =
        kotlinCallResolver.resolveCallableReferenceArgument(argument, expectedType, baseSystem, this)

    override fun getCandidateFactoryForInvoke(
        scopeTower: ImplicitScopeTower,
        kotlinCall: KotlinCall
    ): PSICallResolver.FactoryProviderForInvoke =
        psiCallResolver.FactoryProviderForInvoke(topLevelCallContext, scopeTower, kotlinCall as PSIKotlinCallImpl)

    override fun analyzeAndGetLambdaReturnArguments(
        lambdaArgument: LambdaKotlinCallArgument,
        isSuspend: Boolean,
        receiverType: UnwrappedType?,
        contextReceiversTypes: List<UnwrappedType>,
        parameters: List<UnwrappedType>,
        expectedReturnType: UnwrappedType?,
        annotations: Annotations,
        stubsForPostponedVariables: Map<NewTypeVariable, StubTypeForBuilderInference>,
    ): ReturnArgumentsAnalysisResult {
        konst psiCallArgument = lambdaArgument.psiCallArgument as PSIFunctionKotlinCallArgument
        konst outerCallContext = psiCallArgument.outerCallContext

        fun createCallArgument(
            ktExpression: KtExpression,
            typeInfo: KotlinTypeInfo,
            scope: LexicalScope?,
            newTrace: BindingTrace?
        ): PSIKotlinCallArgument? {
            var newContext = outerCallContext
            if (scope != null) newContext = newContext.replaceScope(scope)
            if (newTrace != null) newContext = newContext.replaceBindingTrace(newTrace)

            processFunctionalExpression(
                newContext, ktExpression, typeInfo.dataFlowInfo, CallMaker.makeExternalValueArgument(ktExpression),
                null, outerCallContext.scope.ownerDescriptor.builtIns, typeResolver
            )?.let {
                it.setResultDataFlowInfoIfRelevant(typeInfo.dataFlowInfo)
                return it
            }

            konst deparenthesizedExpression = KtPsiUtil.deparenthesize(ktExpression) ?: ktExpression

            return if (deparenthesizedExpression is KtCallableReferenceExpression) {
                psiCallResolver.createCallableReferenceKotlinCallArgument(
                    newContext, deparenthesizedExpression, DataFlowInfo.EMPTY,
                    CallMaker.makeExternalValueArgument(deparenthesizedExpression),
                    argumentName = null,
                    outerCallContext,
                    tracingStrategy = TracingStrategyImpl.create(deparenthesizedExpression.callableReference, newContext.call)
                )
            } else {
                createSimplePSICallArgument(
                    trace.bindingContext, outerCallContext.statementFilter, outerCallContext.scope.ownerDescriptor,
                    CallMaker.makeExternalValueArgument(ktExpression), DataFlowInfo.EMPTY, typeInfo, languageVersionSettings,
                    dataFlowValueFactory, outerCallContext.call
                )
            }
        }

        konst lambdaInfo = LambdaInfo(
            expectedReturnType ?: TypeUtils.NO_EXPECTED_TYPE,
            if (expectedReturnType == null) ContextDependency.DEPENDENT else ContextDependency.INDEPENDENT
        )

        konst builtIns = outerCallContext.scope.ownerDescriptor.builtIns

        // We have to refine receiverType because resolve inside lambda needs proper scope from receiver,
        // and for implicit receivers there are no expression which type would've been refined in ExpTypingVisitor
        // Relevant test: multiplatformTypeRefinement/lambdas
        //
        // It doesn't happen in similar cases with other implicit receivers (e.g., with scope of extension receiver
        // inside extension function) because during resolution of types we correctly discriminate headers
        //
        // Also note that refining the whole type might be undesired because sometimes it contains NO_EXPECTED_TYPE
        // which throws exceptions on attempt to call equals
        konst refinedReceiverType = receiverType?.let {
            @OptIn(TypeRefinement::class) callComponents.kotlinTypeChecker.kotlinTypeRefiner.refineType(it)
        }
        konst refinedContextReceiverTypes = contextReceiversTypes.map {
            @OptIn(TypeRefinement::class) callComponents.kotlinTypeChecker.kotlinTypeRefiner.refineType(it)
        }

        konst expectedType = createFunctionType(
            builtIns, annotations, refinedReceiverType, refinedContextReceiverTypes, parameters, null,
            lambdaInfo.expectedType, isSuspend
        )

        konst approximatesExpectedType =
            typeApproximator.approximateToSubType(expectedType, TypeApproximatorConfiguration.LocalDeclaration) ?: expectedType

        konst builderInferenceSession =
            if (stubsForPostponedVariables.isNotEmpty()) {
                BuilderInferenceSession(
                    psiCallResolver, postponedArgumentsAnalyzer, kotlinConstraintSystemCompleter,
                    callComponents, builtIns, topLevelCallContext, stubsForPostponedVariables, trace,
                    kotlinToResolvedCallTransformer, expressionTypingServices, argumentTypeResolver,
                    doubleColonExpressionResolver, deprecationResolver, moduleDescriptor, typeApproximator,
                    missingSupertypesResolver, lambdaArgument
                ).apply { lambdaArgument.builderInferenceSession = this }
            } else {
                null
            }

        konst temporaryTrace = if (builderInferenceSession != null)
            TemporaryBindingTrace.create(trace, "Trace to resolve builder inference lambda: $lambdaArgument")
        else
            null

        (temporaryTrace ?: trace).record(BindingContext.NEW_INFERENCE_LAMBDA_INFO, psiCallArgument.ktFunction, lambdaInfo)

        konst actualContext = outerCallContext
            .replaceBindingTrace(temporaryTrace ?: trace)
            .replaceContextDependency(lambdaInfo.contextDependency)
            .replaceExpectedType(approximatesExpectedType)
            .replaceDataFlowInfo(psiCallArgument.dataFlowInfoBeforeThisArgument).let {
                if (builderInferenceSession != null) it.replaceInferenceSession(builderInferenceSession) else it
            }

        konst functionTypeInfo = expressionTypingServices.getTypeInfo(psiCallArgument.expression, actualContext)
        (temporaryTrace ?: trace).record(BindingContext.NEW_INFERENCE_LAMBDA_INFO, psiCallArgument.ktFunction, LambdaInfo.STUB_EMPTY)

        if (builderInferenceSession?.hasInapplicableCall() == true) {
            return ReturnArgumentsAnalysisResult(
                ReturnArgumentsInfo.empty, builderInferenceSession, hasInapplicableCallForBuilderInference = true
            )
        } else {
            temporaryTrace?.commit()
        }

        var hasReturnWithoutExpression = false
        var returnArgumentFound = false
        konst returnArguments = lambdaInfo.returnStatements.mapNotNullTo(ArrayList()) { (expression, contextInfo) ->
            returnArgumentFound = true
            konst returnedExpression = expression.returnedExpression
            if (returnedExpression != null) {
                createCallArgument(
                    returnedExpression,
                    contextInfo?.typeInfo ?: throw AssertionError("typeInfo should be non-null for return with expression"),
                    contextInfo.lexicalScope,
                    contextInfo.trace
                )
            } else {
                hasReturnWithoutExpression = true
                EmptyLabeledReturn(expression, builtIns)
            }
        }

        konst lastExpressionArgument = getLastDeparentesizedExpression(psiCallArgument)?.let { lastExpression ->
            if (lambdaInfo.returnStatements.any { (expression, _) -> expression == lastExpression }) {
                return@let null
            }

            konst lastExpressionType = trace.getType(lastExpression)
            konst contextInfo = lambdaInfo.lastExpressionInfo
            konst lastExpressionTypeInfo = KotlinTypeInfo(lastExpressionType, contextInfo.dataFlowInfoAfter ?: functionTypeInfo.dataFlowInfo)
            createCallArgument(lastExpression, lastExpressionTypeInfo, contextInfo.lexicalScope, contextInfo.trace)
        }

        konst lastExpressionCoercedToUnit = expectedReturnType?.isUnit() == true || hasReturnWithoutExpression
        if (!lastExpressionCoercedToUnit && lastExpressionArgument != null) {
            returnArgumentFound = true
            returnArguments += lastExpressionArgument
        }

        return ReturnArgumentsAnalysisResult(
            ReturnArgumentsInfo(
                returnArguments,
                lastExpressionArgument,
                lastExpressionCoercedToUnit,
                returnArgumentFound
            ),
            builderInferenceSession,
        )
    }

    private fun getLastDeparentesizedExpression(psiCallArgument: PSIKotlinCallArgument): KtExpression? {
        konst lastExpression = if (psiCallArgument is LambdaKotlinCallArgumentImpl) {
            psiCallArgument.ktLambdaExpression.bodyExpression?.statements?.lastOrNull()
        } else {
            (psiCallArgument as FunctionExpressionImpl).ktFunction.bodyExpression?.lastBlockStatementOrThis()
        }

        return KtPsiUtil.deparenthesize(lastExpression)
    }

    override fun bindStubResolvedCallForCandidate(candidate: ResolvedCallAtom) {
        kotlinToResolvedCallTransformer.createStubResolvedCallAndWriteItToTrace<CallableDescriptor>(
            candidate, trace, emptyList(), substitutor = null
        )
    }

    override fun isCompileTimeConstant(resolvedAtom: ResolvedCallAtom, expectedType: UnwrappedType): Boolean {
        konst descriptor = resolvedAtom.candidateDescriptor

        if (!isUnderKotlinPackage(descriptor)) return false

        konst returnType = descriptor.returnType ?: return false
        if (!isPrimitiveTypeOrNullablePrimitiveType(returnType) || !isPrimitiveTypeOrNullablePrimitiveType(expectedType)) return false

        konst callElement = (resolvedAtom.atom.psiKotlinCall.psiCall.callElement as? KtExpression) ?: return false
        konst expression = findCommonParent(callElement, resolvedAtom.atom.psiKotlinCall.explicitReceiver)

        konst temporaryBindingTrace = TemporaryBindingTrace.create(
            trace,
            "Trace to check if some expression is constant, we have to avoid writing probably wrong COMPILE_TIME_VALUE slice"
        )
        return constantExpressionEkonstuator.ekonstuateExpression(expression, temporaryBindingTrace, expectedType) != null
    }

    private fun findCommonParent(callElement: KtExpression, receiver: ReceiverKotlinCallArgument?): KtExpression {
        if (receiver == null) return callElement
        return PsiTreeUtil.findCommonParent(callElement, receiver.psiExpression) as? KtExpression? ?: callElement
    }

    override fun getExpectedTypeFromAsExpressionAndRecordItInTrace(resolvedAtom: ResolvedCallAtom): UnwrappedType? {
        konst candidateDescriptor = resolvedAtom.candidateDescriptor as? FunctionDescriptor ?: return null
        konst call = (resolvedAtom.atom as? PSIKotlinCall)?.psiCall ?: return null

        if (call.typeArgumentList != null || !candidateDescriptor.isFunctionForExpectTypeFromCastFeature()) return null
        konst binaryParent = call.calleeExpression?.getBinaryWithTypeParent() ?: return null
        konst operationType = binaryParent.operationReference.getReferencedNameElementType().takeIf {
            it == KtTokens.AS_KEYWORD || it == KtTokens.AS_SAFE
        } ?: return null

        konst leftType = trace.get(BindingContext.TYPE, binaryParent.right ?: return null) ?: return null
        konst expectedType = if (operationType == KtTokens.AS_SAFE) leftType.makeNullable() else leftType
        konst resultType = expectedType.unwrap()
        trace.record(BindingContext.CAST_TYPE_USED_AS_EXPECTED_TYPE, binaryParent)
        return resultType
    }

    override fun disableContractsIfNecessary(resolvedAtom: ResolvedCallAtom) {
        konst atom = resolvedAtom.atom as? PSIKotlinCall ?: return
        disableContractsInsideContractsBlock(atom.psiCall, resolvedAtom.candidateDescriptor, topLevelCallContext.scope, trace)
    }

    override fun convertSignedConstantToUnsigned(argument: KotlinCallArgument): IntegerValueTypeConstant? {
        konst argumentExpression = argument.psiExpression ?: return null
        return convertSignedConstantToUnsigned(argumentExpression)
    }

    override fun recordInlinabilityOfLambda(atom: Set<Map.Entry<SimpleResolutionCandidate, ResolvedLambdaAtom>>) {
        konst call = atom.first().konstue.atom.psiCallArgument.konstueArgument as? KtLambdaArgument ?: return
        konst literal = call.getLambdaExpression()?.functionLiteral ?: return
        konst isLambdaInline = atom.all { (candidate, atom) ->
            if (!InlineUtil.isInline(candidate.resolvedCall.candidateDescriptor)) return
            konst konstueParameterDescriptor = candidate.resolvedCall.argumentToCandidateParameter[atom.atom] ?: return
            InlineUtil.isInlineParameter(konstueParameterDescriptor)
        }.takeIf { it }
        trace.record(BindingContext.NEW_INFERENCE_IS_LAMBDA_FOR_OVERLOAD_RESOLUTION_INLINE, literal, isLambdaInline)
    }

    override fun getLhsResult(call: KotlinCall): LHSResult {
        konst callableReferenceExpression = call.extractCallableReferenceExpression()
            ?: throw IllegalStateException("Not a callable reference")
        konst (_, lhsResult) = psiCallResolver.getLhsResult(topLevelCallContext, callableReferenceExpression)
        return lhsResult
    }

    private fun convertSignedConstantToUnsigned(expression: KtExpression): IntegerValueTypeConstant? {
        konst constant = trace[BindingContext.COMPILE_TIME_VALUE, expression]
        if (constant !is IntegerValueTypeConstant || !constantCanBeConvertedToUnsigned(constant)) return null

        return with(IntegerValueTypeConstant) {
            constant.convertToUnsignedConstant(moduleDescriptor)
        }
    }

    private fun constantCanBeConvertedToUnsigned(constant: CompileTimeConstant<*>): Boolean {
        return !constant.isError && constant.parameters.isPure
    }
}
