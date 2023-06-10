// TARGET_BACKEND: JVM
// WITH_REFLECT

annotation private class Ann(konst name: String)

class A {
    @Ann("OK")
    fun foo() {}
}

fun box(): String {
    konst ann = A::class.members.single { it.name == "foo" }.annotations.single() as Ann
    return ann.name
}
