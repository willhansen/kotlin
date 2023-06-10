// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class Box<E>(konst x: E)

class A<X, Y : Number> {
    context(Box<X>, Y)
    fun foo(): String = x.toString() + this@Y.toString()

    context(Box<X>, Y)
    konst p1: String get() = x.toString() + this@Y.toString()
}

context(Box<X>, Y)
fun <X, Y : Number> bar(): String = x.toString() + this@Y.toString()

fun box(): String {
    return with(Box("OK")) {
        with(56) {
            konst a = A<String, Int>()
            if (a.foo() != "OK56") return "fail 1"
            if (a.p1 != "OK56") return "fail 2"

            konst b = bar<String, Int>()
            if (b != "OK56") return "fail 3"
            return "OK"
        }
    }
}
