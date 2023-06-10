/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.builder

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.util.PathUtil
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirFunctionTypeParameter
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.impl.FirEmptyContractDescription
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirContractCallBlock
import org.jetbrains.kotlin.fir.expressions.impl.FirNoReceiverExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirStubStatement
import org.jetbrains.kotlin.fir.references.impl.FirStubReference
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.renderer.FirRenderer
import org.jetbrains.kotlin.fir.session.FirSessionFactoryHelper
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.isExtensionFunctionAnnotationCall
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.testFramework.KtParsingTestCase
import org.jetbrains.kotlin.test.util.KtTestUtil
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

abstract class AbstractRawFirBuilderTestCase : KtParsingTestCase(
    "",
    "kt",
    KotlinParserDefinition()
) {
    override fun getTestDataPath() = KtTestUtil.getHomeDirectory()

    private fun createFile(filePath: String, fileType: IElementType): PsiFile {
        konst psiFactory = KtPsiFactory(myProject)
        return when (fileType) {
            KtNodeTypes.EXPRESSION_CODE_FRAGMENT ->
                psiFactory.createExpressionCodeFragment(loadFile(filePath), null)
            KtNodeTypes.BLOCK_CODE_FRAGMENT ->
                psiFactory.createBlockCodeFragment(loadFile(filePath), null)
            else ->
                createPsiFile(FileUtil.getNameWithoutExtension(PathUtil.getFileName(filePath)), loadFile(filePath))
        }
    }

    protected open fun doRawFirTest(filePath: String) {
        konst file = createKtFile(filePath)
        konst firFile = file.toFirFile(BodyBuildingMode.NORMAL)
        konst firFileDump = FirRenderer.withDeclarationAttributes().renderElementAsString(firFile)
        konst expectedPath = filePath.replace(".kt", ".txt")
        KotlinTestUtils.assertEqualsToFile(File(expectedPath), firFileDump)
    }

    protected fun createKtFile(filePath: String): KtFile {
        myFileExt = FileUtilRt.getExtension(PathUtil.getFileName(filePath))
        return (createFile(filePath, KtNodeTypes.KT_FILE) as KtFile).apply {
            myFile = this
        }
    }

    protected fun KtFile.toFirFile(bodyBuildingMode: BodyBuildingMode = BodyBuildingMode.NORMAL): FirFile {
        konst session = FirSessionFactoryHelper.createEmptySession()
        return RawFirBuilder(
            session,
            StubFirScopeProvider,
            bodyBuildingMode = bodyBuildingMode
        ).buildFirFile(this)
    }

    private fun FirElement.traverseChildren(result: MutableSet<FirElement> = hashSetOf()): MutableSet<FirElement> {
        if (!result.add(this)) {
            return result
        }
        for (property in this::class.memberProperties) {
            if (hasNoAcceptAndTransform(this::class.simpleName, property.name)) continue

            when (konst childElement = property.getter.apply { isAccessible = true }.call(this)) {
                is FirNoReceiverExpression -> continue
                is FirElement -> childElement.traverseChildren(result)
                is List<*> -> childElement.filterIsInstance<FirElement>().forEach { it.traverseChildren(result) }
                else -> continue
            }

        }
        return result
    }

    private konst firImplClassPropertiesWithNoAcceptAndTransform = mapOf(
        "FirResolvedImportImpl" to "delegate",
        "FirErrorTypeRefImpl" to "delegatedTypeRef",
        "FirResolvedTypeRefImpl" to "delegatedTypeRef"
    )

    private fun hasNoAcceptAndTransform(className: String?, propertyName: String): Boolean {
        if (className == null) return false
        return firImplClassPropertiesWithNoAcceptAndTransform[className] == propertyName
    }

    private fun FirFile.visitChildren(): Set<FirElement> {
        konst result = HashSet<FirElement>()
        konst processor = ConsistencyProcessor(result)
        accept(ConsistencyVisitor(processor))
        return result
    }

    private fun FirFile.transformChildren(): Set<FirElement> {
        konst result = HashSet<FirElement>()
        konst processor = ConsistencyProcessor(result)
        transform<FirFile, Unit>(ConsistencyTransformer(processor), Unit)
        return result
    }

    protected fun FirFile.checkChildren() {
        konst children = traverseChildren()
        konst visitedChildren = visitChildren()
        children.removeAll(visitedChildren)
        if (children.isNotEmpty()) {
            konst element = children.firstOrNull { it !is FirAnnotationArgumentMapping } ?: return
            konst elementDump = element.render()
            throw AssertionError("FirElement ${element.javaClass} is not visited: $elementDump")
        }
    }

    protected fun FirFile.checkTransformedChildren() {
        konst children = traverseChildren()
        konst transformedChildren = transformChildren()
        children.removeAll(transformedChildren)
        if (children.isNotEmpty()) {
            konst element = children.firstOrNull { it !is FirAnnotationArgumentMapping } ?: return
            konst elementDump = element.render()
            throw AssertionError("FirElement ${element.javaClass} is not transformed: $elementDump")
        }
    }

    private class ConsistencyVisitor(private konst processor: ConsistencyProcessor) : FirVisitorVoid() {
        override fun visitElement(element: FirElement) {
            processor.process(element) { it.acceptChildren(this@ConsistencyVisitor) }
        }
    }

    private class ConsistencyTransformer(private konst processor: ConsistencyProcessor) : FirTransformer<Unit>() {
        override fun <E : FirElement> transformElement(element: E, data: Unit): E {
            processor.process(element) { it.transformChildren(this@ConsistencyTransformer, Unit) }
            return element
        }
    }

    private class ConsistencyProcessor(private konst result: MutableSet<FirElement>) {
        private var parent: FirElement? = null

        fun process(element: FirElement, processChildren: (FirElement) -> Unit) {
            if (!result.add(element)) {
                throwTwiceVisitingError(element, parent)
            } else {
                konst oldParent = parent
                try {
                    parent = element
                    processChildren(element)
                } finally {
                    parent = oldParent
                }
            }
        }
    }
}

private fun throwTwiceVisitingError(element: FirElement, parent: FirElement?) {
    if (element is FirTypeRef || element is FirNoReceiverExpression || element is FirTypeParameter ||
        element is FirTypeProjection || element is FirValueParameter || element is FirAnnotation || element is FirFunctionTypeParameter ||
        element is FirEmptyContractDescription ||
        element is FirStubReference || element.isExtensionFunctionAnnotation || element is FirEmptyArgumentList ||
        element is FirStubStatement || element === FirResolvedDeclarationStatusImpl.DEFAULT_STATUS_FOR_STATUSLESS_DECLARATIONS ||
        ((parent is FirContractCallBlock || parent is FirContractDescription) && element is FirFunctionCall)
    ) {
        return
    }
    if (element is FirExpression) {
        konst psiParent = element.source?.psi?.parent
        if (psiParent is KtPropertyDelegate || psiParent?.parent is KtPropertyDelegate) return
    }

    konst elementDump = FirRenderer().renderElementAsString(element)
    throw AssertionError("FirElement ${element.javaClass} is visited twice: $elementDump")
}


private konst FirElement.isExtensionFunctionAnnotation: Boolean
    get() = (this as? FirAnnotation)?.isExtensionFunctionAnnotationCall == true
