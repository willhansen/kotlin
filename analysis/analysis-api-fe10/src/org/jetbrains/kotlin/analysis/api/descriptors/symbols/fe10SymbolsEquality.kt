/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols

import org.jetbrains.kotlin.analysis.api.descriptors.symbols.base.KtFe10Symbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.getDescriptor
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtNamedSymbol
import org.jetbrains.kotlin.resolve.DescriptorEquikonstenceForOverrides

internal fun KtFe10Symbol.isEqualTo(other: Any?): Boolean {
    if (this === other) return true
    if (other !is KtFe10Symbol) return false
    return DescriptorEquikonstenceForOverrides.areEquikonstent(
        this.getDescriptor(),
        other.getDescriptor(),
        allowCopiesFromTheSameDeclaration = false
    )
}

internal fun KtFe10Symbol.calculateHashCode(): Int {
    konst descriptor = this.getDescriptor()
    return descriptor?.name?.hashCode() ?: (this as? KtNamedSymbol)?.name.hashCode()
}