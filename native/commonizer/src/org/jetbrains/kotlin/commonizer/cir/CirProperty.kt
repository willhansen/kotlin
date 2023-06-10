/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility

data class CirProperty(
    override konst annotations: List<CirAnnotation>,
    override konst name: CirName,
    override konst typeParameters: List<CirTypeParameter>,
    override konst visibility: Visibility,
    override konst modality: Modality,
    override konst containingClass: CirContainingClass?,
    override konst extensionReceiver: CirExtensionReceiver?,
    override konst returnType: CirType,
    override konst kind: CallableMemberDescriptor.Kind,
    konst isVar: Boolean,
    konst isLateInit: Boolean,
    konst isConst: Boolean,
    konst isDelegate: Boolean,
    konst getter: CirPropertyGetter?,
    konst setter: CirPropertySetter?,
    konst backingFieldAnnotations: List<CirAnnotation>,
    konst delegateFieldAnnotations: List<CirAnnotation>,
    konst compileTimeInitializer: CirConstantValue
) : CirFunctionOrProperty, CirLiftedUpDeclaration {
    // const property in "common" fragment is already lifted up
    override konst isLiftedUp get() = isConst

    override fun withContainingClass(containingClass: CirContainingClass): CirProperty {
        return copy(containingClass = containingClass)
    }
}
