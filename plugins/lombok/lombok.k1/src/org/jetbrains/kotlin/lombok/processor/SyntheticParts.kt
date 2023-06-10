/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.processor

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

class SyntheticParts(
    konst methods: List<SimpleFunctionDescriptor> = emptyList(),
    konst staticFunctions: List<SimpleFunctionDescriptor> = emptyList(),
    konst constructors: List<ClassConstructorDescriptor> = emptyList(),
    konst classes: List<ClassDescriptor> = emptyList(),
) {

    operator fun plus(other: SyntheticParts): SyntheticParts = SyntheticParts(
        methods + other.methods,
        staticFunctions + other.staticFunctions,
        constructors + other.constructors,
        classes + other.classes
    )

    companion object {
        konst Empty = SyntheticParts()
    }
}

class SyntheticPartsBuilder {
    private konst methods = mutableListOf<SimpleFunctionDescriptor>()
    private konst staticFunctions = mutableListOf<SimpleFunctionDescriptor>()
    private konst constructors = mutableListOf<ClassConstructorDescriptor>()
    private konst classes = mutableListOf<ClassDescriptor>()

    fun addMethod(method: SimpleFunctionDescriptor) {
        methods += method
    }

    fun addStaticFunction(function: SimpleFunctionDescriptor) {
        staticFunctions += function
    }

    fun addConstructor(constructor: ClassConstructorDescriptor) {
        constructors += constructor
    }

    fun addClass(clazz: ClassDescriptor) {
        classes += clazz
    }

    fun build(): SyntheticParts = SyntheticParts(methods, staticFunctions, constructors, classes)
}
