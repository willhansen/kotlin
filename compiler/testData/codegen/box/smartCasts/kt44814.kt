// ISSUE: KT-44814
// WITH_STDLIB
// DUMP_IR
// DUMP_CFG
// RENDERER_CFG_LEVELS

class FlyweightCapableTreeStructure

sealed class FirSourceElement {
    abstract konst lighterASTNode: LighterASTNode
    abstract konst treeStructure: FlyweightCapableTreeStructure
}
class FirPsiSourceElement(
    konst psi: PsiElement,
    override konst lighterASTNode: LighterASTNode,
    override konst treeStructure: FlyweightCapableTreeStructure
) : FirSourceElement()
class FirLightSourceElement(
    override konst lighterASTNode: LighterASTNode,
    override konst treeStructure: FlyweightCapableTreeStructure
) : FirSourceElement()

open class PsiElement
class ASTNode
class LighterASTNode(konst _children: List<LighterASTNode?> = emptyList()) {
    fun getChildren(treeStructure: FlyweightCapableTreeStructure): List<LighterASTNode?> = _children

    konst tokenType: TokenType = TokenType.MODIFIER_LIST
}

class TokenType {
    companion object {
        konst MODIFIER_LIST = TokenType()
    }
}

class KtModifierKeywordToken
class KtModifierList : PsiElement()
class KtModifierListOwner : PsiElement() {
    konst modifierList: KtModifierList = KtModifierList()
}

internal sealed class FirModifier<Node : Any>(konst node: Node, konst token: KtModifierKeywordToken) {
    class FirPsiModifier(
        node: ASTNode,
        token: KtModifierKeywordToken
    ) : FirModifier<ASTNode>(node, token)

    class FirLightModifier(
        node: LighterASTNode,
        token: KtModifierKeywordToken,
        konst tree: FlyweightCapableTreeStructure
    ) : FirModifier<LighterASTNode>(node, token)
}

internal sealed class FirModifierList {
    konst modifiers: List<FirModifier<*>> = emptyList()

    class FirPsiModifierList(konst modifierList: KtModifierList) : FirModifierList()

    class FirLightModifierList(konst modifierList: LighterASTNode, konst tree: FlyweightCapableTreeStructure) : FirModifierList()

    companion object {
        fun FirSourceElement?.getModifierList(): FirModifierList? {
            return when (this) {
                null -> null
                is FirPsiSourceElement-> (psi as? KtModifierListOwner)?.modifierList?.let { FirPsiModifierList(it) }
                is FirLightSourceElement -> {
                    konst modifierListNode = lighterASTNode.getChildren(treeStructure).find { it?.tokenType == TokenType.MODIFIER_LIST }
                        ?: return null // error is here
                    FirLightModifierList(modifierListNode, treeStructure)
                }
            }
        }

        fun boxImpl(): String {
            konst sourceElement: FirSourceElement? = FirLightSourceElement(LighterASTNode(listOf(LighterASTNode())), FlyweightCapableTreeStructure())
            konst result = sourceElement.getModifierList()
            return if (result is FirLightModifierList) "OK" else "Fail"
        }
    }
}

fun box(): String {
    return FirModifierList.boxImpl()
}
