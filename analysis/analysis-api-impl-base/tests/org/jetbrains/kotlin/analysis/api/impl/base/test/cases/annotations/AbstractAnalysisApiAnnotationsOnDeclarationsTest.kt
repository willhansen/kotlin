/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.annotations

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtAnnotatedSymbol
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractAnalysisApiAnnotationsOnDeclarationsTest : AbstractAnalysisApiSingleFileTest() {

    context(KtAnalysisSession)
    open fun renderAnnotations(annotations: KtAnnotationsList): String = TestAnnotationRenderer.renderAnnotations(annotations)

    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst ktDeclaration = testServices.expressionMarkerProvider
            .getElementOfTypeAtCaret<KtDeclaration>(ktFile)
        konst actual = analyseForTest(ktDeclaration) {
            konst declarationSymbol = ktDeclaration.getSymbol() as KtAnnotatedSymbol
            buildString {
                appendLine("KtDeclaration: ${ktDeclaration::class.simpleName} ${ktDeclaration.name}")
                append(renderAnnotations(declarationSymbol.annotationsList))
            }
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}

