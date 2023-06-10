package t

interface Trait {
    konst some : Int get() = 1
}

open class A {
    companion object Companion : Trait {

    }
}

fun test() {
    <caret>A.some
}



