/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.types.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.types.FirImplicitTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirImplicitTypeRefImpl
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirImplicitTypeRefBuilder {
    lateinit var source: KtSourceElement

    fun build(): FirImplicitTypeRef {
        return FirImplicitTypeRefImpl(
            source,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildImplicitTypeRef(init: FirImplicitTypeRefBuilder.() -> Unit): FirImplicitTypeRef {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirImplicitTypeRefBuilder().apply(init).build()
}

@OptIn(ExperimentalContracts::class)
inline fun buildImplicitTypeRefCopy(original: FirImplicitTypeRef, init: FirImplicitTypeRefBuilder.() -> Unit): FirImplicitTypeRef {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    konst copyBuilder = FirImplicitTypeRefBuilder()
    original.source?.let { copyBuilder.source = it }
    return copyBuilder.apply(init).build()
}
