var result = ""

interface Base
open class Child : Base

interface A<T : Base> {
    fun <E : T> foo(a : E) {
        result += "A"
    }
}

class B : A<Child> {
    override fun <E : Child> foo(a : E) {
        result += "B"
    }
}

fun box(): String {
    konst b = B()
    b.foo(Child())
    konst a: A<Child> = b
    a.foo(Child())
    return if (result == "BB") "OK" else "Fail: $result"
}
