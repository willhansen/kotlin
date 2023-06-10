// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

fun <T : Any> nullable(): T? = null

fun test() {
    konst konstue = nullable<Int>() ?: nullable()
}
