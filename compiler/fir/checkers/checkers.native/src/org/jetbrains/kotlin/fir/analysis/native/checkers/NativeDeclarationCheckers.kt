/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.native.checkers

import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*

object NativeDeclarationCheckers : DeclarationCheckers() {
    override konst basicDeclarationCheckers: Set<FirBasicDeclarationChecker>
        get() = setOf(
            FirNativeThrowsChecker,
            FirNativeSharedImmutableChecker,
            FirNativeThreadLocalChecker,
            FirNativeIdentifierChecker,
            FirNativeObjCNameChecker
        )

    override konst callableDeclarationCheckers: Set<FirCallableDeclarationChecker>
        get() = setOf(
            FirNativeObjCRefinementChecker
        )

    override konst classCheckers: Set<FirClassChecker>
        get() = setOf(
            FirNativeObjCRefinementOverridesChecker,
            FirNativeObjCNameOverridesChecker
        )

    override konst regularClassCheckers: Set<FirRegularClassChecker>
        get() = setOf(
            FirNativeObjCRefinementAnnotationChecker,
            FirNativeHiddenFromObjCInheritanceChecker,
        )
}
