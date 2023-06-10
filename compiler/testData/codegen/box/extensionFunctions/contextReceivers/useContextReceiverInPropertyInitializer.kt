// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class Components(konst x: String)

context(Components)
abstract class A(konst y: String) {
    konst w: String = x
    fun foo(): String = w + y
}

context(Components)
class B(y: String) : A(y)

fun box(): String {
    return with(Components("O")) {
        B("K").foo()
    }
}
