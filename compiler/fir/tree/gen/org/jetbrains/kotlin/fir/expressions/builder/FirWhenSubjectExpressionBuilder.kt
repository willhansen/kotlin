/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.fir.expressions.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirExpressionRef
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenSubjectExpression
import org.jetbrains.kotlin.fir.expressions.builder.FirExpressionBuilder
import org.jetbrains.kotlin.fir.expressions.impl.FirWhenSubjectExpressionImpl
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirWhenSubjectExpressionBuilder : FirAnnotationContainerBuilder, FirExpressionBuilder {
    override var source: KtSourceElement? = null
    override konst annotations: MutableList<FirAnnotation> = mutableListOf()
    lateinit var whenRef: FirExpressionRef<FirWhenExpression>

    override fun build(): FirWhenSubjectExpression {
        return FirWhenSubjectExpressionImpl(
            source,
            annotations.toMutableOrEmpty(),
            whenRef,
        )
    }


    @Deprecated("Modification of 'typeRef' has no impact for FirWhenSubjectExpressionBuilder", level = DeprecationLevel.HIDDEN)
    override var typeRef: FirTypeRef
        get() = throw IllegalStateException()
        set(_) {
            throw IllegalStateException()
        }
}

@OptIn(ExperimentalContracts::class)
inline fun buildWhenSubjectExpression(init: FirWhenSubjectExpressionBuilder.() -> Unit): FirWhenSubjectExpression {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirWhenSubjectExpressionBuilder().apply(init).build()
}
