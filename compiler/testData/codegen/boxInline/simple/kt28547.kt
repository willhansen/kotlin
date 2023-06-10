// FILE: 1.kt
package test

class C<T>(konst konstue: T) {
    var inserting: Boolean = false
    fun nextSlot(): Any? = null
    fun startNode(key: Any?) {}
    fun endNode() {}
    fun emitNode(node: Any?) {}
    fun useNode(): T = konstue
    fun skipValue() {}
    fun updateValue(konstue: Any?) {}
}

class B<T>(konst composer: C<T>, konst node: T) {
    inline fun <V> bar(konstue: V, block: T.(V) -> Unit) = with(composer) {
        if (inserting || nextSlot() != konstue) {
            updateValue(konstue)
            node.block(konstue)
        } else skipValue()
    }
}

class A<T>(konst composer: C<T>) {
    inline fun foo(key: Any, ctor: () -> T, update: B<T>.() -> Unit) = with(composer) {
        startNode(key)
        konst node = if (inserting)
            ctor().also { emitNode(it) }
        else useNode() as T
        B(this, node).update()
        endNode()
    }
}

// FILE: 2.kt
import test.*

fun box(): String {
    konst a = A(C("foo"))
    konst str = "OK"
    var result = "fail"
    a.foo(
        123,
        { "abc" },
        {
            bar(str) { }
            result = "OK"
        }
    )

    return result
}
