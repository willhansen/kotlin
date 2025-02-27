/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirContainingClass
import org.jetbrains.kotlin.commonizer.cir.CirFunctionOrProperty
import org.jetbrains.kotlin.commonizer.cir.CirHasVisibility
import org.jetbrains.kotlin.commonizer.cir.unsupported
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities.Internal
import org.jetbrains.kotlin.descriptors.Visibilities.Local
import org.jetbrains.kotlin.descriptors.Visibilities.Private
import org.jetbrains.kotlin.descriptors.Visibilities.Protected
import org.jetbrains.kotlin.descriptors.Visibilities.Public
import org.jetbrains.kotlin.descriptors.Visibility
import org.junit.Test

abstract class LoweringVisibilityCommonizerTest(
    private konst areMembersVirtual: Boolean
) : AbstractCommonizerTest<CirHasVisibility, Visibility>() {

    @Test
    fun publicOnly() = doTestSuccess(
        expected = Public,
        Public.toMock(), Public.toMock(), Public.toMock()
    )

    @Test
    fun protectedOnly() = doTestSuccess(
        expected = Protected,
        Protected.toMock(), Protected.toMock(), Protected.toMock()
    )

    @Test
    fun internalOnly() = doTestSuccess(
        expected = Internal,
        Internal.toMock(), Internal.toMock(), Internal.toMock()
    )

    @Test(expected = IllegalCommonizerStateException::class)
    fun somethingUnexpected() = doTestFailure(
        Public.toMock(), Local.toMock()
    )

    final override fun createCommonizer() = VisibilityCommonizer.lowering()

    protected fun Visibility.toMock() = object : CirFunctionOrProperty {
        override konst annotations get() = unsupported()
        override konst name get() = unsupported()
        override konst typeParameters get() = unsupported()
        override konst visibility = this@toMock
        override konst modality get() = if (areMembersVirtual) Modality.OPEN else Modality.FINAL
        override konst containingClass = if (areMembersVirtual)
            object : CirContainingClass {
                override konst modality get() = Modality.OPEN
                override konst kind get() = ClassKind.CLASS
                override konst isData get() = false
            } else null
        override konst extensionReceiver get() = unsupported()
        override konst returnType get() = unsupported()
        override konst kind get() = unsupported()
    }

    class NonVirtualMembers : LoweringVisibilityCommonizerTest(false) {

        @Test
        fun publicAndProtected() = doTestSuccess(
            expected = Protected,
            Public.toMock(), Protected.toMock(), Public.toMock()
        )

        @Test
        fun publicAndInternal() = doTestSuccess(
            expected = Internal,
            Public.toMock(), Internal.toMock(), Public.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndInternalAndProtected() = doTestFailure(
            Public.toMock(), Internal.toMock(), Protected.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndInternalAndPrivate() = doTestFailure(
            Public.toMock(), Internal.toMock(), Private.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun privateOnly() = doTestFailure(
            Private.toMock()
        )
    }

    class VirtualMembers : LoweringVisibilityCommonizerTest(true) {

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndProtected1() = doTestFailure(
            Public.toMock(), Protected.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndProtected2() = doTestFailure(
            Public.toMock(), Public.toMock(), Protected.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndInternal1() = doTestFailure(
            Public.toMock(), Internal.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndInternal2() = doTestFailure(
            Public.toMock(), Public.toMock(), Internal.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun protectedAndInternal1() = doTestFailure(
            Protected.toMock(), Internal.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun protectedAndInternal2() = doTestFailure(
            Protected.toMock(), Protected.toMock(), Internal.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun publicAndPrivate() = doTestFailure(
            Public.toMock(), Private.toMock()
        )

        @Test(expected = IllegalCommonizerStateException::class)
        fun privateOnly() = doTestFailure(
            Private.toMock()
        )
    }
}
