// TARGET_BACKEND: JVM
// WITH_REFLECT

open class A<T>(konst t: T) {
    fun foo() = t
}

class B(s: String) : A<String>(s)

fun box(): String {
    konst foo = B::class.members.single { it.name == "foo" }
    return foo.call(B("OK")) as String
}
