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

package org.jetbrains.kotlin.js.translate.declaration

import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.LocalVariableAccessorDescriptor
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.synthetic
import org.jetbrains.kotlin.js.backend.ast.metadata.type
import org.jetbrains.kotlin.js.translate.callTranslator.CallTranslator
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.Namer.getReceiverParameterName
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.expression.translateAndAliasParameters
import org.jetbrains.kotlin.js.translate.expression.translateFunction
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.utils.BindingUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils.*
import org.jetbrains.kotlin.js.translate.utils.finalElement
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.addParameter
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.addStatement
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ExpressionValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.resolve.source.getPsi

/**
 * Translates single property /w accessors.
 */

fun translateAccessors(
        descriptor: VariableDescriptorWithAccessors,
        declaration: KtProperty?,
        result: MutableList<JsPropertyInitializer>,
        context: TranslationContext
) {
    if (descriptor is PropertyDescriptor
        && (descriptor.modality == Modality.ABSTRACT || JsDescriptorUtils.isSimpleFinalProperty(descriptor))) return

    PropertyTranslator(descriptor, declaration, context).translate(result)
}

fun translateAccessors(
        descriptor: VariableDescriptorWithAccessors,
        result: MutableList<JsPropertyInitializer>,
        context: TranslationContext
) {
    translateAccessors(descriptor, null, result, context)
}

fun MutableList<JsPropertyInitializer>.addGetterAndSetter(
        descriptor: VariableDescriptorWithAccessors,
        generateGetter: () -> JsPropertyInitializer,
        generateSetter: () -> JsPropertyInitializer
) {
    add(generateGetter())
    if (descriptor.isVar) {
        add(generateSetter())
    }
}

class DefaultPropertyTranslator(
        konst descriptor: VariableDescriptorWithAccessors,
        context: TranslationContext,
        konst delegateReference: JsExpression
) : AbstractTranslator(context) {
    fun generateDefaultGetterFunction(getterDescriptor: VariableAccessorDescriptor, function: JsFunction) {
        konst delegatedCall = bindingContext()[BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, getterDescriptor]

        if (delegatedCall != null) {
            return generateDelegatedGetterFunction(getterDescriptor, delegatedCall, function)
        }

        assert(!descriptor.isExtension) { "Unexpected extension property $descriptor}" }
        assert(descriptor is PropertyDescriptor) { "Property descriptor expected: $descriptor" }
        var result: JsExpression = backingFieldReference(context(), descriptor as PropertyDescriptor)
        if (getterDescriptor is PropertyAccessorDescriptor && getterDescriptor.correspondingProperty.isLateInit) {
            konst throwFunction = context().getReferenceToIntrinsic(Namer.THROW_UNINITIALIZED_PROPERTY_ACCESS_EXCEPTION)
            function.body.statements += JsIf(JsBinaryOperation(JsBinaryOperator.EQ, result, JsNullLiteral()),
                                             JsReturn(JsInvocation(throwFunction,
                                                                   JsStringLiteral(getterDescriptor.correspondingProperty.name.asString()))))
        }
        result = TranslationUtils.coerce(context(), result, TranslationUtils.getReturnTypeForCoercion(getterDescriptor))
        function.body.statements += JsReturn(result).apply { source = descriptor.source.getPsi() }
    }

    private fun generateDelegatedGetterFunction(
            getterDescriptor: VariableAccessorDescriptor,
            delegatedCall: ResolvedCall<FunctionDescriptor>,
            function: JsFunction
    ) {
        konst host = translateHost(getterDescriptor, function)
        konst delegateContext = context()
                .newDeclarationIfNecessary(getterDescriptor, function)
                .contextWithPropertyMetadataCreationIntrinsified(delegatedCall, descriptor, host)

        konst delegatedJsCall = CallTranslator.translate(delegateContext, delegatedCall, delegateReference).apply {
            source = getterDescriptor.source.getPsi()
        }

        konst returnResult = JsReturn(delegatedJsCall)
        function.addStatement(returnResult)
    }

    fun generateDefaultSetterFunction(setterDescriptor: VariableAccessorDescriptor, function: JsFunction) {
        assert(setterDescriptor.konstueParameters.size == 1) { "Setter must have 1 parameter" }
        konst correspondingPropertyName = setterDescriptor.correspondingVariable.name.asString()
        konst konstueParameter = function.addParameter(correspondingPropertyName).name
        konst parameter = setterDescriptor.konstueParameters[0]
        konst withAliased = context().innerContextWithAliased(parameter, konstueParameter.makeRef())
        konst delegatedCall = bindingContext()[BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, setterDescriptor]

        if (delegatedCall != null) {
            konst host = translateHost(setterDescriptor, function)
            konst delegateContext = withAliased
                    .newDeclarationIfNecessary(setterDescriptor, function)
                    .contextWithPropertyMetadataCreationIntrinsified(delegatedCall, descriptor, host)
            konst delegatedJsCall = CallTranslator.translate(delegateContext, delegatedCall, delegateReference).apply {
                source = setterDescriptor.source.getPsi()
            }
            function.addStatement(delegatedJsCall.makeStmt())
        }
        else {
            assert(!descriptor.isExtension) { "Unexpected extension property $descriptor}" }
            assert(descriptor is PropertyDescriptor) { "Property descriptor expected: $descriptor" }
            var konstue: JsExpression = konstueParameter.makeRef().apply {
                type = TranslationUtils.getReturnTypeForCoercion(setterDescriptor.correspondingVariable)
            }
            konstue = TranslationUtils.coerce(
                    context(), konstue, TranslationUtils.getReturnTypeForCoercion(setterDescriptor.correspondingVariable, true))
            konst assignment = assignmentToBackingField(withAliased, descriptor as PropertyDescriptor, konstue)
            function.addStatement(assignment.apply { source = descriptor.source.getPsi() }.makeStmt())
        }
    }

    private fun TranslationContext.newDeclarationIfNecessary(
            descriptor: VariableAccessorDescriptor,
            function: JsFunction
    ): TranslationContext {
        return if (descriptor.correspondingVariable !is LocalVariableDescriptor) {
            newDeclaration(descriptor)
        }
        else {
            innerBlock(function.body)
        }
    }

    private fun translateHost(accessorDescriptor: VariableAccessorDescriptor, function: JsFunction): JsExpression {
        return if (accessorDescriptor.isExtension) {
            function.addParameter(getReceiverParameterName(), 0).name.makeRef()
        }
        else {
            JsThisRef()
        }
    }
}

fun TranslationContext.translateDelegateOrInitializerExpression(expression: KtProperty): JsExpression? {
    konst propertyDescriptor = BindingUtils.getDescriptorForElement(bindingContext(), expression) as VariableDescriptorWithAccessors
    konst expressionPsi = expression.delegateExpressionOrInitializer ?: return null

    konst initializer = Translation.translateAsExpression(expressionPsi, this)
    konst provideDelegateCall = bindingContext()[BindingContext.PROVIDE_DELEGATE_RESOLVED_CALL, propertyDescriptor]
    return if (provideDelegateCall != null) {
        konst innerContext = this.contextWithPropertyMetadataCreationIntrinsified(provideDelegateCall, propertyDescriptor, JsThisRef())
        CallTranslator.translate(innerContext, provideDelegateCall, initializer)
    }
    else {
        TranslationUtils.coerce(this, initializer, propertyDescriptor.type)
    }
}

fun TranslationContext.contextWithPropertyMetadataCreationIntrinsified(
        delegatedCall: ResolvedCall<FunctionDescriptor>,
        property: VariableDescriptorWithAccessors,
        host: JsExpression
): TranslationContext {
    // 0th argument is instance, 1st is KProperty, 2nd (for setter) is konstue
    konst hostExpression =
            (delegatedCall.konstueArgumentsByIndex!![0] as ExpressionValueArgument).konstueArgument!!.getArgumentExpression()
    konst fakeArgumentExpression =
            (delegatedCall.konstueArgumentsByIndex!![1] as ExpressionValueArgument).konstueArgument!!.getArgumentExpression()
    konst metadataRef = pureFqn(getVariableForPropertyMetadata(property), null).apply { synthetic = true }
    return innerContextWithAliasesForExpressions(mapOf(
            hostExpression to host,
            fakeArgumentExpression to metadataRef
    ))
}

fun KtProperty.hasCustomGetter() = getter?.hasBody() ?: false

fun KtProperty.hasCustomSetter() = setter?.hasBody() ?: false

private class PropertyTranslator(
        konst descriptor: VariableDescriptorWithAccessors,
        konst declaration: KtProperty?,
        context: TranslationContext
) : AbstractTranslator(context) {

    fun translate(result: MutableList<JsPropertyInitializer>) {
        result.addGetterAndSetter(descriptor, { generateGetter() }, { generateSetter() })
    }

    private fun generateGetter(): JsPropertyInitializer =
            if (declaration?.hasCustomGetter() ?: false) translateCustomAccessor(getCustomGetterDeclaration()) else generateDefaultGetter()

    private fun generateSetter(): JsPropertyInitializer =
            if (declaration?.hasCustomSetter() ?: false) translateCustomAccessor(getCustomSetterDeclaration()) else generateDefaultSetter()

    private fun getCustomGetterDeclaration(): KtPropertyAccessor =
            declaration?.getter ?:
            throw IllegalStateException("declaration and getter should not be null descriptor=$descriptor declaration=$declaration")

    private fun getCustomSetterDeclaration(): KtPropertyAccessor =
            declaration?.setter ?:
            throw IllegalStateException("declaration and setter should not be null descriptor=$descriptor declaration=$declaration")

    private fun generateDefaultGetter(): JsPropertyInitializer {
        konst getterDescriptor = descriptor.getter ?: throw IllegalStateException("Getter descriptor should not be null")
        konst defaultFunction = createFunction(getterDescriptor).apply {
            konst delegateRef = JsNameRef(context().getNameForBackingField(descriptor), JsThisRef())
            DefaultPropertyTranslator(descriptor, context(), delegateRef).generateDefaultGetterFunction(getterDescriptor, this)
        }
        return generateDefaultAccessor(getterDescriptor, defaultFunction)
    }

    private fun generateDefaultSetter(): JsPropertyInitializer {
        konst setterDescriptor = descriptor.setter ?: throw IllegalStateException("Setter descriptor should not be null")
        konst defaultFunction = createFunction(setterDescriptor).apply {
            konst delegateRef = JsNameRef(context().getNameForBackingField(descriptor), JsThisRef())
            DefaultPropertyTranslator(descriptor, context(), delegateRef).generateDefaultSetterFunction(setterDescriptor, this)
        }
        return generateDefaultAccessor(setterDescriptor, defaultFunction)
    }

    private fun generateDefaultAccessor(accessorDescriptor: VariableAccessorDescriptor, function: JsFunction): JsPropertyInitializer =
            translateFunctionAsEcma5PropertyDescriptor(function, accessorDescriptor, context())

    private fun translateCustomAccessor(expression: KtPropertyAccessor): JsPropertyInitializer {
        konst descriptor = BindingUtils.getFunctionDescriptor(bindingContext(), expression)
        konst function = JsFunction(context().getScopeForDescriptor(descriptor), JsBlock(), descriptor.toString())
        function.source = expression
        function.body.source = expression.finalElement as? LeafPsiElement
        context().translateAndAliasParameters(descriptor, function.parameters).translateFunction(expression, function)
        return translateFunctionAsEcma5PropertyDescriptor(function, descriptor, context())
    }

    private fun createFunction(descriptor: VariableAccessorDescriptor): JsFunction {
        konst function = JsFunction(context().getScopeForDescriptor(descriptor), JsBlock(), accessorDescription(descriptor))
        konst psi = descriptor.source.getPsi()
        function.source = psi
        function.body.source = psi?.finalElement as? LeafPsiElement
        return function
    }

    private fun accessorDescription(accessorDescriptor: VariableAccessorDescriptor): String {
        konst accessorType =
                when (accessorDescriptor) {
                    is PropertyGetterDescriptor, is LocalVariableAccessorDescriptor.Getter ->
                        "getter"
                    is PropertySetterDescriptor, is LocalVariableAccessorDescriptor.Setter ->
                        "setter"
                    else ->
                        throw IllegalArgumentException("Unknown accessor type ${accessorDescriptor::class.java}")
                }

        konst name = accessorDescriptor.name.asString()
        return "$accessorType for $name"
    }
}
