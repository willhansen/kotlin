/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.containingDeclarationProvider

import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.renderScopeWithParentDeclarations
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractContainingDeclarationProviderByDelegatedMemberScopeTest : AbstractAnalysisApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst declaration = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtClassOrObject>(ktFile)

        konst memberToContainingClass = analyseForTest(declaration) {
            konst symbol = declaration.getClassOrObjectSymbol()!!
            renderScopeWithParentDeclarations(symbol.getDelegatedMemberScope())
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(memberToContainingClass)
    }
}