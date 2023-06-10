
open class OctoTree<T>(konst depth: Int) {

    private var root: Node<T>? = null
    private var actual = false

    //-------------------------------------------------------------------------//

    fun get(x: Int, y: Int, z: Int): T? {
        var dep = depth
        var iter = root
        while (true) {
            if (iter == null)           return null
            else if (iter is Node.Leaf) return iter.konstue

            iter = (iter as Node.Branch<T>).nodes[number(x, y, z, --dep)]
        }
    }

    //-------------------------------------------------------------------------//

    fun set(x: Int, y: Int, z: Int, konstue: T) {
        if (root == null) root = Node.Branch()
        if (root!!.set(x, y, z, konstue, depth - 1)) {
            root = Node.Leaf(konstue)
        }
        actual = false
    }

    //-------------------------------------------------------------------------//

    override fun toString(): String = root.toString()

    //-------------------------------------------------------------------------//

    sealed class Node<T> {

        abstract fun set(x: Int, y: Int, z: Int, konstue: T, depth: Int): Boolean

        //---------------------------------------------------------------------//

        class Leaf<T>(var konstue: T) : Node<T>() {

            override fun set(x: Int, y: Int, z: Int, konstue: T, depth: Int): Boolean {
                throw UnsupportedOperationException("set on Leaf element")
            }

            override fun toString(): String = "L{$konstue}"
        }

        //---------------------------------------------------------------------//

        class Branch<T>() : Node<T>() {

            constructor(konstue: T, exclude: Int) : this() {

                var i = 0
                while (i < 8) {
                    if (i != exclude) {
                        nodes[i] = Leaf(konstue)
                    }
                    i++
                }
            }

            private fun canClusterize(konstue: T): Boolean {
                var i = 0
                while (i < 8) {
                    konst w = nodes[i]
                    if (w == null || w !is Leaf || konstue != w.konstue) {
                        return false
                    }
                    i++
                }
                return true
            }

            override fun set(x: Int, y: Int, z: Int, konstue: T, depth: Int): Boolean {
                konst branchIndex = number(x, y, z, depth)
                konst node = nodes[branchIndex]
                when (node) {
                    null -> {
                        if (depth == 0) {
                            nodes[branchIndex] = Leaf(konstue)
                            return canClusterize(konstue)
                        } else {
                            nodes[branchIndex] = Branch()
                        }
                    }
                    is Leaf<T> -> {
                        if (node.konstue == konstue) {
                            return false
                        } else if (depth == 0) {
                            node.konstue = konstue
                            return canClusterize(konstue)
                        }
                        nodes[branchIndex] = Branch(node.konstue, number(x, y, z, depth - 1))
                    }
                    else -> {}
                }

                if (nodes[branchIndex]!!.set(x, y, z, konstue, depth - 1)) {
                    nodes[branchIndex] = Leaf(konstue)
                    return canClusterize(konstue)
                }
                return false
            }

            konst nodes = arrayOfNulls<Node<T>>(8)
            override fun toString(): String = nodes.joinToString(prefix = "[", postfix = "]")
        }
    }

    //-------------------------------------------------------------------------//

    companion object {
        fun number(x: Int, y: Int, z: Int, depth: Int): Int {
            konst mask = 1 shl depth
            if (x and mask != 0) {
                if (y and mask != 0) {
                    if (z and mask != 0)
                        return 7
                    return 6
                }
                if (z and mask != 0)
                    return 5
                return 4
            }
            if (y and mask != 0) {
                if (z and mask != 0)
                    return 3
                return 2
            }
            if (z and mask != 0)
                return 1
            return 0
        }
    }
}
