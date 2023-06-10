/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.utils

import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

konst FirTypeAlias.expandedConeType: ConeClassLikeType? get() = expandedTypeRef.coneTypeSafe()

konst FirClassLikeDeclaration.classId: ClassId
    get() = symbol.classId

konst FirClass.superConeTypes: List<ConeClassLikeType> get() = superTypeRefs.mapNotNull { it.coneTypeSafe() }

konst FirClass.anonymousInitializers: List<FirAnonymousInitializer>
    get() = declarations.filterIsInstance<FirAnonymousInitializer>()

konst FirClass.delegateFields: List<FirField>
    get() = declarations.filterIsInstance<FirField>().filter { it.isSynthetic }

inline konst FirDeclaration.isJava: Boolean
    get() = origin is FirDeclarationOrigin.Java
inline konst FirDeclaration.isJavaSource: Boolean
    get() = origin == FirDeclarationOrigin.Java.Source
inline konst FirDeclaration.isFromLibrary: Boolean
    get() = origin == FirDeclarationOrigin.Library || origin == FirDeclarationOrigin.Java.Library
inline konst FirDeclaration.isPrecompiled: Boolean
    get() = origin == FirDeclarationOrigin.Precompiled
inline konst FirDeclaration.isSynthetic: Boolean
    get() = origin == FirDeclarationOrigin.Synthetic

// NB: This function checks transitive localness. That is,
// if a declaration `isNonLocal`, then its parent also `isNonLocal`.
konst FirDeclaration.isNonLocal
    get() = when (this) {
        is FirFile -> true
        is FirCallableDeclaration -> !symbol.callableId.isLocal
        is FirClassLikeDeclaration -> !symbol.classId.isLocal
        else -> false
    }

konst FirCallableDeclaration.isExtension get() = receiverParameter != null

konst FirMemberDeclaration.nameOrSpecialName: Name
    get() = when (this) {
        is FirCallableDeclaration ->
            this.symbol.callableId.callableName
        is FirClass ->
            this.classId.shortClassName
        is FirTypeAlias ->
            this.name
    }
