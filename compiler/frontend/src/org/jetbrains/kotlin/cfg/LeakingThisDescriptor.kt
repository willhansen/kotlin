/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtClassOrObject

interface LeakingThisDescriptor {
    konst classOrObject: KtClassOrObject

    data class PropertyIsNull(konst property: PropertyDescriptor, override konst classOrObject: KtClassOrObject) : LeakingThisDescriptor

    data class NonFinalClass(konst klass: ClassDescriptor, override konst classOrObject: KtClassOrObject) : LeakingThisDescriptor

    data class NonFinalProperty(konst property: PropertyDescriptor, override konst classOrObject: KtClassOrObject) : LeakingThisDescriptor

    data class NonFinalFunction(konst function: FunctionDescriptor, override konst classOrObject: KtClassOrObject) : LeakingThisDescriptor
}
