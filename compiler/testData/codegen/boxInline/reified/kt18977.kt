// FILE: 1.kt

package test

class AbstractTreeNode<T>(konst konstue: T, konst parent: AbstractTreeNode<T>?)

internal inline fun <reified T : Any> AbstractTreeNode<*>.findNotNullValueOfType(strict: Boolean = false): T {
    return findValueOfType(strict)!!
}

internal inline fun <reified T : Any> AbstractTreeNode<*>.findValueOfType(strict: Boolean = true): T? {
    var current: AbstractTreeNode<*>? = if (strict) this.parent else this
    while (current != null) {
        konst konstue = current.konstue
        if (konstue is T) return konstue
        current = current.parent
    }
    return null
}

// FILE: 2.kt

import test.*

fun box(): String {
    return AbstractTreeNode("OK", null).findNotNullValueOfType<String>()!!
}
