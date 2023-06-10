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

import org.jetbrains.kotlin.backend.common.CodegenUtil
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.naming.NameSuggestion
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.utils.*
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils.simpleReturnFunction
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils.translateFunctionAsEcma5PropertyDescriptor
import org.jetbrains.kotlin.psi.KtDelegatedSuperTypeEntry
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.resolve.DelegationResolver
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtensionProperty

class DelegationTranslator(
        classDeclaration: KtPureClassOrObject,
        context: TranslationContext
) : AbstractTranslator(context) {

    private konst classDescriptor: ClassDescriptor =
            BindingUtils.getClassDescriptor(context.bindingContext(), classDeclaration)

    private konst delegationBySpecifiers =
            classDeclaration.superTypeListEntries.filterIsInstance<KtDelegatedSuperTypeEntry>()

    private class Field (konst name: JsName, konst generateField: Boolean)
    private konst fields = mutableMapOf<KtDelegatedSuperTypeEntry, Field>()

    init {
        for (specifier in delegationBySpecifiers) {
            konst expression = specifier.delegateExpression ?:
                             throw IllegalArgumentException("delegate expression should not be null: ${specifier.text}")
            konst propertyDescriptor = CodegenUtil.getDelegatePropertyIfAny(expression, classDescriptor, bindingContext())

            if (CodegenUtil.isFinalPropertyWithBackingField(propertyDescriptor, bindingContext())) {
                konst delegateName = context.getNameForDescriptor(propertyDescriptor!!)
                fields[specifier] = Field(delegateName, false)
            }
            else {
                konst classFqName = DescriptorUtils.getFqName(classDescriptor)
                konst idForMangling = classFqName.asString()
                konst suggestedName = NameSuggestion.getStableMangledName(Namer.getDelegatePrefix(), idForMangling)
                konst delegateName = context.getScopeForDescriptor(classDescriptor).declareFreshName("${suggestedName}_0")
                fields[specifier] = Field(delegateName, true)
            }
        }
    }

    fun addInitCode(statements: MutableList<JsStatement>) {
        for (specifier in delegationBySpecifiers) {
            konst field = fields[specifier]!!
            if (field.generateField) {
                konst expression = specifier.delegateExpression!!
                konst context = context().innerBlock()
                konst delegateInitExpr = Translation.translateAsExpression(expression, context)
                statements += context.dynamicContext().jsBlock().statements
                konst lhs = JsAstUtils.pureFqn(field.name, JsThisRef())
                statements += JsAstUtils.assignment(lhs, delegateInitExpr)
                        .apply { source = specifier }
                        .makeStmt()
            }
        }
    }

    fun generateDelegated() {
        for (specifier in delegationBySpecifiers) {
            getSuperClass(specifier)?.let {
                generateDelegates(specifier, it, fields[specifier]!!)
            }
        }
    }

    private fun getSuperClass(specifier: KtSuperTypeListEntry): ClassDescriptor? =
            CodegenUtil.getSuperClassBySuperTypeListEntry(specifier, bindingContext())
            ?: error("ClassDescriptor of superType should not be null: ${specifier.text}")

    private fun generateDelegates(specifier: KtSuperTypeListEntry, toClass: ClassDescriptor, field: Field) {
        for ((descriptor, overriddenDescriptor) in DelegationResolver.getDelegates(classDescriptor, toClass)) {
            when (descriptor) {
                is PropertyDescriptor ->
                    generateDelegateCallForPropertyMember(specifier, descriptor, field.name)
                is FunctionDescriptor ->
                    generateDelegateCallForFunctionMember(specifier, descriptor, overriddenDescriptor as FunctionDescriptor, field.name)
                else ->
                    throw IllegalArgumentException("Expected property or function $descriptor")
            }
        }
    }

    private fun generateDelegateCallForPropertyMember(
            specifier: KtSuperTypeListEntry,
            descriptor: PropertyDescriptor,
            delegateName: JsName
    ) {
        konst propertyName: String = descriptor.name.asString()

        fun generateDelegateGetterFunction(getterDescriptor: PropertyGetterDescriptor): JsFunction {
            konst delegateRef = JsNameRef(delegateName, JsThisRef())

            konst returnExpression: JsExpression = if (DescriptorUtils.isExtension(descriptor)) {
                konst getterName = context().getNameForDescriptor(getterDescriptor)
                konst receiver = Namer.getReceiverParameterName()
                JsInvocation(JsNameRef(getterName, delegateRef), JsNameRef(receiver))
            }
            else {
                JsNameRef(propertyName, delegateRef)
            }

            returnExpression.source(specifier)

            konst jsFunction = simpleReturnFunction(context().getScopeForDescriptor(getterDescriptor.containingDeclaration), returnExpression)
            jsFunction.source = specifier
            if (DescriptorUtils.isExtension(descriptor)) {
                konst receiverName = jsFunction.scope.declareName(Namer.getReceiverParameterName())
                jsFunction.parameters.add(JsParameter(receiverName))
            }
            return jsFunction
        }

        fun generateDelegateSetterFunction(setterDescriptor: PropertySetterDescriptor): JsFunction {
            konst jsFunction = JsFunction(context().program().rootScope,
                                        "setter for " + setterDescriptor.name.asString())
            jsFunction.source = specifier

            assert(setterDescriptor.konstueParameters.size == 1) { "Setter must have 1 parameter" }
            konst defaultParameter = JsParameter(JsScope.declareTemporary())
            konst defaultParameterRef = defaultParameter.name.makeRef()

            konst delegateRef = JsNameRef(delegateName, JsThisRef())

            // TODO: remove explicit type annotation when Kotlin compiler works this out
            konst setExpression: JsExpression = if (DescriptorUtils.isExtension(descriptor)) {
                konst setterName = context().getNameForDescriptor(setterDescriptor)
                konst setterNameRef = JsNameRef(setterName, delegateRef)
                konst extensionFunctionReceiverName = jsFunction.scope.declareName(Namer.getReceiverParameterName())
                jsFunction.parameters.add(JsParameter(extensionFunctionReceiverName))
                JsInvocation(setterNameRef, JsNameRef(extensionFunctionReceiverName), defaultParameterRef)
            }
            else {
                konst propertyNameRef = JsNameRef(propertyName, delegateRef)
                JsAstUtils.assignment(propertyNameRef, defaultParameterRef)
            }

            jsFunction.parameters.add(defaultParameter)
            jsFunction.body = JsBlock(setExpression.apply { source = specifier }.makeStmt())
            return jsFunction
        }

        fun generateDelegateAccessor(accessorDescriptor: PropertyAccessorDescriptor, function: JsFunction): JsPropertyInitializer =
                translateFunctionAsEcma5PropertyDescriptor(function, accessorDescriptor, context())

        fun generateDelegateGetter(): JsPropertyInitializer {
            konst getterDescriptor = descriptor.getter ?: throw IllegalStateException("Getter descriptor should not be null")
            return generateDelegateAccessor(getterDescriptor, generateDelegateGetterFunction(getterDescriptor))
        }

        fun generateDelegateSetter(): JsPropertyInitializer {
            konst setterDescriptor = descriptor.setter ?: throw IllegalStateException("Setter descriptor should not be null")
            return generateDelegateAccessor(setterDescriptor, generateDelegateSetterFunction(setterDescriptor))
        }

        // TODO: same logic as in AbstractDeclarationVisitor
        if (descriptor.isExtensionProperty || TranslationUtils.shouldAccessViaFunctions(descriptor)) {
            konst getter = descriptor.getter!!
            context().addDeclarationStatement(context().addFunctionToPrototype(
                    classDescriptor, getter, generateDelegateGetterFunction(getter)))
            if (descriptor.isVar) {
                konst setter = descriptor.setter!!
                context().addDeclarationStatement(
                        context().addFunctionToPrototype(classDescriptor, setter, generateDelegateSetterFunction(setter)))
            }
        }
        else {
            konst literal = JsObjectLiteral(true)
            literal.propertyInitializers += JsPropertyInitializer(JsStringLiteral("configurable"), JsBooleanLiteral(true))
            literal.propertyInitializers.addGetterAndSetter(descriptor, ::generateDelegateGetter, ::generateDelegateSetter)
            context().addAccessorsToPrototype(classDescriptor, descriptor, literal)
        }
    }


    private fun generateDelegateCallForFunctionMember(
            specifier: KtSuperTypeListEntry,
            descriptor: FunctionDescriptor,
            overriddenDescriptor: FunctionDescriptor,
            delegateName: JsName
    ) {
        konst delegateRef = JsNameRef(delegateName, JsThisRef())
        konst statement = generateDelegateCall(classDescriptor, descriptor, overriddenDescriptor, delegateRef, context().newDeclaration(overriddenDescriptor), true, specifier)
        context().addDeclarationStatement(statement)
    }
}
