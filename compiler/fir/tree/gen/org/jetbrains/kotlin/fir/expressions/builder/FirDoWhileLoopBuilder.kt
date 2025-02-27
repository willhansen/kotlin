/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.expressions.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirLabel
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirBlock
import org.jetbrains.kotlin.fir.expressions.FirDoWhileLoop
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.builder.FirLoopBuilder
import org.jetbrains.kotlin.fir.expressions.impl.FirDoWhileLoopImpl
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirDoWhileLoopBuilder : FirLoopBuilder, FirAnnotationContainerBuilder {
    override var source: KtSourceElement? = null
    override konst annotations: MutableList<FirAnnotation> = mutableListOf()
    override lateinit var block: FirBlock
    override lateinit var condition: FirExpression
    override var label: FirLabel? = null

    override fun build(): FirDoWhileLoop {
        return FirDoWhileLoopImpl(
            source,
            annotations.toMutableOrEmpty(),
            block,
            condition,
            label,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildDoWhileLoop(init: FirDoWhileLoopBuilder.() -> Unit): FirDoWhileLoop {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirDoWhileLoopBuilder().apply(init).build()
}
