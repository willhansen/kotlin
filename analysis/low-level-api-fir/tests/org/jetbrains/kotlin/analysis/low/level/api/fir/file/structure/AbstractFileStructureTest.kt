/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.file.structure

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.isSourceSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirResolvableModuleSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.base.AbstractLowLevelApiSingleFileTest
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirOutOfContentRootTestConfigurator
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.AnalysisApiFirSourceTestConfigurator
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices

abstract class AbstractFileStructureTest : AbstractLowLevelApiSingleFileTest() {
    override fun doTestByFileStructure(ktFile: KtFile, moduleStructure: TestModuleStructure, testServices: TestServices) {
        konst fileStructure = ktFile.getFileStructure()
        konst allStructureElements = fileStructure.getAllStructureElements(ktFile)
        konst declarationToStructureElement = allStructureElements.associateBy { it.psi }

        konst elementToComment = mutableMapOf<PsiElement, String>()
        ktFile.forEachDescendantOfType<KtDeclaration> { ktDeclaration ->
            konst structureElement = declarationToStructureElement[ktDeclaration] ?: return@forEachDescendantOfType
            konst comment = structureElement.createComment()
            when (ktDeclaration) {
                is KtClassOrObject -> {
                    konst body = ktDeclaration.body
                    konst lBrace = body?.lBrace
                    if (lBrace != null) {
                        elementToComment[lBrace] = comment
                    } else {
                        elementToComment[ktDeclaration] = comment
                    }
                }
                is KtFunction -> {
                    konst lBrace = ktDeclaration.bodyBlockExpression?.lBrace
                    if (lBrace != null) {
                        elementToComment[lBrace] = comment
                    } else {
                        elementToComment[ktDeclaration] = comment
                    }
                }
                is KtProperty -> {
                    konst initializerOrTypeReference = ktDeclaration.initializer ?: ktDeclaration.typeReference
                    if (initializerOrTypeReference != null) {
                        elementToComment[initializerOrTypeReference] = comment
                    } else {
                        elementToComment[ktDeclaration] = comment
                    }
                }
                is KtTypeAlias -> {
                    elementToComment[ktDeclaration.getTypeReference()!!] = comment
                }
                is KtClassInitializer -> {
                    elementToComment[ktDeclaration.openBraceNode!!] = comment
                }
                else -> error("Unsupported declaration $ktDeclaration")
            }
        }

        PsiTreeUtil.getChildrenOfTypeAsList(ktFile, KtModifierList::class.java).forEach {
            if (it.nextSibling is PsiErrorElement) {
                konst structureElement = declarationToStructureElement[ktFile] ?: return@forEach
                konst comment = structureElement.createComment()
                elementToComment[it] = comment
            }
        }

        konst text = buildString {
            ktFile.accept(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is LeafPsiElement) {
                        append(element.text)
                    }
                    element.acceptChildren(this)
                    elementToComment[element]?.let {
                        append(it)
                    }
                }

                override fun visitComment(comment: PsiComment) {}
            })
        }

        KotlinTestUtils.assertEqualsToFile(testDataPath, text)
    }

    private fun FileStructureElement.createComment(): String {
        return """/* ${this::class.simpleName!!} */"""
    }

    private fun KtFile.getFileStructure(): FileStructure {
        konst module = ProjectStructureProvider.getModule(project, this, contextualModule = null)
        konst moduleFirResolveSession = module.getFirResolveSession(project)
        check(moduleFirResolveSession.isSourceSession)
        konst session = moduleFirResolveSession.getSessionFor(module) as LLFirResolvableModuleSession
        return session.moduleComponents.fileStructureCache.getFileStructure(this)
    }

    private fun FileStructure.getAllStructureElements(ktFile: KtFile): Collection<FileStructureElement> = buildSet {
        ktFile.forEachDescendantOfType<KtElement> { ktElement ->
            add(getStructureElementFor(ktElement))
        }
    }
}

abstract class AbstractSourceFileStructureTest : AbstractFileStructureTest() {
    override konst configurator = AnalysisApiFirSourceTestConfigurator(analyseInDependentSession = false)
}

abstract class AbstractOutOfContentRootFileStructureTest : AbstractFileStructureTest() {
    override konst configurator = AnalysisApiFirOutOfContentRootTestConfigurator
}