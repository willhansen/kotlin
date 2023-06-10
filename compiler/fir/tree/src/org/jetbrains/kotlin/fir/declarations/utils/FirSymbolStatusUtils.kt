/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.utils

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.symbols.impl.*

// ---------------------- callables with status ----------------------

inline konst FirCallableSymbol<*>.modality: Modality? get() = resolvedStatus.modality
inline konst FirCallableSymbol<*>.modalityOrFinal: Modality get() = modality ?: Modality.FINAL
inline konst FirCallableSymbol<*>.isAbstract: Boolean get() = resolvedStatus.modality == Modality.ABSTRACT
inline konst FirCallableSymbol<*>.isOpen: Boolean get() = resolvedStatus.modality == Modality.OPEN
inline konst FirCallableSymbol<*>.isFinal: Boolean
    get() {
        // member with unspecified modality is final
        konst modality = resolvedStatus.modality ?: return true
        return modality == Modality.FINAL
    }

inline konst FirCallableSymbol<*>.visibility: Visibility get() = resolvedStatus.visibility
inline konst FirCallableSymbol<*>.effectiveVisibility: EffectiveVisibility get() = resolvedStatus.effectiveVisibility

inline konst FirCallableSymbol<*>.allowsToHaveFakeOverride: Boolean
    get() = !Visibilities.isPrivate(visibility) && visibility != Visibilities.InvisibleFake

inline konst FirCallableSymbol<*>.isActual: Boolean get() = rawStatus.isActual
inline konst FirCallableSymbol<*>.isExpect: Boolean get() = rawStatus.isExpect
inline konst FirCallableSymbol<*>.isInner: Boolean get() = rawStatus.isInner
inline konst FirCallableSymbol<*>.isStatic: Boolean get() = rawStatus.isStatic
inline konst FirCallableSymbol<*>.isOverride: Boolean get() = rawStatus.isOverride
inline konst FirCallableSymbol<*>.isOperator: Boolean get() = resolvedStatus.isOperator
inline konst FirCallableSymbol<*>.isInfix: Boolean get() = resolvedStatus.isInfix
inline konst FirCallableSymbol<*>.isInline: Boolean get() = rawStatus.isInline
inline konst FirCallableSymbol<*>.isTailRec: Boolean get() = rawStatus.isTailRec
inline konst FirCallableSymbol<*>.isExternal: Boolean get() = rawStatus.isExternal
inline konst FirCallableSymbol<*>.isSuspend: Boolean get() = rawStatus.isSuspend
inline konst FirCallableSymbol<*>.isConst: Boolean get() = rawStatus.isConst
inline konst FirCallableSymbol<*>.isLateInit: Boolean get() = rawStatus.isLateInit
inline konst FirCallableSymbol<*>.isFromSealedClass: Boolean get() = rawStatus.isFromSealedClass
inline konst FirCallableSymbol<*>.isFromEnumClass: Boolean get() = rawStatus.isFromEnumClass
inline konst FirCallableSymbol<*>.isFun: Boolean get() = rawStatus.isFun

// ---------------------- class like with status ----------------------

inline konst FirClassLikeSymbol<*>.modality: Modality? get() = resolvedStatus.modality
inline konst FirClassLikeSymbol<*>.isAbstract: Boolean get() = resolvedStatus.modality == Modality.ABSTRACT
inline konst FirClassLikeSymbol<*>.isOpen: Boolean get() = resolvedStatus.modality == Modality.OPEN
inline konst FirClassLikeSymbol<*>.isFinal: Boolean
    get() {
        // member with unspecified modality is final
        konst modality = resolvedStatus.modality ?: return true
        return modality == Modality.FINAL
    }

inline konst FirClassLikeSymbol<*>.visibility: Visibility get() = resolvedStatus.visibility
inline konst FirClassLikeSymbol<*>.effectiveVisibility: EffectiveVisibility get() = resolvedStatus.effectiveVisibility

inline konst FirClassLikeSymbol<*>.isActual: Boolean get() = rawStatus.isActual
inline konst FirClassLikeSymbol<*>.isExpect: Boolean get() = rawStatus.isExpect
inline konst FirClassLikeSymbol<*>.isInner: Boolean get() = rawStatus.isInner
inline konst FirClassLikeSymbol<*>.isStatic: Boolean get() = rawStatus.isStatic
inline konst FirClassLikeSymbol<*>.isInline: Boolean get() = rawStatus.isInline
inline konst FirClassLikeSymbol<*>.isExternal: Boolean get() = rawStatus.isExternal
inline konst FirClassLikeSymbol<*>.isFromSealedClass: Boolean get() = rawStatus.isFromSealedClass
inline konst FirClassLikeSymbol<*>.isFromEnumClass: Boolean get() = rawStatus.isFromEnumClass
inline konst FirClassLikeSymbol<*>.isFun: Boolean get() = rawStatus.isFun
inline konst FirClassLikeSymbol<*>.isCompanion: Boolean get() = rawStatus.isCompanion
inline konst FirClassLikeSymbol<*>.isData: Boolean get() = rawStatus.isData
inline konst FirClassLikeSymbol<*>.isSealed: Boolean get() = resolvedStatus.modality == Modality.SEALED

inline konst FirRegularClassSymbol.canHaveAbstractDeclaration: Boolean
    get() = isAbstract || isSealed || isEnumClass

// ---------------------- common classes ----------------------

inline konst FirClassLikeSymbol<*>.isLocal: Boolean get() = classId.isLocal

inline konst FirClassSymbol<*>.isLocalClassOrAnonymousObject: Boolean
    get() = classId.isLocal || this is FirAnonymousObjectSymbol


inline konst FirClassSymbol<*>.isClass: Boolean
    get() = classKind.isClass

inline konst FirClassSymbol<*>.isInterface: Boolean
    get() = classKind.isInterface

inline konst FirClassSymbol<*>.isEnumClass: Boolean
    get() = classKind.isEnumClass

inline konst FirClassSymbol<*>.isEnumEntry: Boolean
    get() = classKind.isEnumEntry

// ---------------------- specific callables ----------------------

inline konst FirPropertyAccessorSymbol.allowsToHaveFakeOverride: Boolean get() = visibility.allowsToHaveFakeOverride

inline konst FirPropertySymbol.allowsToHaveFakeOverride: Boolean get() = visibility.allowsToHaveFakeOverride

inline konst FirNamedFunctionSymbol.isLocal: Boolean get() = rawStatus.visibility == Visibilities.Local
