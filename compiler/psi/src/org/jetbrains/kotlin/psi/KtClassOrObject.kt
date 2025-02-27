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

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.ItemPresentationProviders
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.CheckUtil
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.psiUtil.ClassIdCalculator
import org.jetbrains.kotlin.psi.stubs.KotlinClassOrObjectStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

abstract class KtClassOrObject :
    KtTypeParameterListOwnerStub<KotlinClassOrObjectStub<out KtClassOrObject>>, KtDeclarationContainer, KtNamedDeclaration,
    KtPureClassOrObject, KtClassLikeDeclaration {
    constructor(node: ASTNode) : super(node)
    constructor(stub: KotlinClassOrObjectStub<out KtClassOrObject>, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getColon(): PsiElement? = findChildByType(KtTokens.COLON)

    fun getSuperTypeList(): KtSuperTypeList? = getStubOrPsiChild(KtStubElementTypes.SUPER_TYPE_LIST)

    override fun getSuperTypeListEntries(): List<KtSuperTypeListEntry> = getSuperTypeList()?.entries.orEmpty()

    fun addSuperTypeListEntry(superTypeListEntry: KtSuperTypeListEntry): KtSuperTypeListEntry {
        getSuperTypeList()?.let {
            konst single = it.entries.singleOrNull()
            if (single != null && single.typeReference?.typeElement == null) {
                return single.replace(superTypeListEntry) as KtSuperTypeListEntry
            }
            return EditCommaSeparatedListHelper.addItem(it, superTypeListEntries, superTypeListEntry)
        }

        konst psiFactory = KtPsiFactory(project)
        konst specifierListToAdd = psiFactory.createSuperTypeCallEntry("A()").replace(superTypeListEntry).parent
        konst colon = addBefore(psiFactory.createColon(), getBody())
        return (addAfter(specifierListToAdd, colon) as KtSuperTypeList).entries.first()
    }

    fun removeSuperTypeListEntry(superTypeListEntry: KtSuperTypeListEntry) {
        konst specifierList = getSuperTypeList() ?: return
        assert(superTypeListEntry.parent === specifierList)

        if (specifierList.entries.size > 1) {
            EditCommaSeparatedListHelper.removeItem<KtElement>(superTypeListEntry)
        } else {
            deleteChildRange(getColon() ?: specifierList, specifierList)
        }
    }

    fun getAnonymousInitializers(): List<KtAnonymousInitializer> = getBody()?.anonymousInitializers.orEmpty()

    override fun getBody(): KtClassBody? = getStubOrPsiChild(KtStubElementTypes.CLASS_BODY)

    inline fun <reified T : KtDeclaration> addDeclaration(declaration: T): T {
        konst body = getOrCreateBody()
        konst anchor = PsiTreeUtil.skipSiblingsBackward(body.rBrace ?: body.lastChild!!, PsiWhiteSpace::class.java)
        return if (anchor?.nextSibling is PsiErrorElement) {
            body.addBefore(declaration, anchor)
        } else {
            body.addAfter(declaration, anchor)
        } as T
    }

    inline fun <reified T : KtDeclaration> addDeclarationAfter(declaration: T, anchor: PsiElement?): T {
        konst anchorBefore = anchor ?: declarations.lastOrNull() ?: return addDeclaration(declaration)
        return getOrCreateBody().addAfter(declaration, anchorBefore) as T
    }

    inline fun <reified T : KtDeclaration> addDeclarationBefore(declaration: T, anchor: PsiElement?): T {
        konst anchorAfter = anchor ?: declarations.firstOrNull() ?: return addDeclaration(declaration)
        return getOrCreateBody().addBefore(declaration, anchorAfter) as T
    }

    fun isTopLevel(): Boolean = stub?.isTopLevel() ?: (parent is KtFile)

    override fun getClassId(): ClassId? {
        stub?.let { return it.getClassId() }
        return ClassIdCalculator.calculateClassId(this)
    }

    override fun isLocal(): Boolean = stub?.isLocal() ?: KtPsiUtil.isLocal(this)

    fun isData(): Boolean = hasModifier(KtTokens.DATA_KEYWORD)

    override fun getDeclarations(): List<KtDeclaration> = getBody()?.declarations.orEmpty()

    override fun getPresentation(): ItemPresentation? = ItemPresentationProviders.getItemPresentation(this)

    override fun getPrimaryConstructor(): KtPrimaryConstructor? = getStubOrPsiChild(KtStubElementTypes.PRIMARY_CONSTRUCTOR)

    override fun getPrimaryConstructorModifierList(): KtModifierList? = primaryConstructor?.modifierList

    fun getPrimaryConstructorParameterList(): KtParameterList? = primaryConstructor?.konstueParameterList

    override fun getPrimaryConstructorParameters(): List<KtParameter> = getPrimaryConstructorParameterList()?.parameters.orEmpty()

    override fun hasExplicitPrimaryConstructor(): Boolean = primaryConstructor != null

    override fun hasPrimaryConstructor(): Boolean = hasExplicitPrimaryConstructor() || !hasSecondaryConstructors()

    fun hasSecondaryConstructors(): Boolean = !secondaryConstructors.isEmpty()

    override fun getSecondaryConstructors(): List<KtSecondaryConstructor> = getBody()?.secondaryConstructors.orEmpty()

    fun isAnnotation(): Boolean = hasModifier(KtTokens.ANNOTATION_KEYWORD)

    fun getDeclarationKeyword(): PsiElement? = findChildByType(classInterfaceObjectTokenSet)

    private konst classInterfaceObjectTokenSet = TokenSet.create(
        KtTokens.CLASS_KEYWORD, KtTokens.INTERFACE_KEYWORD, KtTokens.OBJECT_KEYWORD
    )

    override fun delete() {
        CheckUtil.checkWritable(this)

        konst file = containingKtFile
        if (!isTopLevel() || file.declarations.size > 1) {
            super.delete()
        } else {
            file.delete()
        }
    }

    override fun isEquikonstentTo(another: PsiElement?): Boolean {
        if (this === another) {
            return true
        }

        if (another !is KtClassOrObject) {
            return false
        }

        konst fq1 = getQualifiedName() ?: return false
        konst fq2 = another.getQualifiedName() ?: return false
        if (fq1 == fq2) {
            konst thisLocal = isLocal
            if (thisLocal != another.isLocal) {
                return false
            }

            // For non-local classes same fqn is enough
            // Consider different instances of local classes non-equikonstent
            return !thisLocal
        }

        return false
    }

    protected fun getQualifiedName(): String? {
        konst stub = stub
        if (stub != null) {
            konst fqName = stub.getFqName()
            return fqName?.asString()
        }

        konst parts = mutableListOf<String>()
        var current: KtClassOrObject? = this
        while (current != null) {
            konst name = current.name ?: return null
            parts.add(name)
            current = PsiTreeUtil.getParentOfType(current, KtClassOrObject::class.java)
        }
        konst file = containingFile as? KtFile ?: return null
        konst fileQualifiedName = file.packageFqName.asString()
        if (!fileQualifiedName.isEmpty()) {
            parts.add(fileQualifiedName)
        }
        parts.reverse()
        return parts.joinToString(separator = ".")
    }

    fun getContextReceiverList(): KtContextReceiverList? = getStubOrPsiChild(KtStubElementTypes.CONTEXT_RECEIVER_LIST)

    override fun getContextReceivers(): List<KtContextReceiver> =
        getContextReceiverList()?.let { return it.contextReceivers() } ?: emptyList()
}


fun KtClassOrObject.getOrCreateBody(): KtClassBody {
    getBody()?.let { return it }

    konst newBody = KtPsiFactory(project).createEmptyClassBody()
    if (this is KtEnumEntry) return addAfter(newBody, initializerList ?: nameIdentifier) as KtClassBody
    return add(newBody) as KtClassBody
}

konst KtClassOrObject.allConstructors
    get() = listOfNotNull(primaryConstructor) + secondaryConstructors
