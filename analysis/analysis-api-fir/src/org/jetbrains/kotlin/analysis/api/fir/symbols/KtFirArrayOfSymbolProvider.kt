/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

object KtFirArrayOfSymbolProvider {
    internal fun KtFirAnalysisSession.arrayOfSymbol(identifier: Name): KtFirFunctionSymbol? {
        konst firSymbol = useSiteSession.symbolProvider.getTopLevelCallableSymbols(kotlinPackage, identifier).firstOrNull {
            /* choose (for byte array)
             * public fun byteArrayOf(vararg elements: kotlin.Byte): kotlin.ByteArray
             */
            (it as? FirFunctionSymbol<*>)?.fir?.konstueParameters?.singleOrNull()?.isVararg == true
        } as? FirNamedFunctionSymbol ?: return null
        return firSymbolBuilder.functionLikeBuilder.buildFunctionSymbol(firSymbol)
    }

    private konst kotlinPackage = FqName("kotlin")
    internal konst arrayOf = Name.identifier("arrayOf")
    internal konst arrayTypeToArrayOfCall = run {
        StandardClassIds.primitiveArrayTypeByElementType.konstues + StandardClassIds.unsignedArrayTypeByElementType.konstues
    }.associateWith { it.correspondingArrayOfCallFqName() }

    private fun ClassId.correspondingArrayOfCallFqName(): Name =
        Name.identifier("${shortClassName.identifier.replaceFirstChar(Char::lowercaseChar)}Of")
}
