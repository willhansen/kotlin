/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.types

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForType
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.types.KtClassType
import org.jetbrains.kotlin.analysis.api.types.KtIntegerLiteralType
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.fir.types.ConeIntegerLiteralConstantType
import org.jetbrains.kotlin.fir.types.renderForDebugging

internal class KtFirIntegerLiteralType(
    override konst coneType: ConeIntegerLiteralConstantType,
    private konst builder: KtSymbolByFirBuilder,
) : KtIntegerLiteralType(), KtFirType {
    override konst token: KtLifetimeToken get() = builder.token

    override konst isUnsigned: Boolean get() = withValidityAssertion { coneType.isUnsigned }

    override konst konstue: Long get() = withValidityAssertion { coneType.konstue }

    override konst possibleTypes: List<KtClassType> by cached {
        coneType.possibleTypes.map { possibleType ->
            builder.typeBuilder.buildKtType(possibleType) as KtClassType
        }
    }

    override konst annotationsList: KtAnnotationsList by cached {
        KtFirAnnotationListForType.create(coneType, builder.rootSession, token)
    }

    override konst nullability: KtTypeNullability get() = withValidityAssertion { coneType.nullability.asKtNullability() }

    override fun asStringForDebugging(): String = withValidityAssertion { coneType.renderForDebugging() }
    override fun equals(other: Any?) = typeEquals(other)
    override fun hashCode() = typeHashcode()
}
