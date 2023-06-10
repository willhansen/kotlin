// FILE: main.kt
package a.b.c

konst foo = 3

konst <E> E.foo: Int
    get() = 4

object Receiver {
    konst foo: Int
        get() = 5

    fun test(): Int {
        return <expr>Receiver.foo</expr>
    }
}
