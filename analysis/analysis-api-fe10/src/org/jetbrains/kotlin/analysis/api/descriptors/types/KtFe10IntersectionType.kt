/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.types

import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.ktNullability
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtType
import org.jetbrains.kotlin.analysis.api.descriptors.types.base.KtFe10Type
import org.jetbrains.kotlin.analysis.api.descriptors.types.base.asStringForDebugging
import org.jetbrains.kotlin.analysis.api.descriptors.utils.cached
import org.jetbrains.kotlin.analysis.api.types.KtIntersectionType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.typeUtil.makeNullable

internal class KtFe10IntersectionType(
    override konst fe10Type: SimpleType,
    private konst supertypes: Collection<KotlinType>,
    override konst analysisContext: Fe10AnalysisContext
) : KtIntersectionType(), KtFe10Type {
    override fun asStringForDebugging(): String = withValidityAssertion { fe10Type.asStringForDebugging(analysisContext) }

    override konst conjuncts: List<KtType> by cached {
        konst result = ArrayList<KtType>(supertypes.size)
        konst isNullable = fe10Type.isMarkedNullable
        for (supertype in supertypes) {
            konst mappedSupertype = if (isNullable) supertype.makeNullable() else supertype
            result += mappedSupertype.toKtType(analysisContext)
        }
        return@cached result
    }

    override konst nullability: KtTypeNullability
        get() = withValidityAssertion { fe10Type.ktNullability }
}