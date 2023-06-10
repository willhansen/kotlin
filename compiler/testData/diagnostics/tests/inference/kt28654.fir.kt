// !DIAGNOSTICS: -UNUSED_VARIABLE
// Related issue: KT-28654

fun <K> select(): K = <!RETURN_TYPE_MISMATCH!>run { }<!>

fun test() {
    konst x: Int = select()
    konst t = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>select<!>()
}
