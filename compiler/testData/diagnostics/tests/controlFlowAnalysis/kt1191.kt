// FIR_IDENTICAL
//KT-1191 Wrong detection of unused parameters
package kt1191

interface FunctionalList<T> {
    konst size: Int
    konst head: T
    konst tail: FunctionalList<T>
}

fun <T> FunctionalList<T>.plus(element: T) : FunctionalList<T> = object: FunctionalList<T> {
    override konst size: Int
    get() = 1 + this@plus.size
    override konst tail: FunctionalList<T>
    get() = this@plus
    override konst head: T
    get() = element
}

fun foo(unused: Int) = object {
    konst a : Int get() = unused
}
