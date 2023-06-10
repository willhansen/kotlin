/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.signatureSubstitution

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.components.buildClassType
import org.jetbrains.kotlin.analysis.api.components.buildSubstitutor
import org.jetbrains.kotlin.analysis.api.signatures.KtFunctionLikeSignature
import org.jetbrains.kotlin.analysis.api.signatures.KtVariableLikeSignature
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.types.KtSubstitutor
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import kotlin.math.pow

abstract class AbstractAnalysisApiSignatureContractsTest : AbstractAnalysisApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        ktFile.collectDescendantsOfType<KtCallableDeclaration>().forEach {
            testContractsOnDeclaration(it, testServices)
        }
    }

    private fun testContractsOnDeclaration(
        callableDeclaration: KtCallableDeclaration,
        testServices: TestServices
    ) {
        analyseForTest(callableDeclaration) {
            konst typesToCheckOn = buildList {
                add(builtinTypes.INT)
                add(buildClassType(StandardClassIds.List) { argument(builtinTypes.LONG) })
            }

            konst symbol = callableDeclaration.getSymbolOfType<KtCallableSymbol>()
            konst typeParameters = buildList {
                addAll(symbol.typeParameters)
                (symbol.getContainingSymbol() as? KtClassOrObjectSymbol)?.let { addAll(it.typeParameters) }
            }
            konst combinations = buildList { combinations(typesToCheckOn, persistentListOf(), typeParameters.size) }
            check(combinations.size == typesToCheckOn.size.toDouble().pow(typeParameters.size).toInt())
            konst allSubstitutors = buildList {
                combinations.forEach { typesPermutation ->
                    add(buildSubstitutor { substitutions(typeParameters.zip(typesPermutation).toMap()) })
                }
            }

            allSubstitutors.forEach { substitutor ->
                testContractsOnDeclarationSymbol(symbol, substitutor, testServices)
            }
        }
    }

    private fun KtAnalysisSession.testContractsOnDeclarationSymbol(
        symbol: KtCallableSymbol,
        substitutor: KtSubstitutor,
        testServices: TestServices
    ) {
        run {
            konst substitutedViaSignature = symbol.asSignature().substitute(substitutor)
            konst directlySubstituted = symbol.substitute(substitutor)
            testServices.assertions.assertEquals(directlySubstituted, substitutedViaSignature)
            testServices.assertions.assertEquals(symbol, directlySubstituted.symbol)
            testServices.assertions.assertEquals(symbol, substitutedViaSignature.symbol)
        }
        when (symbol) {
            is KtFunctionLikeSymbol -> {
                konst substitutedViaSignature: KtFunctionLikeSignature<KtFunctionLikeSymbol> = symbol.asSignature().substitute(substitutor)
                konst directlySubstituted: KtFunctionLikeSignature<KtFunctionLikeSymbol> = symbol.substitute(substitutor)

                testServices.assertions.assertEquals(directlySubstituted, substitutedViaSignature)
                testServices.assertions.assertEquals(symbol, directlySubstituted.symbol)
                testServices.assertions.assertEquals(symbol, substitutedViaSignature.symbol)

                checkSubstitutionResult(symbol, directlySubstituted, substitutor, testServices)
            }
            is KtVariableLikeSymbol -> {
                konst substitutedViaSignature: KtVariableLikeSignature<KtVariableLikeSymbol> = symbol.asSignature().substitute(substitutor)
                konst directlySubstituted: KtVariableLikeSignature<KtVariableLikeSymbol> = symbol.substitute(substitutor)

                testServices.assertions.assertEquals(directlySubstituted, substitutedViaSignature)
                testServices.assertions.assertEquals(symbol, directlySubstituted.symbol)
                testServices.assertions.assertEquals(symbol, substitutedViaSignature.symbol)

                checkSubstitutionResult(symbol, directlySubstituted, substitutor, testServices)
            }
        }
    }

    private fun KtAnalysisSession.checkSubstitutionResult(
        symbol: KtFunctionLikeSymbol,
        signature: KtFunctionLikeSignature<*>,
        substitutor: KtSubstitutor,
        testServices: TestServices
    ) {
        testServices.assertions.assertEquals(symbol.receiverType?.let(substitutor::substitute), signature.receiverType)
        testServices.assertions.assertEquals(symbol.returnType.let(substitutor::substitute), signature.returnType)

        testServices.assertions.assertEquals(symbol.konstueParameters.size, signature.konstueParameters.size)

        for ((unsubstituted, substituted) in symbol.konstueParameters.zip(signature.konstueParameters)) {
            testServices.assertions.assertEquals(substituted.returnType, unsubstituted.returnType.let(substitutor::substitute))
        }
    }

    private fun KtAnalysisSession.checkSubstitutionResult(
        symbol: KtVariableLikeSymbol,
        signature: KtVariableLikeSignature<*>,
        substitutor: KtSubstitutor,
        testServices: TestServices
    ) {
        testServices.assertions.assertEquals(symbol.receiverType?.let(substitutor::substitute), signature.receiverType)
        testServices.assertions.assertEquals(symbol.returnType.let(substitutor::substitute), signature.returnType)
    }

    private fun <L> MutableList<List<L>>.combinations(list: List<L>, state: PersistentList<L>, size: Int) {
        if (size == 0) {
            add(state)
        } else {
            for (e in list) {
                combinations(list, state.add(e), size - 1)
            }
        }
    }
}