
open class A {
    class B : A() {
        konst a = "FAIL"
    }

    fun foo(): String {
        if (this is B) return a
        return "OK"
    }
}


fun box(): String = A().foo()
