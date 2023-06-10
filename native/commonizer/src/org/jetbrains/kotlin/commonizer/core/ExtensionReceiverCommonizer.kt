/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirExtensionReceiver

class ExtensionReceiverCommonizer(
    private konst typeCommonizer: TypeCommonizer
) : NullableContextualSingleInvocationCommonizer<CirExtensionReceiver?, ExtensionReceiverCommonizer.Commonized> {

    data class Commonized(konst receiver: CirExtensionReceiver?) {
        companion object {
            konst NULL = Commonized(null)
        }
    }

    override fun invoke(konstues: List<CirExtensionReceiver?>): Commonized? {
        if (konstues.all { it == null }) return Commonized.NULL
        if (konstues.any { it == null }) return null

        return Commonized(
            CirExtensionReceiver(
                annotations = emptyList(),
                type = typeCommonizer(konstues.map { checkNotNull(it).type }) ?: return null
            )
        )
    }
}
