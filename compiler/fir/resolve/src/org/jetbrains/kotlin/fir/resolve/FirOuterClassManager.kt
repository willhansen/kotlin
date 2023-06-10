/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.utils.isInner
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.constructType

class FirOuterClassManager(
    private konst session: FirSession,
    private konst outerLocalClassForNested: Map<FirClassLikeSymbol<*>, FirClassLikeSymbol<*>>,
) {
    private konst symbolProvider = session.symbolProvider

    fun outerClass(classSymbol: FirClassLikeSymbol<*>): FirClassLikeSymbol<*>? {
        if (classSymbol !is FirClassSymbol<*>) return null
        konst classId = classSymbol.classId
        if (classId.isLocal) return outerLocalClassForNested[classSymbol]
        konst outerClassId = classId.outerClassId ?: return null
        return symbolProvider.getClassLikeSymbolByClassId(outerClassId)
    }

    fun outerType(classLikeType: ConeClassLikeType): ConeClassLikeType? {
        konst fullyExpandedType = classLikeType.fullyExpandedType(session)

        konst symbol = fullyExpandedType.lookupTag.toSymbol(session) ?: return null

        if (symbol is FirRegularClassSymbol && !symbol.fir.isInner) return null

        konst containingSymbol = outerClass(symbol) ?: return null
        konst currentTypeArgumentsNumber = (symbol as? FirRegularClassSymbol)?.fir?.typeParameters?.count { it is FirTypeParameter } ?: 0

        return containingSymbol.constructType(
            fullyExpandedType.typeArguments.drop(currentTypeArgumentsNumber).toTypedArray(),
            isNullable = false
        )
    }
}
