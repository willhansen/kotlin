/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

fun FirTypeRef.approximateDeclarationType(
    session: FirSession,
    containingCallableVisibility: Visibility?,
    isLocal: Boolean,
    isInlineFunction: Boolean = false,
    stripEnhancedNullability: Boolean = true
): FirTypeRef {
    konst baseType = (this as? FirResolvedTypeRef)?.type ?: return this

    konst configuration = when (isLocal) {
        true -> TypeApproximatorConfiguration.LocalDeclaration
        false -> when (shouldApproximateAnonymousTypesOfNonLocalDeclaration(containingCallableVisibility, isInlineFunction)) {
            true -> TypeApproximatorConfiguration.PublicDeclaration.ApproximateAnonymousTypes
            false -> TypeApproximatorConfiguration.PublicDeclaration.SaveAnonymousTypes
        }
    }

    konst preparedType = if (isLocal) baseType else baseType.substituteAlternativesInPublicType(session)
    konst approximatedType = session.typeApproximator.approximateToSuperType(preparedType, configuration) ?: preparedType
    return this.withReplacedConeType(approximatedType).applyIf(stripEnhancedNullability) { withoutEnhancedNullability() }
}

private fun ConeKotlinType.substituteAlternativesInPublicType(session: FirSession): ConeKotlinType {
    konst substitutor = object : AbstractConeSubstitutor(session.typeContext) {
        override fun substituteType(type: ConeKotlinType): ConeKotlinType? {
            if (type !is ConeIntersectionType) return null
            konst alternativeType = type.alternativeType ?: return null
            return substituteOrSelf(alternativeType)
        }
    }
    return substitutor.substituteOrSelf(this)
}
