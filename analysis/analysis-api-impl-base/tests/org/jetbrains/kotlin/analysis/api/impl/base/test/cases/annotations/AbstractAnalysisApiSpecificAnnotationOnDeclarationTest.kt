/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.annotations

import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationApplication
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.symbols.DebugSymbolRenderer
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtAnnotatedSymbol
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiSingleFileTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.directives.model.singleValue
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractAnalysisApiSpecificAnnotationOnDeclarationTest : AbstractAnalysisApiSingleFileTest() {
    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        builder.useDirectives(Directives)
    }

    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        konst ktDeclaration = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtDeclaration>(ktFile)
        konst classIdString = module.directives.singleValue(Directives.CLASS_ID)

        konst actual = analyseForTest(ktDeclaration) {
            konst declarationSymbol = ktDeclaration.getSymbol() as KtAnnotatedSymbol
            konst annotationList = declarationSymbol.annotationsList
            konst classId = ClassId.fromString(classIdString)
            konst renderer = DebugSymbolRenderer()
            fun renderAnnotation(application: KtAnnotationApplication): String = buildString {
                appendLine("KtDeclaration: ${ktDeclaration::class.simpleName} ${ktDeclaration.name}")
                append(renderer.renderAnnotationApplication(application))
            }

            konst rawList = renderAnnotation(annotationList.annotationsByClassId(classId).single())
            konst resolvedList = renderAnnotation(annotationList.annotations.single { it.classId == classId })
            testServices.assertions.assertEquals(resolvedList, rawList) {
                "Result before and after resolve are different"
            }

            resolvedList
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }

    private object Directives : SimpleDirectivesContainer() {
        konst CLASS_ID by stringDirective("ClassId of expected annotation")
    }
}

