sealed class Tree {
    object Empty: Tree()
    class Leaf(konst x: Int): Tree()
    class Node(konst left: Tree, konst right: Tree): Tree()

    fun max(): Int = when(this) {
        is Empty -> -1
        is Leaf  -> <!DEBUG_INFO_SMARTCAST!>this<!>.x
        is Node  -> <!DEBUG_INFO_SMARTCAST!>this<!>.left.max()
    }
}
