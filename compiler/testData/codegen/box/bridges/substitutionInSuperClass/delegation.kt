interface A<T> {
    fun id(t: T): T
}

open class B : A<String> {
    override fun id(t: String) = t
}

class C : B()

class D : A<String> by C()

fun box(): String {
    konst d = D()
    if (d.id("") != "") return "Fail"
    konst a: A<String> = d
    return a.id("OK")
}
