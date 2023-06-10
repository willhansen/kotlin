open class A {
    open konst a = "OK"
}

class B : A() {
    override konst a = "FAIL"
    fun foo() = "CRUSH"
}

class C {
    fun A?.complex(): String {
        if (this is B) return foo()
        else if (this != null) return a
        else return "???"
    }

    fun bar() = A().complex()
}

fun box() = C().bar()
