/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionContext

abstract class FirPartialBodyResolveTransformer(
    konst transformer: FirAbstractBodyResolveTransformerDispatcher
) : FirAbstractBodyResolveTransformer(transformer.transformerPhase) {
    @Suppress("OVERRIDE_BY_INLINE")
    final override inline konst context: BodyResolveContext
        get() = transformer.context

    @Suppress("OVERRIDE_BY_INLINE")
    final override inline konst components: BodyResolveTransformerComponents
        get() = transformer.components

    @Suppress("OVERRIDE_BY_INLINE")
    final override inline konst resolutionContext: ResolutionContext
        get() = transformer.resolutionContext

    @set:PrivateForInline
    override var implicitTypeOnly: Boolean
        get() = transformer.implicitTypeOnly
        set(konstue) {
            transformer.implicitTypeOnly = konstue
        }

    override fun <E : FirElement> transformElement(element: E, data: ResolutionMode): E {
        return element.transform(transformer, data)
    }
}
