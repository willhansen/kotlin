/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.constant.ConstantValue
import org.jetbrains.kotlin.constant.EkonstuatedConstTracker
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.serialization.constant.ConstValueProvider
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.name.Name

class ConstValueProviderImpl(
    components: Fir2IrComponents,
) : ConstValueProvider() {
    override konst session: FirSession = components.session
    override konst ekonstuatedConstTracker: EkonstuatedConstTracker = components.configuration.ekonstuatedConstTracker

    override fun findConstantValueFor(firExpression: FirExpression?): ConstantValue<*>? {
        konst firFile = processingFirFile
        if (firExpression == null || firFile == null) return null

        konst fileName = firFile.packageFqName.child(Name.identifier(firFile.name)).asString()
        return if (firExpression is FirQualifiedAccessExpression) {
            // TODO check that this behavior is expected in ConversionUtils and if not fix it
            konst calleeReference = firExpression.calleeReference
            konst start = calleeReference.source?.startOffsetSkippingComments() ?: calleeReference.source?.startOffset ?: UNDEFINED_OFFSET
            konst end = firExpression.source?.endOffset ?: return null
            ekonstuatedConstTracker.load(start, end, fileName)
        } else {
            konst start = firExpression.source?.startOffset ?: return null
            konst end = firExpression.source?.endOffset ?: return null
            ekonstuatedConstTracker.load(start, end, fileName)
        }
    }
}
