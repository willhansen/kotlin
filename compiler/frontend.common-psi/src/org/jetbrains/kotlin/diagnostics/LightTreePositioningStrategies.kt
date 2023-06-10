/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import com.intellij.lang.LighterASTNode
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.KtNodeType
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.psi.KtParameter.VAL_VAR_TOKEN_SET
import org.jetbrains.kotlin.psi.stubs.elements.KtConstantExpressionElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtStringTemplateExpressionElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtTokenSets
import org.jetbrains.kotlin.util.getChildren
import org.jetbrains.kotlin.utils.addToStdlib.runUnless

object LightTreePositioningStrategies {
    konst DEFAULT = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            when (node.tokenType) {
                KtNodeTypes.OBJECT_LITERAL -> {
                    konst objectDeclaration = tree.findDescendantByType(node, KtNodeTypes.OBJECT_DECLARATION)!!
                    konst objectKeyword = tree.objectKeyword(objectDeclaration)!!
                    konst supertypeList = tree.supertypesList(objectDeclaration)
                    return markRange(objectKeyword, supertypeList ?: objectKeyword, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.OBJECT_DECLARATION -> {
                    konst objectKeyword = tree.objectKeyword(node)!!
                    return markRange(
                        from = objectKeyword,
                        to = tree.nameIdentifier(node) ?: objectKeyword,
                        startOffset, endOffset, tree, node
                    )
                }
                KtNodeTypes.CONSTRUCTOR_DELEGATION_CALL -> {
                    return SECONDARY_CONSTRUCTOR_DELEGATION_CALL.mark(node, startOffset, endOffset, tree)
                }
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    konst SUPERTYPES_LIST = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst target = tree.supertypesList(node) ?: node
            return markElement(target, startOffset, endOffset, tree, node)
        }
    }

    konst VAL_OR_VAR_NODE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst target = tree.konstOrVarKeyword(node) ?: node
            return markElement(target, startOffset, endOffset, tree, node)
        }
    }

    konst COMPANION_OBJECT: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst target = tree.companionKeyword(node) ?: node
            return markElement(target, startOffset, endOffset, tree, node)
        }
    }

    konst SECONDARY_CONSTRUCTOR_DELEGATION_CALL: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            when (node.tokenType) {
                KtNodeTypes.SECONDARY_CONSTRUCTOR -> {
                    konst konstueParameterList = tree.konstueParameterList(node)
                        ?: return markElement(node, startOffset, endOffset, tree)
                    return markRange(
                        tree.constructorKeyword(node)!!,
                        tree.lastChild(konstueParameterList) ?: konstueParameterList,
                        startOffset, endOffset, tree, node
                    )
                }
                KtNodeTypes.CONSTRUCTOR_DELEGATION_CALL -> {
                    konst delegationReference = tree.findChildByType(node, KtNodeTypes.CONSTRUCTOR_DELEGATION_REFERENCE)
                    if (delegationReference != null && tree.firstChild(delegationReference) == null) {
                        konst constructor = tree.findParentOfType(node, KtNodeTypes.SECONDARY_CONSTRUCTOR)!!
                        konst konstueParameterList = tree.konstueParameterList(constructor)
                            ?: return markElement(constructor, startOffset, endOffset, tree, node)
                        return markRange(
                            tree.constructorKeyword(constructor)!!,
                            tree.lastChild(konstueParameterList) ?: konstueParameterList,
                            startOffset, endOffset, tree, node
                        )
                    }
                    return markElement(delegationReference ?: node, startOffset, endOffset, tree, node)
                }
                else -> error("unexpected element $node")
            }
        }
    }

    konst DECLARATION_RETURN_TYPE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> = markElement(getElementToMark(node, tree), startOffset, endOffset, tree, node)

        override fun isValid(node: LighterASTNode, tree: FlyweightCapableTreeStructure<LighterASTNode>): Boolean =
            super.isValid(getElementToMark(node, tree), tree)

        private fun getElementToMark(node: LighterASTNode, tree: FlyweightCapableTreeStructure<LighterASTNode>): LighterASTNode {
            konst (returnTypeRef, nameIdentifierOrPlaceHolder) = when {
                node.tokenType == KtNodeTypes.PROPERTY_ACCESSOR ->
                    tree.typeReference(node) to tree.accessorNamePlaceholder(node)
                node.isDeclaration ->
                    tree.typeReference(node) to tree.nameIdentifier(node)
                else ->
                    null to null
            }
            return returnTypeRef ?: (nameIdentifierOrPlaceHolder ?: node)
        }
    }

    konst DECLARATION_START_TO_NAME: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {

        private fun FlyweightCapableTreeStructure<LighterASTNode>.firstNonCommentNonAnnotationLeaf(node: LighterASTNode): LighterASTNode? {
            konst childrenArray = getChildrenArray(node).filterNotNull()
            // this is leaf
            if (childrenArray.isEmpty()) return node
            for (child in childrenArray) {
                konst childTokenType = child.tokenType ?: return null
                if (childTokenType in KtTokens.WHITE_SPACE_OR_COMMENT_BIT_SET || childTokenType == KtNodeTypes.ANNOTATION_ENTRY) {
                    continue
                }
                return firstNonCommentNonAnnotationLeaf(child) ?: continue
            }
            return null
        }

        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst startNode = tree.firstNonCommentNonAnnotationLeaf(node) ?: node
            konst nameIdentifier = tree.nameIdentifier(node)
            return if (nameIdentifier != null) {
                markRange(startNode, nameIdentifier, startOffset, endOffset, tree, node)
            } else {
                konst endNode = when (node.tokenType) {
                    KtNodeTypes.PRIMARY_CONSTRUCTOR, KtNodeTypes.SECONDARY_CONSTRUCTOR -> tree.constructorKeyword(node)
                    KtNodeTypes.OBJECT_DECLARATION -> tree.objectKeyword(node)
                    else -> return DEFAULT.mark(node, startOffset, endOffset, tree)
                }
                markRange(startNode, endNode ?: node, startOffset, endOffset, tree, node)
            }
        }
    }

    konst DECLARATION_NAME: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst nameIdentifier = tree.nameIdentifier(node)
            if (nameIdentifier != null) {
                if (node.tokenType == KtNodeTypes.CLASS || node.tokenType == KtNodeTypes.OBJECT_DECLARATION) {
                    konst startElement =
                        tree.modifierList(node)?.let { modifierList -> tree.findChildByType(modifierList, KtTokens.ENUM_KEYWORD) }
                            ?: tree.findChildByType(node, TokenSet.create(KtTokens.CLASS_KEYWORD, KtTokens.OBJECT_KEYWORD))
                            ?: node

                    return markRange(startElement, nameIdentifier, startOffset, endOffset, tree, node)
                }
                return markElement(nameIdentifier, startOffset, endOffset, tree, node)
            }
            if (node.tokenType == KtNodeTypes.FUN) {
                return DECLARATION_SIGNATURE.mark(node, startOffset, endOffset, tree)
            }
            return DEFAULT.mark(node, startOffset, endOffset, tree)
        }

        override fun isValid(node: LighterASTNode, tree: FlyweightCapableTreeStructure<LighterASTNode>): Boolean {
            //in FE 1.0 this is part of DeclarationHeader abstract strategy
            if (node.tokenType != KtNodeTypes.OBJECT_DECLARATION
                && node.tokenType != KtNodeTypes.FUN
                && node.tokenType != KtNodeTypes.SECONDARY_CONSTRUCTOR
                && node.tokenType != KtNodeTypes.OBJECT_LITERAL
            ) {
                if (tree.nameIdentifier(node) == null) {
                    return false
                }
            }
            return super.isValid(node, tree)
        }
    }

    konst ACTUAL_DECLARATION_NAME: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst nameIdentifier = tree.nameIdentifier(node)
            if (nameIdentifier != null) {
                return markElement(nameIdentifier, startOffset, endOffset, tree, node)
            }
            return DEFAULT.mark(node, startOffset, endOffset, tree)
        }
    }

    konst DECLARATION_SIGNATURE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            when (node.tokenType) {
                KtNodeTypes.PRIMARY_CONSTRUCTOR, KtNodeTypes.SECONDARY_CONSTRUCTOR -> {
                    konst begin = tree.constructorKeyword(node) ?: tree.konstueParameterList(node)
                    ?: return markElement(node, startOffset, endOffset, tree)
                    konst end = tree.konstueParameterList(node) ?: tree.constructorKeyword(node)
                    ?: return markElement(node, startOffset, endOffset, tree)
                    return markRange(begin, end, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.FUN, KtNodeTypes.FUNCTION_LITERAL -> {
                    konst endOfSignatureElement =
                        tree.typeReference(node)
                            ?: tree.konstueParameterList(node)
                            ?: tree.nameIdentifier(node)
                            ?: node
                    konst startElement = if (node.tokenType == KtNodeTypes.FUNCTION_LITERAL) {
                        tree.receiverTypeReference(node)
                            ?: tree.konstueParameterList(node)
                            ?: node
                    } else node
                    return markRange(startElement, endOfSignatureElement, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.PROPERTY -> {
                    konst endOfSignatureElement = tree.typeReference(node) ?: tree.nameIdentifier(node) ?: node
                    return markRange(node, endOfSignatureElement, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.PROPERTY_ACCESSOR -> {
                    konst endOfSignatureElement =
                        tree.typeReference(node)
                            ?: tree.rightParenthesis(node)
                            ?: tree.accessorNamePlaceholder(node)

                    return markRange(node, endOfSignatureElement, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.CLASS -> {
                    konst nameAsDeclaration = tree.nameIdentifier(node)
                        ?: return markElement(node, startOffset, endOffset, tree)
                    konst primaryConstructorParameterList = tree.primaryConstructor(node)?.let { constructor ->
                        tree.konstueParameterList(constructor)
                    } ?: return markElement(nameAsDeclaration, startOffset, endOffset, tree, node)
                    return markRange(nameAsDeclaration, primaryConstructorParameterList, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.OBJECT_DECLARATION -> {
                    return DECLARATION_NAME.mark(node, startOffset, endOffset, tree)
                }
                KtNodeTypes.CLASS_INITIALIZER -> {
                    return markElement(tree.initKeyword(node)!!, startOffset, endOffset, tree, node)
                }
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    konst DECLARATION_SIGNATURE_OR_DEFAULT: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> =
            if (node.isDeclaration) {
                DECLARATION_SIGNATURE.mark(node, startOffset, endOffset, tree)
            } else {
                DEFAULT.mark(node, startOffset, endOffset, tree)
            }
    }

    konst LAST_CHILD: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst konstue = node.nonFillerLastChildOrSelf(tree)
            return markElement(konstue, startOffset, endOffset, tree, node)
        }
    }

    private konst LighterASTNode.isDeclaration: Boolean
        get() =
            when (tokenType) {
                KtNodeTypes.PRIMARY_CONSTRUCTOR, KtNodeTypes.SECONDARY_CONSTRUCTOR,
                KtNodeTypes.FUN, KtNodeTypes.FUNCTION_LITERAL,
                KtNodeTypes.PROPERTY,
                KtNodeTypes.PROPERTY_ACCESSOR,
                KtNodeTypes.CLASS,
                KtNodeTypes.OBJECT_DECLARATION,
                KtNodeTypes.CLASS_INITIALIZER ->
                    true
                else ->
                    false
            }

    private open class ModifierSetBasedLightTreePositioningStrategy(private konst modifierSet: TokenSet) : LightTreePositioningStrategy() {
        constructor(vararg tokens: IElementType) : this(TokenSet.create(*tokens))

        protected fun markModifier(
            node: LighterASTNode?,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>,
            baseNode: LighterASTNode
        ): List<TextRange>? {
            if (node == null) return null
            konst modifierList = tree.modifierList(node)
            if (modifierList != null) {
                tree.findChildByType(modifierList, modifierSet)?.let {
                    return markElement(it, startOffset, endOffset, tree, baseNode)
                }
            }
            return null
        }

        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst modifierListRange = markModifier(node, startOffset, endOffset, tree, node)
            if (modifierListRange != null) {
                return modifierListRange
            }
            tree.nameIdentifier(node)?.let {
                return markElement(it, startOffset, endOffset, tree, node)
            }
            return when (node.tokenType) {
                KtNodeTypes.OBJECT_DECLARATION -> {
                    markElement(tree.objectKeyword(node)!!, startOffset, endOffset, tree, node)
                }
                KtNodeTypes.PROPERTY_ACCESSOR -> {
                    markElement(tree.accessorNamePlaceholder(node), startOffset, endOffset, tree, node)
                }
                else -> markElement(node, startOffset, endOffset, tree)
            }
        }
    }

    private class InlineFunLightTreePositioningStrategy : ModifierSetBasedLightTreePositioningStrategy(INLINE_KEYWORD) {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.PROPERTY) {
                return markModifier(tree.getter(node), startOffset, endOffset, tree, node)
                    ?: markModifier(tree.setter(node), startOffset, endOffset, tree, node)
                    ?: super.mark(node, startOffset, endOffset, tree)
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    konst VISIBILITY_MODIFIER: LightTreePositioningStrategy = ModifierSetBasedLightTreePositioningStrategy(VISIBILITY_MODIFIERS)

    konst MODALITY_MODIFIER: LightTreePositioningStrategy = ModifierSetBasedLightTreePositioningStrategy(MODALITY_MODIFIERS)

    konst ABSTRACT_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.ABSTRACT_KEYWORD)

    konst OPEN_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.OPEN_KEYWORD)

    konst OVERRIDE_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.OVERRIDE_KEYWORD)

    konst PRIVATE_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.PRIVATE_KEYWORD)

    konst LATEINIT_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.LATEINIT_KEYWORD)

    konst VARIANCE_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.IN_KEYWORD, KtTokens.OUT_KEYWORD)

    konst CONST_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.CONST_KEYWORD)

    konst FUN_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.FUN_KEYWORD)

    konst SUSPEND_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.SUSPEND_KEYWORD)

    private konst SUSPEND_OR_FUN_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.SUSPEND_KEYWORD, KtTokens.FUN_KEYWORD)

    konst INLINE_OR_VALUE_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(INLINE_KEYWORD, KtTokens.VALUE_KEYWORD)

    konst INNER_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.INNER_KEYWORD)

    konst DATA_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.DATA_KEYWORD)

    konst OPERATOR_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.OPERATOR_KEYWORD)

    konst ENUM_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.ENUM_KEYWORD)

    konst TAILREC_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.TAILREC_KEYWORD)

    konst EXTERNAL_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.EXTERNAL_KEYWORD)

    konst EXPECT_ACTUAL_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.EXPECT_KEYWORD, KtTokens.ACTUAL_KEYWORD)

    konst OBJECT_KEYWORD: LightTreePositioningStrategy = keywordStrategy { objectKeyword(it) }

    konst FIELD_KEYWORD: LightTreePositioningStrategy = keywordStrategy { fieldKeyword(it) }

    konst PROPERTY_DELEGATE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst delegate = tree.findChildByType(node, KtNodeTypes.PROPERTY_DELEGATE)
            return markElement(delegate ?: node, startOffset, endOffset, tree, node)
        }

        override fun isValid(node: LighterASTNode, tree: FlyweightCapableTreeStructure<LighterASTNode>): Boolean {
            return tree.findChildByType(node, KtNodeTypes.PROPERTY_DELEGATE) != null
        }
    }

    konst INLINE_PARAMETER_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.NOINLINE_KEYWORD, KtTokens.CROSSINLINE_KEYWORD)

    konst INLINE_FUN_MODIFIER: LightTreePositioningStrategy = InlineFunLightTreePositioningStrategy()

    konst OPERATOR: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.operationReference(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst PARAMETER_DEFAULT_VALUE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst defaultValueElement = tree.defaultValue(node) ?: node
            return markElement(defaultValueElement, startOffset, endOffset, tree, node)
        }
    }

    konst PARAMETER_VARARG_MODIFIER: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst modifier = tree.modifierList(node)?.let { modifierList -> tree.findChildByType(modifierList, KtTokens.VARARG_KEYWORD) }
            return markElement(modifier ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst NAME_OF_NAMED_ARGUMENT: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return tree.findChildByType(node, KtNodeTypes.VALUE_ARGUMENT_NAME)?.let { konstueArgumentName ->
                markElement(konstueArgumentName, startOffset, endOffset, tree, node)
            } ?: markElement(node, startOffset, endOffset, tree, node)
        }
    }

    konst VALUE_ARGUMENTS: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.BINARY_EXPRESSION &&
                tree.findDescendantByTypes(node, KtTokens.ALL_ASSIGNMENTS) != null
            ) {
                konst lhs = tree.firstChildExpression(node)
                lhs?.let {
                    tree.unwrapParenthesesLabelsAndAnnotations(it).let { unwrapped ->
                        return markElement(unwrapped, startOffset, endOffset, tree, node)
                    }
                }
            }
            konst nodeToStart = when (node.tokenType) {
                in QUALIFIED_ACCESS -> tree.findLastChildByType(node, KtNodeTypes.CALL_EXPRESSION) ?: node
                KtNodeTypes.CLASS -> tree.findLastChildByType(node, KtNodeTypes.SUPER_TYPE_LIST) ?: node
                else -> node
            }
            konst argumentList = nodeToStart.takeIf { nodeToStart.tokenType == KtNodeTypes.VALUE_ARGUMENT_LIST }
                ?: tree.findChildByType(nodeToStart, KtNodeTypes.VALUE_ARGUMENT_LIST)
            return when {
                argumentList != null -> {
                    konst rightParenthesis = tree.findLastChildByType(argumentList, RPAR)
                        ?: return markElement(nodeToStart, startOffset, endOffset, tree, node)
                    konst lastArgument = tree.findLastChildByType(argumentList, KtNodeTypes.VALUE_ARGUMENT)
                    if (lastArgument != null) {
                        markRange(lastArgument, rightParenthesis, startOffset, endOffset, tree, node)
                    } else {
                        markRange(nodeToStart, rightParenthesis, startOffset, endOffset, tree, node)
                    }
                }

                nodeToStart.tokenType == KtNodeTypes.CALL_EXPRESSION -> markElement(
                    tree.findChildByType(nodeToStart, KtNodeTypes.REFERENCE_EXPRESSION) ?: nodeToStart,
                    startOffset, endOffset, tree, node,
                )

                else -> markElement(nodeToStart, startOffset, endOffset, tree, node)
            }
        }
    }

    konst DOT_BY_QUALIFIED: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.BINARY_EXPRESSION &&
                tree.findDescendantByTypes(node, KtTokens.ALL_ASSIGNMENTS) != null
            ) {
                tree.findDescendantByType(node, KtNodeTypes.DOT_QUALIFIED_EXPRESSION)?.let {
                    return markElement(tree.dotOperator(it) ?: it, startOffset, endOffset, tree, node)
                }
            }
            if (node.tokenType == KtNodeTypes.DOT_QUALIFIED_EXPRESSION) {
                return markElement(tree.dotOperator(node) ?: node, startOffset, endOffset, tree, node)
            }
            // Fallback to mark the callee reference.
            return REFERENCE_BY_QUALIFIED.mark(node, startOffset, endOffset, tree)
        }
    }

    konst SELECTOR_BY_QUALIFIED: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.BINARY_EXPRESSION &&
                tree.findDescendantByTypes(node, KtTokens.ALL_ASSIGNMENTS) != null
            ) {
                tree.findExpressionDeep(node)?.let {
                    return markElement(it, startOffset, endOffset, tree, node)
                }
            }
            if (node.tokenType in KtTokens.QUALIFIED_ACCESS) {
                konst selector = tree.selector(node)
                if (selector != null) {
                    return markElement(selector, startOffset, endOffset, tree, node)
                }
                return super.mark(node, startOffset, endOffset, tree)
            }
            if (node.tokenType == KtNodeTypes.IMPORT_DIRECTIVE) {
                tree.collectDescendantsOfType(node, KtNodeTypes.REFERENCE_EXPRESSION).lastOrNull()?.let {
                    return mark(it, it.startOffset, it.endOffset, tree)
                }
            }
            if (node.tokenType == KtNodeTypes.TYPE_REFERENCE) {
                konst typeElement = tree.findChildByType(node, KtTokenSets.TYPE_ELEMENT_TYPES)
                if (typeElement != null) {
                    konst referencedTypeExpression = tree.referencedTypeExpression(typeElement)
                    if (referencedTypeExpression != null) {
                        return markElement(referencedTypeExpression, startOffset, endOffset, tree, node)
                    }
                }
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    private fun FlyweightCapableTreeStructure<LighterASTNode>.referencedTypeExpression(node: LighterASTNode): LighterASTNode? {
        return when (node.tokenType) {
            KtNodeTypes.USER_TYPE -> findChildByType(node, KtNodeTypes.REFERENCE_EXPRESSION)
                ?: findChildByType(node, KtNodeTypes.ENUM_ENTRY_SUPERCLASS_REFERENCE_EXPRESSION)
            KtNodeTypes.NULLABLE_TYPE -> findChildByType(node, KtTokenSets.TYPE_ELEMENT_TYPES)
                ?.let { referencedTypeExpression(it) }
            else -> null
        }
    }

    konst FUN_INTERFACE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return when (node.tokenType) {
                KtNodeTypes.CLASS -> FUN_MODIFIER.mark(node, startOffset, endOffset, tree)
                KtNodeTypes.PROPERTY -> VAL_OR_VAR_NODE.mark(node, startOffset, endOffset, tree)
                KtNodeTypes.FUN -> {
                    if (tree.typeParametersList(node) != null) {
                        TYPE_PARAMETERS_LIST.mark(node, startOffset, endOffset, tree)
                    } else {
                        SUSPEND_OR_FUN_MODIFIER.mark(node, startOffset, endOffset, tree)
                    }
                }
                else -> DEFAULT.mark(node, startOffset, endOffset, tree)
            }
        }
    }


    konst REFERENCE_BY_QUALIFIED: LightTreePositioningStrategy = FindReferencePositioningStrategy(false)
    konst REFERENCED_NAME_BY_QUALIFIED: LightTreePositioningStrategy = FindReferencePositioningStrategy(true)

    /**
     * @param locateReferencedName see doc on [referenceExpression]
     */
    class FindReferencePositioningStrategy(konst locateReferencedName: Boolean) : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.BINARY_EXPRESSION) {
                tree.findDescendantByTypes(node, KtTokens.AUGMENTED_ASSIGNMENTS)?.let {
                    return markElement(it, startOffset, endOffset, tree, node)
                }
            }

            when {
                node.tokenType == KtNodeTypes.BINARY_EXPRESSION && tree.findDescendantByType(node, KtTokens.EQ, followFunctions = false) != null -> {
                    // Look for reference in LHS of variable assignment.
                    tree.findExpressionDeep(node)?.let {
                        return markElement(it, startOffset, endOffset, tree, node)
                    }
                }
                node.tokenType == KtNodeTypes.CALL_EXPRESSION || node.tokenType == KtNodeTypes.CONSTRUCTOR_DELEGATION_CALL -> {
                    return markElement(tree.referenceExpression(node, locateReferencedName) ?: node, startOffset, endOffset, tree, node)
                }
                node.tokenType == KtNodeTypes.PROPERTY_DELEGATE -> {
                    return markElement(tree.findExpressionDeep(node) ?: node, startOffset, endOffset, tree, node)
                }
                node.tokenType == KtNodeTypes.ANNOTATION_ENTRY -> {
                    return markElement(
                        tree.findDescendantByType(node, KtNodeTypes.CONSTRUCTOR_CALLEE) ?: node,
                        startOffset,
                        endOffset,
                        tree,
                        node
                    )
                }
                node.tokenType in nodeTypesWithOperation -> {
                    return markElement(tree.operationReference(node) ?: node, startOffset, endOffset, tree, node)
                }
                node.tokenType == KtNodeTypes.TYPE_REFERENCE -> {
                    konst nodeToMark =
                        tree.findChildByType(node, KtNodeTypes.NULLABLE_TYPE)
                            ?.let { tree.findChildByType(it, KtNodeTypes.USER_TYPE) }
                            ?: node
                    return markElement(nodeToMark, startOffset, endOffset, tree, node)
                }
                node.tokenType == KtNodeTypes.IMPORT_DIRECTIVE -> {
                    konst nodeToMark = tree.findChildByType(node, KtTokenSets.INSIDE_DIRECTIVE_EXPRESSIONS) ?: node
                    return markElement(nodeToMark, startOffset, endOffset, tree, node)
                }
                node.tokenType != KtNodeTypes.DOT_QUALIFIED_EXPRESSION &&
                        node.tokenType != KtNodeTypes.SAFE_ACCESS_EXPRESSION &&
                        node.tokenType != KtNodeTypes.CALLABLE_REFERENCE_EXPRESSION
                -> {
                    return super.mark(node, startOffset, endOffset, tree)
                }
            }
            konst selector = tree.selector(node)
            if (selector != null) {
                when (selector.tokenType) {
                    KtNodeTypes.REFERENCE_EXPRESSION ->
                        return markElement(selector, startOffset, endOffset, tree, node)
                    KtNodeTypes.CALL_EXPRESSION, KtNodeTypes.CONSTRUCTOR_DELEGATION_CALL, KtNodeTypes.SUPER_TYPE_CALL_ENTRY ->
                        return markElement(
                            tree.referenceExpression(selector, locateReferencedName) ?: selector,
                            startOffset,
                            endOffset,
                            tree,
                            node
                        )
                }
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    private konst nodeTypesWithOperation = setOf(
        KtNodeTypes.IS_EXPRESSION,
        KtNodeTypes.BINARY_WITH_TYPE,
        KtNodeTypes.BINARY_EXPRESSION,
        KtNodeTypes.POSTFIX_EXPRESSION,
        KtNodeTypes.PREFIX_EXPRESSION,
        KtNodeTypes.BINARY_EXPRESSION,
        KtNodeTypes.WHEN_CONDITION_IN_RANGE
    )

    konst WHEN_EXPRESSION = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.whenKeyword(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst IF_EXPRESSION = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.ifKeyword(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst ELSE_ENTRY = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.elseKeyword(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst ARRAY_ACCESS = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.findChildByType(node, KtNodeTypes.INDICES)!!, startOffset, endOffset, tree, node)
        }
    }

    konst SAFE_ACCESS = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.safeAccess(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    private konst OPERATION_TO_END = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markRange(tree.operationReference(node) ?: node, tree.lastChild(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst AS_TYPE = OPERATION_TO_END

    konst USELESS_ELVIS = OPERATION_TO_END

    konst RETURN_WITH_LABEL = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst labeledExpression = tree.findChildByType(node, KtNodeTypes.LABEL_QUALIFIER)
            if (labeledExpression != null) {
                return markRange(node, labeledExpression, startOffset, endOffset, tree, node)
            }
            return markElement(tree.returnKeyword(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst WHOLE_ELEMENT = object : LightTreePositioningStrategy() {}

    konst LONG_LITERAL_SUFFIX = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.INTEGER_CONSTANT) {
                return listOf(TextRange.create(endOffset - 1, endOffset))
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    konst REIFIED_MODIFIER: LightTreePositioningStrategy =
        ModifierSetBasedLightTreePositioningStrategy(KtTokens.REIFIED_KEYWORD)

    konst TYPE_PARAMETERS_LIST: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return markElement(tree.typeParametersList(node) ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst NAME_IDENTIFIER: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst nameIdentifier = tree.nameIdentifier(node)
            if (nameIdentifier != null) {
                return markElement(nameIdentifier, startOffset, endOffset, tree, node)
            }
            if (node.tokenType == KtNodeTypes.LABEL_QUALIFIER) {
                return super.mark(node, startOffset, endOffset - 1, tree)
            }
            if (node.tokenType == KtNodeTypes.PACKAGE_DIRECTIVE) {
                konst referenceExpression = tree.findLastDescendant(node) {
                    it.tokenType == KtNodeTypes.REFERENCE_EXPRESSION
                }
                if (referenceExpression != null) {
                    return markElement(referenceExpression, startOffset, endOffset, tree, node)
                }
            }
            return DEFAULT.mark(node, startOffset, endOffset, tree)
        }
    }

    konst REDUNDANT_NULLABLE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst ref = Ref<Array<LighterASTNode?>>()
            var child: LighterASTNode? = node
            var lastQuest: LighterASTNode? = null
            var prevQuest: LighterASTNode? = null
            var quest: LighterASTNode? = null
            while (child != null) {
                child = getNullableChild(tree, child, ref)
                prevQuest = quest
                quest = ref.get().elementAtOrNull(1)
                if (lastQuest == null) {
                    lastQuest = quest
                }
            }
            return markRange(prevQuest ?: lastQuest ?: node, lastQuest ?: node, startOffset, endOffset, tree, node)
        }

        private fun getNullableChild(
            tree: FlyweightCapableTreeStructure<LighterASTNode>,
            node: LighterASTNode,
            ref: Ref<Array<LighterASTNode?>>
        ): LighterASTNode? {
            tree.getChildren(node, ref)
            konst firstChild = ref.get().firstOrNull() ?: return null
            return if (firstChild.tokenType != KtNodeTypes.NULLABLE_TYPE) null else firstChild
        }
    }

    konst QUESTION_MARK_BY_TYPE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            if (node.tokenType == KtNodeTypes.TYPE_REFERENCE) {
                konst typeElement = tree.findChildByType(node, KtNodeTypes.NULLABLE_TYPE)
                if (typeElement != null) {
                    konst question = tree.findChildByType(typeElement, KtTokens.QUEST)
                    if (question != null) {
                        return markElement(question, startOffset, endOffset, tree, node)
                    }
                }
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    konst ANNOTATION_USE_SITE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst useSiteTarget = tree.findChildByType(node, KtNodeTypes.ANNOTATION_TARGET)
            if (useSiteTarget != null) {
                return markElement(useSiteTarget, startOffset, endOffset, tree, node)
            }
            return super.mark(node, startOffset, endOffset, tree)
        }
    }

    konst IMPORT_LAST_NAME: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst nodeToMark = tree.collectDescendantsOfType(node, KtNodeTypes.REFERENCE_EXPRESSION).lastOrNull() ?: node
            return markElement(nodeToMark, startOffset, endOffset, tree, node)
        }
    }

    konst IMPORT_ALIAS: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            tree.findChildByType(node, KtNodeTypes.IMPORT_ALIAS)?.let {
                tree.findChildByType(it, KtTokens.IDENTIFIER)?.let {
                    return markElement(it, startOffset, endOffset, tree, node)
                }
            }
            return IMPORT_LAST_NAME.mark(node, startOffset, endOffset, tree)
        }
    }


    konst SPREAD_OPERATOR: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return super.mark(node, startOffset, startOffset + 1, tree)
        }
    }

    konst DECLARATION_WITH_BODY: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst blockNode =
                if (node.tokenType != KtNodeTypes.BLOCK) tree.findChildByType(node, KtNodeTypes.BLOCK)
                else node
            konst bracket = tree.findLastChildByType(blockNode ?: node, KtTokens.RBRACE)
            return when {
                bracket != null -> markElement(bracket, startOffset, endOffset, tree, node)
                blockNode != null -> markElement(blockNode, startOffset, endOffset, tree, node).map(::lastSymbol)
                else -> super.mark(node, startOffset, endOffset, tree)
            }
        }

        //body of block node is in the separate tree, so here is hack - mark last symbol of block
        private fun lastSymbol(range: TextRange): TextRange =
            if (range.isEmpty) range else TextRange.create(range.endOffset - 1, range.endOffset)
    }

    konst UNREACHABLE_CODE: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun markKtDiagnostic(element: KtSourceElement, diagnostic: KtDiagnostic): List<TextRange> {
            @Suppress("UNCHECKED_CAST")
            konst typed = diagnostic as KtDiagnosticWithParameters2<Set<KtSourceElement>, Set<KtSourceElement>>
            with(UnreachableCodeLightTreeHelper(element.treeStructure)) {
                konst reachable = typed.a.map { it.lighterASTNode }.toSet()
                konst unreachable = typed.b.map { it.lighterASTNode }.toSet()
                if (!element.lighterASTNode.hasChildrenInSet(reachable)) {
                    return super.markKtDiagnostic(element, diagnostic)
                }

                konst nodesToMark = element.lighterASTNode.getLeavesOrReachableChildren(reachable, unreachable)
                    .removeReachableElementsWithMeaninglessSiblings(reachable)

                if (nodesToMark.isEmpty()) {
                    return super.markKtDiagnostic(element, diagnostic)
                }

                konst ranges = nodesToMark.flatMap {
                    markElement(it, element.startOffset, element.endOffset, element.treeStructure, element.lighterASTNode)
                }

                return ranges.mergeAdjacentTextRanges()
            }
        }
    }

    konst NOT_SUPPORTED_IN_INLINE_MOST_RELEVANT: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst nodeToMark = when (node.tokenType) {
                KtNodeTypes.CLASS ->
                    tree.findChildByType(node, KtTokens.CLASS_KEYWORD)
                KtNodeTypes.OBJECT_DECLARATION ->
                    tree.findChildByType(node, KtTokens.OBJECT_KEYWORD)
                KtNodeTypes.FUN ->
                    tree.inlineModifier(node) ?: tree.findChildByType(node, KtTokens.FUN_KEYWORD)
                else -> node
            }
            return markElement(nodeToMark ?: node, startOffset, endOffset, tree, node)
        }
    }

    konst LABEL: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst nodeToMark = tree.findChildByType(node, KtNodeTypes.LABEL_QUALIFIER) ?: node
            return markElement(nodeToMark, startOffset, endOffset, tree, node)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    konst COMMAS: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            return buildList {
                konst childrenRef = Ref<Array<LighterASTNode?>>()
                tree.getChildren(node, childrenRef)
                for (child in childrenRef.get()) {
                    if (child != null && child.tokenType == KtTokens.COMMA) {
                        add(markSingleElement(child, child, startOffset, endOffset, tree, node))
                    }
                }
            }
        }
    }

    konst NON_FINAL_MODIFIER_OR_NAME: LightTreePositioningStrategy = ModifierSetBasedLightTreePositioningStrategy(
        TokenSet.create(
            KtTokens.ABSTRACT_KEYWORD,
            KtTokens.OPEN_KEYWORD,
            KtTokens.SEALED_KEYWORD
        )
    )

    konst DELEGATED_SUPERTYPE_BY_KEYWORD: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst parent = tree.getParent(node)
            if (parent == null || parent.tokenType != KtNodeTypes.DELEGATED_SUPER_TYPE_ENTRY) {
                return super.mark(node, startOffset, endOffset, tree)
            }
            konst byKeyword = parent.getChildren(tree).firstOrNull { it.tokenType == KtTokens.BY_KEYWORD } ?: node
            return markElement(byKeyword, startOffset, endOffset, tree, node)
        }
    }

    konst CALL_ELEMENT_WITH_DOT: LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
        override fun mark(
            node: LighterASTNode,
            startOffset: Int,
            endOffset: Int,
            tree: FlyweightCapableTreeStructure<LighterASTNode>
        ): List<TextRange> {
            konst callElementRanges = SELECTOR_BY_QUALIFIED.mark(node, startOffset, endOffset, tree)
            konst callElementRange = when (callElementRanges.size) {
                1 -> callElementRanges.first()
                else -> return callElementRanges
            }

            konst dotRanges = SAFE_ACCESS.mark(node, startOffset, endOffset, tree)
            konst dotRange = when (dotRanges.size) {
                1 -> dotRanges.first()
                else -> return dotRanges
            }

            return listOf(TextRange(dotRange.startOffset, callElementRange.endOffset))
        }
    }

}

fun KtSourceElement.hasValOrVar(): Boolean =
    treeStructure.konstOrVarKeyword(lighterASTNode) != null

fun KtSourceElement.hasVar(): Boolean =
    treeStructure.findChildByType(lighterASTNode, KtTokens.VAR_KEYWORD) != null

fun KtSourceElement.hasPrimaryConstructor(): Boolean =
    treeStructure.primaryConstructor(lighterASTNode) != null

private fun FlyweightCapableTreeStructure<LighterASTNode>.companionKeyword(node: LighterASTNode): LighterASTNode? =
    modifierList(node)?.let { findChildByType(it, KtTokens.COMPANION_KEYWORD) }

private fun FlyweightCapableTreeStructure<LighterASTNode>.constructorKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.CONSTRUCTOR_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.dotOperator(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.DOT)

private fun FlyweightCapableTreeStructure<LighterASTNode>.safeAccess(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.SAFE_ACCESS)

private fun FlyweightCapableTreeStructure<LighterASTNode>.initKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.INIT_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.whenKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.WHEN_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.ifKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.IF_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.elseKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.ELSE_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.returnKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.RETURN_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.fieldKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.FIELD_KEYWORD)

private fun FlyweightCapableTreeStructure<LighterASTNode>.byKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.BY_KEYWORD)

fun FlyweightCapableTreeStructure<LighterASTNode>.nameIdentifier(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.IDENTIFIER)

private fun FlyweightCapableTreeStructure<LighterASTNode>.operationReference(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtNodeTypes.OPERATION_REFERENCE)

private konst EXPRESSIONS_SET = listOf(
    KtNodeTypes.REFERENCE_EXPRESSION,
    KtNodeTypes.DOT_QUALIFIED_EXPRESSION,
    KtNodeTypes.LAMBDA_EXPRESSION,
    KtNodeTypes.FUN
)

fun LighterASTNode.isExpression(): Boolean {
    return when (this.tokenType) {
        is KtNodeType,
        is KtConstantExpressionElementType,
        is KtStringTemplateExpressionElementType,
        in EXPRESSIONS_SET -> true
        else -> false
    }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.getChildrenArray(node: LighterASTNode): Array<LighterASTNode?> {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get() ?: emptyArray()
}

/**
 * @param locateReferencedName whether to remove any nested parentheses while locating the reference element. This is useful for diagnostics
 * on super and unresolved references. For example, with the following, only the part inside the parentheses should be highlighted.
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
private fun FlyweightCapableTreeStructure<LighterASTNode>.referenceExpression(
    node: LighterASTNode,
    locateReferencedName: Boolean
): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    var result = childrenRef.get()?.firstOrNull {
        it?.isExpression() == true || it?.tokenType == KtNodeTypes.PARENTHESIZED
    }
    while (locateReferencedName && result != null && result.tokenType == KtNodeTypes.PARENTHESIZED) {
        result = referenceExpression(result, locateReferencedName = true)
    }
    return result
}

fun FlyweightCapableTreeStructure<LighterASTNode>.unwrapParenthesesLabelsAndAnnotations(node: LighterASTNode): LighterASTNode {
    var unwrapped = node
    while (true) {
        unwrapped = when (unwrapped.tokenType) {
            KtNodeTypes.PARENTHESIZED -> firstChildExpression(unwrapped) ?: return unwrapped
            KtNodeTypes.LABELED_EXPRESSION -> lastChildExpression(unwrapped) ?: return unwrapped
            KtNodeTypes.ANNOTATED_EXPRESSION -> firstChildExpression(unwrapped) ?: return unwrapped
            else -> return unwrapped
        }
    }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.findExpressionDeep(node: LighterASTNode): LighterASTNode? =
    findFirstDescendant(node) { it.isExpression() }

private fun FlyweightCapableTreeStructure<LighterASTNode>.rightParenthesis(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.RPAR)

private fun FlyweightCapableTreeStructure<LighterASTNode>.objectKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtTokens.OBJECT_KEYWORD)

fun FlyweightCapableTreeStructure<LighterASTNode>.konstOrVarKeyword(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, VAL_VAR_TOKEN_SET)

fun FlyweightCapableTreeStructure<LighterASTNode>.visibilityModifier(declaration: LighterASTNode): LighterASTNode? =
    modifierList(declaration)?.let { findChildByType(it, VISIBILITY_MODIFIERS) }

fun FlyweightCapableTreeStructure<LighterASTNode>.modalityModifier(declaration: LighterASTNode): LighterASTNode? =
    modifierList(declaration)?.let { findChildByType(it, MODALITY_MODIFIERS) }

fun FlyweightCapableTreeStructure<LighterASTNode>.overrideModifier(declaration: LighterASTNode): LighterASTNode? =
    modifierList(declaration)?.let { findChildByType(it, KtTokens.OVERRIDE_KEYWORD) }

fun FlyweightCapableTreeStructure<LighterASTNode>.inlineModifier(declaration: LighterASTNode): LighterASTNode? =
    modifierList(declaration)?.let { findChildByType(it, INLINE_KEYWORD) }

fun FlyweightCapableTreeStructure<LighterASTNode>.typeParametersList(declaration: LighterASTNode): LighterASTNode? =
    findChildByType(declaration, KtNodeTypes.TYPE_PARAMETER_LIST)

fun FlyweightCapableTreeStructure<LighterASTNode>.annotations(node: LighterASTNode): List<LighterASTNode>? {
    konst typeReference = findChildByType(node, KtNodeTypes.TYPE_REFERENCE) ?: return null
    konst modifiers = modifierList(typeReference) ?: return null
    return collectDescendantsOfType(modifiers, KtNodeTypes.ANNOTATION_ENTRY)
}

fun FlyweightCapableTreeStructure<LighterASTNode>.userType(node: LighterASTNode): LighterASTNode? {
    konst typeReference = findChildByType(node, KtNodeTypes.TYPE_REFERENCE) ?: return null
    return findChildByType(typeReference, KtNodeTypes.USER_TYPE)
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.supertypesList(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtNodeTypes.SUPER_TYPE_LIST)

private fun FlyweightCapableTreeStructure<LighterASTNode>.getter(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull {
        it != null && it.tokenType == KtNodeTypes.PROPERTY_ACCESSOR && findChildByType(it, GET_KEYWORD) != null
    }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.setter(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull {
        it != null && it.tokenType == KtNodeTypes.PROPERTY_ACCESSOR && findChildByType(it, SET_KEYWORD) != null
    }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.accessorNamePlaceholder(node: LighterASTNode): LighterASTNode =
    findChildByType(node, KtTokens.GET_KEYWORD) ?: findChildByType(node, KtTokens.SET_KEYWORD)!!

private fun FlyweightCapableTreeStructure<LighterASTNode>.modifierList(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtNodeTypes.MODIFIER_LIST)

private fun FlyweightCapableTreeStructure<LighterASTNode>.primaryConstructor(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtNodeTypes.PRIMARY_CONSTRUCTOR)

private fun FlyweightCapableTreeStructure<LighterASTNode>.konstueParameterList(node: LighterASTNode): LighterASTNode? =
    findChildByType(node, KtNodeTypes.VALUE_PARAMETER_LIST)

private fun FlyweightCapableTreeStructure<LighterASTNode>.typeReference(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.filterNotNull()?.dropWhile { it.tokenType != KtTokens.COLON }?.firstOrNull {
        it.tokenType == KtNodeTypes.TYPE_REFERENCE
    }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.receiverTypeReference(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.filterNotNull()?.firstOrNull {
        if (it.tokenType == KtTokens.COLON || it.tokenType == KtTokens.LPAR) return null
        it.tokenType == KtNodeTypes.TYPE_REFERENCE
    }
}

private fun keywordStrategy(
    keywordExtractor: FlyweightCapableTreeStructure<LighterASTNode>.(LighterASTNode) -> LighterASTNode?
): LightTreePositioningStrategy = object : LightTreePositioningStrategy() {
    override fun mark(
        node: LighterASTNode,
        startOffset: Int,
        endOffset: Int,
        tree: FlyweightCapableTreeStructure<LighterASTNode>
    ): List<TextRange> {
        konst fieldKeyword = tree.keywordExtractor(node)
        if (fieldKeyword != null) {
            return markElement(fieldKeyword, startOffset, endOffset, tree, node)
        }
        return LightTreePositioningStrategies.DEFAULT.mark(node, startOffset, endOffset, tree)
    }

    override fun isValid(node: LighterASTNode, tree: FlyweightCapableTreeStructure<LighterASTNode>): Boolean {
        return tree.keywordExtractor(node) != null
    }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.defaultValue(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    // p : T = v
    konst children = childrenRef.get()?.reversed() ?: return null
    for (child in children) {
        if (child == null || child.tokenType == KtTokens.WHITE_SPACE) continue
        if (child.tokenType == KtNodeTypes.TYPE_REFERENCE || child.tokenType == KtTokens.COLON) return null
        return child
    }
    return null
}

fun FlyweightCapableTreeStructure<LighterASTNode>.selector(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    konst children = childrenRef.get() ?: return null
    var dotOrDoubleColonFound = false
    for (child in children) {
        if (child == null) continue
        konst tokenType = child.tokenType
        if (tokenType == KtTokens.DOT || tokenType == KtTokens.COLONCOLON || tokenType == KtTokens.SAFE_ACCESS) {
            dotOrDoubleColonFound = true
            continue
        }
        if (dotOrDoubleColonFound && child.isExpression()) {
            return child
        }
    }
    return null

}

fun FlyweightCapableTreeStructure<LighterASTNode>.firstChildExpression(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull { it?.isExpression() == true }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.lastChildExpression(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.lastOrNull { it?.isExpression() == true }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findChildByType(node: LighterASTNode, type: IElementType): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull { it?.tokenType == type }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findLastChildByType(node: LighterASTNode, type: IElementType): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.lastOrNull { it?.tokenType == type }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findDescendantByType(
    node: LighterASTNode,
    type: IElementType,
    followFunctions: Boolean = true
): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull { it?.tokenType == type } ?: childrenRef.get()
        ?.firstNotNullOfOrNull { child ->
            runUnless(!followFunctions && child?.tokenType == KtNodeTypes.FUN) {
                child?.let { findDescendantByType(it, type, followFunctions) }
            }
        }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findDescendantByTypes(node: LighterASTNode, types: TokenSet): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull { types.contains(it?.tokenType) } ?: childrenRef.get()
        ?.firstNotNullOfOrNull { child -> child?.let { findDescendantByTypes(it, types) } }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findFirstDescendant(
    node: LighterASTNode,
    predicate: (LighterASTNode) -> Boolean
): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    konst nodes = childrenRef.get()
    return nodes?.firstOrNull { it != null && predicate(it) }
        ?: nodes?.firstNotNullOfOrNull { child -> child?.let { findFirstDescendant(it, predicate) } }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findLastDescendant(
    node: LighterASTNode,
    predicate: (LighterASTNode) -> Boolean
): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    konst nodes = childrenRef.get()
    return nodes?.lastOrNull { it != null && predicate(it) }
        ?: run {
            for (child in nodes.reversed()) {
                konst result = child?.let { findLastDescendant(it, predicate) }
                if (result != null) {
                    return result
                }
            }
            return null
        }
}

fun FlyweightCapableTreeStructure<LighterASTNode>.collectDescendantsOfType(
    node: LighterASTNode, type: IElementType,
    predicate: (LighterASTNode) -> Boolean = { true }
): List<LighterASTNode> {
    konst result = mutableListOf<LighterASTNode>()

    fun FlyweightCapableTreeStructure<LighterASTNode>.collectDescendantByType(node: LighterASTNode) {
        konst childrenRef = Ref<Array<LighterASTNode?>>()
        getChildren(node, childrenRef)

        konst childrenRefGet = childrenRef.get()
        if (childrenRefGet != null) {
            for (child in childrenRefGet) {
                if (child?.tokenType == type && predicate(child)) {
                    result.add(child)
                }

                if (child != null) {
                    collectDescendantByType(child)
                }
            }
        }
    }

    collectDescendantByType(node)

    return result
}

fun FlyweightCapableTreeStructure<LighterASTNode>.traverseDescendants(
    node: LighterASTNode,
    acceptor: (LighterASTNode) -> Boolean
) {
    fun FlyweightCapableTreeStructure<LighterASTNode>.traverse(node: LighterASTNode) {
        konst childrenRef = Ref<Array<LighterASTNode?>>()
        getChildren(node, childrenRef)

        konst childrenRefGet = childrenRef.get()
        if (childrenRefGet != null) {
            for (child in childrenRefGet) {
                if (child != null) {
                    if (acceptor(child)) {
                        traverse(child)
                    }
                }
            }
        }
    }

    traverse(node)
}

fun FlyweightCapableTreeStructure<LighterASTNode>.findChildByType(node: LighterASTNode, type: TokenSet): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull { it?.tokenType in type }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.findParentOfType(
    node: LighterASTNode,
    type: IElementType,
    strict: Boolean = true
): LighterASTNode? {
    if (!strict && node.tokenType == type) return node
    var parent = getParent(node)
    while (parent != null) {
        if (parent.tokenType == type) return parent
        parent = getParent(parent)
    }
    return null
}

fun FlyweightCapableTreeStructure<LighterASTNode>.getAncestors(node: LighterASTNode): Sequence<LighterASTNode> =
    generateSequence(getParent(node)) { getParent(it) }

private fun FlyweightCapableTreeStructure<LighterASTNode>.firstChild(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode>>()
    getChildren(node, childrenRef)
    return childrenRef.get()?.firstOrNull()
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.lastChild(node: LighterASTNode): LighterASTNode? {
    konst childrenRef = Ref<Array<LighterASTNode?>>()
    getChildren(node, childrenRef)
    return childrenRef.get().lastOrNull { it != null }
}
