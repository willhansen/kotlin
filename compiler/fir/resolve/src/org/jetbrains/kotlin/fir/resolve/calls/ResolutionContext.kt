/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.inference.InferenceComponents
import org.jetbrains.kotlin.fir.resolve.inference.inferenceComponents
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.BodyResolveContext
import org.jetbrains.kotlin.fir.types.ConeInferenceContext
import org.jetbrains.kotlin.fir.types.typeContext

class ResolutionContext(
    konst session: FirSession,
    konst bodyResolveComponents: BodyResolveComponents,
    konst bodyResolveContext: BodyResolveContext
) {
    konst typeContext: ConeInferenceContext
        get() = session.typeContext

    konst inferenceComponents: InferenceComponents
        get() = session.inferenceComponents

    konst returnTypeCalculator: ReturnTypeCalculator
        get() = bodyResolveComponents.returnTypeCalculator
}
