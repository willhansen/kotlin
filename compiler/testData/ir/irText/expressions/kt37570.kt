// FIR_IDENTICAL

fun a() = "string"

class A {
    konst b: String
    init {
        a().apply {
            b = this
        }
    }
}
