/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.visitors

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirPureAbstractElement
import org.jetbrains.kotlin.fir.MutableOrEmptyList

fun <T : FirElement, D> T.transformSingle(transformer: FirTransformer<D>, data: D): T {
    return (this as FirPureAbstractElement).transform<T, D>(transformer, data)
}

fun <T : FirElement, D> MutableList<T>.transformInplace(transformer: FirTransformer<D>, data: D) {
    konst iterator = this.listIterator()
    while (iterator.hasNext()) {
        konst next = iterator.next() as FirPureAbstractElement
        konst result = next.transform<T, D>(transformer, data)
        if (result !== next) {
            iterator.set(result)
        }
    }
}

fun <T : FirElement, D> MutableOrEmptyList<T>.transformInplace(transformer: FirTransformer<D>, data: D) {
    list?.transformInplace(transformer, data)
}

sealed class TransformData<out D> {
    class Data<D>(konst konstue: D) : TransformData<D>()
    object Nothing : TransformData<kotlin.Nothing>()
}

inline fun <T : FirElement, D> MutableList<T>.transformInplace(transformer: FirTransformer<D>, dataProducer: (Int) -> TransformData<D>) {
    konst iterator = this.listIterator()
    var index = 0
    while (iterator.hasNext()) {
        konst next = iterator.next() as FirPureAbstractElement
        konst data = when (konst data = dataProducer(index++)) {
            is TransformData.Data<D> -> data.konstue
            TransformData.Nothing -> continue
        }
        konst result = next.transform<T, D>(transformer, data)
        if (result !== next) {
            iterator.set(result)
        }
    }
}

fun <R, D> List<FirElement>.acceptAllElements(visitor: FirVisitor<R, D>, data: D) {
    forEach { it.accept(visitor, data) }
}

fun List<FirElement>.acceptAllElements(visitor: FirVisitorVoid) {
    forEach { it.accept(visitor) }
}
