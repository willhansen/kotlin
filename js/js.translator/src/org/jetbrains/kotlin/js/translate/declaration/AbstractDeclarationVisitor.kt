/*
 * Copyright 2010-2016 JetBrains s.r.o.
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
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.isOverridable
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.backend.ast.JsName
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.expression.translateAndAliasParameters
import org.jetbrains.kotlin.js.translate.expression.translateFunction
import org.jetbrains.kotlin.js.translate.expression.wrapWithInlineMetadata
import org.jetbrains.kotlin.js.translate.general.TranslatorVisitor
import org.jetbrains.kotlin.js.translate.utils.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtensionProperty

abstract class AbstractDeclarationVisitor : TranslatorVisitor<Unit>()  {
    override fun emptyResult(context: TranslationContext) { }

    open konst enumInitializerName: JsName?
        get() = null

    override fun visitClassOrObject(classOrObject: KtClassOrObject, context: TranslationContext) {
        ClassTranslator.translate(classOrObject, context, enumInitializerName)
        konst descriptor = BindingUtils.getClassDescriptor(context.bindingContext(), classOrObject)
        context.export(descriptor)
    }

    override fun visitProperty(expression: KtProperty, context: TranslationContext) {
        konst descriptor = BindingUtils.getPropertyDescriptor(context.bindingContext(), expression)
        if (descriptor.modality === Modality.ABSTRACT) return

        konst propertyContext = context.newDeclaration(descriptor)

        konst defaultTranslator = DefaultPropertyTranslator(descriptor, context, getBackingFieldReference(descriptor))
        konst getter = descriptor.getter!!
        konst getterExpr = if (expression.hasCustomGetter()) {
            translateFunction(getter, expression.getter!!, propertyContext).first
        }
        else {
            konst function = context.getFunctionObject(getter)
            function.source = expression
            defaultTranslator.generateDefaultGetterFunction(getter, function)
            function
        }

        konst setterExpr = if (descriptor.isVar) {
            konst setter = descriptor.setter!!
            if (expression.hasCustomSetter()) {
                translateFunction(setter, expression.setter!!, propertyContext).first
            }
            else {
                konst function = context.getFunctionObject(setter)
                function.source = expression
                defaultTranslator.generateDefaultSetterFunction(setter, function)
                function
            }
        }
        else {
            null
        }

        if (TranslationUtils.shouldAccessViaFunctions(descriptor) || descriptor.isExtensionProperty) {
            addFunction(descriptor.getter!!, getterExpr, expression.getter ?: expression)
            descriptor.setter?.let { addFunction(it, setterExpr!!, expression.setter ?: expression) }
        }
        else {
            addProperty(descriptor, getterExpr, setterExpr)
        }
    }

    override fun visitNamedFunction(expression: KtNamedFunction, context: TranslationContext) {
        konst descriptor = BindingUtils.getFunctionDescriptor(context.bindingContext(), expression)
        konst functionAndContext = if (descriptor.modality != Modality.ABSTRACT) {
            translateFunction(descriptor, expression, context)
        }
        else {
            null
        }

        addFunction(descriptor, functionAndContext?.first, expression)
    }

    override fun visitTypeAlias(typeAlias: KtTypeAlias, data: TranslationContext?) {}

    private fun translateFunction(
            descriptor: FunctionDescriptor,
            expression: KtDeclarationWithBody,
            context: TranslationContext
    ): Pair<JsExpression, TranslationContext> {
        konst function = context.getFunctionObject(descriptor)
        function.source = expression
        function.body.source = expression.finalElement as? LeafPsiElement
        konst innerContext = context.newDeclaration(descriptor).translateAndAliasParameters(descriptor, function.parameters)

        if (descriptor.isSuspend) {
            function.fillCoroutineMetadata(innerContext, descriptor, hasController = false)
        }

        if (!descriptor.isOverridable) {
            function.body.statements += FunctionBodyTranslator.setDefaultValueForArguments(descriptor, innerContext)
        }
        innerContext.translateFunction(expression, function)

        return Pair(innerContext.wrapWithInlineMetadata(context, function, descriptor), innerContext)
    }

    // used from kotlinx.serialization
    abstract fun addFunction(
            descriptor: FunctionDescriptor,
            expression: JsExpression?,
            psi: KtElement?
    )

    // used from kotlinx.serialization
    abstract fun addProperty(
            descriptor: PropertyDescriptor,
            getter: JsExpression,
            setter: JsExpression?
    )

    // used from kotlinx.serialization
    abstract fun getBackingFieldReference(descriptor: PropertyDescriptor): JsExpression
}