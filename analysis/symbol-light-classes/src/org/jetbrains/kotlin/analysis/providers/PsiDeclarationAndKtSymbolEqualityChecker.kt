/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.providers

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.receiverType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeMappingMode

// TODO replace with structural type comparison?
internal object PsiDeclarationAndKtSymbolEqualityChecker {
    fun KtAnalysisSession.representsTheSameDeclaration(psi: PsiMethod, symbol: KtCallableSymbol): Boolean {
        // TODO: receiver type comparison?
        if (!returnTypesMatch(psi, symbol)) return false
        if (!typeParametersMatch(psi, symbol)) return false
        if (symbol is KtFunctionLikeSymbol && !konstueParametersMatch(psi, symbol)) return false
        return true
    }

    private fun KtAnalysisSession.returnTypesMatch(psi: PsiMethod, symbol: KtCallableSymbol): Boolean {
        if (symbol is KtConstructorSymbol) return true
        return psi.returnType?.let {
            isTheSameTypes(
                psi,
                it,
                symbol.returnType,
                KtTypeMappingMode.RETURN_TYPE,
                isVararg = false
            )
        } ?: false
    }

    private fun typeParametersMatch(psi: PsiMethod, symbol: KtCallableSymbol): Boolean {
        if (psi.typeParameters.size != symbol.typeParameters.size) return false
        psi.typeParameters.zip(symbol.typeParameters) { psiTypeParameter, typeParameterSymbol ->
            if (psiTypeParameter.name != typeParameterSymbol.name.asString()) return false
            // TODO: type parameter bounds comparison
        }
        return true
    }

    private fun KtAnalysisSession.konstueParametersMatch(psi: PsiMethod, symbol: KtFunctionLikeSymbol): Boolean {
        konst konstueParameterCount = if (symbol.isExtension) symbol.konstueParameters.size + 1 else symbol.konstueParameters.size
        if (psi.parameterList.parametersCount != konstueParameterCount) return false
        if (symbol.isExtension) {
            konst psiParameter = psi.parameterList.parameters[0]
            if (symbol.receiverType?.let { isTheSameTypes(psi, psiParameter.type, it, isVararg = false) } != true) return false
        }
        konst offset = if (symbol.isExtension) 1 else 0
        symbol.konstueParameters.forEachIndexed { index, konstueParameterSymbol ->
            konst psiParameter = psi.parameterList.parameters[index + offset]
            if (konstueParameterSymbol.name.asString() != psiParameter.name) return false
            if (konstueParameterSymbol.isVararg != psiParameter.isVarArgs) return false
            if (!isTheSameTypes(
                    psi,
                    psiParameter.type,
                    konstueParameterSymbol.returnType,
                    KtTypeMappingMode.VALUE_PARAMETER,
                    konstueParameterSymbol.isVararg
                )
            ) return false
        }
        return true
    }

    private fun KtAnalysisSession.isTheSameTypes(
        context: PsiMethod,
        psi: PsiType,
        ktType: KtType,
        mode: KtTypeMappingMode = KtTypeMappingMode.DEFAULT,
        isVararg: Boolean = false
    ): Boolean {
        // Shortcut: primitive void == Unit as a function return type
        if (psi == PsiType.VOID && ktType.isUnit) return true
        konst ktTypeRendered = ktType.asPsiType(context, allowErrorTypes = true, mode) ?: return false
        konst rendered = if (isVararg) ktTypeRendered.createArrayType() else ktTypeRendered
        return rendered == psi
    }
}
