/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.expressions.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirInaccessibleReceiverExpression
import org.jetbrains.kotlin.fir.expressions.builder.FirExpressionBuilder
import org.jetbrains.kotlin.fir.expressions.impl.FirInaccessibleReceiverExpressionImpl
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.references.FirThisReference
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirInaccessibleReceiverExpressionBuilder : FirAnnotationContainerBuilder, FirExpressionBuilder {
    override var source: KtSourceElement? = null
    override lateinit var typeRef: FirTypeRef
    override konst annotations: MutableList<FirAnnotation> = mutableListOf()
    lateinit var calleeReference: FirThisReference

    override fun build(): FirInaccessibleReceiverExpression {
        return FirInaccessibleReceiverExpressionImpl(
            source,
            typeRef,
            annotations.toMutableOrEmpty(),
            calleeReference,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildInaccessibleReceiverExpression(init: FirInaccessibleReceiverExpressionBuilder.() -> Unit): FirInaccessibleReceiverExpression {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirInaccessibleReceiverExpressionBuilder().apply(init).build()
}
