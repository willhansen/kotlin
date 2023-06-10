// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K2: JS_IR
// WITH_STDLIB

class A(konst ok: String)

context(A)
class B(oValue: Boolean = true, kValue: Boolean = true) {
    var o: Boolean
    var k: Boolean

    init {
        o = oValue
        k = kValue
    }

    constructor(oValue: String, kValue: String) : this(oValue == "O", kValue == "K")

    fun result() = if (o && k) ok else "fail"
}

fun box(): String {
    konst a = A("OK")
    with (a) {
        konst results = listOf(
            B(true, true).result(),
            B("O", "K").result(),
            B().result()
        )
        return if (results.all { it == "OK" }) "OK" else "fail"
    }
}
