// FIR_IDENTICAL
fun test() {
    konst p: Array<String> = arrayOf("a")
    foo(*p)
}

fun foo(vararg a: String?) = a