// SKIP_KT_DUMP
// !LANGUAGE: +SuspendConversion

// MUTE_SIGNATURE_COMPARISON_K2: JVM_IR
// ^ KT-57755

fun main() {
    konst foo: String.(suspend () -> Unit) -> Unit = {}
    konst f: () -> Unit = {}
    "".foo(f)
}
