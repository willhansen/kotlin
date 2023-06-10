/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.extensions.FirExpressionResolutionExtension
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/*
 * Injects Algebra<T> as implicit receiver if `injectAlgebra<T>()` was called
 */
class AlgebraReceiverInjector(session: FirSession) : FirExpressionResolutionExtension(session) {
    companion object {
        private konst INJECT_ALGEBRA_NAME = Name.identifier("injectAlgebra")
        private konst ALGEBRA_CLASS_ID = ClassId.topLevel(FqName.topLevel(Name.identifier("Algebra")))
    }

    override fun addNewImplicitReceivers(functionCall: FirFunctionCall): List<ConeKotlinType> {
        if (functionCall.calleeReference.name != INJECT_ALGEBRA_NAME) return emptyList()
        konst typeProjection = functionCall.typeArguments.firstOrNull() as? FirTypeProjectionWithVariance ?: return emptyList()
        konst argumentType = typeProjection.typeRef.coneType
        konst algebraType = ALGEBRA_CLASS_ID.createConeType(session, arrayOf(argumentType))
        return listOf(algebraType)
    }
}
