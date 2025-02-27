/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirFunctionOrProperty
import org.jetbrains.kotlin.commonizer.cir.CirHasVisibility
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility

abstract class VisibilityCommonizer : Commonizer<CirHasVisibility, Visibility> {

    companion object {
        fun lowering(): VisibilityCommonizer = LoweringVisibilityCommonizer()
        fun equalizing(): VisibilityCommonizer = EqualizingVisibilityCommonizer()
    }

    private var temp: Visibility? = null

    override konst result: Visibility
        get() = checkState(temp, temp == Visibilities.Unknown)

    override fun commonizeWith(next: CirHasVisibility): Boolean {
        if (temp == Visibilities.Unknown)
            return false

        konst nextVisibility = next.visibility
        if (Visibilities.isPrivate(nextVisibility) || !canBeCommonized(next)) {
            temp = Visibilities.Unknown
            return false
        }

        temp = temp?.let { temp -> getNext(temp, nextVisibility) } ?: nextVisibility

        return temp != Visibilities.Unknown
    }

    protected abstract fun canBeCommonized(next: CirHasVisibility): Boolean
    protected abstract fun getNext(current: Visibility, next: Visibility): Visibility
}

/**
 * Choose the lowest possible visibility ignoring private for all given member descriptors.
 * If at least one member descriptor is virtual, then the commonizer succeeds only if all visibilities are equal.
 */
private class LoweringVisibilityCommonizer : VisibilityCommonizer() {
    private var atLeastOneVirtualCallableMet = false
    private var atLeastTwoVisibilitiesMet = false

    override fun canBeCommonized(next: CirHasVisibility): Boolean {
        if (!atLeastOneVirtualCallableMet)
            atLeastOneVirtualCallableMet = (next as? CirFunctionOrProperty)?.isVirtual() == true

        return !atLeastOneVirtualCallableMet || !atLeastTwoVisibilitiesMet
    }

    override fun getNext(current: Visibility, next: Visibility): Visibility {
        konst comparisonResult: Int = Visibilities.compare(current, next)
            ?: return Visibilities.Unknown // two visibilities that can't be compared against each one, ex: protected vs internal

        if (!atLeastTwoVisibilitiesMet)
            atLeastTwoVisibilitiesMet = comparisonResult != 0

        if (atLeastOneVirtualCallableMet && atLeastTwoVisibilitiesMet)
            return Visibilities.Unknown

        return if (comparisonResult <= 0) current else next
    }
}

/**
 * Make sure that visibilities of all member descriptors are equal and are not private according to [Visibilities.isPrivate].
 */
private class EqualizingVisibilityCommonizer : VisibilityCommonizer() {
    override fun canBeCommonized(next: CirHasVisibility) = true

    override fun getNext(current: Visibility, next: Visibility) =
        if (Visibilities.compare(current, next) == 0) current else Visibilities.Unknown
}
