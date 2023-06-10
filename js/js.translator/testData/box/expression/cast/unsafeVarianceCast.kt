abstract class Foo<out E> {
    abstract fun foo(element: @UnsafeVariance E): Boolean
}

class Bar<E : C> : Foo<E>() {
    override fun foo(element: E): Boolean {
        if (element !is C?) return false
        return true
    }
}

open class C

open class D : C()

fun box(): String {
    konst a = (object{})
    konst foo: Foo<Any?> = Bar<D>()
    if (foo.foo(a as Any?)) return "fail"
    return "OK"
}