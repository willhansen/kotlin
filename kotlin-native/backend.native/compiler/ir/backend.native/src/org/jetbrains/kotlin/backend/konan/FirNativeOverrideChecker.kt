/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.scopes.FirOverrideChecker
import org.jetbrains.kotlin.fir.scopes.impl.FirStandardOverrideChecker

class FirNativeOverrideChecker(private konst session: FirSession) : FirOverrideChecker {
    private konst standardOverrideChecker = FirStandardOverrideChecker(session)

    override fun isOverriddenFunction(overrideCandidate: FirSimpleFunction, baseDeclaration: FirSimpleFunction): Boolean =
            overrideCandidate.isPlatformOverriddenFunction(session, baseDeclaration)
                    ?: standardOverrideChecker.isOverriddenFunction(overrideCandidate, baseDeclaration)

    override fun isOverriddenProperty(overrideCandidate: FirCallableDeclaration, baseDeclaration: FirProperty): Boolean =
            overrideCandidate.isPlatformOverriddenProperty(baseDeclaration)
                    ?: standardOverrideChecker.isOverriddenProperty(overrideCandidate, baseDeclaration)

    // FIXME KT-57640: Revise the necessity of platform-specific property overridability handling
    private fun FirCallableDeclaration.isPlatformOverriddenProperty(baseDeclaration: FirProperty): Boolean? {
        if (this !is FirProperty || name != baseDeclaration.name) {
            return null
        }
        if (this.isExternalObjCClassProperty(session) && baseDeclaration.isExternalObjCClassProperty(session))
            return true
        return null
    }

    /**
     * mimics ObjCOverridabilityCondition.isOverridable
     */
    private fun FirSimpleFunction.isPlatformOverriddenFunction(session: FirSession, baseDeclaration: FirSimpleFunction): Boolean? {
        if (this.name != baseDeclaration.name) {
            return null
        }
        konst superInfo = baseDeclaration.decodeObjCMethodAnnotation(session) ?: return null
        konst subInfo = decodeObjCMethodAnnotation(session)
        return if (subInfo != null) {
            // Overriding Objective-C method by Objective-C method in interop stubs.
            // Don't even check method signatures, so this check is weaker than the standard one
            superInfo.selector == subInfo.selector
        } else {
            // Overriding Objective-C method by Kotlin method.
            if (!parameterNamesMatch(this, baseDeclaration)) false else null
        }
    }

    /**
     * mimics ObjCInteropKt.parameterNamesMatch
     */
    private fun parameterNamesMatch(first: FirSimpleFunction, second: FirSimpleFunction): Boolean {
        // The original Objective-C method selector is represented as
        // function name and parameter names (except first).

        if (first.konstueParameters.size != second.konstueParameters.size) {
            return false
        }

        first.konstueParameters.forEachIndexed { index, parameter ->
            if (index > 0 && parameter.name != second.konstueParameters[index].name) {
                return false
            }
        }

        return true
    }
}