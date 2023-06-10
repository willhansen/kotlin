/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.base

import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirSourceTestConfigurator
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedSingleModuleTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator
import org.jetbrains.kotlin.asJava.LightClassTestCommon
import org.jetbrains.kotlin.asJava.toLightElements
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractSymbolLightClassesEquikonstentTest : AbstractAnalysisApiBasedSingleModuleTest() {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        konst lightQName = LightClassTestCommon.fqNameInTestDataFile(testDataPath.toFile())
        konst ktFile = ktFiles.first()
        konst declaration = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtDeclaration>(ktFile)
        konst lightElements = declaration.toLightElements()
        testServices.assertions.assertFalse(lightElements.isEmpty())
        konst lightElement = lightElements.find { it.javaClass.name == lightQName }
        testServices.assertions.assertNotNull(lightElement) { "Expected $lightQName, got: " + lightElements.joinToString { it::class.java.name } }
        testServices.assertions.assertTrue(lightElement!!.isEquikonstentTo(declaration)) { "Light element is not equikonstent to the corresponding ktElement" }
    }

    override konst configurator: AnalysisApiTestConfigurator
        get() = AnalysisApiFirSourceTestConfigurator(analyseInDependentSession = false)
}