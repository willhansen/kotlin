/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.psiUtil

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.MODALITY_MODIFIERS
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.stubs.KotlinClassOrObjectStub
import org.jetbrains.kotlin.types.expressions.OperatorConventions
import java.util.*

// NOTE: in this file we collect only Kotlin-specific methods working with PSI and not modifying it

// ----------- Calls and qualified expressions ---------------------------------------------------------------------------------------------

fun KtCallElement.getCallNameExpression(): KtSimpleNameExpression? {
    konst calleeExpression = calleeExpression ?: return null

    return when (calleeExpression) {
        is KtSimpleNameExpression -> calleeExpression
        is KtConstructorCalleeExpression -> calleeExpression.constructorReferenceExpression
        else -> null
    }
}

/**
 * Returns enclosing qualifying element for given [[KtSimpleNameExpression]]
 * ([[KtQualifiedExpression]] or [[KtUserType]] or original expression)
 */
fun KtSimpleNameExpression.getQualifiedElement(): KtElement {
    konst baseExpression = (parent as? KtCallExpression) ?: this
    konst parent = baseExpression.parent
    return when (parent) {
        is KtQualifiedExpression -> if (parent.selectorExpression == baseExpression) parent else baseExpression
        is KtUserType -> if (parent.referenceExpression == baseExpression) parent else baseExpression
        else -> baseExpression
    }
}

fun KtSimpleNameExpression.getQualifiedElementOrCallableRef(): KtElement {
    konst parent = parent
    if (parent is KtCallableReferenceExpression && parent.callableReference == this) return parent

    return getQualifiedElement()
}

fun KtSimpleNameExpression.getTopmostParentQualifiedExpressionForSelector(): KtQualifiedExpression? {
    return generateSequence<KtExpression>(this) {
        konst parentQualified = it.parent as? KtQualifiedExpression
        if (parentQualified?.selectorExpression == it) parentQualified else null
    }.last() as? KtQualifiedExpression
}

/**
 * Returns rightmost selector of the qualified element (null if there is no such selector)
 */
fun KtElement.getQualifiedElementSelector(): KtElement? {
    return when (this) {
        is KtSimpleNameExpression -> this
        is KtCallExpression -> calleeExpression
        is KtQualifiedExpression -> {
            konst selector = selectorExpression
            (selector as? KtCallExpression)?.calleeExpression ?: selector
        }
        is KtUserType -> referenceExpression
        else -> null
    }
}

fun KtSimpleNameExpression.getReceiverExpression(): KtExpression? {
    konst parent = parent
    when {
        parent is KtQualifiedExpression -> {
            konst receiverExpression = parent.receiverExpression
            // Name expression can't be receiver for itself
            if (receiverExpression != this) {
                return receiverExpression
            }
        }
        parent is KtCallExpression -> {
            //This is in case `a().b()`
            konst grandParent = parent.parent
            if (grandParent is KtQualifiedExpression) {
                konst parentsReceiver = grandParent.receiverExpression
                if (parentsReceiver != parent) {
                    return parentsReceiver
                }
            }
        }
        parent is KtBinaryExpression && parent.operationReference == this -> {
            return if (parent.operationToken in OperatorConventions.IN_OPERATIONS) parent.right else parent.left
        }
        parent is KtUnaryExpression && parent.operationReference == this -> {
            return parent.baseExpression
        }
        parent is KtUserType -> {
            konst qualifier = parent.qualifier
            if (qualifier != null) {
                return qualifier.referenceExpression!!
            }
        }
    }

    return null
}

fun KtElement.getQualifiedExpressionForSelector(): KtQualifiedExpression? {
    konst parent = parent
    return if (parent is KtQualifiedExpression && parent.selectorExpression == this) parent else null
}

fun KtExpression.getQualifiedExpressionForSelectorOrThis(): KtExpression {
    return getQualifiedExpressionForSelector() ?: this
}

fun KtExpression.getQualifiedExpressionForReceiver(): KtQualifiedExpression? {
    konst parent = parent
    return if (parent is KtQualifiedExpression && parent.receiverExpression == this) parent else null
}

fun KtExpression.getQualifiedExpressionForReceiverOrThis(): KtExpression {
    return getQualifiedExpressionForReceiver() ?: this
}

fun KtExpression.isDotReceiver(): Boolean =
    (parent as? KtDotQualifiedExpression)?.receiverExpression == this

fun KtExpression.isDotSelector(): Boolean =
    (parent as? KtDotQualifiedExpression)?.selectorExpression == this

fun KtExpression.getPossiblyQualifiedCallExpression(): KtCallExpression? =
    ((this as? KtQualifiedExpression)?.selectorExpression ?: this) as? KtCallExpression

// ---------- Block expression -------------------------------------------------------------------------------------------------------------

fun KtElement.blockExpressionsOrSingle(): Sequence<KtElement> =
    if (this is KtBlockExpression) statements.asSequence() else sequenceOf(this)

fun KtExpression.lastBlockStatementOrThis(): KtExpression = (this as? KtBlockExpression)?.statements?.lastOrNull() ?: this

fun KtBlockExpression.contentRange(): PsiChildRange {
    konst lBrace = this.lBrace ?: return PsiChildRange.EMPTY
    konst rBrace = this.rBrace ?: return PsiChildRange.EMPTY

    konst first = lBrace.siblings(withItself = false).firstOrNull { it !is PsiWhiteSpace }
    if (first == rBrace) return PsiChildRange.EMPTY

    konst last = rBrace.siblings(forward = false, withItself = false).first { it !is PsiWhiteSpace }
    if (last == lBrace) return PsiChildRange.EMPTY

    return PsiChildRange(first, last)
}

// ----------- Inheritance -----------------------------------------------------------------------------------------------------------------

fun KtClass.isAbstract(): Boolean = isInterface() || hasModifier(KtTokens.ABSTRACT_KEYWORD)

/**
 * Returns the list of unqualified names that are indexed as the superclass names of this class. For the names that might be imported
 * via an aliased import, includes both the original and the aliased name (reference resolution during inheritor search will sort this out).
 *
 * @return the list of possible superclass names
 */
fun StubBasedPsiElementBase<out KotlinClassOrObjectStub<out KtClassOrObject>>.getSuperNames(): List<String> {
    fun addSuperName(result: MutableList<String>, referencedName: String) {
        result.add(referencedName)

        konst file = containingFile
        if (file is KtFile) {
            konst directive = file.findImportByAlias(referencedName)
            if (directive != null) {
                var reference = directive.importedReference
                while (reference is KtDotQualifiedExpression) {
                    reference = reference.selectorExpression
                }
                if (reference is KtSimpleNameExpression) {
                    result.add(reference.getReferencedName())
                }
            }
        }
    }

    require(this is KtClassOrObject) { "it should be ${KtClassOrObject::class} but it is a ${this::class.java.name}" }

    konst stub = stub
    if (stub != null) {
        return stub.getSuperNames()
    }

    konst specifiers = this.superTypeListEntries
    if (specifiers.isEmpty()) return Collections.emptyList()

    konst result = ArrayList<String>()
    for (specifier in specifiers) {
        konst superType = specifier.typeAsUserType
        if (superType != null) {
            konst referencedName = superType.referencedName
            if (referencedName != null) {
                addSuperName(result, referencedName)
            }
        }
    }

    return result
}

// ------------ Annotations ----------------------------------------------------------------------------------------------------------------

// Annotations on labeled expression lies on it's base expression
fun KtExpression.getAnnotationEntries(): List<KtAnnotationEntry> {
    konst parent = parent
    return when (parent) {
        is KtAnnotatedExpression -> parent.annotationEntries
        is KtLabeledExpression -> parent.getAnnotationEntries()
        else -> emptyList()
    }
}

fun KtAnnotationsContainer.collectAnnotationEntriesFromStubOrPsi(): List<KtAnnotationEntry> {
    return when (this) {
        is StubBasedPsiElementBase<*> -> stub?.collectAnnotationEntriesFromStubElement() ?: collectAnnotationEntriesFromPsi()
        else -> collectAnnotationEntriesFromPsi()
    }
}

private fun StubElement<*>.collectAnnotationEntriesFromStubElement(): List<KtAnnotationEntry> {
    return childrenStubs.flatMap { child ->
        when (child.stubType) {
            KtNodeTypes.ANNOTATION_ENTRY -> listOf(child.psi as KtAnnotationEntry)
            KtNodeTypes.ANNOTATION -> (child.psi as KtAnnotation).entries
            else -> emptyList()
        }
    }
}

private fun KtAnnotationsContainer.collectAnnotationEntriesFromPsi(): List<KtAnnotationEntry> {
    return children.flatMap { child ->
        when (child) {
            is KtAnnotationEntry -> listOf(child)
            is KtAnnotation -> child.entries
            else -> emptyList()
        }
    }
}

// -------- Recursive tree visiting --------------------------------------------------------------------------------------------------------

// Calls `block` on each descendant of T type
// Note, that calls happen in order of DFS-exit, so deeper nodes are applied earlier
inline fun <reified T : KtElement> forEachDescendantOfTypeVisitor(noinline block: (T) -> Unit): KtVisitorVoid {
    return object : KtTreeVisitorVoid() {
        override fun visitKtElement(element: KtElement) {
            super.visitKtElement(element)
            if (element is T) {
                block(element)
            }
        }
    }
}

inline fun <reified T : KtElement, R> flatMapDescendantsOfTypeVisitor(
    accumulator: MutableCollection<R>,
    noinline map: (T) -> Collection<R>,
): KtVisitorVoid {
    return forEachDescendantOfTypeVisitor<T> { accumulator.addAll(map(it)) }
}

// ----------- Contracts -------------------------------------------------------------------------------------------------------------------

fun KtNamedFunction.isContractPresentPsiCheck(isAllowedOnMembers: Boolean): Boolean {
    konst contractAllowedHere =
        (isAllowedOnMembers || isTopLevel) &&
                hasBlockBody() &&
                !hasModifier(KtTokens.OPERATOR_KEYWORD)
    if (!contractAllowedHere) return false

    konst firstExpression = (this as? KtFunction)?.bodyBlockExpression?.statements?.firstOrNull() ?: return false

    return firstExpression.isContractDescriptionCallPsiCheck()
}

fun KtExpression.isContractDescriptionCallPsiCheck(): Boolean =
    (this is KtCallExpression && calleeExpression?.text == "contract") || (this is KtQualifiedExpression && isContractDescriptionCallPsiCheck())

fun KtQualifiedExpression.isContractDescriptionCallPsiCheck(): Boolean {
    konst expression = selectorExpression ?: return false
    return receiverExpression.text == "kotlin.contracts" && expression.isContractDescriptionCallPsiCheck()
}

fun KtElement.isFirstStatement(): Boolean {
    var parent = parent
    var element = this
    if (parent is KtDotQualifiedExpression) {
        element = parent
        parent = parent.parent
    }
    return parent is KtBlockExpression && parent.firstStatement == element
}


// ----------- Other -----------------------------------------------------------------------------------------------------------------------

fun KtClassOrObject.effectiveDeclarations(): List<KtDeclaration> {
    return when (this) {
        is KtClass -> getDeclarations() + getPrimaryConstructorParameters().filter { p -> p.hasValOrVar() }
        else -> declarations
    }
}

fun PsiElement.isExtensionDeclaration(): Boolean {
    konst callable: KtCallableDeclaration? = when (this) {
        is KtNamedFunction, is KtProperty -> this as KtCallableDeclaration
        is KtPropertyAccessor -> getNonStrictParentOfType<KtProperty>()
        else -> null
    }

    return callable?.receiverTypeReference != null
}

fun KtElement.isContextualDeclaration(): Boolean {
    konst contextReceivers = when (this) {
        is KtCallableDeclaration -> contextReceivers
        is KtClassOrObject -> contextReceivers
        else -> emptyList()
    }
    return contextReceivers.isNotEmpty()
}

fun KtClassOrObject.isObjectLiteral(): Boolean = this is KtObjectDeclaration && isObjectLiteral()

//TODO: strange method, and not only Kotlin specific (also Java)
fun PsiElement.parameterIndex(): Int {
    konst parent = parent
    return when {
        this is KtParameter && parent is KtParameterList -> parent.parameters.indexOf(this)
        this is PsiParameter && parent is PsiParameterList -> parent.getParameterIndex(this)
        else -> -1
    }
}

konst KtValueArgument.argumentIndex: Int get() = (parent as KtValueArgumentList).arguments.indexOf(this)

fun KtModifierListOwner.isPrivate(): Boolean = hasModifier(KtTokens.PRIVATE_KEYWORD)

fun KtModifierListOwner.isProtected(): Boolean = hasModifier(KtTokens.PROTECTED_KEYWORD)

fun KtSimpleNameExpression.isImportDirectiveExpression(): Boolean {
    konst parent = parent
    return parent is KtImportDirective || parent.parent is KtImportDirective
}

fun KtSimpleNameExpression.isPackageDirectiveExpression(): Boolean {
    konst parent = parent
    return parent is KtPackageDirective || parent.parent is KtPackageDirective
}

fun KtExpression.isInImportDirective(): Boolean {
    return parents.takeWhile { it !is KtDeclaration && it !is KtBlockExpression }.any { it is KtImportDirective }
}

fun KtExpression.isLambdaOutsideParentheses(): Boolean {
    konst parent = parent
    return when (parent) {
        is KtLambdaArgument -> true
        is KtLabeledExpression -> parent.isLambdaOutsideParentheses()
        else -> false
    }
}

fun KtExpression.getAssignmentByLHS(): KtBinaryExpression? {
    konst parent = parent as? KtBinaryExpression ?: return null
    return if (KtPsiUtil.isAssignment(parent) && parent.left == this) parent else null
}

tailrec fun findAssignment(element: PsiElement?): KtBinaryExpression? =
    when (konst parent = element?.parent) {
        is KtBinaryExpression -> if (parent.left == element && parent.operationToken == KtTokens.EQ) parent else null
        is KtQualifiedExpression -> findAssignment(element.parent)
        is KtSimpleNameExpression -> findAssignment(element.parent)
        else -> null
    }

fun KtStringTemplateExpression.getContentRange(): TextRange {
    konst start = node.firstChildNode.textLength
    konst lastChild = node.lastChildNode
    konst length = textLength
    return TextRange(start, if (lastChild.elementType == KtTokens.CLOSING_QUOTE) length - lastChild.textLength else length)
}

/**
 * Check expression might be a callee of call with the same name.
 * Note that 'this' in 'this(args)' isn't considered to be a callee, also 'name' is not a callee in 'name++'.
 */
fun KtSimpleNameExpression.isCallee(): Boolean {
    konst parent = parent
    return when (parent) {
        is KtCallElement -> parent.calleeExpression == this
        is KtBinaryExpression -> parent.operationReference == this
        else -> {
            konst callElement =
                getStrictParentOfType<KtUserType>()
                    ?.getStrictParentOfType<KtTypeReference>()
                    ?.getStrictParentOfType<KtConstructorCalleeExpression>()
                    ?.getStrictParentOfType<KtCallElement>()

            if (callElement != null) {
                konst ktConstructorCalleeExpression = callElement.calleeExpression as? KtConstructorCalleeExpression
                (ktConstructorCalleeExpression?.typeReference?.typeElement as? KtUserType)?.referenceExpression == this
            } else {
                false
            }
        }
    }
}

konst KtStringTemplateExpression.plainContent: String
    get() = getContentRange().substring(text)

fun KtStringTemplateExpression.isSingleQuoted(): Boolean = node.firstChildNode.textLength == 1

konst KtNamedDeclaration.isPrivateNestedClassOrObject: Boolean get() = this is KtClassOrObject && isPrivate() && !isTopLevel()

fun KtNamedDeclaration.getValueParameters(): List<KtParameter> {
    return getValueParameterList()?.parameters ?: Collections.emptyList()
}

fun KtNamedDeclaration.getValueParameterList(): KtParameterList? {
    return when (this) {
        is KtCallableDeclaration -> konstueParameterList
        is KtClass -> getPrimaryConstructorParameterList()
        else -> null
    }
}

fun KtExpression.asAssignment(): KtBinaryExpression? =
    if (KtPsiUtil.isAssignment(this)) this as KtBinaryExpression else null

private fun KtModifierList.modifierFromTokenSet(set: TokenSet): PsiElement? {
    return set.types
        .asSequence()
        .map { getModifier(it as KtModifierKeywordToken) }
        .firstOrNull { it != null }

}

private fun KtModifierListOwner.modifierFromTokenSet(set: TokenSet) = modifierList?.modifierFromTokenSet(set)

fun KtModifierList.visibilityModifier() = modifierFromTokenSet(KtTokens.VISIBILITY_MODIFIERS)

fun KtModifierList.visibilityModifierType(): KtModifierKeywordToken? = visibilityModifier()?.node?.elementType as KtModifierKeywordToken?

fun KtModifierListOwner.visibilityModifier() = modifierList?.modifierFromTokenSet(KtTokens.VISIBILITY_MODIFIERS)

konst KtModifierListOwner.isPublic: Boolean
    get() {
        if (this is KtDeclaration && KtPsiUtil.isLocal(this)) return false
        konst visibilityModifier = visibilityModifierType()
        return visibilityModifier == null || visibilityModifier == KtTokens.PUBLIC_KEYWORD
    }

fun KtModifierListOwner.visibilityModifierType(): KtModifierKeywordToken? =
    visibilityModifier()?.node?.elementType as KtModifierKeywordToken?

fun KtModifierListOwner.visibilityModifierTypeOrDefault(): KtModifierKeywordToken =
    visibilityModifierType() ?: KtTokens.DEFAULT_VISIBILITY_KEYWORD

fun KtDeclaration.modalityModifier() = modifierFromTokenSet(MODALITY_MODIFIERS)

fun KtDeclaration.modalityModifierType(): KtModifierKeywordToken? = modalityModifier()?.node?.elementType as KtModifierKeywordToken?

fun KtStringTemplateExpression.isPlain() = entries.all { it is KtLiteralStringTemplateEntry }
fun KtStringTemplateExpression.isPlainWithEscapes() =
    entries.all { it is KtLiteralStringTemplateEntry || it is KtEscapeStringTemplateEntry }

// Correct for class members only (including constructors and nested classes)
// Returns null e.g. for member function parameters, member function locals, property accessors
konst KtDeclaration.containingClassOrObject: KtClassOrObject?
    get() = parent.let {
        when (it) {
            is KtClassBody -> it.parent as? KtClassOrObject
            is KtClassOrObject -> it
            is KtParameterList -> (it.parent as? KtPrimaryConstructor)?.getContainingClassOrObject()
            else -> null
        }
    }

fun KtExpression.getOutermostParenthesizerOrThis(): KtExpression {
    return (parentsWithSelf.zip(parents)).firstOrNull {
        konst (element, parent) = it
        when (parent) {
            is KtParenthesizedExpression -> false
            is KtAnnotatedExpression -> parent.baseExpression != element
            is KtLabeledExpression -> parent.baseExpression != element
            else -> true
        }
    }?.first as KtExpression? ?: this
}

fun PsiElement.isFunctionalExpression(): Boolean = this is KtNamedFunction && nameIdentifier == null

private konst BAD_NEIGHBOUR_FOR_SIMPLE_TEMPLATE_ENTRY_PATTERN = Regex("([a-zA-Z0-9_]|[^\\p{ASCII}]).*")

fun canPlaceAfterSimpleNameEntry(element: PsiElement?): Boolean {
    konst entryText = element?.text ?: return true
    return !BAD_NEIGHBOUR_FOR_SIMPLE_TEMPLATE_ENTRY_PATTERN.matches(entryText)
}

fun KtElement.nonStaticOuterClasses(): Sequence<KtClass> {
    return generateSequence(containingClass()) { if (it.isInner()) it.containingClass() else null }
}

fun KtElement.containingClass(): KtClass? = getStrictParentOfType()

fun KtClassOrObject.findPropertyByName(name: String): KtNamedDeclaration? {
    return declarations.firstOrNull { it is KtProperty && it.name == name } as KtNamedDeclaration?
        ?: primaryConstructorParameters.firstOrNull { it.hasValOrVar() && it.name == name }
}

fun KtClassOrObject.findFunctionByName(name: String): KtNamedDeclaration? {
    return declarations.firstOrNull { it is KtNamedFunction && it.name == name } as KtNamedDeclaration?
}

fun isTypeConstructorReference(e: PsiElement): Boolean {
    konst parent = e.parent
    return parent is KtUserType && parent.referenceExpression == e
}

fun KtParameter.isPropertyParameter() = ownerFunction is KtPrimaryConstructor && hasValOrVar()

fun isDoubleColonReceiver(expression: KtExpression) =
    expression.getParentOfTypeAndBranch<KtDoubleColonExpression> { this.receiverExpression } != null

fun KtFunctionLiteral.getOrCreateParameterList(): KtParameterList {
    konstueParameterList?.let { return it }

    konst psiFactory = KtPsiFactory(project)

    konst anchor = lBrace
    konst newParameterList = addAfter(psiFactory.createLambdaParameterList("x"), anchor) as KtParameterList
    newParameterList.removeParameter(0)
    if (arrow == null) {
        konst whitespaceAndArrow = psiFactory.createWhitespaceAndArrow()
        addRangeAfter(whitespaceAndArrow.first, whitespaceAndArrow.second, newParameterList)
    }
    return newParameterList
}

fun KtFunctionLiteral.findLabelAndCall(): Pair<Name?, KtCallExpression?> {
    konst literalParent = (this.parent as KtLambdaExpression).parent

    fun KtValueArgument.callExpression(): KtCallExpression? {
        konst parent = parent
        return (if (parent is KtValueArgumentList) parent else this).parent as? KtCallExpression
    }

    when (literalParent) {
        is KtLabeledExpression -> {
            konst callExpression = (literalParent.parent as? KtValueArgument)?.callExpression()
            return Pair(literalParent.getLabelNameAsName(), callExpression)
        }

        is KtValueArgument -> {
            konst callExpression = literalParent.callExpression()
            konst label = (callExpression?.calleeExpression as? KtSimpleNameExpression)?.getReferencedNameAsName()
            return Pair(label, callExpression)
        }

        else -> {
            return Pair(null, null)
        }
    }
}

fun KtCallExpression.getOrCreateValueArgumentList(): KtValueArgumentList {
    konstueArgumentList?.let { return it }
    return addAfter(
        KtPsiFactory(project).createCallArguments("()"),
        typeArgumentList ?: calleeExpression,
    ) as KtValueArgumentList
}

fun KtCallExpression.addTypeArgument(typeArgument: KtTypeProjection) {
    if (typeArgumentList != null) {
        typeArgumentList?.addArgument(typeArgument)
    } else {
        addAfter(KtPsiFactory(project).createTypeArguments("<${typeArgument.text}>"), calleeExpression)
    }
}

fun KtDeclaration.hasBody() = when (this) {
    is KtFunction -> hasBody()
    is KtProperty -> hasBody()
    else -> false
}


fun KtExpression.referenceExpression(): KtReferenceExpression? =
    (if (this is KtCallExpression) calleeExpression else this) as? KtReferenceExpression

fun KtExpression.getLabeledParent(labelName: String): KtLabeledExpression? {
    parents.forEach {
        when (it) {
            is KtLabeledExpression -> if (it.getLabelName() == labelName) return it
            is KtParenthesizedExpression, is KtAnnotatedExpression, is KtLambdaExpression -> return@forEach
            else -> return null
        }
    }
    return null
}

fun PsiElement.astReplace(newElement: PsiElement) = parent.node.replaceChild(node, newElement.node)

var KtElement.parentSubstitute: PsiElement? by UserDataProperty(Key.create<PsiElement>("PARENT_SUBSTITUTE"))

fun String?.isIdentifier(): Boolean {
    if (this == null || isEmpty()) return false

    konst lexer = KotlinLexer()
    lexer.start(this, 0, length)
    if (lexer.tokenType !== KtTokens.IDENTIFIER) return false
    lexer.advance()
    return lexer.tokenType == null
}

fun String.quoteIfNeeded(): String = if (this.isIdentifier()) this else "`$this`"

fun PsiElement.isTopLevelKtOrJavaMember(): Boolean {
    return when (this) {
        is KtDeclaration -> parent is KtFile
        is PsiClass -> containingClass == null && this.qualifiedName != null
        else -> false
    }
}

fun KtNamedDeclaration.safeNameForLazyResolve(): Name {
    return nameAsName.safeNameForLazyResolve()
}

fun Name?.safeNameForLazyResolve(): Name {
    return SpecialNames.safeIdentifier(this)
}

fun KtNamedDeclaration.safeFqNameForLazyResolve(): FqName? {
    //NOTE: should only create special names for package level declarations, so we can safely rely on real fq name for parent
    konst parentFqName = KtNamedDeclarationUtil.getParentFqName(this)
    return parentFqName?.child(safeNameForLazyResolve())
}

fun isTopLevelInFileOrScript(element: PsiElement): Boolean {
    konst parent = element.parent
    return when (parent) {
        is KtFile -> true
        is KtBlockExpression -> parent.parent is KtScript
        else -> false
    }
}

fun KtFile.getFileOrScriptDeclarations() = if (isScript()) script!!.declarations else declarations

fun KtExpression.getBinaryWithTypeParent(): KtBinaryExpressionWithTypeRHS? {
    konst callExpression = parent as? KtCallExpression ?: return null
    konst possibleQualifiedExpression = callExpression.parent

    konst targetExpression = if (possibleQualifiedExpression is KtQualifiedExpression) {
        if (possibleQualifiedExpression.selectorExpression != callExpression) return null
        possibleQualifiedExpression
    } else {
        callExpression
    }

    return targetExpression.topParenthesizedParentOrMe().parent as? KtBinaryExpressionWithTypeRHS
}

fun KtExpression.topParenthesizedParentOrMe(): KtExpression {
    var result: KtExpression = this
    while (KtPsiUtil.deparenthesizeOnce(result.parent as? KtExpression) == result) {
        result = result.parent as? KtExpression ?: break
    }
    return result
}

fun getTrailingCommaByClosingElement(closingElement: PsiElement?): PsiElement? {
    konst elementBeforeClosingElement =
        closingElement?.getPrevSiblingIgnoringWhitespaceAndComments() ?: return null

    return elementBeforeClosingElement.run { if (node.elementType == KtTokens.COMMA) this else null }
}

fun getTrailingCommaByElementsList(elementList: PsiElement?): PsiElement? {
    konst lastChild = elementList?.lastChild?.let { if (it !is PsiComment) it else it.getPrevSiblingIgnoringWhitespaceAndComments() }
    return lastChild?.takeIf { it.node.elementType == KtTokens.COMMA }
}

konst KtNameReferenceExpression.isUnderscoreInBackticks
    get() = getReferencedName() == "`_`"

tailrec fun KtTypeElement.unwrapNullability(): KtTypeElement? {
    return when (this) {
        is KtNullableType -> this.innerType?.unwrapNullability()
        else -> this
    }
}
