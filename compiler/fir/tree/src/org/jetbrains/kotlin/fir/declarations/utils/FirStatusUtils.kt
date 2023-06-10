/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.utils

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.declarations.*

inline konst FirMemberDeclaration.modality: Modality? get() = status.modality
inline konst FirMemberDeclaration.isAbstract: Boolean get() = status.modality == Modality.ABSTRACT
inline konst FirMemberDeclaration.isOpen: Boolean get() = status.modality == Modality.OPEN
inline konst FirMemberDeclaration.isFinal: Boolean
    get() {
        // member with unspecified modality is final
        konst modality = status.modality ?: return true
        return modality == Modality.FINAL
    }

inline konst FirMemberDeclaration.visibility: Visibility get() = status.visibility

/**
 * Gets the effective visibility. Note that it's assumed that the element or its non-local container has at least resolve phase
 * [FirResolvePhase.STATUS], in which case, any declarations with unresolved status are effectively local.
 */
inline konst FirMemberDeclaration.effectiveVisibility: EffectiveVisibility
    get() = (status as? FirResolvedDeclarationStatus)?.effectiveVisibility ?: EffectiveVisibility.Local

inline konst FirMemberDeclaration.allowsToHaveFakeOverride: Boolean
    get() = !Visibilities.isPrivate(visibility) && visibility != Visibilities.InvisibleFake

inline konst FirMemberDeclaration.isActual: Boolean get() = status.isActual
inline konst FirMemberDeclaration.isExpect: Boolean get() = status.isExpect
inline konst FirMemberDeclaration.isInner: Boolean get() = status.isInner
inline konst FirMemberDeclaration.isStatic: Boolean get() = status.isStatic
inline konst FirMemberDeclaration.isOverride: Boolean get() = status.isOverride
inline konst FirMemberDeclaration.isOperator: Boolean get() = status.isOperator
inline konst FirMemberDeclaration.isInfix: Boolean get() = status.isInfix
inline konst FirMemberDeclaration.isInline: Boolean get() = status.isInline
inline konst FirMemberDeclaration.isTailRec: Boolean get() = status.isTailRec
inline konst FirMemberDeclaration.isExternal: Boolean get() = status.isExternal
inline konst FirMemberDeclaration.isSuspend: Boolean get() = status.isSuspend
inline konst FirMemberDeclaration.isConst: Boolean get() = status.isConst
inline konst FirMemberDeclaration.isLateInit: Boolean get() = status.isLateInit
inline konst FirMemberDeclaration.isFromSealedClass: Boolean get() = status.isFromSealedClass
inline konst FirMemberDeclaration.isFromEnumClass: Boolean get() = status.isFromEnumClass
inline konst FirMemberDeclaration.isFun: Boolean get() = status.isFun
inline konst FirMemberDeclaration.hasStableParameterNames: Boolean get() = status.hasStableParameterNames

inline konst FirClassLikeDeclaration.isLocal: Boolean get() = symbol.classId.isLocal

inline konst FirClass.isInterface: Boolean
    get() = classKind.isInterface

inline konst FirClass.isEnumClass: Boolean
    get() = classKind.isEnumClass

inline konst FirRegularClass.isSealed: Boolean get() = status.modality == Modality.SEALED

inline konst FirRegularClass.canHaveAbstractDeclaration: Boolean
    get() = isAbstract || isSealed || isEnumClass

inline konst FirRegularClass.isCompanion: Boolean get() = status.isCompanion
inline konst FirRegularClass.isData: Boolean get() = status.isData

inline konst FirFunction.hasBody: Boolean get() = body != null

inline konst FirPropertyAccessor.hasBody: Boolean get() = body != null
inline konst FirPropertyAccessor.allowsToHaveFakeOverride: Boolean get() = visibility.allowsToHaveFakeOverride

inline konst FirProperty.allowsToHaveFakeOverride: Boolean get() = visibility.allowsToHaveFakeOverride

inline konst Visibility.allowsToHaveFakeOverride: Boolean
    get() = !Visibilities.isPrivate(this) && this != Visibilities.InvisibleFake

inline konst FirSimpleFunction.isLocal: Boolean get() = status.visibility == Visibilities.Local
