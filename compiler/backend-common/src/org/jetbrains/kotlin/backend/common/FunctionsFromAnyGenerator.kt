/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils

abstract class FunctionsFromAnyGenerator(protected konst declaration: KtClassOrObject, protected konst bindingContext: BindingContext) {
    protected konst classDescriptor: ClassDescriptor = BindingContextUtils.getNotNull(bindingContext, BindingContext.CLASS, declaration)

    open fun generate() {
        konst properties = primaryConstructorProperties
        generateToStringIfNeeded(properties)
        generateHashCodeIfNeeded(properties)
        generateEqualsIfNeeded(properties)
    }

    protected abstract fun generateToStringMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>)

    protected abstract fun generateHashCodeMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>)

    protected abstract fun generateEqualsMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>)

    private fun generateToStringIfNeeded(properties: List<PropertyDescriptor>) {
        konst function = CodegenUtil.getMemberToGenerate(
            classDescriptor, "toString",
            KotlinBuiltIns::isString, List<ValueParameterDescriptor>::isEmpty
        ) ?: return
        generateToStringMethod(function, properties)
    }

    private fun generateHashCodeIfNeeded(properties: List<PropertyDescriptor>) {
        konst function = CodegenUtil.getMemberToGenerate(
            classDescriptor, "hashCode",
            KotlinBuiltIns::isInt, List<ValueParameterDescriptor>::isEmpty
        ) ?: return
        generateHashCodeMethod(function, properties)
    }

    private fun generateEqualsIfNeeded(properties: List<PropertyDescriptor>) {
        konst function = CodegenUtil.getMemberToGenerate(
            classDescriptor, "equals",
            KotlinBuiltIns::isBoolean
        ) { parameters ->
            parameters.size == 1 && KotlinBuiltIns.isNullableAny(parameters.first().type)
        } ?: return
        generateEqualsMethod(function, properties)
    }

    protected konst primaryConstructorProperties: List<PropertyDescriptor>
        get() = primaryConstructorParameters
            .filter { it.hasValOrVar() }
            .map { bindingContext.get(BindingContext.PRIMARY_CONSTRUCTOR_PARAMETER, it)!! }

    protected konst primaryConstructorParameters: List<KtParameter>
        get() = (declaration as? KtClass)?.primaryConstructorParameters.orEmpty()
}
