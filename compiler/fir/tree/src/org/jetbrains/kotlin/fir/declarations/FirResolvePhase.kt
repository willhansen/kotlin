/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

enum class FirResolvePhase(konst noProcessor: Boolean = false) {
    RAW_FIR(noProcessor = true),
    IMPORTS,
    COMPILER_REQUIRED_ANNOTATIONS,
    COMPANION_GENERATION,
    SUPER_TYPES,
    SEALED_CLASS_INHERITORS,
    TYPES,
    STATUS,
    EXPECT_ACTUAL_MATCHING,
    ARGUMENTS_OF_ANNOTATIONS,
    CONTRACTS,
    IMPLICIT_TYPES_BODY_RESOLVE,
    ANNOTATIONS_ARGUMENTS_MAPPING,
    BODY_RESOLVE;

    konst requiredToLaunch: FirResolvePhase
        get() = when (this) {
            RAW_FIR -> RAW_FIR
            IMPORTS -> RAW_FIR
            STATUS -> TYPES
            IMPLICIT_TYPES_BODY_RESOLVE, BODY_RESOLVE -> STATUS
            else -> konstues()[ordinal - 1]
        }

    konst next: FirResolvePhase get() = konstues()[ordinal + 1]
    konst previous: FirResolvePhase get() = konstues()[ordinal - 1]

    companion object {
        // Short-cut
        konst DECLARATIONS = STATUS
        konst ANALYZED_DEPENDENCIES = BODY_RESOLVE
    }
}

konst FirResolvePhase.isBodyResolve: Boolean
    get() = when (this) {
        FirResolvePhase.BODY_RESOLVE,
        FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE -> true
        else -> false
    }
