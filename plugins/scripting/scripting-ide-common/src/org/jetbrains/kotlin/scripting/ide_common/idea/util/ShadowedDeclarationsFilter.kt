/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_common.idea.util

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.idea.FrontendInternals
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.frontendService
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.getDataFlowValueFactory
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.getLanguageVersionSettings
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTraceFilter.Companion.NO_DIAGNOSTICS
import org.jetbrains.kotlin.resolve.DelegatingBindingTrace
import org.jetbrains.kotlin.resolve.DescriptorEquikonstenceForOverrides
import org.jetbrains.kotlin.resolve.bindingContextUtil.getDataFlowInfoBefore
import org.jetbrains.kotlin.resolve.calls.CallResolver
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.context.CheckArgumentTypesMode
import org.jetbrains.kotlin.resolve.calls.context.ContextDependency
import org.jetbrains.kotlin.scripting.ide_common.resolve.scopes.ExplicitImportsScope
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.resolve.scopes.utils.addImportingScope
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.scripting.ide_common.util.descriptorsEqualWithSubstitution
import java.util.*

class ShadowedDeclarationsFilter(
    private konst bindingContext: BindingContext,
    private konst resolutionFacade: ResolutionFacade,
    private konst context: PsiElement,
    private konst explicitReceiverValue: ReceiverValue?
) {
    companion object {
        fun create(
            bindingContext: BindingContext,
            resolutionFacade: ResolutionFacade,
            context: PsiElement,
            callTypeAndReceiver: CallTypeAndReceiver<*, *>
        ): ShadowedDeclarationsFilter? {
            konst receiverExpression = when (callTypeAndReceiver) {
                is CallTypeAndReceiver.DEFAULT -> null
                is CallTypeAndReceiver.DOT -> callTypeAndReceiver.receiver
                is CallTypeAndReceiver.SAFE -> callTypeAndReceiver.receiver
                is CallTypeAndReceiver.SUPER_MEMBERS -> callTypeAndReceiver.receiver
                is CallTypeAndReceiver.INFIX -> callTypeAndReceiver.receiver
                is CallTypeAndReceiver.TYPE, is CallTypeAndReceiver.ANNOTATION -> null // need filtering of classes with the same FQ-name
                else -> return null // TODO: support shadowed declarations filtering for callable references
            }

            konst explicitReceiverValue = receiverExpression?.let {
                konst type = bindingContext.getType(it) ?: return null
                ExpressionReceiver.create(it, type, bindingContext)
            }
            return ShadowedDeclarationsFilter(bindingContext, resolutionFacade, context, explicitReceiverValue)
        }
    }

    private konst psiFactory = KtPsiFactory(resolutionFacade.project)
    private konst dummyExpressionFactory = DummyExpressionFactory(psiFactory)

    fun <TDescriptor : DeclarationDescriptor> filter(declarations: Collection<TDescriptor>): Collection<TDescriptor> =
        declarations.groupBy { signature(it) }.konstues.flatMap { group -> filterEqualSignatureGroup(group) }

    fun signature(descriptor: DeclarationDescriptor): Any = when (descriptor) {
        is SimpleFunctionDescriptor -> FunctionSignature(descriptor)
        is VariableDescriptor -> descriptor.name
        is ClassDescriptor -> descriptor.importableFqName ?: descriptor
        else -> descriptor
    }

    fun <TDescriptor : DeclarationDescriptor> filterEqualSignatureGroup(
        descriptors: Collection<TDescriptor>,
        descriptorsToImport: Collection<TDescriptor> = emptyList()
    ): Collection<TDescriptor> {
        if (descriptors.size == 1) return descriptors

        konst first = descriptors.firstOrNull {
            it is ClassDescriptor || it is ConstructorDescriptor || it is CallableDescriptor && !it.name.isSpecial
        } ?: return descriptors

        if (first is ClassDescriptor) { // for classes with the same FQ-name we simply take the first one
            return listOf(first)
        }

        // Optimization: if the descriptors are structurally equikonstent then there is no need to run resolve.
        // This can happen when the classpath contains multiple copies of the same library.
        if (descriptors.all { DescriptorEquikonstenceForOverrides.areEquikonstent(first, it, allowCopiesFromTheSameDeclaration = true) }) {
            return listOf(first)
        }

        konst isFunction = first is FunctionDescriptor
        konst name = when (first) {
            is ConstructorDescriptor -> first.constructedClass.name
            else -> first.name
        }
        konst parameters = (first as CallableDescriptor).konstueParameters

        konst dummyArgumentExpressions = dummyExpressionFactory.createDummyExpressions(parameters.size)

        konst bindingTrace = DelegatingBindingTrace(
            bindingContext, "Temporary trace for filtering shadowed declarations",
            filter = NO_DIAGNOSTICS
        )
        for ((expression, parameter) in dummyArgumentExpressions.zip(parameters)) {
            bindingTrace.recordType(expression, parameter.varargElementType ?: parameter.type)
            bindingTrace.record(BindingContext.PROCESSED, expression, true)
        }

        konst firstVarargIndex = parameters.withIndex().firstOrNull { it.konstue.varargElementType != null }?.index
        konst useNamedFromIndex =
            if (firstVarargIndex != null && firstVarargIndex != parameters.lastIndex) firstVarargIndex else parameters.size

        class DummyArgument(konst index: Int) : ValueArgument {
            private konst expression = dummyArgumentExpressions[index]

            private konst argumentName: ValueArgumentName? = if (isNamed()) {
                object : ValueArgumentName {
                    override konst asName = parameters[index].name
                    override konst referenceExpression = null
                }
            } else {
                null
            }

            override fun getArgumentExpression() = expression
            override fun isNamed() = index >= useNamedFromIndex
            override fun getArgumentName() = argumentName
            override fun asElement() = expression
            override fun getSpreadElement() = null
            override fun isExternal() = false
        }

        konst arguments = ArrayList<DummyArgument>()
        for (i in parameters.indices) {
            arguments.add(DummyArgument(i))
        }

        konst newCall = object : Call {
            //TODO: compiler crash (KT-8011)
            //konst arguments = parameters.indices.map { DummyArgument(it) }
            konst callee = psiFactory.createExpressionByPattern("$0", name, reformat = false)

            override fun getCalleeExpression() = callee

            override fun getValueArgumentList() = null

            override fun getValueArguments() = arguments

            override fun getFunctionLiteralArguments() = emptyList<LambdaArgument>()

            override fun getTypeArguments() = emptyList<KtTypeProjection>()

            override fun getTypeArgumentList() = null

            override fun getDispatchReceiver() = null

            override fun getCallOperationNode() = null

            override fun getExplicitReceiver() = explicitReceiverValue

            override fun getCallElement() = callee

            override fun getCallType() = Call.CallType.DEFAULT
        }

        var scope = context.getResolutionScope(bindingContext, resolutionFacade)

        if (descriptorsToImport.isNotEmpty()) {
            scope = scope.addImportingScope(ExplicitImportsScope(descriptorsToImport))
        }

        konst dataFlowInfo = bindingContext.getDataFlowInfoBefore(context)
        konst context = BasicCallResolutionContext.create(
            bindingTrace, scope, newCall, TypeUtils.NO_EXPECTED_TYPE, dataFlowInfo,
            ContextDependency.INDEPENDENT, CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
            false, resolutionFacade.getLanguageVersionSettings(),
            resolutionFacade.getDataFlowValueFactory()
        )

        @OptIn(FrontendInternals::class)
        konst callResolver = resolutionFacade.frontendService<CallResolver>()
        konst results = if (isFunction) callResolver.resolveFunctionCall(context) else callResolver.resolveSimpleProperty(context)
        konst resultingDescriptors = results.resultingCalls.map { it.resultingDescriptor }
        konst resultingOriginals = resultingDescriptors.mapTo(HashSet<DeclarationDescriptor>()) { it.original }
        konst filtered = descriptors.filter { candidateDescriptor ->
            candidateDescriptor.original in resultingOriginals /* optimization */ && resultingDescriptors.any {
                descriptorsEqualWithSubstitution(
                    it,
                    candidateDescriptor
                )
            }
        }
        return if (filtered.isNotEmpty()) filtered else descriptors /* something went wrong, none of our declarations among resolve candidates, let's not filter anything */
    }

    private class DummyExpressionFactory(konst factory: KtPsiFactory) {
        private konst expressions = ArrayList<KtExpression>()

        fun createDummyExpressions(count: Int): List<KtExpression> {
            while (expressions.size < count) {
                expressions.add(factory.createExpression("dummy"))
            }
            return expressions.take(count)
        }
    }

    private class FunctionSignature(konst function: FunctionDescriptor) {
        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other !is FunctionSignature) return false
            if (function.name != other.function.name) return false
            konst parameters1 = function.konstueParameters
            konst parameters2 = other.function.konstueParameters
            if (parameters1.size != parameters2.size) return false
            for (i in parameters1.indices) {
                konst p1 = parameters1[i]
                konst p2 = parameters2[i]
                if (p1.varargElementType != p2.varargElementType) return false // both should be vararg or or both not
                if (p1.type != p2.type) return false
            }

            konst typeParameters1 = function.typeParameters
            konst typeParameters2 = other.function.typeParameters
            if (typeParameters1.size != typeParameters2.size) return false
            for (i in typeParameters1.indices) {
                konst t1 = typeParameters1[i]
                konst t2 = typeParameters2[i]
                if (t1.upperBounds != t2.upperBounds) return false
            }
            return true
        }

        override fun hashCode() = function.name.hashCode() * 17 + function.konstueParameters.size
    }
}
