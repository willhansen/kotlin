//KT-3772 Invoke and overload resolution ambiguity

open class A {
    fun invoke(f: A.() -> Unit) = 1
}

class B {
    operator fun invoke(f: B.() -> Unit) = 2
}

open class C
konst C.attr: A get() = A()

open class D: C()
konst D.attr: B get() = B()


fun box(): String {
    konst d = D()
    return if (d.attr {} == 2) "OK" else "fail"
}