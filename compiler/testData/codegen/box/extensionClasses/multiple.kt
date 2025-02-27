// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class A(konst a: String)
class B(konst b: String)

context(A, B)
class C {
    fun foo() = this@A.a + this@B.b
}

fun box(): String {
    konst c = with(A("O")) {
        with(B("K")) {
            C()
        }
    }
    return c.foo()
}
