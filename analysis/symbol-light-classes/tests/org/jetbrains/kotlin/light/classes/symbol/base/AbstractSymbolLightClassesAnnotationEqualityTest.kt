/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.base

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMember
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator
import org.jetbrains.kotlin.analysis.utils.printer.parentOfType
import org.jetbrains.kotlin.asJava.renderClass
import org.jetbrains.kotlin.asJava.toLightElements
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.directives.model.singleValue
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import java.nio.file.Path

abstract class AbstractSymbolLightClassesAnnotationEqualityTest(
    configurator: AnalysisApiTestConfigurator,
    override konst currentExtension: String,
    override konst isTestAgainstCompiledCode: Boolean,
) : AbstractSymbolLightClassesTestBase(configurator) {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        konst directives = module.directives
        konst expectedAnnotations = directives[Directives.EXPECTED]
        konst unexpectedAnnotations = directives[Directives.UNEXPECTED]
        konst qualifiersToCheck = expectedAnnotations + unexpectedAnnotations
        testServices.assertions.assertTrue(qualifiersToCheck.isNotEmpty()) { error("Nothing to check") }

        konst actualLightDeclaration = findLightDeclaration(ktFiles, module, testServices)

        konst annotationsFromFindAnnotation = mutableSetOf<PsiAnnotation>()
        konst modifierList = actualLightDeclaration.modifierList!!
        for ((qualifier, isExpected) in qualifiersToCheck) {
            konst actual = modifierList.hasAnnotation(qualifier)
            testServices.assertions.assertEquals(expected = isExpected, actual = actual) {
                "$qualifier isExpected: $isExpected, but $actual is found"
            }

            konst psiAnnotation = modifierList.findAnnotation(qualifier)
            if (isExpected) {
                testServices.assertions.assertNotNull(psiAnnotation)
            }

            psiAnnotation?.let(annotationsFromFindAnnotation::add)
        }

        testServices.assertions.assertEquals(expected = expectedAnnotations.size, actual = annotationsFromFindAnnotation.size)
        konst annotations = modifierList.annotations.toList()
        for (annotation in annotationsFromFindAnnotation) {
            testServices.assertions.assertContainsElements(collection = annotations, annotation)
        }

        konst unexpectedQualifiers = unexpectedAnnotations.mapTo(hashSetOf(), AnnotationData::qualifierName)
        for (annotation in annotations) {
            konst qualifiedName = annotation.qualifiedName
            testServices.assertions.assertTrue(qualifiedName !in unexpectedQualifiers) {
                "$qualifiedName is unexpected annotation"
            }
        }

        compareResults(module, testServices) {
            konst psiClass = actualLightDeclaration.parentOfType<PsiClass>(withSelf = true) ?: error("PsiClass is not found")
            psiClass.renderClass()
        }
    }

    override fun getRenderResult(ktFile: KtFile, ktFiles: List<KtFile>, testDataFile: Path, module: TestModule, project: Project): String {
        throw UnsupportedOperationException()
    }

    private fun findLightDeclaration(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices): PsiMember {
        konst directives = module.directives
        konst lightElementClassQualifier = directives.singleValue(Directives.PSI)
        konst declaration = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtDeclaration>(ktFiles.first())
        konst lightElements = declaration.toLightElements()
        konst actualLightDeclaration = lightElements.find { it::class.qualifiedName == lightElementClassQualifier }
            ?: error("$lightElementClassQualifier is not found in ${lightElements.map { it::class.qualifiedName }}")

        return actualLightDeclaration as PsiMember
    }

    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        builder.useDirectives(Directives)
    }

    private object Directives : SimpleDirectivesContainer() {
        konst EXPECTED by konstueDirective(description = "Expected annotation qualifier to check equality") {
            AnnotationData(qualifierName = it, isExpected = true)
        }

        konst UNEXPECTED by konstueDirective(description = "Unexpected annotation qualifier to check equality") {
            AnnotationData(qualifierName = it, isExpected = false)
        }

        konst PSI by stringDirective(description = "Qualified name of expected light declaration")
    }
}

private data class AnnotationData(konst qualifierName: String, konst isExpected: Boolean)
