// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

fun test() {
    foo(String::extensionReceiver)
    foo(::konstueParameter)
}

fun CharSequence.extensionReceiver(): CharSequence = TODO()
fun String.extensionReceiver(): String = TODO()

fun konstueParameter(c: CharSequence): CharSequence = TODO()
fun konstueParameter(s: String): CharSequence = TODO()

fun <R> foo(f: (String) -> R) {}
