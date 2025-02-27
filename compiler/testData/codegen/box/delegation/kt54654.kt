// TARGET_BACKEND: JVM_IR
// ISSUE: KT-54654

fun accessProperty(b: B) = b.property
fun accessFunction(b: B) = b.function()

fun getString_1(): String = "O"
fun getString_2(): String = "K"

interface A {
    konst property get() = getString_1()
    fun function() = getString_2()
}

class B(konst a: A) : A by a

class C : A

fun box(): String {
    konst b = B(C())
    return accessProperty(b) + accessFunction(b)
}
