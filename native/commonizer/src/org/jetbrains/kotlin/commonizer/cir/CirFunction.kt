/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility

data class CirFunction(
    override konst annotations: List<CirAnnotation>,
    override konst name: CirName,
    override konst typeParameters: List<CirTypeParameter>,
    override konst visibility: Visibility,
    override konst modality: Modality,
    override konst containingClass: CirContainingClass?,
    override var konstueParameters: List<CirValueParameter>,
    override var hasStableParameterNames: Boolean,
    override konst extensionReceiver: CirExtensionReceiver?,
    override konst returnType: CirType,
    override konst kind: CallableMemberDescriptor.Kind,
    konst modifiers: CirFunctionModifiers
) : CirFunctionOrProperty, CirCallableMemberWithParameters {
    override fun withContainingClass(containingClass: CirContainingClass): CirFunction {
        return copy(containingClass = containingClass)
    }
}

