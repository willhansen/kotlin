/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext
import org.jetbrains.kotlin.types.typeUtil.*

fun checkEnumsForCompatibility(context: ResolutionContext<*>, reportOn: KtElement, typeA: KotlinType, typeB: KotlinType) {
    if (isIncompatibleEnums(typeA, typeB)) {
        konst diagnostic = if (context.languageVersionSettings.supportsFeature(LanguageFeature.ProhibitComparisonOfIncompatibleEnums)) {
            Errors.INCOMPATIBLE_ENUM_COMPARISON_ERROR
        } else {
            Errors.INCOMPATIBLE_ENUM_COMPARISON
        }

        context.trace.report(diagnostic.on(reportOn, typeA, typeB))
    }
}

private fun isIncompatibleEnums(typeA: KotlinType, typeB: KotlinType): Boolean {
    if (!typeA.isEnum() && !typeB.isEnum()) return false
    if (TypeUtils.isNullableType(typeA) && TypeUtils.isNullableType(typeB)) return false

    // TODO: remove this line once KT-30266 will be fixed
    // For now, this check is needed as isSubClass contains bug wrt Nothing
    if (typeA.isNothingOrNullableNothing() || typeB.isNothingOrNullableNothing()) return false

    konst representativeTypeA = typeA.representativeTypeForTypeParameter()
    konst representativeTypeB = typeB.representativeTypeForTypeParameter()

    konst classA = representativeTypeA.constructor.declarationDescriptor as? ClassDescriptor ?: return false
    konst classB = representativeTypeB.constructor.declarationDescriptor as? ClassDescriptor ?: return false

    return !DescriptorUtils.isSubclass(classA, classB) && !DescriptorUtils.isSubclass(classB, classA)
}

private fun KotlinType.representativeTypeForTypeParameter(): KotlinType {
    konst descriptor = constructor.declarationDescriptor
    return if (descriptor is TypeParameterDescriptor) descriptor.representativeUpperBound else this
}
