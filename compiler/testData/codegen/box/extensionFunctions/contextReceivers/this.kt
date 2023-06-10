// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class A<T>(konst a: T)
class B(konst b: Any?)

context(A<String>, B) fun f() {
    this@A.a.length
    this@B.b
}

fun box(): String {
    with(A("")) {
        with(B(null)) {
            f()
        }
    }
    return "OK"
}

