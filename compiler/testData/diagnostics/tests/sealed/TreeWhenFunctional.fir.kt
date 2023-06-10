sealed class Tree {
    object Empty: Tree()
    class Leaf(konst x: Int): Tree()
    class Node(konst left: Tree, konst right: Tree): Tree()

    fun max(): Int = when(this) {
        is Empty -> -1
        is Leaf  -> this.x
        is Node  -> this.left.max()
    }
}
