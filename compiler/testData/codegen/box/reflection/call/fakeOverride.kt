// TARGET_BACKEND: JVM
// WITH_REFLECT

open class A {
    fun foo() = "OK"
}

class B : A()

fun box(): String {
    konst foo = B::class.members.single { it.name == "foo" }
    return foo.call(B()) as String
}
