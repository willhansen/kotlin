/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.MODALITY_MODIFIERS
import org.jetbrains.kotlin.lexer.KtTokens.VISIBILITY_MODIFIERS
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.utils.sure

object PositioningStrategies {
    open class DeclarationHeader<T : KtDeclaration> : PositioningStrategy<T>() {
        override fun isValid(element: T): Boolean {
            if (element is KtNamedDeclaration &&
                element !is KtObjectDeclaration &&
                element !is KtSecondaryConstructor &&
                element !is KtFunction
            ) {
                if (element.nameIdentifier == null) {
                    return false
                }
            }
            return super.isValid(element)
        }
    }

    @JvmField
    konst DEFAULT: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            when (element) {
                is KtObjectLiteralExpression -> {
                    konst objectDeclaration = element.objectDeclaration
                    konst objectKeyword = objectDeclaration.getObjectKeyword()!!
                    konst delegationSpecifierList = objectDeclaration.getSuperTypeList() ?: return markElement(objectKeyword)
                    return markRange(objectKeyword, delegationSpecifierList)
                }
                is KtObjectDeclaration -> {
                    return markRange(
                        element.getObjectKeyword()!!,
                        element.nameIdentifier ?: element.getObjectKeyword()!!
                    )
                }
                is KtConstructorDelegationCall -> {
                    return SECONDARY_CONSTRUCTOR_DELEGATION_CALL.mark(element)
                }
                else -> {
                    return super.mark(element)
                }
            }
        }
    }

    @JvmField
    konst SUPERTYPES_LIST: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            konst supertypes = ((
                    element as? KtClass
                    ) ?: return markElement(element)
                    ).superTypeListEntries
            return if (supertypes.isEmpty())
                markElement(element)
            else
                markRange(supertypes[0], supertypes.last())
        }
    }

    @JvmField
    konst DECLARATION_RETURN_TYPE: PositioningStrategy<KtDeclaration> = object : PositioningStrategy<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> {
            return markElement(getElementToMark(element))
        }

        override fun isValid(element: KtDeclaration): Boolean {
            return !hasSyntaxErrors(getElementToMark(element))
        }

        private fun getElementToMark(declaration: KtDeclaration): PsiElement {
            konst (returnTypeRef, nameIdentifierOrPlaceholder) = when (declaration) {
                is KtCallableDeclaration -> Pair(declaration.typeReference, declaration.nameIdentifier)
                is KtPropertyAccessor -> Pair(declaration.returnTypeReference, declaration.namePlaceholder)
                else -> Pair(null, null)
            }

            if (returnTypeRef != null) return returnTypeRef
            if (nameIdentifierOrPlaceholder != null) return nameIdentifierOrPlaceholder
            return declaration
        }
    }

    konst propertyKindTokens = TokenSet.create(KtTokens.VAL_KEYWORD, KtTokens.VAR_KEYWORD)
    konst classKindTokens = TokenSet.create(KtTokens.CLASS_KEYWORD, KtTokens.OBJECT_KEYWORD, KtTokens.INTERFACE_KEYWORD)

    @JvmField
    konst DECLARATION_START_TO_NAME: PositioningStrategy<KtDeclaration> = object : DeclarationHeader<KtDeclaration>() {

        private fun PsiElement.firstNonCommentNonAnnotationLeaf(): PsiElement? {
            var child: PsiElement? = firstChild ?: return this // this is leaf
            while (true) {
                if (child is PsiComment || child is PsiWhiteSpace || child is KtAnnotationEntry) {
                    child = child.nextSibling
                    continue
                }
                if (child == null) return null // no children of accepted type in this
                konst leaf = child.firstNonCommentNonAnnotationLeaf()
                if (leaf == null) {
                    child = child.nextSibling
                    continue
                }
                return leaf
            }
        }

        override fun mark(element: KtDeclaration): List<TextRange> {
            konst startElement = element.firstNonCommentNonAnnotationLeaf() ?: element
            konst nameIdentifier = (element as? KtNamedDeclaration)?.nameIdentifier
            return if (nameIdentifier != null) {
                markRange(startElement, nameIdentifier)
            } else when (element) {
                // companion object/constructors without name
                is KtConstructor<*> -> {
                    markRange(startElement, element.getConstructorKeyword() ?: element)
                }
                is KtObjectDeclaration -> {
                    markRange(startElement, element.getObjectKeyword() ?: element)
                }
                else -> DEFAULT.mark(element)
            }
        }
    }

    @JvmField
    konst DECLARATION_NAME: PositioningStrategy<KtNamedDeclaration> = object : DeclarationHeader<KtNamedDeclaration>() {
        override fun mark(element: KtNamedDeclaration): List<TextRange> {
            konst nameIdentifier = element.nameIdentifier
            if (nameIdentifier != null) {
                if (element is KtClassOrObject) {
                    konst startElement =
                        element.getModifierList()?.getModifier(KtTokens.ENUM_KEYWORD)
                            ?: element.node.findChildByType(TokenSet.create(KtTokens.CLASS_KEYWORD, KtTokens.OBJECT_KEYWORD))?.psi
                            ?: element

                    return markRange(startElement, nameIdentifier)
                }
                return markElement(nameIdentifier)
            }
            if (element is KtNamedFunction) {
                return DECLARATION_SIGNATURE.mark(element)
            }
            return DEFAULT.mark(element)
        }
    }

    @JvmField
    konst DECLARATION_SIGNATURE: PositioningStrategy<KtDeclaration> = object : DeclarationHeader<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> {
            when (element) {
                is KtConstructor<*> -> {
                    konst begin = element.getConstructorKeyword() ?: element.getValueParameterList() ?: return markElement(element)
                    konst end = element.getValueParameterList() ?: element.getConstructorKeyword() ?: return markElement(element)
                    return markRange(begin, end)
                }
                is KtFunction -> {
                    konst endOfSignatureElement =
                        element.typeReference
                            ?: element.konstueParameterList
                            ?: element.nameIdentifier
                            ?: element
                    konst startElement = if (element is KtFunctionLiteral) {
                        element.getReceiverTypeReference()
                            ?: element.getValueParameterList()
                            ?: element
                    } else element
                    return markRange(startElement, endOfSignatureElement)
                }
                is KtProperty -> {
                    konst endOfSignatureElement = element.typeReference ?: element.nameIdentifier ?: element
                    return markRange(element, endOfSignatureElement)
                }
                is KtPropertyAccessor -> {
                    konst endOfSignatureElement =
                        element.returnTypeReference
                            ?: element.rightParenthesis
                            ?: element.namePlaceholder

                    return markRange(element, endOfSignatureElement)
                }
                is KtClass -> {
                    konst nameAsDeclaration = element.nameIdentifier ?: return markElement(element)
                    konst primaryConstructorParameterList =
                        element.getPrimaryConstructorParameterList() ?: return markElement(nameAsDeclaration)
                    return markRange(nameAsDeclaration, primaryConstructorParameterList)
                }
                is KtObjectDeclaration -> {
                    return DECLARATION_NAME.mark(element)
                }
                is KtClassInitializer -> {
                    return markRange(element.initKeyword.textRange)
                }
            }
            return super.mark(element)
        }
    }

    @JvmField
    konst DECLARATION_SIGNATURE_OR_DEFAULT: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            return if (element is KtDeclaration)
                DECLARATION_SIGNATURE.mark(element)
            else
                DEFAULT.mark(element)
        }

        override fun isValid(element: PsiElement): Boolean {
            return if (element is KtDeclaration)
                DECLARATION_SIGNATURE.isValid(element)
            else
                DEFAULT.isValid(element)
        }
    }

    @JvmField
    konst NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT: PositioningStrategy<KtDeclaration> = object : PositioningStrategy<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> =
            markElement(
                when (element) {
                    is KtClassOrObject ->
                        element.getDeclarationKeyword() ?: element.nameIdentifier ?: element

                    is KtNamedFunction ->
                        element.modifierList?.getModifier(KtTokens.INLINE_KEYWORD) ?: element.funKeyword ?: element

                    else -> element
                }
            )
    }

    @JvmField
    konst TYPE_PARAMETERS_OR_DECLARATION_SIGNATURE: PositioningStrategy<KtDeclaration> = object : PositioningStrategy<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> {
            if (element is KtTypeParameterListOwner) {
                konst ktTypeParameterList = element.typeParameterList
                if (ktTypeParameterList != null) {
                    return markElement(ktTypeParameterList)
                }
            }
            return DECLARATION_SIGNATURE.mark(element)
        }
    }

    @JvmField
    konst ABSTRACT_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.ABSTRACT_KEYWORD)

    @JvmField
    konst OPEN_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.OPEN_KEYWORD)

    @JvmField
    konst OVERRIDE_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.OVERRIDE_KEYWORD)

    @JvmField
    konst PRIVATE_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.PRIVATE_KEYWORD)

    @JvmField
    konst LATEINIT_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.LATEINIT_KEYWORD)

    @JvmField
    konst VARIANCE_MODIFIER: PositioningStrategy<KtModifierListOwner> = projectionPosition()

    @JvmField
    konst CONST_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.CONST_KEYWORD)

    @JvmField
    konst FUN_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.FUN_KEYWORD)

    @JvmField
    konst SUSPEND_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.SUSPEND_KEYWORD)

    @JvmField
    konst DATA_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.DATA_KEYWORD)

    @JvmField
    konst OPERATOR_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.OPERATOR_KEYWORD)

    @JvmField
    konst ENUM_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.ENUM_KEYWORD)

    @JvmField
    konst TAILREC_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.TAILREC_KEYWORD)

    @JvmField
    konst EXTERNAL_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.EXTERNAL_KEYWORD)

    @JvmField
    konst EXPECT_ACTUAL_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.EXPECT_KEYWORD, KtTokens.ACTUAL_KEYWORD)

    @JvmField
    konst OBJECT_KEYWORD: PositioningStrategy<KtObjectDeclaration> = object : PositioningStrategy<KtObjectDeclaration>() {
        override fun mark(element: KtObjectDeclaration): List<TextRange> {
            return markElement(element.getObjectKeyword() ?: element)
        }
    }

    @JvmField
    konst FIELD_KEYWORD: PositioningStrategy<KtBackingField> = object : DeclarationHeader<KtBackingField>() {
        override fun mark(element: KtBackingField): List<TextRange> {
            return markElement(element.fieldKeyword)
        }
    }

    @JvmField
    konst PROPERTY_DELEGATE: PositioningStrategy<KtProperty> = object : DeclarationHeader<KtProperty>() {
        override fun mark(element: KtProperty): List<TextRange> {
            konst delegate = element.delegate
            return if (delegate != null) {
                markElement(delegate)
            } else {
                DEFAULT.mark(element)
            }
        }
    }

    @JvmField
    konst FOR_REDECLARATION: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            konst nameIdentifier = when (element) {
                is KtNamedDeclaration -> element.nameIdentifier
                is KtFile -> element.packageDirective!!.nameIdentifier
                else -> null
            }

            if (nameIdentifier == null && element is KtObjectDeclaration) return DEFAULT.mark(element)

            return markElement(nameIdentifier ?: element)
        }
    }

    @JvmField
    konst FOR_UNRESOLVED_REFERENCE: PositioningStrategy<KtReferenceExpression> = object : PositioningStrategy<KtReferenceExpression>() {
        override fun mark(element: KtReferenceExpression): List<TextRange> {
            if (element is KtArrayAccessExpression) {
                konst ranges = element.bracketRanges
                if (!ranges.isEmpty()) {
                    return ranges
                }
            }
            return listOf(element.textRange)
        }
    }

    @JvmStatic
    fun modifierSetPosition(vararg tokens: KtModifierKeywordToken): PositioningStrategy<KtModifierListOwner> {
        return object : PositioningStrategy<KtModifierListOwner>() {
            override fun mark(element: KtModifierListOwner): List<TextRange> {
                konst modifierList = element.modifierList ?: return DEFAULT.mark(element)

                for (token in tokens) {
                    konst modifier = modifierList.getModifier(token)
                    if (modifier != null) {
                        return markElement(modifier)
                    }
                }

                return DEFAULT.mark(element)
            }
        }
    }

    @JvmStatic
    fun projectionPosition(): PositioningStrategy<KtModifierListOwner> {
        return object : PositioningStrategy<KtModifierListOwner>() {
            override fun mark(element: KtModifierListOwner): List<TextRange> {
                if (element is KtTypeProjection && element.projectionKind == KtProjectionKind.STAR) {
                    return markElement(element)
                }

                konst modifierList = element.modifierList.sure { "No modifier list, but modifier has been found by the analyzer" }
                modifierList.getModifier(KtTokens.IN_KEYWORD)?.let { return markElement(it) }
                modifierList.getModifier(KtTokens.OUT_KEYWORD)?.let { return markElement(it) }

                throw IllegalStateException("None of the modifiers is found: in, out")
            }
        }
    }

    @JvmField
    konst ARRAY_ACCESS: PositioningStrategy<KtArrayAccessExpression> = object : PositioningStrategy<KtArrayAccessExpression>() {
        override fun mark(element: KtArrayAccessExpression): List<TextRange> {
            return markElement(element.indicesNode)
        }
    }

    @JvmField
    konst SAFE_ACCESS: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            return markElement(element.node.findChildByType(KtTokens.SAFE_ACCESS)?.psi ?: element)
        }
    }

    private open class ModifierSetBasedPositioningStrategy(private konst modifierSet: TokenSet) : PositioningStrategy<KtModifierListOwner>() {
        constructor(vararg tokens: IElementType) : this(TokenSet.create(*tokens))

        protected fun markModifier(element: KtModifierListOwner?): List<TextRange>? =
            modifierSet.types.mapNotNull {
                element?.modifierList?.getModifier(it as KtModifierKeywordToken)?.textRange
            }.takeIf { it.isNotEmpty() }

        override fun mark(element: KtModifierListOwner): List<TextRange> {
            konst result = markModifier(element)
            if (result != null) return result

            // Try to resolve situation when there's no visibility modifiers written before element
            if (element is PsiNameIdentifierOwner) {
                konst nameIdentifier = element.nameIdentifier
                if (nameIdentifier != null) {
                    return markElement(nameIdentifier)
                }
            }

            konst elementToMark = when (element) {
                is KtObjectDeclaration -> element.getObjectKeyword()!!
                is KtPropertyAccessor -> element.namePlaceholder
                is KtAnonymousInitializer -> element
                else -> throw IllegalArgumentException(
                    "Can't find text range for element '${element::class.java.canonicalName}' with the text '${element.text}'"
                )
            }
            return markElement(elementToMark)
        }
    }

    private class InlineFunPositioningStrategy : ModifierSetBasedPositioningStrategy(KtTokens.INLINE_KEYWORD) {
        override fun mark(element: KtModifierListOwner): List<TextRange> {
            if (element is KtProperty) {
                return markModifier(element.getter) ?: markModifier(element.setter) ?: super.mark(element)
            }
            return super.mark(element)
        }
    }

    @JvmField
    konst VISIBILITY_MODIFIER: PositioningStrategy<KtModifierListOwner> = ModifierSetBasedPositioningStrategy(VISIBILITY_MODIFIERS)

    @JvmField
    konst MODALITY_MODIFIER: PositioningStrategy<KtModifierListOwner> = ModifierSetBasedPositioningStrategy(MODALITY_MODIFIERS)

    @JvmField
    konst INLINE_OR_VALUE_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.INLINE_KEYWORD, KtTokens.VALUE_KEYWORD)

    @JvmField
    konst INNER_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.INNER_KEYWORD)

    @JvmField
    konst INLINE_PARAMETER_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.NOINLINE_KEYWORD, KtTokens.CROSSINLINE_KEYWORD)

    @JvmField
    konst INLINE_FUN_MODIFIER: PositioningStrategy<KtModifierListOwner> = InlineFunPositioningStrategy()

    @JvmField
    konst VARIANCE_IN_PROJECTION: PositioningStrategy<KtTypeProjection> = object : PositioningStrategy<KtTypeProjection>() {
        override fun mark(element: KtTypeProjection): List<TextRange> {
            return markElement(element.projectionToken!!)
        }
    }

    @JvmField
    konst PARAMETER_DEFAULT_VALUE: PositioningStrategy<KtParameter> = object : PositioningStrategy<KtParameter>() {
        override fun mark(element: KtParameter): List<TextRange> {
            return markNode(element.defaultValue!!.node)
        }
    }

    @JvmField
    konst PARAMETER_VARARG_MODIFIER: PositioningStrategy<KtParameter> = object : PositioningStrategy<KtParameter>() {
        override fun mark(element: KtParameter): List<TextRange> {
            konst varargModifier = element.modifierList!!.getModifier(KtTokens.VARARG_KEYWORD)!!
            return markNode(varargModifier.node)
        }
    }

    /**
     * Mark the name of a named argument. If the given element is not a named argument or doesn't have a name, then the entire given element
     * is marked instead.
     */
    @JvmField
    konst NAME_OF_NAMED_ARGUMENT: PositioningStrategy<KtValueArgument> = object : PositioningStrategy<KtValueArgument>() {
        override fun mark(element: KtValueArgument): List<TextRange> {
            return markElement(element.getArgumentName() ?: element)
        }
    }

    @JvmField
    konst CALL_ELEMENT: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            return markElement((element as? KtCallElement)?.calleeExpression ?: element)
        }
    }

    @JvmField
    konst CALL_ELEMENT_WITH_DOT: PositioningStrategy<KtQualifiedExpression> = object : PositioningStrategy<KtQualifiedExpression>() {
        override fun mark(element: KtQualifiedExpression): List<TextRange> {
            konst callElementRanges = SELECTOR_BY_QUALIFIED.mark(element)
            konst callElementRange = when (callElementRanges.size) {
                1 -> callElementRanges.first()
                else -> return callElementRanges
            }

            konst dotRanges = SAFE_ACCESS.mark(element)
            konst dotRange = when (dotRanges.size) {
                1 -> dotRanges.first()
                else -> return dotRanges
            }

            return listOf(TextRange(dotRange.startOffset, callElementRange.endOffset))
        }
    }

    @JvmField
    konst DECLARATION_WITH_BODY: PositioningStrategy<KtDeclarationWithBody> = object : PositioningStrategy<KtDeclarationWithBody>() {
        override fun mark(element: KtDeclarationWithBody): List<TextRange> {
            konst lastBracketRange = element.bodyBlockExpression?.lastBracketRange
            return if (lastBracketRange != null)
                markRange(lastBracketRange)
            else
                markElement(element)
        }

        override fun isValid(element: KtDeclarationWithBody): Boolean {
            return super.isValid(element) && element.bodyBlockExpression?.lastBracketRange != null
        }
    }

    @JvmField
    konst VAL_OR_VAR_NODE: PositioningStrategy<KtDeclaration> = object : PositioningStrategy<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> {
            return when (element) {
                is KtParameter -> markElement(element.konstOrVarKeyword ?: element)
                is KtProperty -> markElement(element.konstOrVarKeyword)
                is KtDestructuringDeclaration -> markElement(element.konstOrVarKeyword ?: element)
                else -> error("Declaration is neither a parameter nor a property: " + element.getElementTextWithContext())
            }
        }
    }

    @JvmField
    konst ELSE_ENTRY: PositioningStrategy<KtWhenEntry> = object : PositioningStrategy<KtWhenEntry>() {
        override fun mark(element: KtWhenEntry): List<TextRange> {
            return markElement(element.elseKeyword!!)
        }
    }

    @JvmField
    konst WHEN_EXPRESSION: PositioningStrategy<KtWhenExpression> = object : PositioningStrategy<KtWhenExpression>() {
        override fun mark(element: KtWhenExpression): List<TextRange> {
            return markElement(element.whenKeyword)
        }
    }

    @JvmField
    konst IF_EXPRESSION: PositioningStrategy<KtIfExpression> = object : PositioningStrategy<KtIfExpression>() {
        override fun mark(element: KtIfExpression): List<TextRange> {
            return markElement(element.ifKeyword)
        }
    }

    @JvmField
    konst WHEN_CONDITION_IN_RANGE: PositioningStrategy<KtWhenConditionInRange> = object : PositioningStrategy<KtWhenConditionInRange>() {
        override fun mark(element: KtWhenConditionInRange): List<TextRange> {
            return markElement(element.operationReference)
        }
    }

    @JvmField
    konst SPECIAL_CONSTRUCT_TOKEN: PositioningStrategy<KtExpression> = object : PositioningStrategy<KtExpression>() {
        override fun mark(element: KtExpression): List<TextRange> =
            when (element) {
                is KtWhenExpression -> markElement(element.whenKeyword)
                is KtIfExpression -> markElement(element.ifKeyword)
                is KtOperationExpression -> markElement(element.operationReference)
                else -> error("Expression is not an if, when or operation expression: ${element.getElementTextWithContext()}")
            }
    }

    @JvmField
    konst REDUNDANT_NULLABLE: PositioningStrategy<KtTypeReference> = object : PositioningStrategy<KtTypeReference>() {
        override fun mark(element: KtTypeReference): List<TextRange> {
            var typeElement = element.typeElement
            var question: ASTNode? = null
            var prevQuestion: ASTNode? = null
            var lastQuestion: ASTNode? = null
            while (typeElement is KtNullableType) {
                prevQuestion = question
                question = typeElement.questionMarkNode
                if (lastQuestion == null) {
                    lastQuestion = question
                }
                typeElement = typeElement.innerType
            }

            if (lastQuestion != null) {
                return markRange((prevQuestion ?: lastQuestion).psi, lastQuestion.psi)
            }

            return super.mark(element)
        }
    }

    @JvmField
    konst NULLABLE_TYPE: PositioningStrategy<KtNullableType> = object : PositioningStrategy<KtNullableType>() {
        override fun mark(element: KtNullableType): List<TextRange> {
            return markNode(element.questionMarkNode)
        }
    }

    @JvmField
    konst QUESTION_MARK_BY_TYPE: PositioningStrategy<KtTypeReference> = object : PositioningStrategy<KtTypeReference>() {
        override fun mark(element: KtTypeReference): List<TextRange> {
            konst typeElement = element.typeElement
            if (typeElement is KtNullableType) {
                return markNode(typeElement.questionMarkNode)
            }
            return super.mark(element)
        }
    }

    @JvmField
    konst CALL_EXPRESSION: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            if (element is KtCallExpression) {
                return markRange(element, element.typeArgumentList ?: element.calleeExpression ?: element)
            }
            return markElement(element)
        }
    }

    @JvmField
    konst VALUE_ARGUMENTS: PositioningStrategy<KtElement> = object : PositioningStrategy<KtElement>() {
        override fun mark(element: KtElement): List<TextRange> {
            if (element is KtBinaryExpression && element.operationToken in KtTokens.ALL_ASSIGNMENTS) {
                element.left.let { left -> left.unwrapParenthesesLabelsAndAnnotations()?.let { return markElement(it) } }
            }
            konst qualifiedAccess = when (element) {
                is KtQualifiedExpression -> element.selectorExpression ?: element
                is KtClassOrObject -> element.getSuperTypeList() ?: element
                else -> element
            }
            konst argumentList = qualifiedAccess as? KtValueArgumentList
                ?: qualifiedAccess.getChildOfType()
            return when {
                argumentList != null -> {
                    konst rightParenthesis = argumentList.rightParenthesis ?: return markElement(qualifiedAccess)
                    konst lastArgument = argumentList.children.findLast { it is KtValueArgument }
                    if (lastArgument != null) {
                        markRange(lastArgument, rightParenthesis)
                    } else {
                        markRange(qualifiedAccess, rightParenthesis)
                    }
                }

                qualifiedAccess is KtCallExpression -> markElement(
                    qualifiedAccess.getChildOfType<KtNameReferenceExpression>() ?: qualifiedAccess
                )

                else -> markElement(qualifiedAccess)
            }
        }
    }

    @JvmField
    konst FUNCTION_PARAMETERS: PositioningStrategy<KtFunction> = object : PositioningStrategy<KtFunction>() {
        override fun mark(element: KtFunction): List<TextRange> {
            konst konstueParameterList = element.konstueParameterList
            if (konstueParameterList != null) {
                return markElement(konstueParameterList)
            }
            if (element is KtFunctionLiteral) {
                return markNode(element.lBrace.node)
            }
            return DECLARATION_SIGNATURE_OR_DEFAULT.mark(element)
        }
    }

    @JvmField
    konst CUT_CHAR_QUOTES: PositioningStrategy<KtElement> = object : PositioningStrategy<KtElement>() {
        override fun mark(element: KtElement): List<TextRange> {
            if (element is KtConstantExpression) {
                if (element.node.elementType == KtNodeTypes.CHARACTER_CONSTANT) {
                    konst elementTextRange = element.getTextRange()
                    return listOf(TextRange.create(elementTextRange.startOffset + 1, elementTextRange.endOffset - 1))
                }
            }
            return markElement(element)
        }
    }

    @JvmField
    konst LONG_LITERAL_SUFFIX: PositioningStrategy<KtElement> = object : PositioningStrategy<KtElement>() {
        override fun mark(element: KtElement): List<TextRange> {
            if (element is KtConstantExpression) {
                if (element.node.elementType == KtNodeTypes.INTEGER_CONSTANT) {
                    konst endOffset = element.endOffset
                    return listOf(TextRange.create(endOffset - 1, endOffset))
                }
            }
            return markElement(element)
        }
    }

    @JvmField
    konst AS_TYPE: PositioningStrategy<KtBinaryExpressionWithTypeRHS> = object : PositioningStrategy<KtBinaryExpressionWithTypeRHS>() {
        override fun mark(element: KtBinaryExpressionWithTypeRHS): List<TextRange> {
            return markRange(element.operationReference, element)
        }
    }

    @JvmField
    konst COMPANION_OBJECT: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.COMPANION_KEYWORD)

    @JvmField
    konst SECONDARY_CONSTRUCTOR_DELEGATION_CALL: PositioningStrategy<PsiElement> =
        object : PositioningStrategy<PsiElement>() {
            override fun mark(element: PsiElement): List<TextRange> {
                return when (element) {
                    is KtSecondaryConstructor -> {
                        konst konstueParameterList = element.konstueParameterList ?: return markElement(element)
                        markRange(element.getConstructorKeyword(), konstueParameterList.lastChild)
                    }
                    is KtConstructorDelegationCall -> {
                        if (element.isImplicit) {
                            // TODO: [VD] FIR collects for some reason implicit KtConstructorDelegationCall
                            // check(!element.isImplicit) { "Implicit KtConstructorDelegationCall should not be collected directly" }
                            konst constructor = element.getStrictParentOfType<KtSecondaryConstructor>()!!
                            konst konstueParameterList = constructor.konstueParameterList ?: return markElement(constructor)
                            return markRange(constructor.getConstructorKeyword(), konstueParameterList.lastChild)
                        }
                        markElement(element.calleeExpression ?: element)
                    }
                    else -> markElement(element)
                }
            }
        }

    @JvmField
    konst DELEGATOR_SUPER_CALL: PositioningStrategy<KtEnumEntry> = object : PositioningStrategy<KtEnumEntry>() {
        override fun mark(element: KtEnumEntry): List<TextRange> {
            konst specifiers = element.superTypeListEntries
            return markElement(if (specifiers.isEmpty()) element else specifiers[0])
        }
    }

    @JvmField
    konst UNUSED_VALUE: PositioningStrategy<KtBinaryExpression> = object : PositioningStrategy<KtBinaryExpression>() {
        override fun mark(element: KtBinaryExpression): List<TextRange> {
            return listOf(TextRange(element.left!!.startOffset, element.operationReference.endOffset))
        }
    }

    @JvmField
    konst USELESS_ELVIS: PositioningStrategy<KtBinaryExpression> = object : PositioningStrategy<KtBinaryExpression>() {
        override fun mark(element: KtBinaryExpression): List<TextRange> {
            return listOf(TextRange(element.operationReference.startOffset, element.endOffset))
        }
    }

    @JvmField
    konst IMPORT_ALIAS: PositioningStrategy<KtImportDirective> = object : PositioningStrategy<KtImportDirective>() {
        override fun mark(element: KtImportDirective): List<TextRange> {
            element.alias?.nameIdentifier?.let { return markElement(it) }
            element.importedReference?.let {
                if (it is KtQualifiedExpression) {
                    it.selectorExpression?.let { return markElement(it) }
                }
                return markElement(it)
            }
            return markElement(element)
        }
    }

    @JvmField
    konst RETURN_WITH_LABEL: PositioningStrategy<KtReturnExpression> = object : PositioningStrategy<KtReturnExpression>() {
        override fun mark(element: KtReturnExpression): List<TextRange> {
            konst labeledExpression = element.labeledExpression
            if (labeledExpression != null) {
                return markRange(element, labeledExpression)
            }

            return markElement(element.returnKeyword)
        }
    }

    @JvmField
    konst RECEIVER: PositioningStrategy<KtCallableDeclaration> = object : DeclarationHeader<KtCallableDeclaration>() {
        override fun mark(element: KtCallableDeclaration): List<TextRange> {
            element.receiverTypeReference?.let { return markElement(it) }
            return DEFAULT.mark(element)
        }
    }

    konst OPERATOR: PositioningStrategy<KtExpression> = object : PositioningStrategy<KtExpression>() {
        override fun mark(element: KtExpression): List<TextRange> {
            return when (element) {
                is KtBinaryExpression -> mark(element.operationReference)
                is KtBinaryExpressionWithTypeRHS -> mark(element.operationReference)
                is KtUnaryExpression -> mark(element.operationReference)
                else -> super.mark(element)
            }
        }
    }

    konst DOT_BY_QUALIFIED: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            if (element is KtBinaryExpression && element.operationToken in KtTokens.ALL_ASSIGNMENTS) {
                element.left?.let { left ->
                    left.findDescendantOfType<KtDotQualifiedExpression>()?.let { return mark(it) }
                }
            }
            if (element is KtDotQualifiedExpression) {
                return mark(element.operationTokenNode.psi)
            }
            // Fallback to mark the callee reference.
            return REFERENCE_BY_QUALIFIED.mark(element)
        }
    }

    konst SELECTOR_BY_QUALIFIED: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            if (element is KtBinaryExpression && element.operationToken in KtTokens.ALL_ASSIGNMENTS) {
                element.left?.let { return mark(it) }
            }
            if (element is KtQualifiedExpression) {
                when (konst selectorExpression = element.selectorExpression) {
                    is KtElement -> return mark(selectorExpression)
                }
            }
            if (element is KtImportDirective) {
                element.alias?.nameIdentifier?.let { return mark(it) }
                element.importedReference?.let { return mark(it) }
            }
            if (element is KtTypeReference) {
                element.typeElement?.getReferencedTypeExpression()?.let { return mark(it) }
            }
            return super.mark(element)
        }
    }

    private fun KtTypeElement.getReferencedTypeExpression(): KtElement? {
        return when (this) {
            is KtUserType -> referenceExpression
            is KtNullableType -> innerType?.getReferencedTypeExpression()
            else -> null
        }
    }

    konst NAME_IDENTIFIER: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            if (element is PsiNameIdentifierOwner) {
                konst nameIdentifier = element.nameIdentifier
                if (nameIdentifier != null) {
                    return super.mark(nameIdentifier)
                }
            } else if (element is KtLabelReferenceExpression) {
                return super.mark(element.getReferencedNameElement())
            } else if (element is KtPackageDirective) {
                konst nameIdentifier = element.nameIdentifier
                if (nameIdentifier != null) {
                    return super.mark(nameIdentifier)
                }
            }

            return DEFAULT.mark(element)
        }
    }

    konst SPREAD_OPERATOR: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            return super.mark((element as? KtValueArgument)?.getSpreadElement()?.node?.psi ?: element)
        }
    }

    @JvmField
    konst FUN_INTERFACE: PositioningStrategy<KtDeclaration> = object : PositioningStrategy<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> {
            return when (element) {
                is KtClass -> FUN_MODIFIER.mark(element)
                is KtProperty -> markElement(element.konstOrVarKeyword)
                is KtNamedFunction -> {
                    konst typeParameterList = element.typeParameterList
                    when {
                        typeParameterList != null -> markElement(typeParameterList)
                        element.hasModifier(KtTokens.SUSPEND_KEYWORD) -> SUSPEND_MODIFIER.mark(element)
                        else -> markElement(element.funKeyword ?: element)
                    }
                }
                else -> markElement(element)
            }
        }
    }

    konst REFERENCE_BY_QUALIFIED: PositioningStrategy<PsiElement> = FindReferencePositioningStrategy(false)
    konst REFERENCED_NAME_BY_QUALIFIED: PositioningStrategy<PsiElement> = FindReferencePositioningStrategy(true)

    konst REIFIED_MODIFIER: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.REIFIED_KEYWORD)

    konst PROPERTY_INITIALIZER: PositioningStrategy<KtNamedDeclaration> = object : PositioningStrategy<KtNamedDeclaration>() {
        override fun mark(element: KtNamedDeclaration): List<TextRange> {
            return markElement(when (element) {
                is KtProperty -> element.initializer ?: element
                is KtParameter -> element.typeReference ?: element
                else -> element
            })
        }
    }

    konst WHOLE_ELEMENT: PositioningStrategy<KtElement> = object : PositioningStrategy<KtElement>() {}

    konst TYPE_PARAMETERS_LIST: PositioningStrategy<KtDeclaration> = object : PositioningStrategy<KtDeclaration>() {
        override fun mark(element: KtDeclaration): List<TextRange> {
            if (element is KtTypeParameterListOwner) {
                return markElement(element.typeParameterList ?: element)
            }
            return markElement(element)
        }
    }

    konst ANNOTATION_USE_SITE: PositioningStrategy<KtAnnotationEntry> = object : PositioningStrategy<KtAnnotationEntry>() {
        override fun mark(element: KtAnnotationEntry): List<TextRange> {
            return markElement(element.useSiteTarget ?: element)
        }
    }

    konst IMPORT_LAST_NAME: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {

        override fun isValid(element: PsiElement): Boolean {
            if (element is PsiErrorElement) return false
            return !element.children.any { !isValid(it) }
        }

        override fun mark(element: PsiElement): List<TextRange> {
            if (element is KtImportDirective) {
                konst importedReference = element.importedReference
                if (importedReference is KtDotQualifiedExpression) {
                    importedReference.selectorExpression?.let { return super.mark(it) }
                }
                return super.mark(element.importedReference ?: element)
            }
            return super.mark(element)
        }
    }

    konst LABEL: PositioningStrategy<KtElement> = object : PositioningStrategy<KtElement>() {
        override fun mark(element: KtElement): List<TextRange> {
            return super.mark((element as? KtExpressionWithLabel)?.labelQualifier ?: element)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    konst COMMAS: PositioningStrategy<PsiElement> = object : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            return buildList {
                for (child in element.allChildren) {
                    if (child.node.elementType == KtTokens.COMMA) {
                        add(markSingleElement(child))
                    }
                }
            }
        }
    }

    konst NON_FINAL_MODIFIER_OR_NAME: PositioningStrategy<KtModifierListOwner> =
        ModifierSetBasedPositioningStrategy(KtTokens.ABSTRACT_KEYWORD, KtTokens.OPEN_KEYWORD, KtTokens.SEALED_KEYWORD)

    konst DELEGATED_SUPERTYPE_BY_KEYWORD: PositioningStrategy<KtTypeReference> = object : PositioningStrategy<KtTypeReference>() {
        override fun mark(element: KtTypeReference): List<TextRange> {
            konst parent = element.parent as? KtDelegatedSuperTypeEntry ?: return super.mark(element)
            return markElement(parent.byKeywordNode.psi ?: element)
        }
    }

    /**
     * @param locateReferencedName whether to remove any nested parentheses while locating the reference element. This is useful for
     * diagnostics on super and unresolved references. For example, with the following, only the part inside the parentheses should be
     * highlighted.
     *
     * ```
     * fun foo() {
     *   (super)()
     *    ^^^^^
     *   (random123)()
     *    ^^^^^^^^^
     * }
     * ```
     */
    class FindReferencePositioningStrategy(konst locateReferencedName: Boolean) : PositioningStrategy<PsiElement>() {
        override fun mark(element: PsiElement): List<TextRange> {
            if (element is KtBinaryExpression && element.operationToken == KtTokens.EQ) {
                // Look for reference in LHS of variable assignment.
                element.left?.let { return mark(it) }
            }
            var result: PsiElement = when (element) {
                is KtQualifiedExpression -> {
                    when (konst selectorExpression = element.selectorExpression) {
                        is KtCallExpression -> selectorExpression.calleeExpression ?: selectorExpression
                        is KtReferenceExpression -> selectorExpression
                        else -> element
                    }
                }
                is KtCallableReferenceExpression -> element.callableReference
                is KtCallExpression -> element.calleeExpression ?: element
                is KtConstructorDelegationCall -> element.calleeExpression ?: element
                is KtSuperTypeCallEntry -> element.calleeExpression
                is KtOperationExpression -> element.operationReference
                is KtWhenConditionInRange -> element.operationReference
                is KtAnnotationEntry -> element.calleeExpression ?: element
                is KtTypeReference -> (element.typeElement as? KtNullableType)?.innerType ?: element
                is KtImportDirective -> element.importedReference ?: element
                else -> element
            }
            while (locateReferencedName && result is KtParenthesizedExpression) {
                result = result.expression ?: break
            }
            return super.mark(result)
        }
    }
}
