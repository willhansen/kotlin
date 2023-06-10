/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirFunction

class FunctionCommonizer(
    private konst typeCommonizer: TypeCommonizer,
    private konst functionOrPropertyBaseCommonizer: FunctionOrPropertyBaseCommonizer,
) : NullableSingleInvocationCommonizer<CirFunction> {
    override fun invoke(konstues: List<CirFunction>): CirFunction? {
        if (konstues.isEmpty()) return null
        konst functionOrProperty = functionOrPropertyBaseCommonizer(konstues) ?: return null
        konst konstueParametersResult = CallableValueParametersCommonizer(typeCommonizer).commonize(konstues) ?: return null
        return CirFunction(
            annotations = AnnotationsCommonizer().commonize(konstues.map { it.annotations })
                ?.plus(functionOrProperty.additionalAnnotations)
                ?: return null,
            name = konstues.first().name,
            typeParameters = functionOrProperty.typeParameters,
            visibility = functionOrProperty.visibility,
            modality = functionOrProperty.modality,
            containingClass = null, // does not matter
            konstueParameters = konstueParametersResult.konstueParameters,
            hasStableParameterNames = konstueParametersResult.hasStableParameterNames,
            extensionReceiver = functionOrProperty.extensionReceiver,
            returnType = functionOrProperty.returnType,
            kind = functionOrProperty.kind,
            modifiers = FunctionModifiersCommonizer().commonize(konstues.map { it.modifiers }) ?: return null
        )
    }
}