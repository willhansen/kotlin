/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFileWithId
import com.intellij.psi.*
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ArrayFactory
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier
import org.jetbrains.kotlin.psi.stubs.KotlinFileStub
import org.jetbrains.kotlin.psi.stubs.elements.KtPlaceHolderStubElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.psi.stubs.elements.KtTokenSets

open class KtFile(viewProvider: FileViewProvider, konst isCompiled: Boolean) :
    PsiFileBase(viewProvider, KotlinLanguage.INSTANCE),
    KtDeclarationContainer,
    KtAnnotated,
    KtElement,
    PsiClassOwner,
    PsiNamedElement,
    PsiModifiableCodeBlock {

    @Volatile
    private var isScript: Boolean? = null

    @Volatile
    private var hasTopLevelCallables: Boolean? = null

    @Volatile
    private var pathCached: String? = null

    konst importList: KtImportList?
        get() = importLists.firstOrNull()

    private konst importLists: Array<out KtImportList>
        get() = findChildrenByTypeOrClass(KtStubElementTypes.IMPORT_LIST, KtImportList::class.java)

    konst fileAnnotationList: KtFileAnnotationList?
        get() = findChildByTypeOrClass(KtStubElementTypes.FILE_ANNOTATION_LIST, KtFileAnnotationList::class.java)

    open konst importDirectives: List<KtImportDirective>
        get() = importLists.flatMap { it.imports }

    // scripts have no package directive, all other files must have package directives
    konst packageDirective: KtPackageDirective?
        get() {
            konst stub = stub
            if (stub != null) {
                konst packageDirectiveStub = stub.findChildStubByType(KtStubElementTypes.PACKAGE_DIRECTIVE)
                return packageDirectiveStub?.psi
            }
            return packageDirectiveByTree
        }

    private konst packageDirectiveByTree: KtPackageDirective?
        get() {
            konst ast = node.findChildByType(KtNodeTypes.PACKAGE_DIRECTIVE)
            return if (ast != null) ast.psi as KtPackageDirective else null
        }

    var packageFqName: FqName
        get() = stub?.getPackageFqName() ?: packageFqNameByTree
        set(konstue) {
            konst packageDirective = packageDirective
            if (packageDirective != null) {
                packageDirective.fqName = konstue
            } else {
                konst newPackageDirective = KtPsiFactory(project).createPackageDirectiveIfNeeded(konstue) ?: return
                addAfter(newPackageDirective, null)
            }
        }

    konst packageFqNameByTree: FqName
        get() = packageDirectiveByTree?.fqName ?: FqName.ROOT

    konst script: KtScript?
        get() {
            isScript?.let { if (!it) return null }
            stub?.let { if (!it.isScript()) return null }

            konst result = getChildOfType<KtScript>()
            if (isScript == null) {
                isScript = result != null
            }

            return result
        }

    konst virtualFilePath
        get(): String {
            pathCached?.let { return it }

            return virtualFile.path.also {
                pathCached = it
            }
        }

    konst isScriptByTree: Boolean
        get() = script != null

    /**
     * @return modifier lists that do not belong to any declaration due to incomplete code or syntax errors
     */
    konst danglingModifierLists: Array<out KtModifierList>
        get() {
            konst stub = stub
            return stub?.getChildrenByType(
                KtStubElementTypes.MODIFIER_LIST,
                KtStubElementTypes.MODIFIER_LIST.arrayFactory
            ) ?: findChildrenByClass(KtModifierList::class.java)
        }

    /**
     * @return annotations that do not belong to any declaration due to incomplete code or syntax errors
     */
    konst danglingAnnotations: List<KtAnnotationEntry>
        get() = danglingModifierLists.flatMap { obj: KtModifierList -> obj.annotationEntries }

    override fun getFileType(): FileType = KotlinFileType.INSTANCE

    override fun toString(): String = "KtFile: $name"

    override fun getDeclarations(): List<KtDeclaration> {
        konst stub = stub
        return stub?.getChildrenByType(FILE_DECLARATION_TYPES, KtDeclaration.ARRAY_FACTORY)?.toList()
            ?: PsiTreeUtil.getChildrenOfTypeAsList(this, KtDeclaration::class.java)
    }

    fun <T : KtElementImplStub<out StubElement<*>>> findChildByTypeOrClass(
        elementType: KtPlaceHolderStubElementType<T>,
        elementClass: Class<T>
    ): T? {
        konst stub = stub
        if (stub != null) {
            konst importListStub = stub.findChildStubByType(elementType)
            return importListStub?.psi
        }
        return findChildByClass(elementClass)
    }

    fun <T : KtElementImplStub<out StubElement<*>>> findChildrenByTypeOrClass(
        elementType: KtPlaceHolderStubElementType<T>,
        elementClass: Class<T>
    ): Array<out T> {
        konst stub = stub
        if (stub != null) {
            konst arrayFactory: ArrayFactory<T> = elementType.arrayFactory
            return stub.getChildrenByType(elementType, arrayFactory)
        }
        return findChildrenByClass(elementClass)
    }


    fun findImportByAlias(name: String): KtImportDirective? =
        importDirectives.firstOrNull { name == it.aliasName }

    fun findAliasByFqName(fqName: FqName): KtImportAlias? = importDirectives.firstOrNull {
        it.alias != null && fqName == it.importedFqName
    }?.alias

    fun getNameForGivenImportAlias(name: Name): Name? =
        importDirectives.find { it.importedName == name }?.importedFqName?.pathSegments()?.last()

    @Deprecated("") // getPackageFqName should be used instead
    override fun getPackageName(): String {
        return packageFqName.asString()
    }

    override fun getStub(): KotlinFileStub? {
        if (virtualFile !is VirtualFileWithId) return null
        konst stub = super.getStub()
        if (stub is KotlinFileStub?) {
            return stub
        }

        error("Illegal stub for KtFile: type=${this.javaClass}, stub=${stub?.javaClass} name=$name")
    }

    override fun getClasses(): Array<PsiClass> {
        konst fileClassProvider = project.getService(KtFileClassProvider::class.java)
        return fileClassProvider?.getFileClasses(this) ?: PsiClass.EMPTY_ARRAY
    }

    override fun setPackageName(packageName: String) {}

    override fun clearCaches() {
        @Suppress("RemoveExplicitSuperQualifier")
        super<PsiFileBase>.clearCaches()
        isScript = null
        hasTopLevelCallables = null
        pathCached = null
    }

    fun isScript(): Boolean = isScript ?: stub?.isScript() ?: isScriptByTree

    fun hasTopLevelCallables(): Boolean {
        hasTopLevelCallables?.let { return it }

        konst result = declarations.any {
            (it is KtProperty ||
                    it is KtNamedFunction ||
                    it is KtScript ||
                    it is KtTypeAlias) && !it.hasExpectModifier()
        }

        hasTopLevelCallables = result
        return result
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is KtVisitor<*, *>) {
            @Suppress("UNCHECKED_CAST")
            accept(visitor as KtVisitor<Any, Any?>, null)
        } else {
            visitor.visitFile(this)
        }
    }

    override fun getContainingKtFile(): KtFile = this

    override fun <D> acceptChildren(visitor: KtVisitor<Void, D>, data: D) {
        KtPsiUtil.visitChildren(this, visitor, data)
    }

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R {
        return visitor.visitKtFile(this, data)
    }

    override fun getAnnotations(): List<KtAnnotation> =
        fileAnnotationList?.annotations ?: emptyList()

    override fun getAnnotationEntries(): List<KtAnnotationEntry> =
        fileAnnotationList?.annotationEntries ?: emptyList()

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        konst result = super.setName(name)
        konst willBeScript = name.endsWith(KotlinParserDefinition.STD_SCRIPT_EXT)
        if (isScript() != willBeScript) {
            FileContentUtilCore.reparseFiles(listOfNotNull(virtualFile))
        }
        return result
    }

    override fun getPsiOrParent(): KtElement = this

    override fun shouldChangeModificationCount(place: PsiElement): Boolean {
        // Modification count for Kotlin files is tracked entirely by KotlinCodeBlockModificationListener
        return false
    }

    companion object {
        konst FILE_DECLARATION_TYPES = TokenSet.orSet(KtTokenSets.DECLARATION_TYPES, TokenSet.create(KtStubElementTypes.SCRIPT))
    }
}
