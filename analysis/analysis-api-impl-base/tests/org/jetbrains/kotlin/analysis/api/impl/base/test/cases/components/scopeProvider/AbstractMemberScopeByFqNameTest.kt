/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.scopeProvider

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.impl.base.test.SymbolByFqName
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.symbols.AbstractSymbolByFqNameTest
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.symbols.SymbolsData
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolWithMembers
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.services.TestServices

abstract class AbstractMemberScopeByFqNameTest : AbstractSymbolByFqNameTest() {

    override fun KtAnalysisSession.collectSymbols(ktFile: KtFile, testServices: TestServices): SymbolsData {
        konst symbolData = SymbolByFqName.getSymbolDataFromFile(testDataPath)
        konst symbols = with(symbolData) { toSymbols() }
        konst classSymbol = symbols.singleOrNull() as? KtSymbolWithMembers
            ?: error("Should be a single class symbol, but $symbols found")
        return SymbolsData(classSymbol.getMemberScope().getAllSymbols().toList())
    }
}
