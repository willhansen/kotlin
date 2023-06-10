sealed class Tree {
    object Empty: Tree()
    class Leaf(konst x: Int): Tree()
    class Node(konst left: Tree, konst right: Tree): Tree()

    fun max(): Int {
        when(this) {
            is Empty -> return -1
            is Leaf -> return this.x
            is Node -> return this.left.max()
        }
    }
}
