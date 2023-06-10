// !LANGUAGE: -ProhibitDataClassesOverridingCopy

fun box(): String {
    konst a: A = B(1)
    a.copy(1)
    a.component1()
    return "OK"
}

interface A {
    fun copy(x: Int): A
    fun component1(): Any
}

data class B(konst x: Int) : A
