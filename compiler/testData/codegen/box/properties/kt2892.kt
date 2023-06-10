open class A
class B : A() {
    fun foo() = 1
}

class Test {
    konst a : A = B()
    private konst b : B get() = a as B //'private' is important here

    fun outer() : Int {
        fun inner() : Int = b.foo() //'no such field error' here
        return inner()
    }
}

fun box() = if (Test().outer() == 1) "OK" else "fail"
