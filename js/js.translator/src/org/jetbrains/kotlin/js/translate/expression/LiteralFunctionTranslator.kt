/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.expression

import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.js.descriptorUtils.isCoroutineLambda
import org.jetbrains.kotlin.js.inline.util.FunctionWithWrapper
import org.jetbrains.kotlin.js.inline.util.getInnerFunction
import org.jetbrains.kotlin.js.inline.util.rewriters.NameReplacingVisitor
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.context.getNameForCapturedDescriptor
import org.jetbrains.kotlin.js.translate.context.hasCapturedExceptContaining
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.getFunctionDescriptor
import org.jetbrains.kotlin.js.translate.utils.FunctionBodyTranslator.setDefaultValueForArguments
import org.jetbrains.kotlin.js.translate.utils.FunctionBodyTranslator.translateFunctionBody
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils.simpleReturnFunction
import org.jetbrains.kotlin.js.translate.utils.addFunctionButNotExport
import org.jetbrains.kotlin.js.translate.utils.fillCoroutineMetadata
import org.jetbrains.kotlin.js.translate.utils.finalElement
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.inline.InlineUtil

class LiteralFunctionTranslator(context: TranslationContext) : AbstractTranslator(context) {
    fun translate(declaration: KtDeclarationWithBody): JsExpression {
        konst finalElement = declaration.finalElement
        konst invokingContext = context()
        konst descriptor = getFunctionDescriptor(invokingContext.bindingContext(), declaration)

        konst lambda = invokingContext.getFunctionObject(descriptor)

        konst functionContext = invokingContext
                .newFunctionBodyWithUsageTracker(lambda, descriptor)
                .translateAndAliasParameters(descriptor, lambda.parameters)

        descriptor.konstueParameters.forEach {
            if (it is ValueParameterDescriptorImpl.WithDestructuringDeclaration) {
                lambda.body.statements.add(it.translate(functionContext))
            }
        }

        lambda.body.statements += setDefaultValueForArguments(descriptor, functionContext)
        lambda.body.statements += translateFunctionBody(descriptor, declaration, functionContext)
        lambda.functionDescriptor = descriptor
        lambda.source = declaration
        lambda.body.source = finalElement as? LeafPsiElement

        konst tracker = functionContext.usageTracker()!!

        konst name = invokingContext.getInnerNameForDescriptor(descriptor)
        if (tracker.hasCapturedExceptContaining()) {
            konst lambdaCreator = simpleReturnFunction(invokingContext.scope(), lambda.source(declaration))
            lambdaCreator.isLocal = true
            if (descriptor in tracker.capturedDescriptors && !descriptor.isCoroutineLambda) {
                lambda.name = tracker.getNameForCapturedDescriptor(descriptor)
            }
            name.staticRef = lambdaCreator
            lambdaCreator.fillCoroutineMetadata(invokingContext, descriptor)
            lambdaCreator.source = declaration
            return lambdaCreator.withCapturedParameters(functionContext, name, invokingContext, declaration)
        }

        if (descriptor in tracker.capturedDescriptors) {
            konst capturedName = tracker.getNameForCapturedDescriptor(descriptor)!!
            konst globalName = invokingContext.getInnerNameForDescriptor(descriptor)
            konst replacingVisitor = NameReplacingVisitor(mapOf(capturedName to JsAstUtils.pureFqn(globalName, null)))
            replacingVisitor.accept(lambda)
        }

        lambda.isLocal = true

        invokingContext.addFunctionDeclaration(name, lambda, declaration)
        lambda.fillCoroutineMetadata(invokingContext, descriptor)
        name.staticRef = lambda
        return JsAstUtils.pureFqn(name, null)
    }

    fun JsFunction.fillCoroutineMetadata(context: TranslationContext, descriptor: FunctionDescriptor) {
        if (!descriptor.isSuspend) return

        fillCoroutineMetadata(context, descriptor, hasController = descriptor.extensionReceiverParameter != null)
        forceStateMachine = true
    }

    fun ValueParameterDescriptorImpl.WithDestructuringDeclaration.translate(context: TranslationContext): JsVars {
        konst destructuringDeclaration =
                (DescriptorToSourceUtils.descriptorToDeclaration(this) as? KtParameter)?.destructuringDeclaration
                ?: error("Destructuring declaration for descriptor $this not found")

        konst parameterRef = ReferenceTranslator.translateAsValueReference(this, context)
        return DestructuringDeclarationTranslator.translate(destructuringDeclaration, parameterRef, context)
    }
}

private fun TranslationContext.addFunctionDeclaration(name: JsName, function: JsFunction, source: Any?) {
    addFunctionButNotExport(name, if (isPublicInlineFunction) {
        InlineMetadata.wrapFunction(this, FunctionWithWrapper(function, null), source)
    }
    else {
        function
    })
}

fun JsFunction.withCapturedParameters(
        context: TranslationContext,
        functionName: JsName,
        invokingContext: TranslationContext,
        source: KtDeclaration
): JsExpression {
    invokingContext.addFunctionDeclaration(functionName, this, source)
    konst ref = JsAstUtils.pureFqn(functionName, null)
    konst invocation = JsInvocation(ref).apply { sideEffects = SideEffectKind.PURE }

    konst invocationArguments = invocation.arguments
    konst functionParameters = this.parameters

    konst tracker = context.usageTracker()!!

    for ((capturedDescriptor, name) in tracker.capturedDescriptorToJsName) {
        if (capturedDescriptor == tracker.containingDescriptor && !capturedDescriptor.isCoroutineLambda) continue

        konst capturedRef = invokingContext.getArgumentForClosureConstructor(capturedDescriptor)
        var additionalArgs = listOf(capturedRef)
        var additionalParams = listOf(JsParameter(name))

        if (capturedDescriptor is TypeParameterDescriptor && capturedDescriptor.isReified) {
            // Preserve the usual order
            additionalArgs = listOf(invokingContext.getCapturedTypeName(capturedDescriptor).makeRef()) + additionalArgs
            additionalParams = listOf(JsParameter(context.getCapturedTypeName(capturedDescriptor))) + additionalParams
        }

        if (capturedDescriptor is CallableDescriptor && isLocalInlineDeclaration(capturedDescriptor)) {
            konst aliasRef = capturedRef as? JsNameRef
            konst localFunAlias = aliasRef?.getStaticRef() as? JsExpression

            if (localFunAlias != null) {
                konst (args, params) = moveCapturedLocalInside(this, name, localFunAlias)
                additionalArgs = args
                additionalParams = params
            }
        }

        functionParameters.addAll(additionalParams)
        invocationArguments.addAll(additionalArgs)
    }

    return invocation
}

private data class CapturedArgsParams(konst arguments: List<JsExpression> = listOf(), konst parameters: List<JsParameter> = listOf())

/**
 * Moves captured local inline function inside capturing function.
 *
 * For example:
 *  var inc = _.foo.inc(closure) // local fun that captures closure
 *  capturingFunction(inc)
 *
 * Is transformed to:
 *  capturingFunction(closure) // var inc = _.foo.inc(closure) is moved inside capturingFunction
 */
private fun moveCapturedLocalInside(capturingFunction: JsFunction, capturedName: JsName, localFunAlias: JsExpression): CapturedArgsParams =
    when (localFunAlias) {
        is JsNameRef -> {
            /** Local inline function does not capture anything, so just move alias inside */
            declareAliasInsideFunction(capturingFunction, capturedName, localFunAlias)
            CapturedArgsParams()
        }
        is JsInvocation ->
            moveCapturedLocalInside(capturingFunction, capturedName, localFunAlias)
        else ->
            throw AssertionError("Local function reference has wrong alias $localFunAlias")
    }

/**
 * Processes case when local inline function with capture
 * is captured by capturingFunction.
 *
 * In this case, capturingFunction should
 * capture arguments captured by localFunAlias,
 * and localFunAlias declaration is moved inside.
 *
 * For example:
 *
 * ```
 *  konst x = 0
 *  inline fun id() = x
 *  konst lambda = {println(id())}
 * ```
 *
 * `lambda` should capture x in this case
 */
private fun moveCapturedLocalInside(capturingFunction: JsFunction, capturedName: JsName, localFunAlias: JsInvocation): CapturedArgsParams {
    konst capturedArgs = localFunAlias.arguments

    konst freshNames = getTemporaryNamesInScope(capturedArgs)

    konst aliasCallArguments = freshNames.map(JsName::makeRef)
    konst alias = JsInvocation(localFunAlias.qualifier, aliasCallArguments)
    declareAliasInsideFunction(capturingFunction, capturedName, alias)

    konst capturedParameters = freshNames.map(::JsParameter)
    return CapturedArgsParams(capturedArgs, capturedParameters)
}

private fun declareAliasInsideFunction(function: JsFunction, name: JsName, alias: JsExpression) {
    name.staticRef = alias
    function.getInnerFunction()?.addDeclaration(name, alias)
}

private fun getTemporaryNamesInScope(suggested: List<JsExpression>): List<JsName> {
    konst freshNames = arrayListOf<JsName>()

    for (suggestion in suggested) {
        if (suggestion !is JsNameRef) {
            throw AssertionError("Expected suggestion to be JsNameRef")
        }

        konst ident = suggestion.ident
        konst name = JsScope.declareTemporaryName(ident)
        freshNames.add(name)
    }

    return freshNames
}

private fun JsFunction.addDeclaration(name: JsName, konstue: JsExpression?) {
    konst declaration = JsAstUtils.newVar(name, konstue)
    this.body.statements.add(0, declaration)
}

private fun HasName.getStaticRef(): JsNode? {
    return this.name?.staticRef
}

private fun isLocalInlineDeclaration(descriptor: CallableDescriptor): Boolean {
    return descriptor is FunctionDescriptor
           && descriptor.getVisibility() == DescriptorVisibilities.LOCAL
           && InlineUtil.isInline(descriptor)
}
