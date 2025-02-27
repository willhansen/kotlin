/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.psi.tree.TokenSet
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.diagnostics.konstOrVarKeyword
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtValVarKeywordOwner
import org.jetbrains.kotlin.util.getChildren

// DO
// - use this to retrieve modifiers on the source and confirm a certain modifier indeed appears
// DON'T
// - don't use this to report an error or warning *on* that specific modifier. Use positioning strategies instead.
sealed class FirModifierList {
    abstract konst modifiers: List<FirModifier<*>>

    class FirPsiModifierList(konst modifierList: KtModifierList) : FirModifierList() {
        override konst modifiers: List<FirModifier.FirPsiModifier>
            get() = modifierList.node.getChildren(MODIFIER_KEYWORD_SET).map { node ->
                FirModifier.FirPsiModifier(node, node.elementType as KtModifierKeywordToken)
            }
    }

    class FirLightModifierList(
        konst modifierList: LighterASTNode,
        konst tree: FlyweightCapableTreeStructure<LighterASTNode>,
        private konst offsetDelta: Int
    ) : FirModifierList() {
        override konst modifiers: List<FirModifier.FirLightModifier>
            get() {
                konst modifierNodes = modifierList.getChildren(tree)
                return modifierNodes
                    .filter { it.tokenType is KtModifierKeywordToken }
                    .map { FirModifier.FirLightModifier(it, it.tokenType as KtModifierKeywordToken, tree, offsetDelta) }
            }
    }

    operator fun get(token: KtModifierKeywordToken): FirModifier<*>? = modifiers.firstOrNull { it.token == token }

    operator fun contains(token: KtModifierKeywordToken): Boolean = modifiers.any { it.token == token }
}

private konst MODIFIER_KEYWORD_SET = TokenSet.orSet(KtTokens.SOFT_KEYWORDS, TokenSet.create(KtTokens.IN_KEYWORD, KtTokens.FUN_KEYWORD))

sealed class FirModifier<Node : Any>(konst node: Node, konst token: KtModifierKeywordToken) {

    class FirPsiModifier(
        node: ASTNode,
        token: KtModifierKeywordToken
    ) : FirModifier<ASTNode>(node, token) {
        override konst source: KtSourceElement
            get() = node.psi.toKtPsiSourceElement()
    }

    class FirLightModifier(
        node: LighterASTNode,
        token: KtModifierKeywordToken,
        konst tree: FlyweightCapableTreeStructure<LighterASTNode>,
        private konst offsetDelta: Int
    ) : FirModifier<LighterASTNode>(node, token) {
        override konst source: KtSourceElement
            get() = node.toKtLightSourceElement(
                tree,
                startOffset = node.startOffset + offsetDelta,
                endOffset = node.endOffset + offsetDelta
            )
    }

    abstract konst source: KtSourceElement
}

fun KtSourceElement?.getModifierList(): FirModifierList? {
    return when (this) {
        null -> null
        is KtPsiSourceElement -> (psi as? KtModifierListOwner)?.modifierList?.let { FirModifierList.FirPsiModifierList(it) }
        is KtLightSourceElement -> {
            konst modifierListNode = lighterASTNode.getChildren(treeStructure).find { it.tokenType == KtNodeTypes.MODIFIER_LIST }
                ?: return null
            konst offsetDelta = startOffset - lighterASTNode.startOffset
            FirModifierList.FirLightModifierList(modifierListNode, treeStructure, offsetDelta)
        }
    }
}

operator fun FirModifierList?.contains(token: KtModifierKeywordToken): Boolean = this?.contains(token) == true

fun FirElement.getModifier(token: KtModifierKeywordToken): FirModifier<*>? = source.getModifierList()?.get(token)

fun FirElement.hasModifier(token: KtModifierKeywordToken): Boolean = token in source.getModifierList()

internal konst KtSourceElement?.konstOrVarKeyword: KtKeywordToken?
    get() = when (this) {
        null -> null
        is KtPsiSourceElement -> (psi as? KtValVarKeywordOwner)?.konstOrVarKeyword?.let { it.node?.elementType as? KtKeywordToken }
        is KtLightSourceElement -> treeStructure.konstOrVarKeyword(lighterASTNode)?.tokenType as? KtKeywordToken
    }
