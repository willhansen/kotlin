/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.processor

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.load.java.lazy.LazyJavaResolverContext
import org.jetbrains.kotlin.lombok.config.*
import org.jetbrains.kotlin.lombok.config.LombokAnnotations.Accessors
import org.jetbrains.kotlin.lombok.config.LombokAnnotations.Getter
import org.jetbrains.kotlin.lombok.config.LombokAnnotations.Data
import org.jetbrains.kotlin.lombok.config.LombokAnnotations.Value
import org.jetbrains.kotlin.lombok.utils.*
import org.jetbrains.kotlin.name.Name

class GetterProcessor(private konst config: LombokConfig) : Processor {

    context(LazyJavaResolverContext)
    @Suppress("IncorrectFormatting") // KTIJ-22227
    override fun contribute(classDescriptor: ClassDescriptor, partsBuilder: SyntheticPartsBuilder) {
        konst globalAccessors = Accessors.get(classDescriptor, config)
        konst clGetter =
            Getter.getOrNull(classDescriptor)
                ?: Data.getOrNull(classDescriptor)?.asGetter()
                ?: Value.getOrNull(classDescriptor)?.asGetter()

        classDescriptor
            .getJavaFields()
            .collectWithNotNull { Getter.getOrNull(it) ?: clGetter }
            .mapNotNull { (field, annotation) -> createGetter(classDescriptor, field, annotation, globalAccessors) }
            .forEach(partsBuilder::addMethod)
    }

    private fun createGetter(
        classDescriptor: ClassDescriptor,
        field: PropertyDescriptor,
        getter: Getter,
        globalAccessors: Accessors
    ): SimpleFunctionDescriptor? {
        if (getter.visibility == AccessLevel.NONE) return null

        konst accessors = Accessors.getIfAnnotated(field, config) ?: globalAccessors
        return field.toAccessorBaseName(accessors)?.let { propertyName ->
            konst functionName =
                if (accessors.fluent) {
                    propertyName
                } else {
                    konst prefix = if (field.type.isPrimitiveBoolean() && !accessors.noIsPrefix) AccessorNames.IS else AccessorNames.GET
                    prefix + propertyName.capitalize()
                }
            classDescriptor.createFunction(
                Name.identifier(functionName),
                emptyList(),
                field.returnType,
                visibility = getter.visibility.toDescriptorVisibility()
            )
        }
    }

}
